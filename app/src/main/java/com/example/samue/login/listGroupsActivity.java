package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class listGroupsActivity extends AppCompatActivity {
    private GroupsAdapter adapter;
    private ListView listView;
    private ArrayList<Groups> listGroups;
    private String username;
    static DatabaseHelper groupDatabaseHelper;
    static ArrayList<Groups> new_groups;
    static ArrayList<Groups> delete_groups;
    static boolean returnGroups;
    private Handler handler=new Handler();
    private final int TIME = 2000;
    private boolean buscador;
    private SearchView searchGroup;

    Dialog mdialogCreate;
    EditText nameGroupText;
    Button bf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_groups);
        Toolbar toolbar = findViewById(R.id.listGroups_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Grupos");
        groupDatabaseHelper = new DatabaseHelper(this);

        ArrayList<Friends> listFriends= new ArrayList<>();
        listGroups= new ArrayList<Groups>();
        Bundle extras = getIntent().getExtras();
        username=extras.getString("username");
        new_groups= new ArrayList<Groups>();
        delete_groups = new ArrayList<Groups>();
        returnGroups=false;
        buscador=false;

        loadGroupList();
        actualizar();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Groups grupoactual = listGroups.get(i);
                final Dialog dialog = new Dialog(listGroupsActivity.this);
                dialog.setContentView(R.layout.dialog_group);

                dialog.show();
                //Boton ver archivos del dialogo del grupo seleccionado
                Button seeFiles = dialog.findViewById(R.id.files_button);
                seeFiles.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*
                         * Se abre la actividad que permite ver, añadir y eliminar algún fichero
                         * del grupo seleccionado.
                         */
                        final ArrayList<String> files = grupoactual.getListFiles();
                        Intent intent = new Intent(listGroupsActivity.this, filesGroupActivity.class);
                        intent.putExtra("username",username);
                        intent.putExtra("group",grupoactual);
                        startActivityForResult(intent, 6);
                        dialog.dismiss();
                    }
                });
                //Boton ver amigos del dialogo del grupo seleccionado
                Button seeFriends = dialog.findViewById(R.id.friends_button);
                seeFriends.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*
                         * Se abre la actividad que permite ver, añadir y eliminar algún amigo
                         * de la lista de acceso al grupo seleccionado. Si se borran todos
                         * entonces se elimina la carpeta de la aplicación.
                         */
                        dialog.dismiss();
                        Intent intent = new Intent(listGroupsActivity.this, friendsGroupActivity.class);
                        intent.putExtra("username",username);
                        intent.putExtra("group",grupoactual);
                        startActivityForResult(intent, 6);
                        loadGroupList();
                    }
                });
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Groups grupoactual = listGroups.get(position);
                final Dialog deletedialog = new Dialog(listGroupsActivity.this);
                deletedialog.setContentView(R.layout.dialog_deletegroup);
                deletedialog.show();

                Button yes = deletedialog.findViewById(R.id.delete_group_yes);
                yes.setOnClickListener(new View.OnClickListener() {
                    // Si se bloquea a un amigo este se borra de la lista de amigos.
                    @Override
                    public void onClick(View view) {
                        String user = grupoactual.getAdministrador();
                        if(!username.equals(user)){
                            for(int i=0;i<grupoactual.getListFriends().size();i++){
                                Friends f;
                                f=grupoactual.getListFriends().get(i);
                                if(f.getNombre().equals(username)){
                                    grupoactual.getListFriends().remove(f);
                                }
                            }
                           new_groups.add(grupoactual);
                        }else {
                            delete_groups.add(grupoactual);
                        }
                        listGroups.remove(grupoactual);
                        groupDatabaseHelper.deleteGroup(grupoactual.getNameGroup(), groupDatabaseHelper.GROUPS_TABLE_NAME);
                        Toast.makeText(getApplicationContext(), grupoactual.getNameGroup() + " se ha eliminado", Toast.LENGTH_SHORT).show();

                        returnGroups=true;
                        onBackPressed();
                        //deletedialog.dismiss();
                    }
                });
                Button no = deletedialog.findViewById(R.id.delete_group_no);
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {deletedialog.dismiss();}
                });
                return true;
            }
        });

        searchGroup = findViewById(R.id.search_group);
        searchGroup.setQueryHint(getText(R.string. search_group));
        searchGroup.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               return false;
           }

           @Override
           public boolean onQueryTextChange(String newText) {
               if(newText.equals("")){
                   buscador=false;
               }else{
                   buscador = true;
               }
               listGroupsSearch(newText);
               return true;
           }

       });

        FloatingActionButton createGroup = findViewById(R.id.createGroup);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdialogCreate = new Dialog(listGroupsActivity.this);
                mdialogCreate.setContentView(R.layout.dialog_newgroup);
                mdialogCreate.show();
                nameGroupText = (EditText) mdialogCreate.findViewById(R.id.nameGroup);

                bf = (Button) mdialogCreate.findViewById(R.id.button_addFriends);
                bf.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (nameGroupText.getText().toString().isEmpty()) {
                            Toast.makeText(getApplicationContext(), "ERROR: No puede tener nombre vacio", Toast.LENGTH_SHORT).show();
                        } else {
                            mdialogCreate.dismiss();
                            Intent myIntent = new Intent(listGroupsActivity.this, friendsgroup.class);
                            myIntent.putExtra("nameGroup", nameGroupText.getText().toString());
                            myIntent.putExtra("username", username);
                            myIntent.putExtra("valor", 1); //valor=1, crear grupo, valor=2, añadir amigos nuevos
                            startActivityForResult(myIntent, 6);
                        }
                    }
                });
            }
        });
        FloatingActionButton backFriends = findViewById(R.id.backFriends);
        backFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    /**
     * Carga de los grupos que estan almacenados en la BD.
     */
    private void loadGroupList() {
        Cursor c = groupDatabaseHelper.getData(DatabaseHelper.GROUPS_TABLE_NAME);
        if (listGroups != null){listGroups.clear();}
        else {listGroups = new ArrayList<>();}
        while (c.moveToNext()) {
            ArrayList<Friends> friends = stringtoArrayListFriend(c.getString(1));
            ArrayList files = stringtoArrayList(c.getString(2));
            ArrayList<Friends> owners = stringtoArrayListFriend(c.getString(3));
            Groups g = new Groups(c.getString(0), R.drawable.icongroup, friends, files, owners, c.getString(4));
            listGroups.add(g);
        }
        adapter = new GroupsAdapter(this, listGroups);
        listView = findViewById(R.id.groups_list);
        listView.setAdapter(adapter);
    }
    private void actualizar(){
        handler.postDelayed(new Runnable() {
            public void run() {
                if(!buscador && searchGroup.isIconified() ) {
                    loadGroupList();
                }
                handler.postDelayed(this, TIME);
            }
        }, TIME);
    }
    private void listGroupsSearch(String text){
        ArrayList<Groups> groupsSearch = new ArrayList<>();
        if (text.length()== 0){
            groupsSearch.addAll(listGroups);
        }else{
            for(Groups temp : listGroups){
                if(temp.getNameGroup().toLowerCase(Locale.getDefault()).contains(text)) {
                    groupsSearch.add(temp);
                }
            }
        }
        adapter = new GroupsAdapter(this, groupsSearch);
        listView = findViewById(R.id.groups_list);
        listView.setAdapter(adapter);
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
    private String ArrayListToString (ArrayList list){
        String resultado =null;
        for (int i=0; i<list.size(); i++){
            resultado=resultado + list.get(i).toString();
        }
        return resultado;
    }
    //pasar de un array lists de amigos a un string
    private String arrayListFriendsToString(ArrayList<Friends> listfriend) {
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
    //pasar de un array lists de amigos a un string
    private String arrayListToString(ArrayList list) {
        String myString =null;

        for (int i = 0; i<list.size();i++){
            if (myString==null){
                myString=list.get(i).toString();
                if (i < (list.size() - 1)){myString = myString + ",";}
            }else {
                myString = myString + list.get(i);
                if (i < (list.size() - 1)) {
                    myString = myString + ",";
                }
            }
        }
        return myString;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 6:
                try{
                    boolean download = data.getBooleanExtra("download",false);
                    if (download==true){    //vienes directamente de descargar un fichero
                        String name = data.getStringExtra("name");
                        String owner = data.getStringExtra("owner");

                        Intent resultado = new Intent();
                        resultado.putExtra("name", name);
                        resultado.putExtra("owner", owner);
                        resultado.putExtra("download",true);
                        setResult(RESULT_OK, resultado);
                        finish();

                    }else {                 // Vienes de cambiar o añadir grupo
                        Groups newGroup = (Groups) data.getSerializableExtra("newGroup");
                        Groups deleteGroup = (Groups) data.getSerializableExtra("deleteGroup");
                        if (newGroup != null) {
                            new_groups.add(newGroup);
                            if (listGroups.contains(newGroup)) {
                                listGroups.remove(newGroup);
                                listGroups.add(newGroup);
                            } else {
                                listGroups.add(newGroup);
                            }
                            loadGroupList();
                        }
                        if (deleteGroup != null) {
                            delete_groups.add(deleteGroup);
                        }
                    }
                    returnGroups=true;
                    onBackPressed();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("newgroups",new_groups);
        intent.putExtra("deletegroups",delete_groups);
        intent.putExtra("returnGroups",returnGroups);
        setResult(Activity.RESULT_OK,intent);
        super.onBackPressed();
    }
}
