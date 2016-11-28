package com.flysafely.probando;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

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
 * Created by Administrador on 17/11/2016.
 */

public class ListAlertFragment extends Fragment {

    private View parent;
    private FlightAdapter adapter;
    private Menu menu;
    private Integer itemsSelected = 0;
    private ActionMode actionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parent = inflater.inflate(R.layout.fragment_list,null);

        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity.setActionBarTitle(getString(R.string.title_fragment_alerts));

        MainActivity.showDrawerToggle();

        final SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

        final Set<String> alerts = settings.getStringSet("ALERTS", null);

        if(alerts != null){
            updateFlight(alerts.iterator(), null);
        }

        FloatingActionButton button = (FloatingActionButton) parent.findViewById(R.id.add_alert);
        if(button != null) {

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(actionMode != null) {
                        actionMode.finish();
                    }

                    MainActivity.AddtoBackStack(new SaveAlertFragment(), getString(R.string.title_fragment_addalert));
                }
            });
        }
        final ArrayList<Flight> flights = getFlights();
        final ArrayList<Flight> listItems = new ArrayList<Flight>();
        if(flights != null) {
            adapter = new FlightAdapter(getActivity(),R.layout.fragment_listview_alert, flights);
            ListView listView = (ListView) parent.findViewById(R.id.list_view1);

            if(listView != null) {
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Fragment detailAlertFragment = new DetailAlertFragment();

                        Bundle args = new Bundle();
                        Flight flight = (Flight)parent.getAdapter().getItem(position);

                        args.putString("AIRLINE", flight.getAirline().getId());
                        args.putString("FLIGHT_NUMBER",flight.getNumber().toString());
                        detailAlertFragment.setArguments(args);

                        MainActivity.AddtoBackStack(detailAlertFragment, getString(R.string.title_fragment_detailalert));
                    }
                });

                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                    @Override
                    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                        if(!listItems.contains(flights.get(position))){
                            itemsSelected ++;
                            mode.setTitle(itemsSelected.toString());
                            listItems.add(flights.get(position));
                        } else {
                            itemsSelected --;
                            mode.setTitle(itemsSelected.toString());
                            listItems.remove(flights.get(position));
                        }
                    }

                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        menu.clear();
                        mode.getMenuInflater().inflate(R.menu.menu_detail_alert, menu);
                        actionMode = mode;
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.action_filter:
                                deleteFlight(listItems);
                                Toast.makeText(getActivity(), "deletee anda", Toast.LENGTH_LONG).show();
                                itemsSelected = 0;
                                flights.removeAll(listItems);
                                listItems.clear();
                                mode.finish();
                                return true;
                        }
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        itemsSelected = 0;
                        listItems.clear();
                    }
                });
            }
        }
    }

    private void deleteFlight(ArrayList<Flight> listItems) {

        SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

        Set<String> alertsGet = settings.getStringSet("ALERTS", null);
        Set<String> alerts;
        if (alertsGet == null) {
            return;
        } else {
            alerts = new HashSet<String>(alertsGet);
        }

        Gson gson = new Gson();
        for(Flight delflight : listItems){
            String delete = "";
            for(String alert : alerts) {
                Flight flight = gson.fromJson(alert, Flight.class);
                if(flight.getNumber().equals(delflight.getNumber()) && flight.getAirline().getId().equals(delflight.getAirline().getId())){
                    delete = alert;
                    break;
                }
            }

            alerts.remove(delete);
        }


        SharedPreferences.Editor editor = settings.edit();

        editor.putStringSet("ALERTS", alerts);
        editor.apply();
    }

    private ArrayList<Flight> getFlights() {

        SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

        Set<String> alertsGet = settings.getStringSet("ALERTS", null);
        Set<String> alerts;

        Gson gson = new Gson();

        if (alertsGet == null) {
            alerts = new HashSet<String>();
        } else {
            alerts = new HashSet<String>(alertsGet);
        }

        ArrayList<Flight> flights = new ArrayList<Flight>();

        for(String alert : alerts) {
            Flight flight = gson.fromJson(alert,Flight.class);
            flights.add(flight);
        }

        return flights;
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

                    updateFlight(iterator, newFavs);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                                    }
            });
            RequestManager.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
        }
        else {

            if ( !isAdded() || getActivity() == null)
                return;

            SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

            SharedPreferences.Editor editor = settings.edit();
            if(updatedFavorites!=null) {
                editor.putStringSet("ALERTS", updatedFavorites);
                editor.apply();
            }
        }
    }

}
