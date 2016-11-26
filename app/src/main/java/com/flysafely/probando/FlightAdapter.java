package com.flysafely.probando;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrador on 17/11/2016.
 */

public class FlightAdapter extends ArrayAdapter<Flight> {


    private Context context;
    int layoutResourceID;
    ArrayList<Flight> data = null;

    public FlightAdapter(Context context, int layoutResourceID, ArrayList<Flight> data) {
        super(context, layoutResourceID, data);

        this.context = context;
        this.layoutResourceID = layoutResourceID;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        FlightHolder holder = null;

        if(row == null) {

            row = LayoutInflater.from(getContext()).inflate(layoutResourceID, parent, false);

            holder = new FlightHolder();

            holder.departureArrival = (TextView) row.findViewById(R.id.textView_origin_dest_alert);
            holder.flightNumber = (TextView) row.findViewById(R.id.textView_flightNumber_alert);
            holder.airlane = (TextView) row.findViewById(R.id.textView_Airlane_alert);
            holder.date = (TextView) row.findViewById(R.id.textView_date_alert);
            holder.state = (TextView) row.findViewById(R.id.textView_state_alert);
            row.setTag(holder);

        } else {
            holder = (FlightHolder)row.getTag();
        }

        Flight flight;
        flight = getItem(position);


        holder.departureArrival.setText(flight.getDeparture().getAirport().getCity().getId().toUpperCase() + "-" + flight.getArrival().getAirport().getCity().getId().toUpperCase());
        holder.flightNumber.setText(flight.getNumber().toString());
        holder.airlane.setText(flight.getAirline().getId().toUpperCase());
        holder.date.setText(Converter.getDate(flight.getDeparture().getScheduled_time()));
        holder.state.setText(flight.getStatus().toUpperCase());
        holder.state.setTextColor(Converter.statusColor(flight.getStatus().toUpperCase()));

        return row;
    }

    static class FlightHolder {
        TextView departureArrival;
        TextView flightNumber;
        TextView airlane;
        TextView date;
        TextView state;
    }


}
