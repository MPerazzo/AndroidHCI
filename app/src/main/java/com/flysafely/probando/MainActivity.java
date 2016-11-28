package com.flysafely.probando;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String SELECTED_DRAWER = "sdrawer";
    private static final String CURRENT_HIGHLIGHT = "chighlight";


    private final int HEADER_POSITION = 0;
    private final int ALERTS_POSITION = 1;
    private final int OFFERS_POSITION = 2;
    private final int CALIFICATIONS_POSTION = 3;
    private final int SEPARATOR_POSITION = 4;
    private final int CONFIGURATION_POSITION = 5;

    private static String home_title;
    private String alerts_title;
    private String offers_title;
    private String califications_title;
    private String configuration_title;
    private static String alerts_add_title;
    private static String alerts_detail_title;

    /* usada para resaltar las opciones que fueron seleccionadas previamente en el drawer (no contiene la actual)
       donde el int representa la posición del elemento en la lista. Declarado como ArrayList para serializarlo.
     */
    private ArrayList<Integer> selectedDrawerOptions;

    private int currentHighlighted;

    private  static DrawerLayout drawerLayout;

    private  static ListView drawerList;

    private static Toolbar toolbar;

    private static TextView actionBarText;

    private static ImageView actionBarHomeButton;

    private static ActionBarDrawerToggle drawerToggle;

    private static FragmentManager fragmentManager;

    private static GPSTracker tracker;

    private Bundle bundle;

    private static String detailAlertBarTitle;

    private static ActionBar supportActionBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();

        bundle = savedInstanceState;

        //Obtener los strings desde los recursos para el drawer
        home_title = getString(R.string.app_name);
        alerts_title = getString(R.string.title_fragment_alerts);
        alerts_add_title = getString(R.string.title_fragment_addalert);
        alerts_detail_title = getString(R.string.title_fragment_detailalert);
        offers_title = getString(R.string.title_fragment_offers);
        califications_title = getString(R.string.title_fragment_califications);
        configuration_title = getString(R.string.title_fragment_settings);

        //Obtener drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //Obtener listview
        drawerList = (ListView) findViewById(R.id.left_drawer);

        // Obtener texto de la actionBar para modificar su contenido en cada sección
        actionBarText = (TextView) findViewById(R.id.titlebar);

        //inicializar los elementos usados para setiar su background de un item del drawerList al elegir una opción específica
        selectedDrawerOptions = new ArrayList<>();
        currentHighlighted = -1 ;

        /* Listener del click del logo en la app bar. Disableamos el botón hasta que la ejecución del
        mainFragment finalize para que el usuario no pueda usarlo exhaustivamente interrumpiendo el
        flujo normal de la aplicación.
         */
        actionBarHomeButton = (ImageView) findViewById(R.id.logobar);
        actionBarHomeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showDrawerToggle();

                actionBarHomeButton.setEnabled(false);

                backStackAdd(new MainFragment(), home_title);
                setActionBarTitle(home_title);

                if (currentHighlighted != -1) {
                    getViewByPosition(currentHighlighted).setBackgroundColor(Color.TRANSPARENT);

                    // como volvemos al home, allí no debería haber ningun item seleccionado
                    currentHighlighted = -1;
                }

                selectedDrawerOptions.clear();
            }
        });


        /* Cambiamos el color del botón del "Home" de la app bar una vez que es tocado,
        lo que facilita que el usuario asocie a ese botoón como un botón de interacción y
        no como una imágen fija.
         */
        actionBarHomeButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        //overlay is black with transparency of 0x77 (119)
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }

                return false;
            }
        });


        //Nueva lista de drawer items
        ArrayList<DrawerItem> items = new ArrayList<>();
        items.add(new DrawerItem(alerts_title,R.drawable.notification));
        items.add(new DrawerItem(offers_title,R.drawable.ofertas));
        items.add(new DrawerItem(califications_title,R.drawable.experience));
        items.add(new DrawerItem("Separator",0));
        items.add(new DrawerItem(configuration_title,R.drawable.configuracion));


        // Relacionar el adaptador y la escucha de la lista del drawer
        drawerList.setAdapter(new DrawerListAdapter(this, R.layout.drawer_list_item, items));

        // set drawer listener
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        setupDrawerToggle();

