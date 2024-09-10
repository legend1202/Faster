package faster.com.ec.fooddelivery;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import faster.com.ec.R;
import faster.com.ec.helper.DatabaseHandler;
import faster.com.ec.helper.SessionManager;
import faster.com.ec.utils.Config;
import faster.com.ec.utils.GPSTracker;
import faster.com.ec.utils.check_sesion;

public class Splash extends AppCompatActivity {
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private boolean isDeliveryAccountActive = false;
    private SessionManager session;
    private DatabaseHandler db;

    ProgressDialog progressDialog;
    private double latitudecur;
    private double longitudecur;
    private String direccion_envio;
    private String userId1;
    private String regId;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String[] permission_location = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    private static final int SPLASH_TIME_OUT = 100; // Delay of 1 Seconds

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        changeStatsBarColor(Splash.this);

        SharedPreferences prefs1 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userId1 = prefs1.getString("userid", null);
        displayFirebaseRegId();

        // check user is already logged in
        // create sqlite database
        db = new DatabaseHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String phonenumber = prefs.getString("phonenumber", null);
        String orderId= prefs.getString("orderId", null);
        Boolean rating= prefs.getBoolean("rating", false);

        // check user is already logged in
        if (session.isLoggedIn() && phonenumber!=null) {
            if (orderId==null ) {
                int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);

                if (result != PackageManager.PERMISSION_GRANTED) {
                    verifyPermissions(Splash.this);
                } else {
                    gettingGPSLocation();
                    if(latitudecur!=0){
                        new LongOperation().execute();
                    }
                }
            }
            else {
                Intent i = new Intent(Splash.this, CompleteOrder.class);
                startActivity(i);
                finish();
            }
        }

        else if (phonenumber!=null) {
            Intent iv = new Intent(Splash.this, Loginphoto.class);
            startActivity(iv);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
        else{
            Intent iv = new Intent(Splash.this, Login4.class);
            startActivity(iv);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }

    }

    @Override
    protected void onStart () {
        super.onStart();

    }

    private static void verifyPermissions (final Activity activity){
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, permission_location, PERMISSION_REQUEST_CODE);

        } else {
            ActivityCompat.requestPermissions(activity, permission_location, PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult (int requestCode, @NotNull String[] permissions,
                                            @NotNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                gettingGPSLocation();

                if (latitudecur != 0) {
                    new LongOperation().execute();
                }

            } else {
                verifyPermissions(Splash.this);
            }
        }

    }


    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            //some heavy processing resulting in a Data String
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String phonenumber = prefs.getString("phonenumber", null);
            String orderId = prefs.getString("orderId", null);
            Boolean rating = prefs.getBoolean("rating", false);
            session = new SessionManager(getApplicationContext());

            if (session.isLoggedIn() && phonenumber != null) {
                if (orderId == null) {

                    String hp;
                    hp = getString(R.string.link) + getString(R.string.servicepath) + "user_check_restaurant_location.php?";

                    StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            Log.e("PlaceOrder5", "ResponseFromServer " + response + "user id: " + userId1);
                            try {
                                JSONObject responsedat = new JSONObject(response);
                                String txt_success = responsedat.getString("success");
                                String txt_message = responsedat.getString("message");

                                switch (txt_success) {
                                    case "1": {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // This method will be executed once the timer is over
                                                Intent iv = new Intent(Splash.this, MenuActivity.class);
                                                iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                iv.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                startActivity(iv);
                                                // close this activity
                                                finish();
                                            }
                                        }, SPLASH_TIME_OUT);
                                        //IntentOrderInfo
                                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                        editor.putString("city", responsedat.getString("city"));
                                        editor.apply();
                                        break;
                                    }
                                    case "200":
                                    case "201":
                                    case "202":
                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Splash.this, R.style.MyDialogTheme);
                                        builder1.setTitle("Información");
                                        builder1.setCancelable(false);
                                        builder1.setMessage(txt_message);
                                        builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                        AlertDialog alert11 = builder1.create();
                                        try {
                                            alert11.show();
                                        } catch (Exception e) {
                                            Log.e("Error_alert11", e.getMessage());
                                        }

                                        break;
                                    case "4":
                                    case "5": {

                                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                        editor.putString("phonenumber", null);
                                        editor.apply();

                                        SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0).edit();
                                        editor2.putString("regId", null);
                                        editor2.apply();

                                        // session manager
                                        session.setLogin(false);

                                        Intent i = new Intent(Splash.this, Login4.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                        finish();

                                        break;
                                    }
                                    default:
                                        check_sesion se = new check_sesion();
                                        se.validate_sesion(Splash.this, txt_success, txt_message);
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
                                String message = null;
                                if (error instanceof TimeoutError || error instanceof NetworkError) {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(Splash.this, R.style.MyDialogTheme);
                                    builder1.setTitle("Información");
                                    builder1.setCancelable(false);
                                    builder1.setMessage("Por favor verifica tu conexión a Internet");
                                    builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            new LongOperation().execute();
                                        }
                                    });
                                    builder1.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            finishAffinity();
                                        }
                                    });

                                    AlertDialog alert11 = builder1.create();
                                    try {
                                        alert11.show();
                                    } catch(Exception e) {
                                        Log.e("Error_alert11", e.getMessage());
                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("user_id", userId1);
                            params.put("lat", String.valueOf(latitudecur));
                            params.put("lon", String.valueOf(longitudecur));
                            params.put("code", getString(R.string.version_app));
                            params.put("operative_system", getString(R.string.sistema_operativo));
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Authorization", "Bearer " + regId);
                            return headers;
                        }

                        @Override
                        public String getBodyContentType() {
                            return super.getBodyContentType();
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(Splash.this);
                    requestQueue.add(postRequest);


                } else {
                    Intent i = new Intent(Splash.this, CompleteOrder.class);
                    startActivity(i);
                    finish();
                }
            } else if (phonenumber != null) {
                Intent iv = new Intent(Splash.this, Loginphoto.class);
                startActivity(iv);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
            return "whatever result you have";
        }

        @Override
        protected void onPostExecute(String result) {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    private void displayFirebaseRegId () {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }


    private void gettingGPSLocation () {
        checkPermissions();
        GPSTracker gps = new GPSTracker();
        gps.init(Splash.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
                latitudecur = gps.getLatitude();
                longitudecur = gps.getLongitude();
                // latitudecur = -0.25074675177414346;
                // longitudecur = -79.15352340227966;
                direccion_envio = Objects.requireNonNull(getAddress()).get(0).getAddressLine(0);

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("latitud", String.valueOf(latitudecur));
                editor.putString("longitud", String.valueOf(longitudecur));
                editor.putString("direccion_envio", String.valueOf(direccion_envio));
                editor.apply();

                LatLng position = new LatLng(latitudecur, longitudecur);

                Log.w("Current Location", "Lat: " + latitudecur + "Long: " + longitudecur);
            } catch (NullPointerException | NumberFormatException e) {
                // TODO: handle exception
            }


        } else {
            createLocationRequest();
        }
    }

    private void checkPermissions () {
        int permissionLocation = ContextCompat.checkSelfPermission(Splash.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }

    }

    private List<Address> getAddress () {
        if (latitudecur != 0 && longitudecur != 0) {
            try {
                Geocoder geocoder = new Geocoder(Splash.this);
                List<Address> addresses = geocoder.getFromLocation(latitudecur, longitudecur, 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                Log.d("TAG", "address = " + address + ", city = " + city + ", country = " + country);
                return addresses;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Toast.makeText(PlaceOrder.this, R.string.error_lat_long, Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...

                Toast.makeText(Splash.this, "GPS ya está activado",
                        Toast.LENGTH_LONG).show();
                Log.d("location settings",locationSettingsResponse.toString());
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(Splash.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CHECK_SETTINGS){

            if(resultCode==RESULT_OK){


                gettingGPSLocation();
                if (direccion_envio != null) {
                    new LongOperation().execute();
                }
                else{

                    activategps();
                }


            }else if(resultCode==RESULT_CANCELED){

                createLocationRequest();
            }

        }
    }

    private void activategps(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(Splash.this, R.style.MyDialogTheme);
        builder1.setTitle("GPS ACTIVADO");
        builder1.setCancelable(false);
//        builder1.setMessage("Tu Gps ya se encuentra activado, clic en reintentar para obtener tu ubicación");
        builder1.setMessage(Html.fromHtml("Tu Gps ya se encuentra activado, clic en reintentar para obtener tu ubicación"));
        builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                gettingGPSLocation();
                if (direccion_envio != null) {
                    new LongOperation().execute();
                }
                else{
                    activategps();
                }

            }
        });

        AlertDialog alert11 = builder1.create();
        try {
            alert11.show();
        } catch(Exception e) {
            Log.e("Error_alert11", e.getMessage());
        }
    }

}

