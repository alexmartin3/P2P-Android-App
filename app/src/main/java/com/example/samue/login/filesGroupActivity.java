package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.Toast;

import java.util.ArrayList;


public class filesGroupActivity extends AppCompatActivity {
	private ArrayAdapter<String> adaptador;
	private ListView listview;
	private Groups grupoactual;
	private String username;
	static DatabaseHelper filesgroupDatabaseHelper;
	FloatingActionButton saveGroup;

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
		saveGroup = findViewById(R.id.saveFiles);
		Bundle extras = getIntent().getExtras();
		listview = findViewById(R.id.listfilesgroups);
		username = extras.getString("username");
		grupoactual =(Groups) extras.getSerializable("group");
		getSupportActionBar().setTitle(grupoactual.getNameGroup() + " - Ficheros");
		listnamefiles = new ArrayList();
		changeGroup=false;
		loadfilesGroup(grupoactual);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
				final String name = listnamefiles.get(i).toString();

				if (isOwner(i)){
					mdialog = new Dialog(filesGroupActivity.this);
					mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
					mdialog.show();

					TextView tv = (TextView) mdialog.findViewById(R.id.confirm_archive_tv);
					tv.setText("¿Quieres borrar " + name.substring(name.lastIndexOf('/')+1) + "?");

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
							Toast.makeText(getApplicationContext(), "El fichero se ha eliminado", Toast.LENGTH_SHORT).show();
							mdialog.dismiss();
							changeGroup=true;
							loadfilesGroup(grupoactual);

						}
					});

				}
				else{
					mdialog = new Dialog(filesGroupActivity.this);
					mdialog.setContentView(R.layout.dialog_confirmdownload);
					mdialog.show();

					TextView tv = mdialog.findViewById(R.id.confirm_archive_tv);
					tv.setText("¿Quieres descargar " + name.substring(name.lastIndexOf('/')+1) + "?");

					Button yes = mdialog.findViewById(R.id.confirm_archive_yes);
					Button no = mdialog.findViewById(R.id.confirm_archive_no);
					//lo mantenemos oculto por no implementarse en grupos
					Button preview = mdialog.findViewById(R.id.confirm_archive_preview);
					//preview.setVisibility(view.INVISIBLE);

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
							Intent resultado = new Intent();
							resultado.putExtra("name", name);
							resultado.putExtra("owner", grupoactual.getListOwners().get(i).getNombre());
							resultado.putExtra("download",true);
							resultado.putExtra(Utils.REQ_PREVIEW, false);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
					preview.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
							Intent resultado = new Intent();
							resultado.putExtra("name", name);
							resultado.putExtra("owner", grupoactual.getListOwners().get(i).getNombre());
							resultado.putExtra("download",true);
							resultado.putExtra(Utils.REQ_PREVIEW, true);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
				}
			}
		});
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

		saveGroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//TODO EL PROCESO DE GUARDAR LOS CAMBIOS EN LA BBDD

				if (changeGroup) {
					final Intent result = new Intent();
					result.putExtra("download",false);
					result.putExtra("newGroup", grupoactual);
					setResult(Activity.RESULT_OK, result);
					updateGroupBBDD(grupoactual.getNameGroup(),grupoactual.getListFiles(),grupoactual.getListOwners());

					finish();
					Toast.makeText(getApplicationContext(), "Los cambios se han guardado", Toast.LENGTH_SHORT).show();
				}
				saveGroup.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.holo_blue_light)));
			}
		});

		FloatingActionButton backGroups = findViewById(R.id.backToFiles);
		backGroups.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});
	}

	private void loadfilesGroup(Groups group){
		listnamefiles = group.getListFiles();
		ArrayList listfinal= new ArrayList();
		if(!listnamefiles.isEmpty()) {
			for (int i = 0; i < listnamefiles.size(); i++) {
				String path = listnamefiles.get(i).toString();
				listfinal.add(path.substring(path.lastIndexOf('/') + 1));
			}
		}
		adaptador = new AEArrayAdapter(this, android.R.layout.simple_expandable_list_item_1,listfinal);
		listview.setAdapter(adaptador);

		if (changeGroup==false){
			saveGroup.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
		}else{
			saveGroup.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.red)));
		}
	}
	private boolean isOwner(int position){
		boolean result=false;
		String user = grupoactual.listOwners.get(position).getNombre();
		if (username.equals(user)){
			result = true;
		}
		return result;
	}
	private boolean updateGroupBBDD(String nameupdate,ArrayList filesupdate, ArrayList<Friends> ownersupdate){
		ArrayList files=filesupdate;
		String ownerssupdatestring = arrayListToString(ownersupdate);
		boolean inserted = filesgroupDatabaseHelper.addFileGroup(nameupdate,Utils.joinStrings(",",files),ownerssupdatestring, filesgroupDatabaseHelper.GROUPS_TABLE_NAME);
		if (inserted)
			return inserted;
		else
			return false;
	}    //pasar de un array lists de amigos a un string
	private String arrayListToString(ArrayList<Friends> listfriend) {
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
		final Intent result = new Intent();

		if (changeGroup == true) {
			final Dialog backdialog = new Dialog(filesGroupActivity.this);
			backdialog.setContentView(R.layout.dialog_backgroups);
			backdialog.show();

			Button yes = backdialog.findViewById(R.id.back_groups_yes);
			yes.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					setResult(Activity.RESULT_OK, result);
					filesGroupActivity.super.onBackPressed();
				}
			});
			Button no = backdialog.findViewById(R.id.back_groups_no);
			no.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					backdialog.dismiss();
				}
			});

		} else {
			setResult(Activity.RESULT_OK, result);
			super.onBackPressed();
		}
	}
}
