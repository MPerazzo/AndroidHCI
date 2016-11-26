package com.flysafely.probando;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by matias on 17/11/16.
 */

public class GPSTracker extends Service  {

    protected static final double LAT_ERROR_VALUE = -200;

    protected static final double LONG_ERROR_VALUE = -200;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    public boolean canGetLocation = false;

    Location currentLocation;

    LocationListener listener;

    double latitude;
    double longitude;

    protected LocationManager locationManager;

    public GPSTracker(LocationManager lm) {

        latitude = LAT_ERROR_VALUE;

        longitude = LONG_ERROR_VALUE;

        this.locationManager = lm;

        listener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                currentLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        calculateLocation();
    }

    public Location calculateLocation() {

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isGPSEnabled && !isNetworkEnabled) {

            return currentLocation=null;

        } else {

            this.canGetLocation = true;

            if (isNetworkEnabled) {

                try {

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);

                    if (locationManager != null) {
                        currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (currentLocation != null) {

                            latitude = currentLocation.getLatitude();
                            longitude = currentLocation.getLongitude();
                        }
                    }
                }

                catch (SecurityException e) {
                    return currentLocation=null;
                }
            }

            if(isGPSEnabled) {
                if(currentLocation == null) {

                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
                        if (locationManager != null) {
                            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (currentLocation != null) {

                                latitude = currentLocation.getLatitude();
                                longitude = currentLocation.getLongitude();
                            }
                        }
                    }

                    catch (SecurityException e) {
                        return currentLocation=null;
                    }
                }
            }
        }

        return currentLocation;
    }


    public double getLatitude() {
        if(currentLocation != null) {
            latitude = currentLocation.getLatitude();
        }

        if (latitude == LAT_ERROR_VALUE || longitude == LONG_ERROR_VALUE)
            calculateLocation();

        return latitude;
    }

    public double getLongitude() {
        if(currentLocation != null) {
            longitude = currentLocation.getLongitude();
        }

        if (latitude == LAT_ERROR_VALUE || longitude == LONG_ERROR_VALUE)
            calculateLocation();

        return longitude;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}