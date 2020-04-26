package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ArchiveExplorerGroups extends AppCompatActivity {
    private Dialog mdialog;
    private List listaRutasArchivos;
    private String directorioRaiz;
    private TextView carpetaActual;
    private String currentFolder;
    private ListView listaItems;
    private static DatabaseHelper filesDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_explorer_groups);
        filesDatabaseHelper = new DatabaseHelper(this);

        carpetaActual = findViewById(R.id.rutaActual_grupos);
        listaItems = findViewById(R.id.lista_items_grupos);
        directorioRaiz = Environment.getExternalStorageDirectory().getPath();
        verArchivosDirectorio(directorioRaiz);

        Bundle extras = getIntent().getExtras();
        String username = Objects.requireNonNull(extras).getString("username");

        // Compartir un archivo:
        listaItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File archivo = new File((String)listaRutasArchivos.get(position));

                // Si es un archivo se muestra un Toast con su nombre y si es un directorio
                // se cargan los archivos que contiene en el listView
                if (archivo.isFile()) {
                    final String name = archivo.getName();
                    final String path = archivo.getPath();

                    mdialog = new Dialog(ArchiveExplorerGroups.this);
                    mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
                    mdialog.show();

                    TextView tv = mdialog.findViewById(R.id.confirm_archive_tv);
                    String tmp="¿Quieres compartir " + archivo.getName() + " con tus amigos?";
                    tv.setText(tmp);

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
                            final ProgressDialog progressDialog = new ProgressDialog(ArchiveExplorerGroups.this);
                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage("Subiendo " + name + "...");
                            progressDialog.show();

                            Toast.makeText(getApplicationContext(), "File selected has been added", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.putExtra("file",path);
                            setResult(Activity.RESULT_OK,intent);
                            finish();

                        }
                    });
                } else {
                    // Si es un directorio mostramos todos los archivos que contiene
                    verArchivosDirectorio((String)listaRutasArchivos.get(position));
                }
            }
        });
    }
    private void verArchivosDirectorio(String rutaDirectorio) {
        String tmp="Estas en: " + rutaDirectorio;
        carpetaActual.setText(tmp);
        currentFolder = rutaDirectorio;
        ArrayList listaNombresArchivos = new ArrayList();
        listaRutasArchivos = new ArrayList();
        File directorioActual = new File(rutaDirectorio);
        if(!directorioActual.exists()){
            return;
        }
        File[] listaArchivos = directorioActual.listFiles();

        int x = 0;
        if (listaArchivos == null) {
            Toast.makeText(ArchiveExplorerGroups.this, "No se puede acceder",Toast.LENGTH_LONG).show(); return;
        }
        // Si no es nuestro directorio raiz creamos un elemento que nos
        // permita volver al directorio padre del directorio actual
        if (!rutaDirectorio.equals(directorioRaiz)) {
            listaNombresArchivos.add("../");
            listaRutasArchivos.add(directorioActual.getParent());
            x = 1;
        }
        // Almacenamos las rutas de todos los archivos y carpetas del directorio
        for (File archivo : listaArchivos) {
            listaRutasArchivos.add(archivo.getPath());
        }
        // Ordenamos la lista de archivos para que se muestren en orden alfabetico
        Collections.sort(listaRutasArchivos, String.CASE_INSENSITIVE_ORDER);

        // Recorremos la lista de archivos ordenada para crear la lista de los nombres
        // de los archivos que mostraremos en el listView
        for (int i = x; i < listaRutasArchivos.size(); i++){
            File archivo = new File((String)listaRutasArchivos.get(i));
            if (archivo.isFile()) {
                listaNombresArchivos.add(archivo.getName());
            } else {
                listaNombresArchivos.add("/" + archivo.getName());
            }
        }
        // Si no hay ningun archivo en el directorio lo indicamos
        if (listaArchivos.length < 1) {
            listaNombresArchivos.add("No hay ningun archivo");
            listaRutasArchivos.add(rutaDirectorio);
        }
        // Creamos el adaptador y le asignamos la lista de los nombres de los
        // archivos y el layout para los elementos de la lista
        ArrayAdapter adaptador = new AEArrayAdapter(this, android.R.layout.simple_list_item_1, listaNombresArchivos);
        listaItems.setAdapter(adaptador);
    }

    private String arrayListToString(ArrayList<Friends> listfriend) {
        String myString ="";

        for (int i = 0; i<listfriend.size();i++){
            if (myString.equals("")){
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
    public void onBackPressed(){
        if (!currentFolder.equalsIgnoreCase(directorioRaiz)) {
            verArchivosDirectorio(new File(currentFolder).getParent());
        }
        else
            super.onBackPressed();
    }
}