//        // set map Popup
//
//        popupView = getLayoutInflater().inflate(R.layout.maps_popup, null);
//
//        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//
//        popupWindow = new PopupWindow(popupView, popupView.getMeasuredWidth(), popupView.getMeasuredHeight() , true);
//
//        popupWindow.setFocusable(true);
//
//        /* el color coincide con el background del popUp y posibilita el backpressed listener.
//        Tmabién otorga la posibilidad de que se cierre el popup al tocar la pantalla fuera de su recuadro,
//        lo cuál resulta práctico ystatus.getText().toString() útil para el usuario.
//         */
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
//
////        popupWindow.setElevation(70);
//
//
//        /* soluciona el caso en que el usuario presione fuera del recuadro del popup y luego presione back.
//        Actualiza la variable boolean asi no tiene que presionar back dos veces.
//        */
//        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                popUpMapVisible = false;
//            }
//        });
//
//        Button btn_Cerrar = (Button) popupView.findViewById(R.id.id_cerrar);
//
//        btn_Cerrar.setOnClickListener(new Button.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                popupWindow.dismiss();
//            }
//        });

        // GPS

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        tracker = new GPSTracker(lm);


        //set MainFragment and header

        setActionBarTitle(home_title);

        View header;

        fragmentManager = getFragmentManager();

        supportActionBar = getSupportActionBar();

        // inflate header depending on orientation
        switch (getResources().getConfiguration().orientation) {

            case Configuration.ORIENTATION_LANDSCAPE:
                header = getLayoutInflater().inflate(R.layout.header_rotated, null);
                break;

            default:
                header = getLayoutInflater().inflate(R.layout.header, null);
                break;
        }

        drawerList.addHeaderView(header);

        drawerList.post(new Runnable() {
            @Override
            public void run() {

                if (bundle != null) {
                    selectedDrawerOptions = (ArrayList<Integer>) bundle.getSerializable(SELECTED_DRAWER);
                    currentHighlighted = bundle.getInt(CURRENT_HIGHLIGHT);

                    if (currentHighlighted != -1) {
                        Log.e("$$$$$$POSITION", ": " + currentHighlighted);
                        View aux = getViewByPosition(currentHighlighted);
                        if (aux != null)
                            aux.setBackgroundColor(Color.rgb(227, 227, 227)); // #e3e3e3 palette color
                    }
                }
            }
        });

        ContentObserver contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
        };

        getContentResolver().registerContentObserver(Settings.System.getUriFor
                        (Settings.System.ACCELEROMETER_ROTATION),
                true,contentObserver);


        String mainFragment = getIntent().getStringExtra("mainFragment");

        if (mainFragment != null) {

            if (mainFragment.equals("detailAlertFragment")) {

                Bundle args = new Bundle();

                String arg1 = getIntent().getStringExtra("AIRLINE");
                String arg2 = getIntent().getStringExtra("FLIGHT_NUMBER");

                args.putString("AIRLINE", arg1);
                args.putString("FLIGHT_NUMBER", arg2);

                Fragment detailAlertFragment = new DetailAlertFragment();

                detailAlertFragment.setArguments(args);

                backStackAdd(new ListAlertFragment(), alerts_title);
                backStackAdd(detailAlertFragment, alerts_detail_title);
                selectedDrawerOptions.add(ALERTS_POSITION - 1);
                currentHighlighted = ALERTS_POSITION - 1;
            }
        }
        else
            if (savedInstanceState == null)
                backStackAdd(new MainFragment(), home_title);
    }


    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SELECTED_DRAWER, selectedDrawerOptions);
        outState.putInt(CURRENT_HIGHLIGHT, currentHighlighted);

    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.action_bar_items, menu);
