package com.flysafely.probando;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrador on 17/11/2016.
 */

public class SaveAlertFragment extends Fragment {

    private View parent;
    private EditText airlaneInput;
    private EditText flightNumberInput;
    private ProgressDialog progressDialog;
    private boolean airlineCompleted;
    private boolean flightCompleted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parent = inflater.inflate(R.layout.fragment_save_alert, null);

        MainActivity.setActionBarTitle(getString(R.string.title_fragment_addalert));
        MainActivity.showUpButton();

        airlineCompleted= false;
        flightCompleted = false;

        airlaneInput = (EditText) parent.findViewById(R.id.editText_AirlaneCode);
        flightNumberInput = (EditText) parent.findViewById(R.id.editText_FlightNumber);

        Button search = (Button) parent.findViewById(R.id.save_button);

        if (search != null) {
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (validData()) {

                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage(getActivity().getString(R.string.sending_alerts));
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        String url = "http://hci.it.itba.edu.ar/v1/api/status.groovy?method=getflightstatus&airline_id=" + airlaneInput.getText().toString() + "&flight_number=" + flightNumberInput.getText().toString();

                        final JsonObjectRequest
                                jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                if (response.has("error")) {
                                    displayError(0);
                                } else {
                                    JSONObject data = null;
                                    try {
                                        data = response.getJSONObject("status");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

                                    Set<String> alertsGet = settings.getStringSet("ALERTS", null);
                                    Set<String> alertsToSave;
                                    if (alertsGet == null) {
                                        alertsToSave = new HashSet<String>();
                                    } else {
                                        alertsToSave = new HashSet<String>(alertsGet);
                                    }
                                    alertsToSave.add(data.toString());

                                    SharedPreferences.Editor editor = settings.edit();

                                    if(alertsToSave != null) {
                                        editor.putStringSet("ALERTS", alertsToSave);
                                        editor.apply();
                                    }

                                    airlaneInput.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null), PorterDuff.Mode.SRC_ATOP);
                                    flightNumberInput.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorTeal, null), PorterDuff.Mode.SRC_ATOP);

                                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(airlaneInput.getWindowToken(), 0);
                                    imm.hideSoftInputFromWindow(flightNumberInput.getWindowToken(), 0);

                                    progressDialog.dismiss();

                                    MainActivity.AddtoBackStack(new ListAlertFragment(), getString(R.string.title_fragment_alerts));

                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                displayError(1);
                            }
                        });
                        RequestManager.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
                    } else {

                        if (!airlineCompleted && !flightCompleted) {
                            Toast.makeText(getActivity(), R.string.fields_incomplete_msg, Toast.LENGTH_LONG).show();
                            airlaneInput.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorRed, null), PorterDuff.Mode.SRC_ATOP);
                            flightNumberInput.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorRed, null), PorterDuff.Mode.SRC_ATOP);
                        }

                        else if (!airlineCompleted) {
                            Toast.makeText(getActivity(), R.string.fields_incomplete_msg, Toast.LENGTH_LONG).show();
                            airlaneInput.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorRed, null), PorterDuff.Mode.SRC_ATOP);
                        }

                        else {
                            Toast.makeText(getActivity(), R.string.fields_incomplete_msg, Toast.LENGTH_LONG).show();
                            flightNumberInput.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorRed, null), PorterDuff.Mode.SRC_ATOP);
                        }

                    }
                }
            });
        }

        return parent;
    }

    private boolean validData() {

        if(airlaneInput.getText().toString().equals("")) {
            airlineCompleted = false;
        }
        else
            airlineCompleted = true;

        if(flightNumberInput.getText().toString().equals("")) {
            flightCompleted = false;
        }
        else
            flightCompleted = true;

        return airlineCompleted && flightCompleted;
    }
    private void displayError(int error) {
        int
                errorMsg;

        switch (error) {

            case 0:

                errorMsg = R.string.invalid_flight;
                Selection.setSelection(airlaneInput.getText(), airlaneInput.getSelectionStart());
                airlaneInput.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorRed, null), PorterDuff.Mode.SRC_ATOP);
                flightNumberInput.getBackground().mutate().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorRed, null), PorterDuff.Mode.SRC_ATOP);
                break;

            case 1:

                errorMsg = R.string.conection_error;
                break;

            default:

                errorMsg = R.string.request_error;

        }

        progressDialog.dismiss();
        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
    }
}
