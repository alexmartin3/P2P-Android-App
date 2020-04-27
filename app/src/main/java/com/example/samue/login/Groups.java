package com.example.samue.login;

import java.io.Serializable;
import java.util.ArrayList;

class Groups implements Serializable {
    String nameGroup;
    private int imgGroup;
    private ArrayList<Friends> listFriends;
    private ArrayList listFiles;
    private ArrayList<Friends> listOwners;
    private String administrator;

    public Groups(){}

    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends,String admin){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
        this.listFiles=new ArrayList();
        this.listOwners=new ArrayList<>();
        this.administrator=admin;
    }
    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends, ArrayList listFiles, ArrayList<Friends> listOwners, String admin){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
        this.listFiles=listFiles;
        this.listOwners=listOwners;
        this.administrator=admin;
    }

    public String getNameGroup(){return this.nameGroup;}

    public int getImgGroup() { return this.imgGroup; }

    public ArrayList<Friends> getListFriends() { return this.listFriends; }

    public ArrayList getListFiles() {return this.listFiles;}

    public ArrayList<Friends> getListOwners() {return this.listOwners;}

    public String getAdministrador() { return this.administrator;}


}