//
//        infoItem = menu.findItem(R.id.action_popup);
//        infoItem.setVisible(false);
//
//        return true;
//    }

//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        System.exit(0);
//
//        switch (item.getItemId()) {
//
//            case R.id.action_popup:
//
//
//                    if (popUpMapVisible == true)
//                        popupWindow.dismiss();
//
//
//                    else {
//                        popupWindow.showAtLocation(findViewById(R.id.content_frame), Gravity.CENTER, 0, 0);
//                        popUpMapVisible = true;
//                    }
//        }
//
//
//        return true;
//    }


    /* una vez selecionada una opción del drawer, dejar un color de fondo en la misma así
    cuando el usuario abra el drawer sabe que opción eligió en caso de que lo olvide
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
    @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        /* en algunos casos se le resta uno a position dado que hay dos listas conceptualmente,
           la lista del drawer que tiene header, y la lista de opciones seleccionables e interactuables
           que no tienen header. Entonces las posiciones difieren en 1.
         */

            if (position == HEADER_POSITION || position == SEPARATOR_POSITION)
                return;

            if (currentHighlighted != -1)
                getViewByPosition(currentHighlighted).setBackgroundColor(Color.TRANSPARENT);

            if (!RefreshSelectedDrawerOptions(position-1)) {
                selectedDrawerOptions.add(position-1);
            }

            for (Integer i : selectedDrawerOptions) {
                Log.e("$$$$$$ITEMS", ": " + i);
            }

            currentHighlighted = position-1;

            view.setBackgroundColor(Color.rgb(227, 227, 227)); // #e3e3e3 palette color

            view.setSelected(true);

            selectItem(position);
        }
    }


    private void selectItem(int position) {

        switch (position) {

            case ALERTS_POSITION:

                backStackAdd(new ListAlertFragment(), alerts_title);

                break;

            case OFFERS_POSITION:

                showDrawerToggle();
                backStackAdd(new OffersFragment(), offers_title);
                setActionBarTitle(offers_title);

                break;

            case CALIFICATIONS_POSTION:

                showDrawerToggle();
                backStackAdd(new rankingFragment(), califications_title);
                setActionBarTitle(califications_title);

                break;

            case CONFIGURATION_POSITION:

                showDrawerToggle();
                backStackAdd(new SettingsFragment(), configuration_title);
                setActionBarTitle(configuration_title);

                break;
        }

        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(drawerList)){
            drawerLayout.closeDrawer(drawerList);
        }

        else {

            int index = fragmentManager.getBackStackEntryCount() - 1;

            Log.e("$$$$iINDEX", "ES" + index);

            /* Estamos en el home y queremos irnos de la app. Se handlea aca y se retorna porque
            si llamamos a super queda la aplicación sin ningun frgamento, sin layout. (solo con toolbar
            y drawer)
             */
            if (index == 0) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            }

            /* el fragmento que esta en focus fue removido al presionar back, calculamos el nombre del fragmento
            actual para colocar su nombre en la action bar. Volvemos a restar menos 1.
             */

            String currentTitle = getTopStackName(index);

            String previousTitle = getTopStackName(index - 1);

            if (currentTitle == alerts_add_title || currentTitle == alerts_detail_title)
                showDrawerToggle();

            // estos fragmentos no modifican el highlight dado que ocurren dentro de una misma categoría
            if (currentTitle != alerts_add_title && currentTitle != alerts_detail_title) {

                int previousHighlighted = -1;

                // actualizamos el elemento remarcado de las opciones del drawer

                if (!selectedDrawerOptions.isEmpty())
                    currentHighlighted = popSelectedDrawerOptions();

                if (currentHighlighted != -1)
                    getViewByPosition(currentHighlighted).setBackgroundColor(Color.TRANSPARENT);


                /* si el index es 1 significa que al presionar back, vamos a estar en el home.
                    Y en el home no debería haber ningun elemento remarcado de las opciones del drawer.
                 */
                if (index > 1 ) {

                    if (previousTitle == alerts_add_title || previousTitle == alerts_detail_title) {
                        previousHighlighted = ALERTS_POSITION - 1;

                        if (previousTitle == alerts_detail_title)
                            previousTitle = detailAlertBarTitle;
                    }
                    else {
                        previousHighlighted = popSelectedDrawerOptions();
                    }

                    getViewByPosition(previousHighlighted).setBackgroundColor(Color.rgb(227, 227, 227)); // #e3e3e3 palette color
                }


                currentHighlighted = previousHighlighted;
            }

            setActionBarTitle(previousTitle);

            super.onBackPressed();


        }
    }

    private static void backStackAdd(Fragment fragment, String tag) {

        if ( fragmentManager.findFragmentByTag(tag) != null) {

           fragmentManager.popBackStack(tag, fragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, tag).addToBackStack(tag).commit();
    }

    private boolean RefreshSelectedDrawerOptions(Integer position) {

        // equals only considers tag name
        if (selectedDrawerOptions.contains(position)) {

            int popped;
            while (! ((popped=peekSelectedDrawerOptions()) == position) ) {
                Log.e("$$$$$POPIADO","=" + popped);
                popSelectedDrawerOptions();
            }

            return true;
        }

        return false;
    }

    private int popSelectedDrawerOptions() {

        int lastItem = selectedDrawerOptions.size() - 1;

        int aux = selectedDrawerOptions.get(lastItem);

        selectedDrawerOptions.remove(lastItem);

        return aux;
    }

    private int peekSelectedDrawerOptions() {

        int lastItem = selectedDrawerOptions.size() - 1;

        return selectedDrawerOptions.get(lastItem);
    }

    public View getViewByPosition(int position) {

        int firstPosition = drawerList.getFirstVisiblePosition() - drawerList.getHeaderViewsCount(); // This is the same as child #0
        int wantedChild = position - firstPosition;

        if (wantedChild < 0 || wantedChild >= drawerList.getChildCount()) {
            return null;
        }
        return drawerList.getChildAt(wantedChild);
    }


    private void setupToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);
    }

    public static void showUpButton() {

        drawerToggle.setDrawerIndicatorEnabled(false);

        supportActionBar.setDisplayHomeAsUpEnabled(true);

        backListener();

    }

    private void setupDrawerToggle(){
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        drawerToggle.syncState();
    }

    public static void showDrawerToggle() {

        supportActionBar.setDisplayHomeAsUpEnabled(false);
        drawerToggle.setDrawerIndicatorEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerList);
            }
        });

    }

    private static void backListener() {

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = getTopStackName(fragmentManager.getBackStackEntryCount() - 1);
                setActionBarTitle(tag);

                showDrawerToggle();

                fragmentManager.popBackStack();
            }
        });

    }

    private static String getTopStackName(int index) {
        FragmentManager.BackStackEntry backEntry = fragmentManager.getBackStackEntryAt(index);
        return backEntry.getName();
    }

    public static void AddtoBackStack(Fragment fragment, String tag) {backStackAdd(fragment, tag); }

    public static void setActionBarTitle(String title) {
        actionBarText.setText(title);
    }

    public static void setDetailAlertBarTitle(String title) {
        setActionBarTitle(title);
        detailAlertBarTitle = title;
    }

    public static void setHomeTitle() { setActionBarTitle(home_title);}

    public static double getLatitude() {
        return tracker.getLatitude();
    }

    public static double getLongitud() {
        return tracker.getLongitude();
    }

    public static void EnableGoHome() {actionBarHomeButton.setEnabled(true);}

    public static void goHome() { actionBarHomeButton.performClick(); }
}
