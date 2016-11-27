package com.flysafely.probando;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class rankingFragment extends Fragment {

    private static final String AIRPORTS = "airports";

    private static final float MIN_RATING = (float) 0.5;
    private static final int RATING_FIELDS = 6;

    private static final float HALF_STAR = (float) 0.5;
    private static final float ONE_STAR = (float) 1.0;
    private static final float ONE_PLUS_HALF_STAR = (float) 1.5;
    private static final float TWO_STARS = (float) 2.0;
    private static final float TWO_PLUS_HALF_STAR = (float) 2.5;
    private static final float THREE_STARS = (float) 3.0;
    private static final float THREE_PLUS_HALF_STARS = (float) 3.5;
    private static final float FOUR_STARS = (float) 4.0;
    private static final float FOUR_PLUS_HALF_STARS = (float) 4.5;
    private static final float FIVE_STARS = (float) 5.0;

    private static final float STARS_TO_POINTS_FACTOR = (float) 0.5;

    private static final int MIN_FLIGHT_VALUE = 1;
    private static final int MAX_FLIGHT_VALUE = 9999;

    private String urlstr = "";
    private ScrollView scroller;
    private EditText aerolineaET;
    private EditText nVuelo;
    private RatingBar amabilidadRB;
    private RatingBar comidaRB;
    private RatingBar puntualidadRB;
    private RatingBar PDVFSRB;
    private RatingBar confortRB;
    private RatingBar qpRB;
    private RatingBar generalRB;
    private RadioButton yesRB;
    private EditText comentariosET;

    private Button sendButton;


    private float prevAmabilidadRating;
    private float prevComidaRating;
    private float prevPuntualidadRating;
    private float prevPDVFSRating;
    private float prevConfortRating;
    private float prevQpRating;

    private float totalRatingSum;

    private ArrayList<String> airlinesTags;

    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogSend;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        switch (getResources().getConfiguration().orientation) {

            case Configuration.ORIENTATION_LANDSCAPE:
                return inflater.inflate(R.layout.fragment_ranking_rotated, null);

            default:
                return inflater.inflate(R.layout.fragment_ranking, null);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        airlinesTags = new ArrayList<String>();

        prevAmabilidadRating = MIN_RATING;
        prevComidaRating = MIN_RATING;
        prevPuntualidadRating = MIN_RATING;
        prevPDVFSRating = MIN_RATING;
        prevConfortRating = MIN_RATING;
        prevQpRating = MIN_RATING;

        totalRatingSum = MIN_RATING * RATING_FIELDS;

        scroller = (ScrollView) getView().findViewById(R.id.scroller);

        aerolineaET = (EditText) getView().findViewById(R.id.aerolineaET);
        nVuelo = (EditText) getView().findViewById(R.id.nVueloET);
        amabilidadRB = (RatingBar) getView().findViewById(R.id.amabilidadRB);
        comidaRB = (RatingBar) getView().findViewById(R.id.comidaRB);
        puntualidadRB = (RatingBar) getView().findViewById(R.id.puntualidadRB);
        PDVFSRB = (RatingBar) getView().findViewById(R.id.PDVFSRB);
        confortRB = (RatingBar) getView().findViewById(R.id.confortRB);
        qpRB = (RatingBar) getView().findViewById(R.id.qpRB);
        generalRB = (RatingBar) getView().findViewById(R.id.generalRB);
        yesRB = (RadioButton) getView().findViewById(R.id.YesRB);
        comentariosET = (EditText) getView().findViewById(R.id.comentariosET);

        sendButton = (Button) getView().findViewById(R.id.sendButton);

        setRatingListeners();

//        amabilidadRB.getProgressDrawable().setTint(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null));
//        comidaRB.getProgressDrawable().setTint(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null));
//        puntualidadRB.getProgressDrawable().setTint(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null));
//        PDVFSRB.getProgressDrawable().setTint(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null));
//        confortRB.getProgressDrawable().setTint(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null));
//        qpRB.getProgressDrawable().setTint(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null));
//        generalRB.getProgressDrawable().setTint(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null));


        if (sendButton != null) {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkForm()) {
                        updateUrl();
                        new rankingFragment.HttpGetTask().execute();
                    }
                }
            });

        }

        if (savedInstanceState == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getActivity().getString(R.string.loading_airlines));
            progressDialog.setCancelable(false);
            progressDialog.show();

            getAirlines();
        }
        else {
            airlinesTags = (ArrayList<String>) savedInstanceState.getSerializable(AIRPORTS);
        }

    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(AIRPORTS, airlinesTags);

    }

    private void updateUrl(){

        boolean check = yesRB.isChecked();

        String comments = comentariosET.getText().toString().replaceAll(" ", "%20");

        urlstr = "http://hci.it.itba.edu.ar/v1/api/review.groovy?method=reviewairline2&review=%7b%22flight%22:%7b%22airline%22:%7b%22id%22:%22"
                + aerolineaET.getText().toString() + "%22%7d,%22number%22:" + nVuelo.getText().toString() + "%7d,%22rating%22:%7b%22friendliness%22:"
                + convertRB(amabilidadRB) + ",%22food%22:" + convertRB(comidaRB) + ",%22punctuality%22:" + convertRB(puntualidadRB)
                + ",%22mileage_program%22:" + convertRB(PDVFSRB) + ",%22comfort%22:" + convertRB(confortRB) + ",%22quality_price%22:" + convertRB(qpRB) + "%7d,%22yes_recommend%22:"
                + check + ",%22comments%22:%22" + comments + "%22%7d" ;

    }

    private int convertRB(RatingBar rb) {

        float ratingValue = rb.getRating();

        float pointsValue = MIN_RATING / STARS_TO_POINTS_FACTOR;

        // no consideramos el caso de 0 estrellas, ya que este es redondiado a 1 punto

        if (ratingValue == HALF_STAR)
            pointsValue = HALF_STAR / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == ONE_STAR)
            pointsValue = ONE_STAR / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == ONE_PLUS_HALF_STAR)
            pointsValue = ONE_PLUS_HALF_STAR / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == TWO_STARS)
            pointsValue = TWO_STARS / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == TWO_PLUS_HALF_STAR)
            pointsValue = TWO_PLUS_HALF_STAR / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == THREE_STARS)
            pointsValue = THREE_STARS / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == THREE_PLUS_HALF_STARS)
            pointsValue = THREE_STARS / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == FOUR_STARS)
            pointsValue = FOUR_STARS / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == FOUR_PLUS_HALF_STARS)
            pointsValue = FOUR_PLUS_HALF_STARS / STARS_TO_POINTS_FACTOR;

        else if (ratingValue == FIVE_STARS)
            pointsValue = FIVE_STARS / STARS_TO_POINTS_FACTOR;

        return (int) pointsValue;
    }

    private void getAirlines() {

        String  url = "http://hci.it.itba.edu.ar/v1/api/misc.groovy?method=getairlines";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    loadAirlines(response.getJSONArray("airlines"));
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), R.string.processing_airlines_error , Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.download_airlines_error, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
        RequestManager.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }

    private void loadAirlines(JSONArray airlines) throws JSONException {


        for(int i=0; i<airlines.length(); i++)
            airlinesTags.add((airlines.getJSONObject(i)).getString("id"));

        progressDialog.dismiss();

    }


    public boolean checkForm() {

        boolean airlineValidates = true;
        boolean flightnumberValidates = true;
        boolean networkIsAvaible = true;

        for ( String s : airlinesTags)
            Log.i("INFO", s);

        if (!isNetworkAvailable()) {
            Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
            networkIsAvaible = false;
        }

        if (!(airlinesTags.contains(aerolineaET.getText().toString()))) {
            airlineValidates = false;
        }

        if (nVuelo.getText().toString().equals("")) {
            flightnumberValidates = false;
        }

        if (flightnumberValidates) {
            int num = Integer.parseInt(nVuelo.getText().toString());

            if (!(num >= MIN_FLIGHT_VALUE && num <= MAX_FLIGHT_VALUE)) {
                flightnumberValidates = false;
            }
        }

        if (airlineValidates)
            aerolineaET.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null), PorterDuff.Mode.SRC_ATOP);

        if (flightnumberValidates)
            nVuelo.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null), PorterDuff.Mode.SRC_ATOP);

        if (!airlineValidates) {
            scroller.fullScroll(ScrollView.FOCUS_UP);
            Selection.setSelection(aerolineaET.getText(),aerolineaET.getSelectionStart());
            aerolineaET.requestFocus();
            Toast.makeText(getActivity(), R.string.airline_error, Toast.LENGTH_LONG).show();
            aerolineaET.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorRed, null), PorterDuff.Mode.SRC_ATOP);
        }

        if (!flightnumberValidates) {

            if (airlineValidates) {
                scroller.fullScroll(ScrollView.FOCUS_UP);
                Selection.setSelection(nVuelo.getText(), nVuelo.getSelectionStart());
                nVuelo.requestFocus();
            }
            Toast.makeText(getActivity(), R.string.flightnumber_error, Toast.LENGTH_LONG).show();
            nVuelo.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorRed, null), PorterDuff.Mode.SRC_ATOP);
        }


        return (airlineValidates && flightnumberValidates && networkIsAvaible);
    }


    public void setRatingListeners() {


        amabilidadRB.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override public void onRatingChanged(RatingBar ratingBar, float rating,
                                                  boolean fromUser) {

                if (rating < MIN_RATING) {
                    rating = MIN_RATING;
                    Toast.makeText(getActivity(), R.string.error_minRating, Toast.LENGTH_LONG).show();

                }

                totalRatingSum -= prevAmabilidadRating;

                totalRatingSum += rating;

                prevAmabilidadRating = rating;

                generalRB.setRating(totalRatingSum / RATING_FIELDS);

                ratingBar.setRating(rating);
            }
        });

        comidaRB.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override public void onRatingChanged(RatingBar ratingBar, float rating,
                                                  boolean fromUser) {

                if (rating < MIN_RATING) {
                    Toast.makeText(getActivity(), R.string.error_minRating, Toast.LENGTH_LONG).show();
                    rating = MIN_RATING;
                }

                totalRatingSum -= prevComidaRating;

                totalRatingSum += rating;

                prevComidaRating = rating;

                generalRB.setRating(totalRatingSum / RATING_FIELDS);

                ratingBar.setRating(rating);
            }
        });

        puntualidadRB.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override public void onRatingChanged(RatingBar ratingBar, float rating,
                                                  boolean fromUser) {

                if (rating < MIN_RATING) {
                    Toast.makeText(getActivity(), R.string.error_minRating, Toast.LENGTH_LONG).show();
                    rating = MIN_RATING;
                }

                totalRatingSum -= prevPuntualidadRating;

                totalRatingSum += rating;

                prevPuntualidadRating = rating;

                generalRB.setRating(totalRatingSum / RATING_FIELDS);

                ratingBar.setRating(rating);
            }
        });

        PDVFSRB.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override public void onRatingChanged(RatingBar ratingBar, float rating,
                                                  boolean fromUser) {

                if (rating < MIN_RATING) {
                    Toast.makeText(getActivity(), R.string.error_minRating, Toast.LENGTH_LONG).show();
                    rating = MIN_RATING;
                }

                totalRatingSum -= prevPDVFSRating;

                totalRatingSum += rating;

                prevPDVFSRating = rating;

                generalRB.setRating(totalRatingSum / RATING_FIELDS);

                ratingBar.setRating(rating);
            }
        });

        confortRB.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override public void onRatingChanged(RatingBar ratingBar, float rating,
                                                  boolean fromUser) {

                if (rating < MIN_RATING) {
                    Toast.makeText(getActivity(), R.string.error_minRating, Toast.LENGTH_LONG).show();
                    rating = MIN_RATING;
                }

                totalRatingSum -= prevConfortRating;

                totalRatingSum += rating;

                prevConfortRating = rating;

                generalRB.setRating(totalRatingSum / RATING_FIELDS);

                ratingBar.setRating(rating);
            }
        });

        qpRB.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override public void onRatingChanged(RatingBar ratingBar, float rating,
                                                  boolean fromUser) {

                if (rating < MIN_RATING) {
                    Toast.makeText(getActivity(), R.string.error_minRating, Toast.LENGTH_LONG).show();
                    rating = MIN_RATING;
                }

                totalRatingSum -= prevQpRating;

                totalRatingSum += rating;

                prevQpRating = rating;

                generalRB.setRating(totalRatingSum / RATING_FIELDS);

                ratingBar.setRating(rating);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
 private class HttpGetTask extends AsyncTask<Void, Void, String> {


     @Override
     protected void onPreExecute() {
         sendButton.setEnabled(false);
         progressDialog = new ProgressDialog(getActivity());
         progressDialog.setMessage(getActivity().getString(R.string.sending_calificate));
         progressDialog.setCancelable(false);
         progressDialog.show();
     }

     @Override
    protected String doInBackground(Void... params) {

        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlstr);

            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return readStream(in);
        } catch (Exception exception) {
            return null;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    @Override

    protected void onPostExecute(String result) {

        sendButton.setEnabled(true);

        if (result == null) {
            Toast.makeText(getActivity(), R.string.error_send, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getActivity(), R.string.error_succeed, Toast.LENGTH_LONG).show();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(aerolineaET.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(nVuelo.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(comentariosET.getWindowToken(), 0);

            MainActivity.goHome();
        }

    }

    private String readStream(InputStream inputStream) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int i = inputStream.read();
            while (i != -1) {
                outputStream.write(i);
                i = inputStream.read();
            }

            return outputStream.toString();
        } catch (IOException e) {
            return null;
        }
    }
}



}




