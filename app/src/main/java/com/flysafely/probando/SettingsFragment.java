package com.flysafely.probando;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.Locale;

/**
 * Created by Administrador on 25/11/2016.
 */

public class SettingsFragment extends Fragment {

    private View parent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parent = inflater.inflate(R.layout.fragment_settings,null);

        final Spinner spinnerLeng = (Spinner) parent.findViewById(R.id.lenguage);

        final String[] lenguages = {getResources().getString(R.string.english_text),getResources().getString(R.string.spanish_text)};

        MainActivity.setActionBarTitle(getString(R.string.title_fragment_settings));

        if(spinnerLeng != null) {
            spinnerLeng.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, lenguages));

            final SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

            String lenguageGet = settings.getString("LENGUAGE", null);

            Configuration config = new Configuration();
            Locale locale;

            if(lenguageGet == null) {
                spinnerLeng.setSelection(0);
                locale = new Locale("en");
                Locale.setDefault(locale);
                config = new Configuration();
                config.setLocale(locale);
                getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("LENGUAGE", getResources().getString(R.string.english_text));
                editor.apply();

                MainActivity.popBackstack();
                MainActivity.AddtoBackStack(new SettingsFragment(), getActivity().getResources().getString(R.string.title_fragment_califications));

            } else if(lenguageGet.equals(getResources().getString(R.string.english_text))) {
                spinnerLeng.setSelection(0);
                locale = new Locale("en");
                Locale.setDefault(locale);
                config = new Configuration();
                config.setLocale(locale);
                getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());

            } else {
                spinnerLeng.setSelection(1);
                locale = new Locale("es");
                Locale.setDefault(locale);
                config = new Configuration();
                config.setLocale(locale);
                getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());

            }

            DrawerListAdapter drawerList = MainActivity.probando();

            drawerList.getItem(0).setName(getString(R.string.title_fragment_alerts));
            drawerList.getItem(1).setName(getString(R.string.title_fragment_offers));
            drawerList.getItem(2).setName(getString(R.string.title_fragment_califications));
            drawerList.getItem(4).setName(getString(R.string.title_fragment_settings));

            MainActivity.setAdapter(drawerList);


            spinnerLeng.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                    Configuration config = new Configuration();
                    Locale locale;

                    final SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

                    String lenguageGet = settings.getString("LENGUAGE", getResources().getString(R.string.english_text));

                    String lenguage = lenguages[position];

                    if(!lenguage.equals(lenguageGet)) {

                        if(lenguage.equals(getResources().getString(R.string.english_text))){
                            locale = new Locale("en");
                            Locale.setDefault(locale);
                            config = new Configuration();
                            config.setLocale(locale);
                            getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());
                        } else {
                            /* Hay un xml strings-es que es el xml que representa este idioma*/
                            locale = new Locale("es");
                            Locale.setDefault(locale);
                            config = new Configuration();
                            config.setLocale(locale);
                            getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());
                        }

                        SharedPreferences.Editor editor = settings.edit();

                        if(lenguage != null) {
                            editor.putString("LENGUAGE", lenguage);
                            editor.apply();
                        }

                        MainActivity.popBackstack();
                        MainActivity.AddtoBackStack(new SettingsFragment(), getActivity().getResources().getString(R.string.title_fragment_califications));

                        DrawerListAdapter drawerList = MainActivity.probando();

                        drawerList.getItem(0).setName(getString(R.string.title_fragment_alerts));
                        drawerList.getItem(1).setName(getString(R.string.title_fragment_offers));
                        drawerList.getItem(2).setName(getString(R.string.title_fragment_califications));
                        drawerList.getItem(4).setName(getString(R.string.title_fragment_settings));

                        MainActivity.setAdapter(drawerList);

                    }




                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {


                }
            });

        }


        final Spinner spinner = (Spinner) parent.findViewById(R.id.time_notification);
        final String[] values = {getResources().getString(R.string.one_minute_text),getResources().getString(R.string.five_minute_text),getResources().getString(R.string.ten_minute_text),
                getResources().getString(R.string.thirty_minute_text),getResources().getString(R.string.one_hour_text)};
        if(spinner != null) {
            spinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, values));

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                    final SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

                    boolean hasAlerts = settings.getBoolean("HASALERTS", false);

                    Integer interval = getInterval(values[position]);

                    if(hasAlerts) {
                        setAlarm(interval);
                    }

                    SharedPreferences.Editor editor = settings.edit();

                    if(interval != null) {
                        editor.putInt("INTERVAL", interval);
                        editor.apply();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {


                }
            });

        }

        Switch switchNotification = (Switch) parent.findViewById(R.id.switch_notificaion);
        if(switchNotification != null) {

            final SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

            if(settings.getBoolean("HASALERTS", false)) {
                switchNotification.setChecked(true);
            } else {
                //switchNotification.setChecked(false);
            }

            switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked) {

                        final SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

                        Integer interval = settings.getInt("INTERVAL", 60000);

                        setAlarm(interval);

                        settings.edit().putBoolean("HASALERTS", true).apply();

                        spinner.setEnabled(true);

                    } else {

                        final SharedPreferences settings = getActivity().getSharedPreferences("com.example.administrador.flysafaly", MainActivity.MODE_PRIVATE);

                        cancelAlarm();

                        settings.edit().putBoolean("HASALERTS", false).apply();

                        spinner.setEnabled(false);

                    }

                }
            });
        }

        return parent;

    }

    private Integer getInterval(String interval) {

        String one = getResources().getString(R.string.one_minute_text);

        String five = getResources().getString(R.string.five_minute_text);

        String ten = getResources().getString(R.string.ten_minute_text);

        String thirty = getResources().getString(R.string.thirty_minute_text);

        String hour = getResources().getString(R.string.one_hour_text);

        if(one.equals(interval)) {
            return 60000;
        } else if(five.equals(interval)) {
            return 5*60000;
        } else if(ten.equals(interval)) {
            return 10*60000;
        } else if(thirty.equals(interval)) {
            return 30*60000;
        } else if(hour.equals(interval)) {
            return 60*60000;
        }

        return 60000;
    }

    private void setAlarm(Integer interval) {

        AlarmManager alarmManager;
        PendingIntent alarmNotificationReceiverPendingIntent;

        alarmManager = (AlarmManager) getActivity().getSystemService(MainActivity.ALARM_SERVICE);
        Intent alarmNotificationReceiverIntent = new Intent(getActivity(), NotificationBroadcastReceiver.class);

        alarmNotificationReceiverPendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmNotificationReceiverIntent, 0);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval, alarmNotificationReceiverPendingIntent);
    }

    private void cancelAlarm() {

        AlarmManager alarmManager;
        PendingIntent alarmNotificationReceiverPendingIntent;

        alarmManager = (AlarmManager) getActivity().getSystemService(MainActivity.ALARM_SERVICE);

        Intent alarmNotificationReceiverIntent = new Intent(getActivity(), NotificationBroadcastReceiver.class);

        alarmNotificationReceiverPendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmNotificationReceiverIntent, 0);

        alarmManager.cancel(alarmNotificationReceiverPendingIntent);
    }
}
