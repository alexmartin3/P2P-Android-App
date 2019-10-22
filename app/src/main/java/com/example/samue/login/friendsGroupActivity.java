package com.example.samue.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class friendsGroupActivity extends AppCompatActivity {
    private FriendsAdapter adapter;
    private ListView listView;
    private String username;
    private Groups grupoactual;
    private Groups grupoeliminado;
    static DatabaseHelper friendsGroupDatabaseHelper;

    FloatingActionButton addFriend;
    String nameFriend;
    String friendsupdate;
    boolean changeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friends_group);
        Toolbar toolbar = findViewById(R.id.listfriendsgroup_toolbar);
        setSupportActionBar(toolbar);
        friendsGroupDatabaseHelper = new DatabaseHelper(this);
        addFriend = findViewById(R.id.addFriends);
        Bundle extras = getIntent().getExtras();
        username = extras.getString("username");
        grupoactual = (Groups) extras.get("group");
        changeGroup=false;

        loadFriendsList(grupoactual.getListFriends());
        isadmin();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                nameFriend =grupoactual.getListFriends().get(position).getNombre();

                if (!username.equals(nameFriend)) {
                    final Dialog deletedialog = new Dialog(friendsGroupActivity.this);
                    deletedialog.setContentView(R.layout.dialog_deletefriendgroup);
                    deletedialog.show();

                    Button yes = deletedialog.findViewById(R.id.delete_friend_yes);
                    yes.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            //removeGroup(nameGroup);
                            grupoactual.getListFriends().remove(grupoactual.getListFriends().get(position));
                            friendsupdate = arrayListToString(grupoactual.getListFriends());
                            friendsGroupDatabaseHelper.deleteFriendToGroup(grupoactual.getNameGroup(), friendsupdate, friendsGroupDatabaseHelper.GROUPS_TABLE_NAME);
                            Toast.makeText(getApplicationContext(), nameFriend + " se ha eliminado", Toast.LENGTH_SHORT).show();
                            deletedialog.dismiss();
                            loadFriendsList(grupoactual.getListFriends());
                            changeGroup=true;
                            ArrayList<Friends> nuevo=new ArrayList<>();
                            nuevo.add(grupoactual.getListFriends().get(position));
                            grupoeliminado = new Groups(grupoactual.getNameGroup(),R.drawable.icongroup,nuevo,grupoactual.getListFriends().get(position).getNombre());

                        }
                    });
                    Button no = deletedialog.findViewById(R.id.delete_friend_no);
                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deletedialog.dismiss();
                        }
                    });
                }
                return true;
            }
        });
        FloatingActionButton addfriendgroup = findViewById(R.id.addFriends);
        addfriendgroup.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Intent myIntent = new Intent(friendsGroupActivity.this, friendsgroup.class);
                  myIntent.putExtra("nameGroup", grupoactual.getNameGroup());
                  myIntent.putExtra("username",username);
                  myIntent.putExtra("valor",2); //valor=1, crear grupo, valor=2, añadir amigos nuevos
                  myIntent.putExtra("friendsold",arrayListToString(grupoactual.getListFriends()));
                  startActivityForResult(myIntent, 1);
              }
        });
    }

    private void loadFriendsList(ArrayList<Friends> friendsreload) {
        adapter = new FriendsAdapter(this, friendsreload);
        listView = findViewById(R.id.listfriendgroups);
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
    public void isadmin(){
        String user = grupoactual.getAdministrador();
        if(!username.equals(user)){
            addFriend.setEnabled(false);
        }
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
        Intent result = new Intent();
        if (changeGroup) {
            result.putExtra("newGroup", grupoactual);
            result.putExtra("deleteGroup",grupoeliminado);
        }
        setResult(Activity.RESULT_OK, result);
        super.onBackPressed();
    }
}
