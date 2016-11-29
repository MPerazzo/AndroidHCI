package com.flysafely.probando;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by matias on 16/11/16.
 */

public class MainFragment extends Fragment {

    private static final String OFFERS_ITEMS = "items";

    private static final String CITIES_STRING_BASE = "http://hci.it.itba.edu.ar/v1/api/geo.groovy?method=getcitiesbyposition";

    private static final int RADIUS = 100;

    private ArrayList<Offer> offerArray;

    private static final int CANT_OFFERS = 3;

    private ProgressDialog progressDialog;

    private TextView mainText;

    private ListView listView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        switch (getResources().getConfiguration().orientation) {

            case Configuration.ORIENTATION_LANDSCAPE:
                return inflater.inflate(R.layout.fragment_main_rotated, null);

            default:
                return inflater.inflate(R.layout.fragment_main, null);
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        offerArray= new ArrayList<>();

        mainText = (TextView) getView().findViewById(R.id.main_text);

        listView = (ListView) getView().findViewById(R.id.offers_resume_list_view);

    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        MainActivity.setHomeTitle();

        if ( MainActivity.getLatitude() == GPSTracker.LAT_ERROR_VALUE || MainActivity.getLongitud() == GPSTracker.LONG_ERROR_VALUE) {
            mainText.setText(R.string.location_failed);
            MainActivity.EnableGoHome();
            return;
        }

        if (savedInstanceState != null) {
            offerArray = (ArrayList<Offer>) savedInstanceState.getSerializable(OFFERS_ITEMS);

            if (offerArray == null) {
                offerArray= new ArrayList<>();
                getNearestCity();
            }
            else
                listView.setAdapter(new OfferArrayAdapter(getActivity(), offerArray));
            return;
        }

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getActivity().getString(R.string.loading_offers));
        progressDialog.setCancelable(false);
        progressDialog.show();
        getNearestCity();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(OFFERS_ITEMS, offerArray);

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
                    mainText.setText(R.string.city_not_found);
                    MainActivity.EnableGoHome();
                    progressDialog.dismiss();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.download_nearCity_error, Toast.LENGTH_LONG).show();
                MainActivity.EnableGoHome();
                mainText.setText(R.string.city_not_found);
                progressDialog.dismiss();

            }
        });
        RequestManager.getInstance(getActivity()).addToRequestQueue(jsObjRequest);


    }

    private void CityNearBy(JSONObject data) throws JSONException {

        // First city in array is took as "From" for GoogleMap.

        JSONArray cities = data.getJSONArray("cities");

        JSONObject Firstelem = cities.getJSONObject(0);

        String cityFrom = Firstelem.getString("id");

        if (cityFrom == null) {
            mainText.setText(R.string.city_not_found);
            return;
        }

        getLastMinuteOffers(cityFrom);

    }


    private void getLastMinuteOffers(String cityFrom) {

        String  url = "http://hci.it.itba.edu.ar/v1/api/booking.groovy?method=getlastminuteflightdeals&from=" + cityFrom;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (isAdded()) {
                        loadLastMinuteOffers(response);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), R.string.processing_deals_error , Toast.LENGTH_LONG).show();
                    MainActivity.EnableGoHome();
                    progressDialog.dismiss();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.download_deals_error, Toast.LENGTH_LONG).show();
                MainActivity.EnableGoHome();
                progressDialog.dismiss();

            }
        });
        RequestManager.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }

    private void loadLastMinuteOffers(JSONObject data) throws JSONException {

        JSONArray deals = data.getJSONArray("deals");

        JSONObject dealObject;

        JSONObject cityObject;

        Bitmap bitmap;

        for(int i=0; i<CANT_OFFERS; i++) {

            dealObject = deals.getJSONObject(i);
            cityObject = dealObject.getJSONObject("city");

            String landscapeID = "offer_landscape_" + (i+1);
            int resID = getResources().getIdentifier(landscapeID, "drawable", BuildConfig.APPLICATION_ID);
            bitmap = BitmapFactory.decodeResource(getResources(), resID);

            offerArray.add(new Offer(cityObject.getString("name"), dealObject.getDouble("price"), bitmap));
        }

        listView.setAdapter(new OfferArrayAdapter(getActivity(), offerArray));

        if (progressDialog != null)
            progressDialog.dismiss();

        MainActivity.EnableGoHome();
    }

    @Override
    public void onStop() {
        super.onStop();

        if(progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onStop();

        if(progressDialog != null)
            progressDialog.dismiss();
    }

}
