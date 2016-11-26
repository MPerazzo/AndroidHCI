package com.flysafely.probando;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrador on 20/11/2016.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    Context context;
    public static final String ACTION_NEW_MESSAGE = "ar.edu.itba.hci.ACTION_NEW_MESSAGE";


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        SharedPreferences settings = context.getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

        Set<String> alerts = settings.getStringSet("ALERTS", null);

        if (alerts==null){
            return;
        }

        updateFlight(alerts.iterator(), null);
    }

    private void updateFlight(final Iterator<String> iterator, final Set<String> updatedFavorites) {

        Gson
                gson = new Gson();

        if(iterator.hasNext()) {
            final Flight
                    flight = gson.fromJson(iterator.next(), Flight.class);

            String
                    url = "http://hci.it.itba.edu.ar/v1/api/status.groovy?method=getflightstatus&airline_id=" + flight.getAirline().getId() + "&flight_number=" + flight.getNumber().toString();
            JsonObjectRequest
                    jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    JSONObject
                            data = null;
                    try {
                        data = response.getJSONObject("status");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Gson
                            gson = new Gson();
                    String
                            serializedFlight = data.toString();
                    Set<String>
                            newFavs;

                    if(updatedFavorites!=null)
                        newFavs = new HashSet<>(updatedFavorites);
                    else
                        newFavs = new HashSet<>();

                    newFavs.add(serializedFlight);

                    Flight newFlight = gson.fromJson(serializedFlight, Flight.class);

                    checkChange(flight, newFlight);

                    updateFlight(iterator, newFavs);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            RequestManager.getInstance(context).addToRequestQueue(jsObjRequest);
        }
        else {
            SharedPreferences
                    settings = context.getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

            SharedPreferences.Editor editor = settings.edit();
            if(updatedFavorites!=null) {
                editor.putStringSet("ALERTS", updatedFavorites);
                editor.apply();
            }
        }

    }

    private void checkChange(Flight flight, Flight newFlight) {

        boolean hasDiff = true;

        String contextTitle, contextText;
        ArrayList<String> contextBigText = new ArrayList<String>();
        contextTitle = context.getString(R.string.flight_text) + newFlight.getDeparture().getAirport().getCity().getId() + "-" + newFlight.getArrival().getAirport().getCity().getId();
        contextText = context.getString(R.string.state_text) + Converter.getStatus(context, newFlight.getStatus());
        contextBigText.add(context.getString(R.string.state_text) + Converter.getStatus(context, newFlight.getStatus()));

        if(!flight.getStatus().equals(newFlight.getStatus())) {
            hasDiff = true;
        }

        if(flight.getDeparture().getAirport().getTerminal() != null) {
            if(!flight.getDeparture().getAirport().getTerminal().equals(newFlight.getDeparture().getAirport().getTerminal())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.terminal_dep_text) + newFlight.getDeparture().getAirport().getTerminal());
            }
        } else if(newFlight.getDeparture().getAirport().getTerminal() != null){
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.terminal_dep_text) + newFlight.getDeparture().getAirport().getTerminal());
        }

        if(flight.getDeparture().getAirport().getGate() != null) {
            if(!flight.getDeparture().getAirport().getGate().equals(newFlight.getDeparture().getAirport().getGate())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.gate_dep_text) + newFlight.getDeparture().getAirport().getGate());
            }
        } else if(newFlight.getDeparture().getAirport().getGate() != null){
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.gate_dep_text) + newFlight.getDeparture().getAirport().getGate());
        }

        if(flight.getArrival().getAirport().getTerminal() != null) {
            if(!flight.getArrival().getAirport().getTerminal().equals(newFlight.getArrival().getAirport().getTerminal())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.terminal_arr_text) + newFlight.getArrival().getAirport().getTerminal());
            }
        } else if(newFlight.getArrival().getAirport().getTerminal() != null){
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.terminal_arr_text) + newFlight.getArrival().getAirport().getTerminal());
        }

        if(flight.getArrival().getAirport().getGate() != null) {
            if(!flight.getArrival().getAirport().getGate().equals(newFlight.getArrival().getAirport().getGate())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.gate_arr_text) + newFlight.getArrival().getAirport().getGate());
            }
        } else if(newFlight.getArrival().getAirport().getGate() != null){
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.gate_arr_text) + newFlight.getArrival().getAirport().getGate());
        }

        if(flight.getArrival().getAirport().getBaggage() != null) {
            if(!flight.getArrival().getAirport().getBaggage().equals(newFlight.getArrival().getAirport().getBaggage())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.baggageGate_text) + newFlight.getArrival().getAirport().getBaggage());
            }
        } else if(newFlight.getArrival().getAirport().getBaggage() != null){
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.baggageGate_text) + newFlight.getArrival().getAirport().getBaggage());
        }

        if(flight.getDeparture().getActual_time() != null) {

            if(!flight.getDeparture().getActual_time().equals(newFlight.getDeparture().getActual_time())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.time_dep_text) + newFlight.getDeparture().getActual_time());
            }

        } else if(flight.getDeparture().getScheduled_time() != null) {

            if(!flight.getDeparture().getScheduled_time().equals(newFlight.getDeparture().getScheduled_time())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.time_dep_text) + newFlight.getDeparture().getScheduled_time());
            }

        } else if(newFlight.getDeparture().getActual_gate_time() != null) {
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.time_dep_text) + newFlight.getDeparture().getActual_time());
        } else if(newFlight.getDeparture().getScheduled_time() != null) {
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.time_dep_text) + newFlight.getDeparture().getActual_time());
        }

        if(flight.getArrival().getActual_time() != null) {

            if(!flight.getArrival().getActual_time().equals(newFlight.getArrival().getActual_time())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.time_arr_text) + newFlight.getArrival().getActual_time());
            }

        } else if(flight.getArrival().getScheduled_time() != null) {

            if(!flight.getArrival().getScheduled_time().equals(newFlight.getArrival().getScheduled_time())) {
                hasDiff = true;
                contextBigText.add(contextBigText + context.getString(R.string.time_arr_text) + newFlight.getArrival().getScheduled_time());
            }

        } else if(newFlight.getArrival().getActual_gate_time() != null) {
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.time_arr_text) + newFlight.getArrival().getActual_time());
        } else if(newFlight.getArrival().getScheduled_time() != null) {
            hasDiff = true;
            contextBigText.add(contextBigText + context.getString(R.string.time_arr_text) + newFlight.getArrival().getScheduled_time());
        }

        if(hasDiff) {
            sendNotification(newFlight, contextTitle, contextText, contextBigText);
        }
        sendNotification(newFlight, contextTitle, contextText, contextBigText);
    }

    private void sendNotification(Flight newFlight, String contextTitle, String contextText, ArrayList<String> contextBigText) {

        String airlane = newFlight.getAirline().getId();
        Integer flightNumber = newFlight.getNumber();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        //notificationIntent.putExtra("mainFragment", "flightStatusFragment");
        //notificationIntent.putExtra("AIRLINE",airlane);
        //notificationIntent.putExtra("FLIGHT_NUMBER",flightNumber.toString());

        android.app.TaskStackBuilder stackBuilder = android.app.TaskStackBuilder.create(context);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        final PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(contextTitle)
                .setContentText(contextText)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo))
                .setSmallIcon(R.mipmap.logo)
                .setContentIntent(contentIntent);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(contextTitle)
                .setSummaryText(airlane + "-" + flightNumber.toString());
        for(String line : contextBigText) {

            inboxStyle.addLine(line);
        }
        mBuilder.setStyle(inboxStyle);
        Notification notification = mBuilder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(getNotificationId(airlane, flightNumber), notification);

    }

    private int getNotificationId(String airlane, Integer flightNumber) {

        return airlane.hashCode() * flightNumber;
    }
}


