package com.flysafely.probando;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
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
import java.util.zip.Inflater;

/**
 * Created by Administrador on 17/11/2016.
 */

public class DetailAlertFragment extends Fragment {

    private View parent;
    private Flight displayedFlight;
    private String serializedFlight;
    private String airlineCode, flightNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
            airlineCode = (String) savedInstanceState.getSerializable("AIRLINE");
            flightNumber = (String) savedInstanceState.getSerializable("FLIGHT_NUMBER");
        }

        getFlightStatus(airlineCode, flightNumber);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parent =  inflater.inflate(R.layout.fragment_status_alert, null);

        return parent;
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
        if(id == R.id.action_filter){
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

        TextView status = (TextView) parent.findViewById(R.id.textView_state_detail_alert);
        TextView departure = (TextView) parent.findViewById(R.id.textView_departure_detail_alert);
        TextView hourDeparture = (TextView) parent.findViewById(R.id.textView_hourDep_detail_alert);
        TextView terminalDep = (TextView) parent.findViewById(R.id.textView_terminalDep_detail_alert);
        TextView gateDep = (TextView) parent.findViewById(R.id.textView_gateDep_detail_alert);
        TextView arrival = (TextView) parent.findViewById(R.id.textView_arrival_detail_alert);
        TextView hourArr = (TextView) parent.findViewById(R.id.textView_hourArr_detail_alert);
        TextView terminalArr = (TextView) parent.findViewById(R.id.textView_terminalArr_detail_alert);
        TextView gateArr = (TextView) parent.findViewById(R.id.textView_gateArr_detail_alert);
        TextView baggateGateArr = (TextView) parent.findViewById(R.id.textView_baggateGateArr_detail_alert);

        if(displayedFlight.getStatus() != null) {
            status.setText(Converter.getStatus(getActivity(), displayedFlight.getStatus()));
            status.setTextColor(Converter.statusColor(displayedFlight.getStatus()));
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
            baggateGateArr.setText(displayedFlight.getArrival().getAirport().getBaggage());
        } else {
            baggateGateArr.setText("-");
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
