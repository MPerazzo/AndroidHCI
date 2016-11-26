package com.flysafely.probando;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matias on 16/11/16.
 */

public class OffersFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private static final String SELECTED_LAT = "slat";

    private static final String SELECTED_LONG = "slong";

    private static final String CITY_FROM = "cfrom";

    private static final String CITY_DESC = "cdesc";

    private static final String CITIES_STRING_BASE = "http://hci.it.itba.edu.ar/v1/api/geo.groovy?method=getcitiesbyposition";

    private static final String OFFERS_STRING_BASE = "http://hci.it.itba.edu.ar/v1/api/booking.groovy?method=getflightdeals&from=";

    private static final int RADIUS = 100;

    private static final float CHEAP_PRICE_LIMIT = 325;

    private static final float MEDIUM_PRICE_LIMIT = 600;

    private MapView mapView;

    private GoogleMap map;

    private TextView offer_text;

    private LatLng user_position;

    private Polyline line;

    private ProgressDialog progressDialog;

    private Marker currentSelected;

    private String cityFrom;

    private String cityDesc;

    private Bundle bundle;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offers, null);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        cityFrom = null;

        cityDesc = null;

        mapView = (MapView) view.findViewById(R.id.map);

        offer_text = (TextView) getActivity().findViewById(R.id.offer_text);
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        bundle = savedInstanceState;

        super.onActivityCreated(savedInstanceState);

        mapView.onCreate(savedInstanceState);
