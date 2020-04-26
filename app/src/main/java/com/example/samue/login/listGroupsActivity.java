package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class listGroupsActivity extends AppCompatActivity {
    private GroupsAdapter adapter;
    private ListView listView;
    private ArrayList<Groups> listGroups;
    private String username;
    private static DatabaseHelper groupDatabaseHelper;
    private static ArrayList<Groups> new_groups;
    private static ArrayList<Groups> delete_groups;
    private static boolean returnGroups;
    private final Handler handler=new Handler();
    private final int TIME = 2000;
    private boolean buscador;
    private SearchView searchGroup;

    private Dialog mdialogCreate;
    private EditText nameGroupText;
    private Button bf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_groups);
        Toolbar toolbar = findViewById(R.id.listGroups_toolbar);
        setSupportActionBar(toolbar);

        groupDatabaseHelper = new DatabaseHelper(this);

        listGroups= new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        username= Objects.requireNonNull(extras).getString("username");
        Objects.requireNonNull(getSupportActionBar()).setTitle(username + " - Grupos");
        new_groups= new ArrayList<>();
        delete_groups = new ArrayList<>();
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
                TextView  t = deletedialog.findViewById(R.id.delete_group_title);
                if(!username.equals(grupoactual.getAdministrador())) {
                    String tmp="¿Desea salir del grupo?";
                    t.setText(tmp);
                }

                Button yes = deletedialog.findViewById(R.id.delete_group_yes);
                yes.setOnClickListener(new View.OnClickListener() {
                    // Si se bloquea a un amigo este se borra de la lista de amigos.
                    @Override
                    public void onClick(View view) {
                        String user = grupoactual.getAdministrador();
                        if(!username.equals(user)){
                            Groups g = exitToGroup(grupoactual);
                            new_groups.add(g);
                        }else {
                            delete_groups.add(grupoactual);
                        }
                        listGroups.remove(grupoactual);
                        groupDatabaseHelper.deleteGroup(grupoactual.getNameGroup(), DatabaseHelper.GROUPS_TABLE_NAME);
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
               buscador= !newText.equals("");
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
                nameGroupText = mdialogCreate.findViewById(R.id.nameGroup);

                bf = mdialogCreate.findViewById(R.id.button_addFriends);
                bf.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (nameGroupText.getText().toString().isEmpty()) {
                            Toast.makeText(getApplicationContext(), "ERROR: No puede tener nombre vacio", Toast.LENGTH_SHORT).show();
                        } else {
                            if(customListContains(nameGroupText.getText().toString(),listGroups)){
                                Toast.makeText(getApplicationContext(), "ERROR: Este nombre ya existe", Toast.LENGTH_SHORT).show();
                                nameGroupText.getText().clear();
                            }else {
                                mdialogCreate.dismiss();
                                Intent myIntent = new Intent(listGroupsActivity.this, friendsgroup.class);
                                myIntent.putExtra("nameGroup", nameGroupText.getText().toString());
                                myIntent.putExtra("username", username);
                                myIntent.putExtra("valor", 1); //valor=1, crear grupo, valor=2, añadir amigos nuevos
                                startActivityForResult(myIntent, 6);
                            }
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
            Groups g = new Groups(c.getString(0), R.drawable.cohete, friends, files, owners, c.getString(4));
            listGroups.add(g);
        }
        adapter = new GroupsAdapter(this, listGroups);
        listView = findViewById(R.id.groups_list);
        listView.setAdapter(adapter);
        c.close();
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
                if(temp.getNameGroup().toLowerCase(Locale.getDefault()).contains(text.toLowerCase())) {
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
        for (String s : friendsSeparate) {
            resultado.add(new Friends(s, R.drawable.astronaura));
        }
        return resultado;
    }
    private ArrayList stringtoArrayList(String files){
        if (files == null){
            return new ArrayList<>();
        }
        ArrayList resultado= new ArrayList();
        String[] filesSeparate = files.split(",");
        for (String s : filesSeparate) {
            resultado.add(s);
        }
        return resultado;
    }

    //borrar la informacion del usuario en el grupo
    private Groups exitToGroup(Groups g){
        for(int i=0;i<g.getListFriends().size();i++){
            Friends f;
            f=g.getListFriends().get(i);
            if(f.getNombre().equals(username)){
                g.getListFriends().remove(f);
            }
        }
        int i = 0;
        while(i<g.getListFiles().size()){
            if (g.getListOwners().get(i).getNombre().equals(username)) {
                g.getListOwners().remove(i);
                g.getListFiles().remove(i);
            }else{
                i++;
            }
        }
        return g;
    }
    //Comprobar que el nombre del nuevo grupo no existe ya
    private boolean customListContains(String nameGroup, ArrayList<Groups> gr) {
        Boolean result=false;
        for (Groups g : gr) {
            if (g.getNameGroup().equals(nameGroup)) {
                return true;
            }
        }
        return result;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6) {
            try {
                boolean download = data.getBooleanExtra("download", false);
                if (download) {    //vienes directamente de descargar un fichero
                    String name = data.getStringExtra("name");
                    String owner = data.getStringExtra("owner");
                    Boolean preview = data.getBooleanExtra(Utils.REQ_PREVIEW, false);

                    Intent resultado = new Intent();
                    resultado.putExtra("name", name);
                    resultado.putExtra("owner", owner);
                    resultado.putExtra("download", true);
                    resultado.putExtra(Utils.REQ_PREVIEW, preview);
                    setResult(RESULT_OK, resultado);
                    finish();

                } else {                 // Vienes de cambiar o añadir grupo
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
                returnGroups = true;
                onBackPressed();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
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
