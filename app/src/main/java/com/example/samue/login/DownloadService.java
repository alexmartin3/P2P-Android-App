package com.example.samue.login;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.util.Pair;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by jotagalilea on 12/03/2019.
 *
 * Servicio que almacena el estado de las descargas durante su ejecución.
 */
public class DownloadService extends Service{

	private ArrayList<Download> al_downloads;
	private final IBinder binder = new DownloadBinder();
	private ManagerThread managerThread;
	private JSONObject jsonMsg;
	private boolean newMsgReceived = false;
	private final Object serviceMonitor = new Object();
	private byte threadsRunning;
	private final byte MAX_DL_THREADS = 2;
	// HashMap con clave nombre del fichero y valor el par monitor del hilo y el hilo.
	// Útil para el paso de los JSON entrantes al hilo que corresponda y controlar qué descargas están activas.
	private HashMap<String, Pair<Object, ManagerThread.DownloadThread>> hm_downloads;
	// Cola de mensajes para pedir archivos pensada para cuando no tengo hilos de descarga disponibles.
	private ArrayDeque<Pair<String,JSONObject>> msgQueue;



	@Override
	public void onCreate(){
		al_downloads = new ArrayList<>();
		hm_downloads = new HashMap<>(2);
		managerThread = new ManagerThread();
		threadsRunning = 0;
		msgQueue = new ArrayDeque<>();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Toast.makeText(this, "Servicio de descargas iniciado", Toast.LENGTH_LONG).show();
		managerThread.start();
		return START_REDELIVER_INTENT;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}


	/**
	 * Comprueba si hay hilos disponibles, o mejor dicho, si hay menos de 2 hilos descargando.
	 * @return true si hay menos de 2 hilos funcionando, false en otro caso.
	 */
	public boolean hasFreeThreads(){
		return (threadsRunning < MAX_DL_THREADS);
	}

	/**
	 * Método que debe ser llamado desde la lógica de recepción de mensajes cuando se trata de
	 * datos para una descarga. Lo notifica al monitor del servicio.
	 * @param json Mensaje recibido.
	 */
	public void handleMsg(final JSONObject json){
		synchronized (serviceMonitor){
			jsonMsg = json;
			newMsgReceived = true;
			serviceMonitor.notify();
		}
	}


	/**
	 * Añade una descarga a la colección.
	 * @param d Descarga nueva.
	 */
	private void addDownload(Download d){
		al_downloads.add(d);
	}


	/**
	 * Obtiene colección de descargas.
	 * @return Arraylist de descargas.
	 */
	public ArrayList<Download> getDownloads(){
		return al_downloads;
	}


	/**
	 * Ordeno al gestor que debe parar una descarga.
	 * @param dl_path Ruta del fichero perteneciente al objeto Download.
	 * @param dl_fileName Nombre del fichero perteneciente al objeto Download.
	 * @return
	 */
	public boolean stopDownload(String dl_path, String dl_fileName){
		return managerThread.stopDownload(dl_path, dl_fileName);
	}

	/**
	 * Encola un mensaje para pedir un archivo.
	 * @param sendTo nombre del amigo.
	 * @param json	mensaje json.
	 */
	public void queueMsg(String sendTo, JSONObject json){
		msgQueue.add(new Pair<>(sendTo, json));
	}


	/**
	 * Detiene el servicio.
	 */
	public void stop(){
		this.stopSelf();
	}



	/////////////////////////////////// Clases extra ///////////////////////////////////

	/**
	 * Administrador de los hilos en los que se ejecutan las descargas según los mensajes que se van recibiendo.
	 */
	private class ManagerThread extends Thread{
		private long fileLength;
		private String name;
		private boolean newDownload = false;
		private Download dl;
		private Timer timer;


