package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class filesGroupActivity extends AppCompatActivity {
	private ArrayAdapter<String> adaptador;
	private ListView listview;
	private Groups grupoactual;
	private String username;
	static DatabaseHelper filesgroupDatabaseHelper;

	private Dialog mdialog;
	private ArrayList listnamefiles;

	private String filesupdate;
	private String ownersupdate;
	private boolean changeGroup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_files_group);
		Toolbar toolbar = findViewById(R.id.listfilesgroup_toolbar);
		setSupportActionBar(toolbar);
		filesgroupDatabaseHelper = new DatabaseHelper(this);
		Bundle extras = getIntent().getExtras();
		listview = findViewById(R.id.listfilesgroups);
		username = extras.getString("username");
		grupoactual =(Groups) extras.getSerializable("group");
		listnamefiles = new ArrayList();
		loadfilesGroup(grupoactual);

		boolean listener = extras.getBoolean("listener");
		final String sendTo = extras.getString("sendTo");
		final boolean isFS = extras.getBoolean("isFS", false);
		final String folderName;
		if (isFS)
			folderName = extras.getString("folderName");
		else
			folderName = null;

		//	COMENZAMOS REESTRUCTURACIÓN
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
				final String name = listnamefiles.get(i).toString();

				if (isOwner(i)){
					mdialog = new Dialog(filesGroupActivity.this);
					mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
					mdialog.show();

					TextView tv = (TextView) mdialog.findViewById(R.id.confirm_archive_tv);
					tv.setText("¿Quieres borrar " + name + "?");

					Button yes = mdialog.findViewById(R.id.confirm_archive_yes);
					Button no = mdialog.findViewById(R.id.confirm_archive_no);

					no.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
						}
					});
					yes.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							grupoactual.getListFiles().remove(i);
							grupoactual.getListOwners().remove(i);
							filesupdate=Utils.joinStrings(",",grupoactual.getListFiles());
							ownersupdate=arrayListFriendsToString(grupoactual.getListOwners());
							filesgroupDatabaseHelper.deleteFileToGroup(grupoactual.getNameGroup(), filesupdate, ownersupdate, filesgroupDatabaseHelper.GROUPS_TABLE_NAME);
							mdialog.dismiss();
							loadfilesGroup(grupoactual);
							changeGroup=true;
						}
					});

				}
				else{
					mdialog = new Dialog(filesGroupActivity.this);
					mdialog.setContentView(R.layout.dialog_confirmdownload);
					mdialog.show();

					TextView tv = mdialog.findViewById(R.id.confirm_archive_tv);
					tv.setText("¿Quieres descargar " + name + "?");

					Button yes = mdialog.findViewById(R.id.confirm_archive_yes);
					Button no = mdialog.findViewById(R.id.confirm_archive_no);
					Button preview = mdialog.findViewById(R.id.confirm_archive_preview);

					no.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
						}
					});
					//aqui tenemos que ver con atencion el name sendto etc
					preview.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							mdialog.dismiss();
							Uri dato = Uri.parse("content://name/" + name);
							Intent resultado = new Intent(null, dato);
							resultado.putExtra("name", name);
							resultado.putExtra("sendTo", sendTo);
							resultado.putExtra(Utils.REQ_PREVIEW, true);
							if (isFS)
								resultado.putExtra("folderName", folderName);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
					//aqui tenemos que ver con atencion  todo el funcionamiento
					yes.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
							Uri dato = Uri.parse("content://name/" + name);
							Intent resultado = new Intent(null, dato);
							resultado.putExtra("name", name);
							resultado.putExtra("sendTo", sendTo);
							resultado.putExtra(Utils.REQ_PREVIEW, false);
							if (isFS)
								resultado.putExtra("folderName", folderName);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
				}
			}
		});

		/*
		if(listener){
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final String name = listnamefiles.get(position).toString();

					mdialog = new Dialog(filesGroupActivity.this);
					mdialog.setContentView(R.layout.dialog_confirmdownload);
					mdialog.show();

					TextView tv = mdialog.findViewById(R.id.confirm_archive_tv);
					tv.setText("¿Quieres descargar " + name + "?");

					Button yes = mdialog.findViewById(R.id.confirm_archive_yes);
					Button no = mdialog.findViewById(R.id.confirm_archive_no);
					Button preview = mdialog.findViewById(R.id.confirm_archive_preview);

					no.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
						}
					});

					String ext = name.substring(name.lastIndexOf('.')+1);
					if (!Utils.SUPPORTED_PREVIEW_FORMATS.contains(ext)) {
						preview.setEnabled(false);
						preview.setAlpha(.5f);
					}
					preview.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							mdialog.dismiss();
							Uri dato = Uri.parse("content://name/" + name);
							Intent resultado = new Intent(null, dato);
							resultado.putExtra("name", name);
							resultado.putExtra("sendTo", sendTo);
							resultado.putExtra(Utils.REQ_PREVIEW, true);
							if (isFS)
								resultado.putExtra("folderName", folderName);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
					yes.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
							Uri dato = Uri.parse("content://name/" + name);
							Intent resultado = new Intent(null, dato);
							resultado.putExtra("name", name);
							resultado.putExtra("sendTo", sendTo);
							resultado.putExtra(Utils.REQ_PREVIEW, false);
							if (isFS)
								resultado.putExtra("folderName", folderName);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
				}
			});
		}else{
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final String name = listnamefiles.get(position).toString();

					mdialog = new Dialog(filesGroupActivity.this);
					mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
					mdialog.show();

					TextView tv = (TextView) mdialog.findViewById(R.id.confirm_archive_tv);
					tv.setText("¿Quieres borrar " + name + "?");

					Button yes = mdialog.findViewById(R.id.confirm_archive_yes);
					Button no = mdialog.findViewById(R.id.confirm_archive_no);

					no.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
						}
					});

					yes.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
							Uri dato = Uri.parse("content://name/" + name);
							Intent resultado = new Intent(null, dato);
							resultado.putExtra("name", name);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
				}
			});
		}
		 */

		FloatingActionButton addFile = findViewById(R.id.addfile);
		// Botón para compartir un archivo o una carpeta.
		addFile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(filesGroupActivity.this, ArchiveExplorerGroups.class);
				intent.putExtra("username", username);
				intent.putExtra("group",grupoactual);
				startActivityForResult(intent,1);
			}
		});
	}

	private void loadfilesGroup(Groups group){
		//if (listnamefiles != null){listnamefiles.clear();}
		//else {listnamefiles = new ArrayList();}

		listnamefiles = group.getListFiles();

		adaptador = new AEArrayAdapter(this, android.R.layout.simple_expandable_list_item_1,listnamefiles);
		listview.setAdapter(adaptador);
	}
	private boolean isOwner(int position){
		boolean result=false;
		String user = grupoactual.listOwners.get(position).getNombre();
		if (username.equals(user)){
			result = true;
		}
		return result;
	}
	private ArrayList<Friends> stringtoArrayListFriend(String friends){
		if (friends == null){return new ArrayList<>();}
		ArrayList<Friends> resultado= new ArrayList<>();
		String[] friendsSeparate = friends.split(",");
		for (int i=0; i<friendsSeparate.length; i++){
			resultado.add(new Friends(friendsSeparate[i],R.drawable.astronaura));
		}
		return resultado;
	}
	private ArrayList stringtoArrayList(String files){
		if (files == null){
			return new ArrayList<>();
		}
		ArrayList resultado= new ArrayList();
		String[] filesSeparate = files.split(",");
		for (int i=0; i<filesSeparate.length; i++){
			resultado.add(filesSeparate[i]);
		}
		return resultado;
	}
	//pasar de un array lists de amigos a un string
	private String arrayListFriendsToString(ArrayList<Friends> listfriend) {
		String myString =null;

		for (int i = 0; i<listfriend.size();i++){
			if (myString==null){
				myString=listfriend.get(i).getNombre();
				if (i < (listfriend.size() - 1)){myString = myString + ",";}
			}else {
				myString = myString + listfriend.get(i).getNombre();
				if (i < (listfriend.size() - 1)) {
					myString = myString + ",";
				}
			}
		}
		return myString;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		switch(requestCode){
			case 1:
				if(resultCode == Activity.RESULT_OK){
					String newFile = data.getStringExtra("file");
						if (!listnamefiles.contains(newFile)){
							grupoactual.getListFiles().add(newFile);
							grupoactual.getListOwners().add(new Friends(username,R.drawable.ic_launcher_foreground));
							changeGroup=true;
						}
					loadfilesGroup(grupoactual);
					break;
				}
		}
	}
	@Override
	public void onBackPressed() {
		Intent result = new Intent();
		if (changeGroup) {
			result.putExtra("newGroup", grupoactual);
		}
		setResult(Activity.RESULT_OK, result);
		super.onBackPressed();
	}

}
