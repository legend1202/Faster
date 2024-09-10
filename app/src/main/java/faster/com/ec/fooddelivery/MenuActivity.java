package faster.com.ec.fooddelivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import faster.com.ec.R;
import faster.com.ec.helper.SessionManager;
import faster.com.ec.utils.Config;
import faster.com.ec.utils.GPSTracker;

public class MenuActivity extends AppCompatActivity {

    private ProgressDialog pd;
    String Error;
    String filterName;
    private double latitudecur = 0;
    private double longitudecur = 0;
    private String direccion_envio;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private String timezoneID;
    private String search = "";
    private TextView txt_nameuser;
    public static int numberOfRecord;
    private static int pageCount;
    private int radius;
    private String Location;
    private SharedPreferences prefs;
    String userId1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_menu);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String uname = prefs.getString("username", null);

        TextView textView = findViewById(R.id.username);
        // assert uname != null;
        if (!uname.equals("")){
            if(uname.contains(" ")) {
                int num = uname.indexOf(" ");
                String fname = uname.substring(0,num);
                textView.setText("Hola "+fname);
            } else {
                textView.setText("Hola "+uname);
            }
        } else {
            Toast.makeText(this, "Debe ingresar un Nombre y Apellido valido.", Toast.LENGTH_SHORT).show();
        }
        initView();
    }

    private void initView(){
        ImageView img_menu = findViewById(R.id.img_menu);
        Picasso.get()
                .load("https://app.faster.com.ec/uploads/menu/menu_market.png")
                .placeholder(R.drawable.menu_market)
                .error(R.drawable.menu_market)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(img_menu);
    }

    public void cardViewClicked(View view) {
        filterName = "";
        switch (view.getId()) {
            case R.id.restaurantes:
                filterName = "Restaurante";
                break;
            case R.id.supermercados:
                filterName = "Supermercado";
                break;
            case R.id.farmacias:
                filterName = "Farmacia";
                break;
            case R.id.regalos:
                filterName = "Regalo";
                break;
            case R.id.loquesea:
                filterName = "Lo Que Sea";
                break;
            case R.id.pastelerias:
                filterName = "Panadería";
                break;
            case R.id.licores:
                filterName = "Licorería";
                break;
            case R.id.transporte:
                filterName = "Transporte";
                break;
            case R.id.market:
                filterName = "Market";
                break;
            case R.id.advertising:
                Intent iv = new Intent(MenuActivity.this, AdvertiseActivity2.class);
                startActivity(iv);
                return;
            default:
                break;
        }
        new GetDataAsyncTask().execute();
    }

    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MenuActivity.this);
            pd.setMessage("Buscando servicios, empresas o negocios...");
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {

                timezoneID = TimeZone.getDefault().getID();
                numberOfRecord = getResources().getInteger(R.integer.numberOfRecords);
                pageCount = 1;
                gettingSharedPref();

                //String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list_filter.php?timezone=" + TimeZone.getDefault().getID() + "&" + "lat=" + "latitudecur" + "&lon=" + "-78.4636728" + "&search=" + "" + "&noofrecords=" + "5" + "&pageno=" + "1" + "&radius=" + "4" + "&city=" + "Santo" + "&user_id=" + "1102" + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo) + "&type_store=" + filterName;
                String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list_filter.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&lon=" + longitudecur + "&search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo) + "&type_store=" + filterName;

                hp = new URL(user.replace(" ", "%20"));

                Log.e("URLs", "" + hp);
                URLConnection hpCon = hp.openConnection();

                SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                String regId = pref.getString("regId", null);
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
                    if(jsonArray.length()==0) {

                    }

                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                    intent.putExtra("filterName", filterName);
                    startActivity(intent);

                } else if (txt_success.equals("201") || txt_success.equals("203") || txt_success.equals("204")) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(MenuActivity.this, txt_message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (txt_success.equals("202")) {
                    Error = "all_load";
                }  if (txt_success.equals("2")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MenuActivity.this, R.style.MyDialogTheme);
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
                                                Toast.makeText(MenuActivity.this, "No se puede conectar intente nuevamente...",
                                                        Toast.LENGTH_LONG).show();

                                            }
                                        });

                                        e.printStackTrace();
                                    }
                                }
                            });
                            AlertDialog alert11 = builder1.create();
                            try {
                                alert11.show();
                            } catch(Exception e) {
                                Log.e("Error_alert12", Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    });

                } else if (txt_success.equals("3")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MenuActivity.this, R.style.MyDialogTheme);
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
                            try {
                                alert11.show();
                            } catch(Exception e) {
                                Log.e("Error_alert12", Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    });

                } else if (txt_success.equals("4")||txt_success.equals("5"))  {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MenuActivity.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage(txt_message);
                            builder1.setPositiveButton(Html.fromHtml("<font color=#48dd79>Entiendo</font>"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {

                                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                    editor.putString("phonenumber",null);
                                    editor.apply();

                                    SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0).edit();
                                    editor2.putString("regId", null);
                                    editor2.apply();

                                    SessionManager session;
                                    session = new SessionManager(getApplicationContext());
                                    // session manager
                                    session.setLogin(false);

                                    Intent iv = new Intent(MenuActivity.this, Login4.class);
                                    iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(iv);
                                    finish();

                                }
                            });
                            AlertDialog alert11 = builder1.create();
                            try {
                                alert11.show();
                            } catch(Exception e) {
                                Log.e("Error_alert12", Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    });

                } else if (txt_success.equals("6"))  {
                    Intent iv = new Intent(MenuActivity.this, CompleteOrder.class);
                    startActivity(iv);
                    finish();
                }



            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
                Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                // TODO: handle exception
                Error = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            } catch(Exception e) {
                Log.e("Error_pd", Objects.requireNonNull(e.getMessage()));
            }
            if (Error != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MenuActivity.this, Error, Toast.LENGTH_SHORT).show();
                    }
                });

            } /*else {

            }*/
        }
    }


    @Override
    public void onBackPressed(){
        //ventana flotante para saliring(R.string.yes), new DialogInterface.OnClickListener() {
        finishAffinity();
    }

    private boolean GPSLocationisactive() {
        GPSTracker gps = new GPSTracker();
        gps.init(MenuActivity.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
//                latitudecur = gps.getLatitude();
//                longitudecur = gps.getLongitude();
                latitudecur = -0.25074675177414346;
                longitudecur = -79.15352340227966;
                direccion_envio = Objects.requireNonNull(getAddress()).get(0).getAddressLine(0);

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("latitud", String.valueOf(latitudecur));
                editor.putString("longitud", String.valueOf(longitudecur));
                editor.putString("direccion_envio", String.valueOf(direccion_envio));
                editor.apply();
                return true;
            } catch (NullPointerException | NumberFormatException e) {
                // TODO: handle exception
            }

        } else {
            createLocationRequest();
            return false;
        }
        return true;

    }

    private List<Address> getAddress () {
        if (latitudecur != 0 && longitudecur != 0) {
            try {
                Geocoder geocoder = new Geocoder(MenuActivity.this);
                List<Address> addresses = geocoder.getFromLocation(latitudecur, longitudecur, 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                Log.d("TAG", "address = " + address + ", city = " + city + ", country = " + country);
                return addresses;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } /*else {
            Toast.makeText(PlaceOrder.this, R.string.error_lat_long, Toast.LENGTH_LONG).show();
        }*/
        return null;
    }

    public void createLocationRequest() {
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

                Toast.makeText(MenuActivity.this, "GPS ya está activado",
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
                        resolvable.startResolutionForResult(MenuActivity.this,
                                Splash.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private void gettingSharedPref() {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Location = prefs.getString("city", null);
        radius = 4;
        userId1 = prefs.getString("userid", null);
    }
}
