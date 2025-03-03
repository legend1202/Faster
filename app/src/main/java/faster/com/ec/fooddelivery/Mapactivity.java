package faster.com.ec.fooddelivery;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;
import static faster.com.ec.fooddelivery.MainActivity.tf_opensense_regular;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import faster.com.ec.Getset.CustomMarker;
import faster.com.ec.Getset.restaurentGetSet;
import faster.com.ec.R;
import faster.com.ec.helper.SessionManager;
import faster.com.ec.utils.Config;
import faster.com.ec.utils.ConnectionDetector;
import faster.com.ec.utils.GPSTracker;

public class Mapactivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String MY_PREFS_NAME = "Fooddelivery";

    private double latitudecur;
    private double longitudecur;
    private GoogleMap googleMap;
    private HashMap<CustomMarker, Marker> markersHashMap;
    private Iterator<Map.Entry<CustomMarker, Marker>> iter;
    private ArrayList<restaurentGetSet> restaurentlist;
    private int page = 1;
    private int numberOfRecords;
    private String Error;
    private ImageButton btn_more;
    private String[] separateddata;
    private double destlat;
    private double destlng;
    View layout;
    private ProgressDialog pd;
    private String CategoryTotal = "";
    private final String[] permission_location = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private String Location;
    private String timezoneID;
    private int radius;
    private String regId,userId1;
    private String search="";
    private SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapactivity);
        session = new SessionManager(getApplicationContext());
        displayFirebaseRegId();
        TextView txt_header = findViewById(R.id.txt_header);
        txt_header.setTypeface(tf_opensense_regular);
        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());

        numberOfRecords = getResources().getInteger(R.integer.numberOfRecords);

        Boolean isInternetPresent = cd.isConnectingToInternet();
        // check for Internet status
        if (isInternetPresent) {

            // arrayPoints = new ArrayList<LatLng>();
            markersHashMap = new HashMap<>();
            GPSTracker gps = new GPSTracker();
            gps.init(Mapactivity.this);
            // check if GPS enabled
            if (gps.canGetLocation()) {
                try {
                    latitudecur = gps.getLatitude();
                    longitudecur = gps.getLongitude();
                    //latitudecur = -0.1267201;
                    //longitudecur = -78.4636728;
                } catch (NumberFormatException e) {
                    // TODO: handle exception
                }

            } else {

                gps.showSettingsAlert("Activa tu GPS para buscar las tiendas más cercanas a ti.");
            }
            changeStatsBarColor(Mapactivity.this);


            initialize();
            gettingSharedPref();


        } else {
            Toast.makeText(Mapactivity.this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    private void displayFirebaseRegId() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);

        SharedPreferences prefs1 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userId1 = prefs1.getString("userid", null);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Mapactivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        //startActivity(i);
        finish();
    }

    private void gettingSharedPref() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Location = prefs.getString("city", null);
        int noRadius = 100000;
        radius = prefs.getInt("radius", noRadius);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        try {
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), R.string.no_map, Toast.LENGTH_SHORT).show();
            }
            this.googleMap = googleMap;
            // Loading map
            //MapFragment sup = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment));
            //sup.getMapAsync(this);

            initializeUiSettings();
            initializeMapLocationSettings();
            initializeMapTraffic();
            initializeMapType();
            initializeMapViewSettings();

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            new GetDataAsyncTask().execute();
            //setCustomMarkerOnePosition();

        } catch (NullPointerException | NumberFormatException e) {
            // TODO: handle exception
        }
    }

    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Mapactivity.this);
            pd.setMessage("Cargando...");
            pd.setCancelable(false);
            pd.show();
        }


        @Override
        protected Void doInBackground(Void... params) {
            URL hp = null;
            try {

                String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&lon=" + longitudecur + "&search=" + search + "&noofrecords=" + numberOfRecords + "&pageno=" + page + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo);
                hp = new URL(user.replace(" ", "%20"));

                Log.e("URLs", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.setRequestProperty("Authorization", "Bearer " + regId);

                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("input", "" + input);
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                String x;
                x = r.readLine();
                StringBuilder total = new StringBuilder();
                while (x != null) {
                    total.append(x);
                    x = r.readLine();
                }
                Log.e("error",total.toString());
                JSONObject responsedat = new JSONObject(total.toString());
                Log.e("error",responsedat.toString());
                String txt_success = responsedat.getString("success");
                final String txt_message = responsedat.getString("message");

                if (txt_success.equals("1")) {
                    JSONArray jsonArray = responsedat.getJSONArray("restaurant_list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        restaurentGetSet temp = new restaurentGetSet();
                        JSONObject Obj = jsonArray.getJSONObject(i);
                        temp.setId(Obj.getString("id"));
                        temp.setName(Obj.getString("name"));
                        temp.setLat(Obj.getString("lat"));
                        temp.setLon(Obj.getString("lon"));
                        temp.setDistance(Obj.getString("distance"));
                        temp.setOpen_time(Obj.getString("close_time"));
                        temp.setClose_time(Obj.getString("open_time"));
                        temp.setCurrency(Obj.getString("currency"));
                        temp.setDelivery_time(Obj.getString("delivery_time"));
                        temp.setImage(Obj.getString("photo"));
                        temp.setRatting(Obj.getString("ratting"));
                        temp.setRes_status(Obj.getString("res_status"));
                        temp.setRes_enable(Obj.getString("res_status_manual"));

                        try {
                            JSONArray jCategory = Obj.getJSONArray("Category");
                            String[] temprory = new String[jCategory.length()];
                            for (int j = 0; j < jCategory.length(); j++) {
                                temprory[j] = jCategory.getString(j);
                                CategoryTotal += temprory[j];
                                Log.e("catname12121", "" + CategoryTotal);
                                temp.setCategory(temprory);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        restaurentlist.add(temp);
                        Error =null;

                    }
                } else if (txt_success.equals("201") || txt_success.equals("203") || txt_success.equals("204")) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(Mapactivity.this, txt_message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (txt_success.equals("202")) {
                    Error = "all_load";
                }  if (txt_success.equals("2")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(Mapactivity.this, R.style.MyDialogTheme);
                            builder1.setTitle("Existe nueva versión de Faster");
                            builder1.setCancelable(false);
                            builder1.setMessage(txt_message);
                            builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {


                                    try {
                                        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=faster.com.ec"));
                                        startActivity(viewIntent);

                                    } catch (Exception e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(Mapactivity.this, "No se puede conectar intente nuevamente...",
                                                        Toast.LENGTH_LONG).show();

                                            }
                                        });

                                        e.printStackTrace();
                                    }
                                }
                            });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }
                    });

                } else if (txt_success.equals("3")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(Mapactivity.this, R.style.MyDialogTheme);
                            builder1.setTitle("Tu cuenta ha sido bloqueada");
                            builder1.setCancelable(false);
                            builder1.setMessage(txt_message);
                            builder1.setPositiveButton("Contactar a Soporte", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    String toNumber = "+593969764774";
                                    toNumber = toNumber.replace("+", "").replace(" ", "");
                                    Intent sendIntent = new Intent("android.intent.action.MAIN");
                                    // sendIntent.setComponent(new ComponentName(“com.whatsapp”, “com.whatsapp.Conversation”));
                                    sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, "");
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.setPackage("com.whatsapp");
                                    sendIntent.setType("text/plain");
                                    startActivity(sendIntent);
                                }
                            });
                            builder1.setNegativeButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }
                    });

                } else if (txt_success.equals("4")||txt_success.equals("5"))  {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(Mapactivity.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage(txt_message);
                            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {

                                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                    editor.putString("phonenumber",null);
                                    editor.apply();

                                    SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0).edit();
                                    editor2.putString("regId",null);
                                    editor2.apply();

                                    // session manager
                                    session.setLogin(false);

                                    Intent iv = new Intent(Mapactivity.this, Login4.class);
                                    iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(iv);
                                    finish();

                                }
                            });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }
                    });


                } else if (txt_success.equals("6"))  {
                    Intent iv = new Intent(Mapactivity.this, CompleteOrder.class);
                    startActivity(iv);
                    finish();
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
            } catch (NullPointerException e) {
                // TODO: handle exception
                Error = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (Error != null) {
                Toast.makeText(Mapactivity.this, R.string.no_restaurant, Toast.LENGTH_LONG).show();
            } else {
                setCustomMarkerOnePosition();
            }

        }
    }

    private void setCustomMarkerOnePosition() {


        Log.d("sizerest", "" + restaurentlist.size());
        for (int i = 0; i < restaurentlist.size(); i++) {

            try {
                String lat1 = restaurentlist.get(i).getLat();
                String lng1 = restaurentlist.get(i).getLon();
                String name = restaurentlist.get(i).getName() + ":" + restaurentlist.get(i).getId() + ":" + restaurentlist.get(i).getRatting();
                String idf = restaurentlist.get(i).getId();
                String ratting = restaurentlist.get(i).getRatting();
                Log.d("lat1", "" + lat1);
                Log.d("lng1", "" + lng1);
                CustomMarker customMarkerOne = new CustomMarker("markerOne", Double.parseDouble(lat1), Double.parseDouble(lng1));

                if (restaurentlist.get(i).getCategory() != null) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : restaurentlist.get(i).getCategory()) {
                        sb.append(s).append(",");
                    }

                    String cat = sb.toString().replace("\"", "").replace("[", "").replace("]", "");
                    if (cat.endsWith(",")) {
                        cat = cat.substring(0, cat.length() - 1);
                    }


                    // addMarker(customMarkerOne);
                    MarkerOptions markerOptions = new MarkerOptions().position(

                            new LatLng(customMarkerOne.getCustomMarkerLatitude(), customMarkerOne.getCustomMarkerLongitude()))
                            .icon(BitmapDescriptorFactory.defaultMarker())
                            .snippet(
                                    restaurentlist.get(i).getName() + "-:" +
                                            cat + "-:" +
                                            restaurentlist.get(i).getDistance() + "-:" +
                                            restaurentlist.get(i).getOpen_time() + "-:" +
                                            restaurentlist.get(i).getClose_time() + "-:" +
                                            restaurentlist.get(i).getDelivery_time() + "-:" +
                                            restaurentlist.get(i).getRatting() + "-:" +
                                            restaurentlist.get(i).getImage() + "-:" +
                                            restaurentlist.get(i).getRes_status()
                            )
                            .title(
                                    restaurentlist.get(i).getName() + ":" +
                                            restaurentlist.get(i).getId() + ":" +
                                            restaurentlist.get(i).getDistance() + ":" +
                                            restaurentlist.get(i).getAddress() + ":" +
                                            restaurentlist.get(i).getLat() + ":" +
                                            restaurentlist.get(i).getLon()+ ":" +
                                            restaurentlist.get(i).getRes_status()+ ":" +
                                            restaurentlist.get(i).getRes_enable()
                            );

                    final Marker newMark = googleMap.addMarker(markerOptions);
                    //newMark.showInfoWindow();

                    addMarkerToHashMap(customMarkerOne, newMark);
                    zoomToMarkers(btn_more);
                }
            } catch (NullPointerException | NumberFormatException e) {
                // TODO: handle exception
                e.printStackTrace();
            }

            googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

            // on click of google map marker
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {

                    try {
                        String title = marker.getTitle();
                        separateddata = title.split(":");
                        if(separateddata[6].equals("open") && separateddata[7].equals("open")){
                            Log.d("1", "" + separateddata[0]);
                            Log.d("2", "" + separateddata[1]);
                            Log.d("3", "" + separateddata[2]);
                            String addressmap = separateddata[3].toLowerCase();
                            destlat = Double.parseDouble(separateddata[4]);
                            destlng = Double.parseDouble(separateddata[5]);
                            Intent intent = new Intent(Mapactivity.this, MenuList.class);
                            intent.putExtra("detail_id", separateddata[1]);
                            intent.putExtra("restaurent_name", "" + separateddata[0]);
                            intent.putExtra("distance", separateddata[2]);

                            startActivity(intent);
                        }else{
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(Mapactivity.this, R.style.MyDialogTheme);
                            builder1.setTitle("Tienda Cerrada");
                            builder1.setMessage("Lo sentimos, la Tienda "+separateddata[0]+ " se encuentra cerrada, pero puedes ingresar a ver su información.");
                            builder1.setPositiveButton("Ingresar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {


                                    Intent iv = new Intent(Mapactivity.this, DetailPage.class);

                                    iv.putExtra("res_id", "" + separateddata[1]);
                                    iv.putExtra("distance", "" + separateddata[2]);
                                    startActivity(iv);
                                }
                            });
                            builder1.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }

                    } catch (NullPointerException | NumberFormatException e) {
                        e.printStackTrace();
                        // TODO: handle exception
                    }

                }
            });


        }


    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;
        boolean not_first_time_showing_info_window;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.cell_home, null);
            myContentsView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getInfoWindow(Marker marker) {


            String named = marker.getSnippet();
            String tu = marker.getTitle();
            if (tu.equals("Current Location")) {
                myContentsView.setVisibility(View.INVISIBLE);
                return null;
            } else {

                Typeface opensansregular = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
                String[] sept = named.split("-:");
                Log.e("data", sept[0] + "::" + sept[1] + "::" + sept[2] + "::" + sept[3] + "::" + sept[4] + "::" + sept[5] + "::" + sept[6] + "::" + sept[7] + "::" + sept[8]);
                TextView txt_name = myContentsView.findViewById(R.id.txt_name);
                txt_name.setText("" + sept[0]);
                txt_name.setTypeface(opensansregular);

                TextView txt_address = myContentsView.findViewById(R.id.txt_category);
                txt_address.setText("" + sept[1]);
                txt_address.setTypeface(opensansregular);


                TextView txt_status = myContentsView.findViewById(R.id.txt_status);
                TextView txt_time = myContentsView.findViewById(R.id.time);
                txt_time.setTypeface(opensansregular);
                txt_status.setTypeface(opensansregular);
                String status = sept[8];
                Log.e("res_status", "" + status);

                TextView txt_radio = myContentsView.findViewById(R.id.txt_radio);
                txt_radio.setText(" ");
                txt_radio.setTypeface(opensansregular);



                if (status.equals("open")) {
                    txt_status.setText(R.string.opentill);
                    txt_time.setText("" + sept[4]);

                } else if (status.equals("closed")) {
                    Log.e("dataimage", "" + sept[4]);
                    txt_status.setText(R.string.closetill);
                    txt_time.setText("" + sept[3]);

                }


                TextView txt_deltime = myContentsView.findViewById(R.id.txt_distance);
                txt_deltime.setText("" + sept[5]);
                txt_deltime.setTypeface(opensansregular);

                TextView txt_invisible = myContentsView.findViewById(R.id.txt_invisible);
                txt_invisible.setVisibility(View.GONE);

                TextView txt_rating = myContentsView.findViewById(R.id.txt_rating);
                txt_rating.setText("" + Float.parseFloat(sept[6])+"★");
                txt_rating.setTypeface(opensansregular);

                ImageView img_near = myContentsView.findViewById(R.id.image);
                Picasso.get().load(getString(R.string.link) + getString(R.string.imagepath) + sept[7].replace(" ", "%20"))
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .resize(640, 360)
                        .into(img_near);

                return myContentsView;
            }


        }

        //		Use getInfoWindow instead of getInfoContents to provide full info window.
        @Override
        public View getInfoContents(Marker marker) {


            // TODO Auto-generated method stub
            return null;
        }

    }

    private class InfoWindowRefresher implements Callback {
        private final Marker markerToRefresh;

        private InfoWindowRefresher(Marker markerToRefresh) {
            this.markerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            markerToRefresh.showInfoWindow();
        }

        @Override
        public void onError(Exception e) {

        }

        /*@Override
        public void onError() {
        }*/
    }

    private void zoomToMarkers(View v) {
        zoomAnimateLevelToFitMarkers(120);
    }

    private void initialize() {
        // TODO Auto-generated method stub
        // rest1 = Home.rest;

        restaurentlist = new ArrayList<>();
        btn_more = findViewById(R.id.btn_more);
        timezoneID = TimeZone.getDefault().getID();

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Error = null;
                page = page + 1;
                new GetDataAsyncTask().execute();
            }
        });
        MapsInitializer.initialize(getApplicationContext());
        initilizeMap();

    }

    private void setUpMarkersHashMap() {
        if (markersHashMap == null) {
            markersHashMap = new HashMap<>();
        }
    }


    private void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
        setUpMarkersHashMap();
        markersHashMap.put(customMarker, marker);
    }

    private void zoomAnimateLevelToFitMarkers(int padding) {
        iter = markersHashMap.entrySet().iterator();
        LatLngBounds.Builder b = new LatLngBounds.Builder();

        LatLng ll = null;
        while (iter.hasNext()) {
            Map.Entry mEntry = iter.next();
            CustomMarker key = (CustomMarker) mEntry.getKey();
            ll = new LatLng(key.getCustomMarkerLatitude(), key.getCustomMarkerLongitude());
            b.include(ll);
        }
        LatLngBounds bounds = b.build();
        Log.d("bounds", "" + bounds);

        // Change the padding as per needed
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 400, 400, 12);

        googleMap.animateCamera(cu);
        googleMap.moveCamera(cu);

    }

    // this is method to help us find a Marker that is stored into the hashmap
    public Marker findMarker(CustomMarker customMarker) {
        iter = markersHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry mEntry = iter.next();
            CustomMarker key = (CustomMarker) mEntry.getKey();
            if (customMarker.getCustomMarkerId().equals(key.getCustomMarkerId())) {
                Marker value = (Marker) mEntry.getValue();
                return value;
            }
        }
        return null;
    }

    private void initilizeMap() {

        SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment));
        supportMapFragment.getMapAsync(this);
        SharedPreferences sharedPreferences = getSharedPreferences("location", 0);

        try {

            int locationCount = sharedPreferences.getInt("locationCount", 0);
        } catch (NumberFormatException e) {
            // TODO: handle exception
        }


        (findViewById(R.id.mapFragment)).getViewTreeObserver()
                .addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        if (Build.VERSION.SDK_INT >= 16) {
                            (findViewById(R.id.mapFragment)).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            (findViewById(R.id.mapFragment)).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                    }
                });
    }

    private void initializeUiSettings() {
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private void initializeMapLocationSettings() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermission();
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    private void initializeMapTraffic() {
        googleMap.setTrafficEnabled(true);
    }

    private void initializeMapType() {
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    private void initializeMapViewSettings() {
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(false);
    }

    private void requestPermission() {

        int PERMISSION_REQUEST_CODE = 999;
        ActivityCompat.requestPermissions(this, permission_location, PERMISSION_REQUEST_CODE);

    }
}
