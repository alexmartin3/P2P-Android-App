package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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

 /* para volver a comentar y ver las cosas bien
public class filesGroupActivityCopiaAntesTocarTodoLogica extends AppCompatActivity {
	private ArrayAdapter<String> adaptador;
	private ListView listview;
	private Groups grupoactual;
	private String username;
	static DatabaseHelper filesgroupDatabaseHelper;

	private Dialog mdialog;
	private ArrayList listnamefiles;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_files_group);
		Toolbar toolbar = findViewById(R.id.listfilesgroup_toolbar);
		setSupportActionBar(toolbar);
		Bundle extras = getIntent().getExtras();
		listview = findViewById(R.id.listfilesgroups);
		username = extras.getString("username");
		grupoactual =(Groups) extras.getSerializable("group");
		listnamefiles = new ArrayList();
		loadfilesGroup();



		boolean listener = extras.getBoolean("listener");
		final String sendTo = extras.getString("sendTo");
		final boolean isFS = extras.getBoolean("isFS", false);
		final String folderName;
		if (isFS)
			folderName = extras.getString("folderName");
		else
			folderName = null;

		if(listener){
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final String name = listnamefiles.get(position).toString();

					mdialog = new Dialog(filesGroupActivityCopiaAntesTocarTodoLogica.this);
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
		}
	}

	private void loadfilesGroup(){

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		switch(requestCode){
			case 1:
				if(resultCode == Activity.RESULT_OK){
					ArrayList newListFiles = data.getStringArrayListExtra("files");
						if (listnamefiles.size() < newListFiles.size()){
							listnamefiles.add(newListFiles.get(newListFiles.size()-1));
							newFiles = new ArrayList(newListFiles);
						}
					adaptador = new AEArrayAdapter(this, android.R.layout.simple_expandable_list_item_1,listnamefiles);
					listview = findViewById(R.id.listfilesgroups);
					listview.setAdapter(adaptador);
					break;
				}
		}
	}


}*/