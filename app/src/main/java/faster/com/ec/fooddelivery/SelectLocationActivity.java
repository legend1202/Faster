package faster.com.ec.fooddelivery;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;
import static faster.com.ec.fooddelivery.MainActivity.tf_opensense_regular;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
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

public class SelectLocationActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String MY_PREFS_NAME = "Fooddelivery";

    private double latitudecur;
    private double longitudecur;
    private String delivery_id;
    private String delivery_user_id;
    private String delivery_lat;
    private String delivery_lon;
    private String delivery_address;
    private String delivery_alias;
    private String delivery_phone;
    private String delivery_note;
    private String delivery_department_number;
    private float zoomLevel = 18;
    private GoogleMap googleMap;
    private ArrayList<restaurentGetSet> restaurentlist;
    private int page = 1;
    private ImageButton btn_select_location;
    private String CategoryTotal = "";
    private final String[] permission_location = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private String Location;
    private String timezoneID;
    private int radius;
    private String regId,userId1;
    private String search="";
    private SessionManager session;
    private Marker marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectlocationactivity);
        session = new SessionManager(getApplicationContext());
        displayFirebaseRegId();
        getLocation();
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
        Intent i = new Intent(SelectLocationActivity.this, MainActivity.class);
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
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), R.string.no_map, Toast.LENGTH_SHORT).show();
            }
            this.googleMap = googleMap;

            initializeUiSettings();
            initializeMapLocationSettings();
            initializeMapTraffic();
            initializeMapType();
            initializeMapViewSettings();

            setCustomMarkerOnePosition();

        } catch (Exception e) {
            e.printStackTrace();
        }




        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                latitudecur = latLng.latitude;
                longitudecur = latLng.longitude;

                setCustomMarkerOnePosition();
            }
        });

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location location = googleMap.getMyLocation();
                latitudecur = location.getLatitude();
                longitudecur = location.getLongitude();
                setCustomMarkerOnePosition();
                zoomAnimateLevelToFitMarkers();
                return false;
            }
        });

    }

    private void setCustomMarkerOnePosition() {

        try {
            if (marker == null) {
                CustomMarker customMarkerOne = new CustomMarker("markerOne", latitudecur, longitudecur);

                MarkerOptions markerOptions = new MarkerOptions().position(

                        new LatLng(customMarkerOne.getCustomMarkerLatitude(), customMarkerOne.getCustomMarkerLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker());
                marker = googleMap.addMarker(markerOptions);
                zoomAnimateLevelToFitMarkers();
            } else {
                marker.setPosition(new LatLng(latitudecur, longitudecur));
            }
        } catch (NullPointerException | NumberFormatException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void initialize() {
        // TODO Auto-generated method stub
        // rest1 = Home.rest;

        restaurentlist = new ArrayList<>();
        btn_select_location = findViewById(R.id.btn_select_location);
        timezoneID = TimeZone.getDefault().getID();

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_select_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectLocationActivity.this, EditLocation.class);
                intent.putExtra("delivery_id", delivery_id);
                intent.putExtra("delivery_user_id", delivery_user_id);
                intent.putExtra("delivery_lat", String.valueOf(latitudecur));
                intent.putExtra("delivery_lon", String.valueOf(longitudecur));
                intent.putExtra("delivery_address", delivery_address);
                intent.putExtra("delivery_alias", delivery_alias);
                intent.putExtra("delivery_phone", delivery_phone);
                intent.putExtra("delivery_note", delivery_note);
                intent.putExtra("delivery_department_number", delivery_department_number);
                startActivity(intent);
            }
        });
        MapsInitializer.initialize(getApplicationContext());
        initilizeMap();

    }

    private void zoomAnimateLevelToFitMarkers() {
        LatLngBounds.Builder b = new LatLngBounds.Builder();

        LatLng ll = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        b.include(ll);

        LatLngBounds bounds = b.build();
        Log.d("bounds", "" + bounds);

        // Change the padding as per needed
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(latitudecur, longitudecur), zoomLevel);
        googleMap.animateCamera(cu);
        googleMap.moveCamera(cu);

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

    public void getLocation() {
        Intent intent = getIntent();
        String latString = intent.getStringExtra("delivery_lat");
        String lngString = intent.getStringExtra("delivery_lon");
        delivery_id = intent.getStringExtra("delivery_id");
        delivery_user_id = intent.getStringExtra("delivery_user_id");
        delivery_address = intent.getStringExtra("delivery_address");
        delivery_alias = intent.getStringExtra("delivery_alias");
        delivery_phone = intent.getStringExtra("delivery_phone");
        delivery_note = intent.getStringExtra("delivery_note");
        delivery_department_number = intent.getStringExtra("delivery_department_number");

        if (!latString.isEmpty() && !lngString.isEmpty()) {
            latitudecur = Double.parseDouble(latString);
            longitudecur = Double.parseDouble(lngString);
        } else {
            getMyLocation();
        }

        changeStatsBarColor(SelectLocationActivity.this);

        initialize();
        gettingSharedPref();
    }

    private void getMyLocation() {

        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        boolean isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {

            GPSTracker gps = new GPSTracker();
            gps.init(SelectLocationActivity.this);
            // check if GPS enabled
            if (gps.canGetLocation()) {
                try {
                    latitudecur = gps.getLatitude();
                    longitudecur = gps.getLongitude();
                } catch (NumberFormatException e) {
                    // TODO: handle exception
                }

            } else {
                gps.showSettingsAlert("Activa tu GPS para agregar una ubicación de entrega.");
            }
        } else {
            Toast.makeText(SelectLocationActivity.this, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }
}