		@Override
		public void run(){
			try{
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (hasFreeThreads() && !msgQueue.isEmpty()){
							/* Si hay hilos disponibles se coge uno de los mensajes de petición de archivo
							 * previamente preparado y se transmite la solicitud. Hago esta comprobación cada
							 * 10 segundos para interferir lo menos posible.
							 */
							Pair<String,JSONObject> p = msgQueue.poll();
							String sendTo = p.first;
							JSONObject msg = p.second;
							Profile.downloaderClient.transmit(sendTo, msg);
						}
					}
				}, 10010, 10010);

				while (true) {
					// El servicio se pausa si no se recibe el json.
					synchronized (serviceMonitor) {
						while (!newMsgReceived)
							serviceMonitor.wait();

						name = jsonMsg.getString(Utils.NAME);
						newDownload = jsonMsg.getBoolean(Utils.NEW_DL);

						// Si es una descarga nueva se añade al ArrayList.
						if (newDownload) {
							String friendName = jsonMsg.getString(Utils.FRIEND_NAME);
							fileLength = jsonMsg.getLong(Utils.FILE_LENGTH);
							String path = MainActivity.downloadsFolder + name;
							dl = new Download(name, path, fileLength, friendName);
							addDownload(dl);
						}

						// Si hay hilos disponibles...
						if (hasFreeThreads()){
							// Si es nueva descarga se lanza.
							if (newDownload)
								startDownload();

							// Si no es nueva descarga el json es de la única descarga activa y hay que notificar a su monitor.
							else
								notifyAndSetJson();
						}
						/* Si no hay hilos disponibles, entonces se trata seguro de un json para alguna de
						 * las descargas activas y hay que pasárselo y notificárselo, puesto que no es posible
						 * que se haya hecho una petición para una tercera descarga si no había al menos un
						 * hilo libre. Sólo cuando esto sucede es cuando se transmite una de las solicitudes en cola.
						 */
						else
							try {
								notifyAndSetJson();
							}
							catch (NullPointerException e){
								e.printStackTrace();
							}

						newDownload = false;
						newMsgReceived = false;
					}
				}

			} catch(Exception e){
				e.printStackTrace();
			}
		}


		/**
		 * Método que notifica a una descarga que han llegado datos para ella. Para ello se obtiene
		 * el monitor del hilo y el hilo con ayuda del hashMap hm_downloads donde están almacenados
		 * y se le pasa el json. Por último se llama a notify() para que continúe.
		 */
		private void notifyAndSetJson(){
			Pair<Object, DownloadThread> dl_pair = hm_downloads.get(name);
			if (dl_pair != null) {
				Object dl_monitor = dl_pair.first;
				DownloadThread th = dl_pair.second;
				synchronized (dl_monitor) {
					th.setJSON(jsonMsg);
					dl_monitor.notify();
				}
			}
		}



		/**
		 * Arranca un hilo de descarga.
		 */
		private void startDownload(){
			Object monitor = new Object();
			dl.setRunning();
			DownloadThread dl_th = new DownloadThread(monitor, dl);
			dl_th.setJSON(jsonMsg);
			Pair<Object, DownloadThread> pair = new Pair<>(monitor, dl_th);
			hm_downloads.put(dl.getFileName(), pair);
			dl_th.setName("DownloaderThread_" + threadsRunning);
			++threadsRunning;
			dl_th.start();
		}



		/**
		 * Para una descarga activa.
		 * @param dl_path Ruta del archivo.
		 * @param dl_fileName Nombre del archivo.
		 * @return
		 */
		public boolean stopDownload(String dl_path, String dl_fileName){
			Pair<Object,ManagerThread.DownloadThread> dl_Pair = hm_downloads.get(dl_fileName);
			boolean success = false;
			DownloadThread th = dl_Pair.second;
			hm_downloads.remove(dl_fileName);
			th.interrupt();
			--threadsRunning;
			File f = new File(dl_path);
			f.delete();
			success = true;
			return success;
		}



		/**
		 * Clase que implementa el hilo en el que se ejecuta una descarga.
		 */
		private class DownloadThread extends Thread{
			private JSONObject json;
			private Timer dl_timer;
			private int storedLastSecond, bps, bytesWritten;
			private StringBuilder codedData = new StringBuilder();
			private byte[] decodedData;
			private FileOutputStream fos;
			private Download dl;
			private final Object dl_monitor;
			private boolean lastPiece, newJson;


			public DownloadThread(Object m, Download d){
				dl_monitor = m;
				dl = d;
				lastPiece = false;
				newJson = true;
			}

			@Override
			public void run(){
				try{
					name = jsonMsg.getString(Utils.NAME);
					String path = dl.getPath();
					fos = new FileOutputStream(path);
					File file = new File(path);
					bytesWritten = 0;
					int count = 0;

					//CIFRADO Paso5. Obtenemos el string con la secretkey para crear el objeto
					//con el cual vamos a ir descifrando los mensajes
					String secretKey = jsonMsg.getString("secretKey");
					Log.i("paso5-secretkey",secretKey);
					Cryptography rsaTemp = new Cryptography();
					rsaTemp.setSecretKeyString(secretKey);


					// Actualiza el estado de la descarga en el último segundo para que se muestre más tarde en la interfaz gráfica.
					dl_timer = new Timer();
					dl_timer.schedule(new TimerTask() {
						@Override
						public void run() {
							updateDownload();
						}
					}, 1000, 1000);

					// Código que gestiona la recepción de los datos según van llegando los mensajes:
					while (!lastPiece){
						synchronized (dl_monitor){
							while (!newJson)
								dl_monitor.wait();

							//CIFRADO Paso5.Se recibe la info cifrada, se descifra con la secretKey
							//se recibe string se usa byte[]
							Log.i("paso5-cifrado",json.getString(Utils.DATA));
							decodedData=rsaTemp.decipherSimetric(json.getString(Utils.DATA));
							count=json.getInt("count");
							Log.i("count", String.valueOf(count));
							Log.i("paso5-descifrado",new String(decodedData));

							//como estaba antes
							//codedData.replace(0, codedData.length(), json.getString(Utils.DATA));
							//decodedData = Base64.decode(codedData.toString(), Base64.URL_SAFE);

							fos.write(decodedData);
							bytesWritten += decodedData.length;
							storedLastSecond += decodedData.length;

							lastPiece = json.getBoolean(Utils.LAST_PIECE);
							if (lastPiece) {
								fos.close();
								dl_timer.cancel();
								dl_timer.purge();
								bytesWritten = (int) dl.getSize();
								storedLastSecond = 0;
								updateDownload();
								dl.setStopped();
								hm_downloads.remove(dl.getFileName());
								--threadsRunning;
							}
							newJson = false;
						}
					}
					// Si se pidió una previsualización se abre el archivo al terminar:
					boolean isPreview = jsonMsg.getBoolean(Utils.PREVIEW_SENT);
					if (isPreview){
						Utils.openFile(name, file, getApplicationContext());
					}

					//this.interrupt();
				} catch(Exception e){
					e.printStackTrace();
					dl_timer.cancel();
					this.interrupt();
				}
			}


			/**
			 * Actualiza los atributos de la descarga en este hilo en cada llamada.
			 */
			private void updateDownload(){
				if (dl != null){
					int prog = (int) ((bytesWritten * 100L) / dl.getSize());
					dl.updateProgress(prog);
					bps = storedLastSecond;
					dl.updateSpeed(bps);
					dl.updateETA(bps);
					storedLastSecond = 0;
				}
			}


			/**
			 * Actualiza el mensaje JSON y señaliza que lo ha recibido.
			 * @param j
			 */
			public void setJSON(JSONObject j){
				json = j;
				newJson = true;
			}
		}
	}



	public class DownloadBinder extends Binder {
		DownloadService getService(){
			return DownloadService.this;
		}
	}


}
