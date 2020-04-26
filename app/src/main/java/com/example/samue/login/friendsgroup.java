package com.example.samue.login;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;


public class friendsgroup extends AppCompatActivity {
    private RVadapter rvadapter;
    private ArrayList<Friends> friends;
    private ArrayList<Friends> friendsviews;
    private ArrayList<Friends> friendsSelected;
    private ArrayList<Friends> friendsSelectedfinish;
    private String nameGroup;
    private String aux;
    private String administrator;
    private Groups newGroup;
    private DatabaseHelper helperGroup;
    private String username;
    private static DatabaseHelper groupDatabaseHelper;
    private int valor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendsgroup);
        Toolbar toolbar = findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Selecciona los amigos");
        helperGroup = new DatabaseHelper(this);
        groupDatabaseHelper = new DatabaseHelper(this);

        Bundle extras = getIntent().getExtras();
        nameGroup = Objects.requireNonNull(extras).getString("nameGroup");
        administrator = extras.getString("username");
        username =extras.getString("username");
        valor = extras.getInt("valor");

        if(valor == 1) {friendslist();}
        if(valor == 2) {
            aux = extras.getString("friendsold");
            friendslist2(aux);
        }
        friendsSelected = new ArrayList<>();
        friendsSelectedfinish = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.rv_friendsgroup);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        rvadapter = new RVadapter(friendsviews);
        recyclerView.setAdapter(rvadapter);

        FloatingActionButton button = findViewById(R.id.createGroup);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean add;
                friendsSelected = rvadapter.obtenerSeleccionados();
                if (friendsSelected.size() == 0){
                    Toast.makeText(getApplicationContext(), "ERROR: No has seleccionado ningún amigo", Toast.LENGTH_SHORT).show();
                }else {

                    if (valor == 1) {
                        friendsSelectedfinish.add(new Friends(username, R.drawable.ic_launcher_foreground));
                        friendsSelectedfinish.addAll(friendsSelected);

                        newGroup = new Groups(nameGroup, R.drawable.cohete, friendsSelectedfinish, administrator);
                        add = addGroupBBDD(nameGroup, friendsSelectedfinish, administrator);
                        if (add) {
                            Toast.makeText(getApplicationContext(), "Group " + nameGroup + " has been created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.putExtra("newGroup", newGroup);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                        }
                    } else if (valor == 2) {
                        friendsSelectedfinish.addAll(stringtoArrayListFriend(aux));
                        friendsSelectedfinish.addAll(friendsSelected);

                        Toast.makeText(getApplicationContext(), "Friends selected has been added", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("friends", friendsSelectedfinish);
                        setResult(Activity.RESULT_OK, intent);
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    // cargar lista de amigos
    private void friendslist() {
        Cursor data = groupDatabaseHelper.getData(DatabaseHelper.FRIENDS_TABLE_NAME);
        friends = new ArrayList<>();
        friendsviews= new ArrayList<>();
        while(data.moveToNext()){
            friends.add(new Friends(data.getString(1), R.drawable.astronaura));
            friendsviews.add(new Friends(data.getString(1), R.drawable.astronaura));
        }
    }
    private void friendslist2(String friendsold) {
        ArrayList<Friends> friendsaux;
        friendsaux=stringtoArrayListFriend(friendsold);
        Cursor data = groupDatabaseHelper.getData(DatabaseHelper.FRIENDS_TABLE_NAME);
        friends = new ArrayList<>();
        friendsviews= new ArrayList<>();
        while(data.moveToNext()){
            if (stringisfriend(friendsaux,data.getString(1))){}
            else {
                friendsviews.add(new Friends(data.getString(1), R.drawable.astronaura));
            }
            friends.add(new Friends(data.getString(1), R.drawable.astronaura));
        }
    }
    private boolean stringisfriend(ArrayList<Friends> friendsold, String nuevo){
        ArrayList<Friends> friendstmp;
        friendstmp=friendsold;
        for(Friends f : friendstmp){
            if (f.getNombre().equals(nuevo))
                return true;
        }
        return false;
    }
    //añadir el grupo creado a la BBDD
    private boolean addGroupBBDD(String name, ArrayList<Friends> listFriends, String administrator) {
        String listFriendStrings = arrayListToString(listFriends);

        boolean inserted = helperGroup.addGroup(name, listFriendStrings, administrator);
        if (inserted)
            return true;
        else
            return false;
    }

    // ----------------------a partir de aqui revisar que esto no sobre-------------------

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
    private ArrayList<Friends> stringtoArrayListFriend(String friends){
        if (friends == null){return new ArrayList<>();}
        ArrayList<Friends> resultado= new ArrayList<>();
        String[] friendsSeparate = friends.split(",");
        for (String s : friendsSeparate) {
            resultado.add(new Friends(s, R.drawable.astronaura));
        }
        return resultado;
    }
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 6:
                if (resultCode == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
        }
    }
     */
}