//        mapView.onResume();
        mapView.getMapAsync(this);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        double selectedLatitude;
        double selectedLongitude;

        if (currentSelected == null) {

            selectedLatitude = GPSTracker.LAT_ERROR_VALUE;

            selectedLongitude = GPSTracker.LONG_ERROR_VALUE;
        }
        else {

            selectedLatitude = currentSelected.getPosition().latitude;

            selectedLongitude = currentSelected.getPosition().longitude;
        }

        outState.putDouble(SELECTED_LAT, selectedLatitude);
        outState.putDouble(SELECTED_LONG, selectedLongitude);
        outState.putString(CITY_FROM, cityFrom);
        outState.putString(CITY_DESC, cityDesc);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.clear();
        map.setOnMarkerClickListener(this);

        if ( MainActivity.getLatitude() == GPSTracker.LAT_ERROR_VALUE || MainActivity.getLongitud() == GPSTracker.LONG_ERROR_VALUE) {
            offer_text.setText(R.string.location_failed);
            return;
        }

        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getActivity().getString(R.string.loading_map));
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (bundle != null) {

            cityFrom = bundle.getString(CITY_FROM);
            cityDesc = bundle.getString(CITY_DESC);

            if ( cityFrom != null && cityDesc != null) {
                loadOffers(cityFrom, cityDesc);
                return;
            }
        }

        // this function calls loadOffers
        getNearestCity();
    }

    public void getNearestCity() {

        String location_stringEnd = "&latitude=" + MainActivity.getLatitude() + "&longitude=" + MainActivity.getLongitud()
                + "&radius=" + RADIUS;

        String url = CITIES_STRING_BASE + location_stringEnd;

        JsonObjectRequest
                jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    CityNearBy(response);
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), R.string.processing_nearCity_error, Toast.LENGTH_LONG).show();
                    offer_text.setText(R.string.city_not_found);
                    progressDialog.dismiss();
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.download_nearCity_error, Toast.LENGTH_LONG).show();
                offer_text.setText(R.string.city_not_found);
                progressDialog.dismiss();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        });
        RequestManager.getInstance(getActivity()).addToRequestQueue(jsObjRequest);


    }

    private void CityNearBy(JSONObject data) throws JSONException {

        // First city in array is took as "From" for GoogleMap.

        JSONArray cities = data.getJSONArray("cities");

        JSONObject Firstelem = cities.getJSONObject(0);

        String cityFrom = Firstelem.getString("id");

        String cityDesc = Firstelem.getString("name");

        if (cityFrom == null) {
            offer_text.setText(R.string.city_not_found);
            return;
        }

        loadOffers(cityFrom, cityDesc);
    }

    private void loadOffers(String cityFrom, String cityDesc) {

        String url = OFFERS_STRING_BASE + cityFrom;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override public void onResponse(JSONObject response) {

                if (response == null)
                    return;

                JSONArray data = null;
                try {
                    data = response.getJSONArray("deals");
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), R.string.download_deals_error , Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
                for (int i = 0; i < data.length(); i++)
                {
                    double latitude;
                    double longitude;
                    try {
                        latitude = Double.parseDouble(data.getJSONObject(i).getJSONObject("city").getString("latitude"));
                        longitude =  Double.parseDouble(data.getJSONObject(i).getJSONObject("city").getString("longitude"));
                        LatLng city = new LatLng(latitude, longitude);

                        Marker marker;

                        if(Double.parseDouble(data.getJSONObject(i).getString("price")) < new Double(CHEAP_PRICE_LIMIT)){
                            marker = map.addMarker(new MarkerOptions()
                                    .position(city)
                                    .title(data.getJSONObject(i).getJSONObject("city").getString("name"))
                                    .snippet(getString(R.string.precio) + " " + response.getJSONObject("currency").getString("id") + " " + data.getJSONObject(i).getString("price"))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }
                        else if(Double.parseDouble(data.getJSONObject(i).getString("price")) <= new Double(MEDIUM_PRICE_LIMIT)){
                             marker =map.addMarker(new MarkerOptions()
                                    .position(city)
                                    .title(data.getJSONObject(i).getJSONObject("city").getString("name"))
                                    .snippet(getString(R.string.precio) + " " + response.getJSONObject("currency").getString("id") + " " + data.getJSONObject(i).getString("price"))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                        }else {
                            marker = map.addMarker(new MarkerOptions()
                                    .position(city)
                                    .title(data.getJSONObject(i).getJSONObject("city").getString("name"))
                                    .snippet(getString(R.string.precio) + " " + response.getJSONObject("currency").getString("id") + " " + data.getJSONObject(i).getString("price"))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }

                        if (bundle != null) {

                            double latSelected = bundle.getDouble(SELECTED_LAT);
                            double longSelected = bundle.getDouble(SELECTED_LONG);

                            if (latSelected != GPSTracker.LAT_ERROR_VALUE && longSelected != GPSTracker.LONG_ERROR_VALUE) {
                                line = map.addPolyline(new PolylineOptions()
                                        .add(user_position, new LatLng(latSelected, longSelected))
                                        .width(5)
                                        .color(Color.DKGRAY));

                                if ( latitude == latSelected && longitude == longSelected ) {
                                    marker.showInfoWindow();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), R.string.processing_deals_error , Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.database_connection_failed , Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        });
        RequestManager.getInstance(getActivity()).addToRequestQueue(jsObjRequest);


        // move camera to user location

        user_position = new LatLng(MainActivity.getLatitude(), MainActivity.getLongitud());

        CameraUpdate touserLocation = CameraUpdateFactory.newLatLngZoom(user_position, 2);

        map.animateCamera(touserLocation);

        // We set the user marker once we know there is no API or Location error
        Marker userMarker;

        userMarker = map.addMarker(new MarkerOptions()
                .position(user_position)
                .title(cityDesc)
                .snippet(getString(R.string.map_userpos_text))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

        userMarker.showInfoWindow();

        progressDialog.dismiss();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();

        currentSelected = marker;

        if (line!=null)
            line.remove();

        line = map.addPolyline(new PolylineOptions()
                .add(user_position, marker.getPosition())
                .width(5)
                .color(Color.DKGRAY));

        return true;
    }


    @Override
    public void onStop() {
        super.onStop();

        if(progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mapView != null)
            mapView.onDestroy();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}

