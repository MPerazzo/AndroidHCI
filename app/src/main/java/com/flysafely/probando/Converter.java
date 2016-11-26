package com.flysafely.probando;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;

/**
 * Created by Administrador on 17/11/2016.
 */

public class Converter {

    public static final String SCHEDULED = "S";
    public static final String ACTIVE = "A";
    public static final String DIVERTED = "R";
    public static final String LANDED = "L";
    public static final String CANCELLED = "C";


    public static String getDate(String date) {
        String[] split = date.split(" ");

        String[] dma = split[0].split("-");

        return dma[2] + "/" + dma[1] + "/" + dma[0];
    }

    public static String getTime(String date) {
        String[] split = date.split(" ");

        String[] time = split[1].split(":");

        return time[0] + ":" + time[1];
     }

    public static int statusColor(String status) {

        switch (status){
            case SCHEDULED:
                return Color.GREEN;
            case ACTIVE:
                return Color.GREEN;
            case LANDED:
                return Color.GREEN;
            case DIVERTED:
                return Color.YELLOW;
            case CANCELLED:
                return Color.RED;
            default:
                return Color.BLACK;
        }
    }

    @NonNull
    public static String getStatus(Context context, String status){

        switch (status){
            case SCHEDULED:
                return context.getString(R.string.scheduled_text);
            case ACTIVE:
                return context.getString(R.string.active_text);
            case LANDED:
                return context.getString(R.string.landed_text);
            case DIVERTED:
                return context.getString(R.string.diverted_text);
            case CANCELLED:
                return context.getString(R.string.cancelled_text);
            default:
                return context.getString(R.string.err_status);
        }
    }
}
