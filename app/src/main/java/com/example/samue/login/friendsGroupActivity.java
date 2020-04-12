package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class friendsGroupActivity extends AppCompatActivity {
    private FriendsAdapter adapter;
    private ListView listView;
    private String username;
    private Groups grupoactual;
    private Groups grupoeliminado;
    static DatabaseHelper friendsGroupDatabaseHelper;

    FloatingActionButton addFriend;
    FloatingActionButton saveGroup;
    String nameFriend;
    private boolean changeGroup;
    ArrayList<Friends> nuevo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friends_group);
        Toolbar toolbar = findViewById(R.id.listfriendsgroup_toolbar);
        setSupportActionBar(toolbar);
        friendsGroupDatabaseHelper = new DatabaseHelper(this);
        addFriend = findViewById(R.id.addFriends);
        saveGroup = findViewById(R.id.saveFriends);
        Bundle extras = getIntent().getExtras();
        username = extras.getString("username");
        grupoactual = (Groups) extras.get("group");
        changeGroup=false;
        nuevo=new ArrayList<>();


        loadFriendsList(grupoactual.getListFriends());
        isAdmin();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String user = grupoactual.getAdministrador();
                if(!username.equals(user)){
                    Toast.makeText(getApplicationContext(), "No eres administrador", Toast.LENGTH_SHORT).show();
                }else {
                    nameFriend = grupoactual.getListFriends().get(position).getNombre();

                    if (!username.equals(nameFriend)) {
                        final Dialog deletedialog = new Dialog(friendsGroupActivity.this);
                        deletedialog.setContentView(R.layout.dialog_deletefriendgroup);
                        deletedialog.show();

                        Button yes = deletedialog.findViewById(R.id.delete_friend_yes);
                        yes.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                nuevo.add(grupoactual.getListFriends().get(position));
                                grupoeliminado = new Groups(grupoactual.getNameGroup(), R.drawable.cohete, nuevo, grupoactual.getListFriends().get(position).getNombre());
                                grupoactual.getListFriends().remove(position);
                                Toast.makeText(getApplicationContext(), nameFriend + " se ha eliminado", Toast.LENGTH_SHORT).show();
                                deletedialog.dismiss();
                                changeGroup = true;
                                loadFriendsList(grupoactual.getListFriends());
                            }
                        });
                        Button no = deletedialog.findViewById(R.id.delete_friend_no);
                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                deletedialog.dismiss();
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "Eres tú mismo", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        addFriend.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  String user = grupoactual.getAdministrador();
                  if(!username.equals(user)){
                      Toast.makeText(getApplicationContext(), "No eres administrador", Toast.LENGTH_SHORT).show();
                  }else {
                      if(haveMoreFriends()) {
                          Intent myIntent = new Intent(friendsGroupActivity.this, friendsgroup.class);
                          myIntent.putExtra("nameGroup", grupoactual.getNameGroup());
                          myIntent.putExtra("username", username);
                          myIntent.putExtra("valor", 2); //valor=1, crear grupo, valor=2, añadir amigos nuevos
                          myIntent.putExtra("friendsold", arrayListFriendsToString(grupoactual.getListFriends()));
                          startActivityForResult(myIntent, 1);
                      }else{
                          Toast.makeText(getApplicationContext(), "Lo siento, no tienes más amigos", Toast.LENGTH_SHORT).show();
                      }
                  }
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
                    result.putExtra("deleteGroup",grupoeliminado);
                    setResult(Activity.RESULT_OK, result);
                    updateGroupBBDD(grupoactual.getNameGroup(),grupoactual.getListFriends());
                }
                finish();
                Toast.makeText(getApplicationContext(), "Los cambios se han guardado", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton backGroups = findViewById(R.id.backToFriends);
        backGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadFriendsList(ArrayList<Friends> friendsreload) {
        ArrayList<Friends> friendsEdited = new ArrayList<>();
        for (Friends f: friendsreload){
            friendsEdited.add(new Friends(f.getNombre(),f.getImg()));
        }
        for (Friends f : friendsEdited){
            if(f.getNombre().equals(username) && f.getNombre().equals(grupoactual.getAdministrador())){
                f.setNombre("Tú (Admin)");
            }
            else if (f.getNombre().equals(username)){
                f.setNombre("Tú");
            }
            else if(f.getNombre().equals(grupoactual.getAdministrador())){
                f.setNombre(f.getNombre() + " (Admin)");
            }
        }
        adapter = new FriendsAdapter(this, friendsEdited);
        listView = findViewById(R.id.listfriendgroups);
        listView.setAdapter(adapter);
        if (changeGroup==false){
            saveGroup.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }else{
            saveGroup.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.holo_blue_light)));
        }
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
    public void isAdmin(){
        String user = grupoactual.getAdministrador();
        if(!username.equals(user)){
           addFriend.hide();
           saveGroup.hide();
        }
    }
    public boolean haveMoreFriends(){
        boolean result=false;
        int friendsGroup = grupoactual.listFriends.size()-1;
        Cursor data = friendsGroupDatabaseHelper.getData(DatabaseHelper.FRIENDS_TABLE_NAME);
        int friendsUser = data.getCount();

        if(friendsUser > friendsGroup) result = true;

        return result;
    }
    private boolean updateGroupBBDD(String nameupdate, ArrayList<Friends> friendsupdate){
        String friendsupdatestring = arrayListToString(friendsupdate);
        boolean inserted = friendsGroupDatabaseHelper.addFriendsGroup(nameupdate,friendsupdatestring, friendsGroupDatabaseHelper.GROUPS_TABLE_NAME);
        if (inserted)
            return inserted;
        else
            return false;
    }
    //pasar de un array lists de amigos a un string
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
                    ArrayList<Friends> newListFriends = (ArrayList<Friends>) data.getSerializableExtra("friends");
                    for (Friends f: newListFriends)
                        if (!grupoactual.getListFriends().contains(f)) {
                            grupoactual.getListFriends().add(f);
                            changeGroup=true;
                        }
                    loadFriendsList(grupoactual.getListFriends());
                    break;
                }
        }
    }
    @Override
    public void onBackPressed() {
        final Intent result = new Intent();

        if(changeGroup==true){
            final Dialog backdialog = new Dialog(friendsGroupActivity.this);
            backdialog.setContentView(R.layout.dialog_backgroups);
            backdialog.show();

            Button yes = backdialog.findViewById(R.id.back_groups_yes);
            yes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    setResult(Activity.RESULT_OK, result);
                    friendsGroupActivity.super.onBackPressed();
                }
            });
            Button no = backdialog.findViewById(R.id.back_groups_no);
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    backdialog.dismiss();
                }
            });

        }else{
            setResult(Activity.RESULT_OK, result);
            super.onBackPressed();
        }
    }
}
