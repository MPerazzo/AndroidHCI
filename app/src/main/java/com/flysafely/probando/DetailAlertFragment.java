package com.flysafely.probando;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class DetailAlertFragment extends Fragment {

    private static final String STATUS_DETAIL = "status";
    private static final String STATUS_COLOR = "status_color";
    private static final String DEPARTURE_DETAIL = "departure";
    private static final String HDEPARTURE_DETAIL = "hdeparture";
    private static final String TDEP_DETAIL = "tdep";
    private static final String GDEP_DETAIL = "gdep";
    private static final String ARRIVAL_DETAIL = "arrival";
    private static final String HARR_DETAIL = "harr";
    private static final String TARR_DETAIL = "tarr";
    private static final String GARR_DETAIL = "garr";
    private static final String BAGARR_DETAIL = "bagarr";


    private Flight displayedFlight;
    private String serializedFlight;
    private String airlineCode, flightNumber;

    TextView status;
    int color;
    TextView departure;
    TextView hourDeparture;
    TextView terminalDep;
    TextView gateDep;
    TextView arrival;
    TextView hourArr;
    TextView terminalArr;
    TextView gateArr;
    TextView baggageGateArr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        switch (getResources().getConfiguration().orientation) {

            case Configuration.ORIENTATION_LANDSCAPE:
                return inflater.inflate(R.layout.fragment_status_alert_rotated, null);

            default:
                return inflater.inflate(R.layout.fragment_status_alert, null);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.showUpButton();

        status = (TextView) getView().findViewById(R.id.textView_state_detail_alert);
        departure = (TextView) getView().findViewById(R.id.textView_departure_detail_alert);
        hourDeparture = (TextView) getView().findViewById(R.id.textView_hourDep_detail_alert);
        terminalDep = (TextView) getView().findViewById(R.id.textView_terminalDep_detail_alert);
        gateDep = (TextView) getView().findViewById(R.id.textView_gateDep_detail_alert);
        arrival = (TextView) getView().findViewById(R.id.textView_arrival_detail_alert);
        hourArr = (TextView) getView().findViewById(R.id.textView_hourArr_detail_alert);
        terminalArr = (TextView) getView().findViewById(R.id.textView_terminalArr_detail_alert);
        gateArr = (TextView) getView().findViewById(R.id.textView_gateArr_detail_alert);
        baggageGateArr = (TextView) getView().findViewById(R.id.textView_baggateGateArr_detail_alert);
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null) {
            Bundle
                    args = getArguments();

            if(args == null) {
                airlineCode= null;
                flightNumber=null;
            } else {
                airlineCode= args.getString("AIRLINE");
                flightNumber= args.getString("FLIGHT_NUMBER");

            }
        }

        else {
            airlineCode = savedInstanceState.getString("AIRLINE");
            flightNumber = savedInstanceState.getString("FLIGHT_NUMBER");
        }

        MainActivity.setDetailAlertBarTitle(airlineCode + " " + flightNumber);

        getFlightStatus(airlineCode, flightNumber);

    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("AIRLINE", airlineCode);
        outState.putString("FLIGHT_NUMBER", flightNumber);
    }

    private void deleteFlight(String airlaneCode, String flightNumber) {

        SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

        Set<String> alertsGet = settings.getStringSet("ALERTS", null);
        Set<String> alerts;
        if (alertsGet == null) {
            alerts = new HashSet<String>();
        } else {
            alerts = new HashSet<String>(alertsGet);
        }

        Gson gson = new Gson();

        String delete = "";
        for(String alert : alerts) {
            Flight flight = gson.fromJson(alert, Flight.class);
            if(flight.getNumber().toString().equals(flightNumber) && flight.getAirline().getId().equals(airlaneCode)){
                delete = alert;
                break;
            }
        }

        alerts.remove(delete);

        SharedPreferences.Editor editor = settings.edit();

        editor.putStringSet("ALERTS", alerts);
        editor.apply();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail_alert, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_popup){
            deleteFlight();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getFlightStatus(String airlineCode, String flightNumber) {

        String url = "http://hci.it.itba.edu.ar/v1/api/status.groovy?method=getflightstatus&airline_id=" + airlineCode + "&flight_number=" + flightNumber;

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

                    String serializedFlight = data.toString();
                    Gson gson = new Gson();
                    Flight flight = gson.fromJson(serializedFlight, Flight.class);

                    deleteFlight(flight.getAirline().getId(), flight.getNumber().toString());

                    alertsToSave.add(data.toString());

                    SharedPreferences.Editor editor = settings.edit();

                    if(alertsToSave != null) {
                        editor.putStringSet("ALERTS", alertsToSave);
                        editor.apply();
                    }

                    displayFlight(data);

                }
            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayError(1);
            }
        });
        RequestManager.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }

    private void displayFlight(JSONObject data) {
        Gson gson = new Gson();

        serializedFlight = data.toString();
        displayedFlight = gson.fromJson(serializedFlight, Flight.class);

        if(displayedFlight.getStatus() != null) {
            status.setText(Converter.getStatus(getActivity(), displayedFlight.getStatus()));
            color = Converter.statusColor(displayedFlight.getStatus());
            status.setTextColor(color);
        } else {
            status.setText("-");
        }
        if(displayedFlight.getDeparture().getAirport().getCity().getId() != null){
            departure.setText(displayedFlight.getDeparture().getAirport().getCity().getId());
        } else {
            departure.setText("-");
        }
        if(displayedFlight.getDeparture().getActual_time() != null){
            hourDeparture.setText(Converter.getTime(displayedFlight.getDeparture().getActual_time()));
        } else if(displayedFlight.getDeparture().getScheduled_time() != null) {
            hourDeparture.setText(Converter.getTime(displayedFlight.getDeparture().getScheduled_time()));
        } else {
            hourDeparture.setText("-");
        }
        if(displayedFlight.getDeparture().getAirport().getTerminal() != null){
            terminalDep.setText(displayedFlight.getDeparture().getAirport().getTerminal());
        } else {
            terminalDep.setText("-");
        }
        if(displayedFlight.getDeparture().getAirport().getGate() != null) {
            gateDep.setText(displayedFlight.getDeparture().getAirport().getGate());
        } else {
            gateDep.setText("-");
        }
        if(displayedFlight.getArrival().getAirport().getCity().getId() != null){
            arrival.setText(displayedFlight.getArrival().getAirport().getCity().getId());
        } else {
            arrival.setText("-");
        }
        if(displayedFlight.getArrival().getActual_time() != null) {
            hourArr.setText(Converter.getTime(displayedFlight.getArrival().getActual_time()));
        } else if(displayedFlight.getArrival().getScheduled_time() != null) {
            hourArr.setText(Converter.getTime(displayedFlight.getArrival().getScheduled_time()));
        } else {
            hourArr.setText("-");
        }
        if(displayedFlight.getArrival().getAirport().getTerminal() != null){
            terminalArr.setText(displayedFlight.getArrival().getAirport().getTerminal());
        } else {
            terminalArr.setText("-");
        }
        if(displayedFlight.getArrival().getAirport().getGate() != null){
            gateArr.setText(displayedFlight.getArrival().getAirport().getGate());
        } else {
            gateArr.setText("-");
        }
        if(displayedFlight.getArrival().getAirport().getBaggage() != null){
            baggageGateArr.setText(displayedFlight.getArrival().getAirport().getBaggage());
        } else {
            baggageGateArr.setText("-");
        }

        return;
    }

    private void displayError(int error) {
        int
                errorMsg;

        switch (error) {
            case 0:
                errorMsg = R.string.invalid_flight;
                break;
            case 1:
                errorMsg = R.string.conection_error;
                break;
            default:
                errorMsg = R.string.request_error;
        }

        if (isAdded() && getActivity()!= null)
            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
    }

    public boolean deleteFlight() {

        if(airlineCode == null || flightNumber == null){
            return false;
        }
        deleteFlight(airlineCode, flightNumber);

        Toast.makeText(getActivity(), getActivity().getString(R.string.alert_delete)  + " - " + flightNumber.toString(), Toast.LENGTH_LONG).show();

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new ListAlertFragment()).commit();

        return true;
    }

}
