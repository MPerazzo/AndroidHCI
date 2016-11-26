package com.flysafely.probando;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by matias on 14/11/16.
 */

public class DrawerListAdapter extends ArrayAdapter <DrawerItem>{

    private static final int NEW_GROUP_POSITION = 4;

    private Context context;

    int layoutResourceID;

    ArrayList<DrawerItem> data = null;

    public DrawerListAdapter(Context context, int layoutResourceID,  ArrayList<DrawerItem> data) {
        super(context, layoutResourceID, data);

        this.context = context;
        this.layoutResourceID = layoutResourceID;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ItemHolder holder = null;

        if(row == null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceID, parent, false);

            holder = new ItemHolder();
            holder.imagen = (ImageView) row.findViewById(R.id.icon);
            holder.texto = (TextView) row.findViewById(R.id.name);
            row.setTag(holder);
        }

        else {
            holder = (ItemHolder)row.getTag();
        }

        // Only one separator in position NEW_GROUP_POSITION-1 of the ListVIew
        if(position == NEW_GROUP_POSITION-1){
            LayoutInflater inflater = (LayoutInflater)parent.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View divider = inflater.inflate(R.layout.separator, null);
            return divider;
        }

        DrawerItem item = data.get(position);

        if ( item != null && holder != null) {

            holder.texto.setText(item.getName());
            holder.imagen.setImageResource(item.getIconId());
        }

        return row;
    }

    @Override
    public int getViewTypeCount() {
        return 2; //we will create 2 types of views so we can have a separator.
    }

    static private class ItemHolder {
        public TextView texto;
        public ImageView imagen;

        private ItemHolder() {
            super();
        }
    }
}
