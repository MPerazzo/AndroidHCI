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
 * Created by matias on 16/11/16.
 */

public class OfferArrayAdapter extends ArrayAdapter<Offer> {

    private Context context;

    public OfferArrayAdapter(Context context, ArrayList<Offer> objects) {
        super(context, R.layout.offer_list_item, objects);

        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        OfferViewHolder holder;

        if(row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            row = inflater.inflate(R.layout.offer_list_item, parent, false);

            holder = new OfferViewHolder();
            holder.imageView = (ImageView) row.findViewById(R.id.offer_image);
            holder.nameTextView = (TextView) row.findViewById(R.id.offer_name);
            row.setTag(holder);

        }
        else {
            holder = (OfferViewHolder) convertView.getTag();
        }

        Offer offer = getItem(position);

        holder.imageView.setImageBitmap(offer.getBitmap());
        holder.nameTextView.setText(offer.getName() + "  -  U$S " + offer.getPrice().toString());

        return row;
    }

    private class OfferViewHolder {
        public ImageView imageView;
        public TextView nameTextView;

    }


}
