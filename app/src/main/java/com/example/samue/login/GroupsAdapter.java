package com.example.samue.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupsAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Groups> listItems;

    public GroupsAdapter(Context context, ArrayList<Groups> listItems) {
        this.context = context;
        this.listItems = listItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Groups groups = (Groups) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.listview_row, null);
        ImageView img = (ImageView) convertView.findViewById(R.id.img_user);
        TextView fn = (TextView) convertView.findViewById(R.id.friend_name);

        img.setImageResource(groups.getImgGroup());
        fn.setText(groups.getNameGroup());

        return convertView;
    }
}
