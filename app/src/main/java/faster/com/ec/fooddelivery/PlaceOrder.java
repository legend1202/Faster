package faster.com.ec.fooddelivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import faster.com.ec.Getset.cartgetset;
import faster.com.ec.R;
import faster.com.ec.deliveryData;
import faster.com.ec.utils.GPSTracker;
import faster.com.ec.utils.sqliteHelper;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;
import static faster.com.ec.fooddelivery.MainActivity.tf_opensense_regular;

public class PlaceOrder extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    private double latitudecur;
    private double longitudecur;
    private GoogleMap googleMap;
    String Error;
    private String address;
    private EditText edt_address;
    faster.com.ec.utils.sqliteHelper sqliteHelper;
    private static ArrayList<cartgetset> cartlist;
    private String res_id, res_notes;
    private String description;
    //    private SQLiteDatabase db;
    private String userid;
    private EditText edt_note;

    private static final String MY_PREFS_NAME = "Fooddelivery";
    private String detail_id;
    private String price="0.0";
    private String foodprice="0.0";
    private double total = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        changeStatsBarColor(PlaceOrder.this);


        getSharedPref();
        initialization();
        gettingIntent();

    }

    @Override
    protected void onStart() {
        super.onStart();

        gettingLocation();
        new GetDataAsyncTask().execute();


    }



    private void gettingIntent() {
        Intent iv = getIntent();
        detail_id = iv.getStringExtra("detail_id");
        res_id = iv.getStringExtra("detail_id");
        foodprice = iv.getStringExtra("order_price");
        res_notes= iv.getStringExtra("res_notes");
        Log.e("detail_id", "" + detail_id);
    }

    private void getSharedPref() {
        SharedPreferences sp = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userid = sp.getString("userid", "");

    }

    private void gettingLocation() {
        latitudecur = Double.parseDouble(deliveryData.delivery_lat);
        longitudecur = Double.parseDouble(deliveryData.delivery_lon);
    }

    private void initialization() {

        getSupportActionBar().hide();
        TextView txt_header = findViewById(R.id.txt_title);
        txt_header.setTypeface(tf_opensense_regular);
        edt_address = findViewById(R.id.edt_address);
        edt_address.setTypeface(tf_opensense_regular);
        edt_address.setKeyListener(null);

        edt_note = findViewById(R.id.edt_note);
        edt_note.setTypeface(tf_opensense_regular);


        Button btn_placeorder = findViewById(R.id.btn_placeorder);
        btn_placeorder.setTypeface(tf_opensense_regular);

        MapsInitializer.initialize(getApplicationContext());
        initilizeMap();

        btn_placeorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });

        new getList().execute();


    }

    private void validation() {
        if (edt_address.getText().toString().trim().isEmpty()) {
            edt_address.setError("Clic en Seleccionar Ubicación");
        } else if (edt_note.getText().toString().trim().isEmpty()) {
            edt_note.setError("Ejemplo: casa azul");
        } else {

            address = edt_address.getText().toString().replace(" ", "%20");
            description = edt_note.getText().toString().replace(" ", "%20");
            // new PostDataAsyncTask().execute();
            getDataEnvio();

        }

    }


    private void initilizeMap() {

        SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment));
        supportMapFragment.getMapAsync(this);
        (findViewById(R.id.mapFragment)).getViewTreeObserver()
                .addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        (findViewById(R.id.mapFragment)).getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_map), Toast.LENGTH_SHORT).show();
                return;
            }
            this.googleMap = googleMap;
            googleMap.setOnMarkerDragListener(this);


            initializeUiSettings();
            initializeMapLocationSettings();
            initializeMapTraffic();
            initializeMapType();
            initializeMapViewSettings();

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            LatLng position = new LatLng(latitudecur, longitudecur);
            googleMap.addMarker(new MarkerOptions().position(position).draggable(true).title(address));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));


        } catch (NullPointerException | NumberFormatException e) {
            // TODO: handle exception
        }

    }

    private void initializeUiSettings() {
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void initializeMapLocationSettings() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng field = marker.getPosition();
        System.out.println("LatitudenLongitude:" + field.latitude + " " + field.longitude);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng field = marker.getPosition();

        latitudecur = field.latitude;
        longitudecur = field.longitude;

    }


    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(PlaceOrder.this);
            pd.setMessage(getString(R.string.txt_load));
            pd.setCancelable(false);
            pd.show();


        }

        @Override
        protected Void doInBackground(Void... params) {

            if (getAddress() != null)
                address = getAddress().get(0).getAddressLine(0);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            LatLng position = new LatLng(latitudecur, longitudecur);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));

            if(address!=null)
            {
                edt_address.setText(address);
                edt_note.requestFocus();
                edt_note.requestFocusFromTouch();
            }
            else{
                AlertDialog.Builder builder1 = new AlertDialog.Builder(PlaceOrder.this, R.style.MyDialogTheme);
                builder1.setTitle("Ubicación no obtenida");
                builder1.setCancelable(false);
                builder1.setMessage("Lo sentimos no pudimos obtener tu ubicación GPS, por favor ubícate en un lugar abierto, e inténtalo nuevamente");
                builder1.setPositiveButton("Activar GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        PlaceOrder.this.startActivity(intent);

                    }
                });
                builder1.setNegativeButton("Reintentar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        gettingLocation();
                        new GetDataAsyncTask().execute();

                    }
                });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }

        }

    }



    private List<Address> getAddress() {
        if (latitudecur != 0 && longitudecur != 0) {
            try {
                Geocoder geocoder = new Geocoder(PlaceOrder.this);
                List<Address> addresses = geocoder.getFromLocation(latitudecur, longitudecur, 1);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                Log.d("TAG", "address = " + address + ", city = " + city + ", country = " +country);
                return addresses;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Toast.makeText(PlaceOrder.this, R.string.error_lat_long, Toast.LENGTH_LONG).show();
        }
        return null;
    }


    private class getList extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            sqliteHelper = new sqliteHelper(PlaceOrder.this);
            SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();


//            DBAdapter myDbHelper;
//            myDbHelper = new DBAdapter(PlaceOrder.this);
//            try {
//                myDbHelper.createDataBase();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                myDbHelper.openDataBase();
//            } catch (SQLException sqle) {
//                sqle.printStackTrace();
//            }
//            db = myDbHelper.getReadableDatabase();
            try {
                Cursor cur = db1.rawQuery("select * from cart where foodprice >=1 and resid="+detail_id+";", null);
                Log.e("cartlistingplaceorder", "" + ("select * numberOfRecords cart where foodprice >=1;"));
                Log.d("SIZWA", "" + cur.getCount());
                if (cur.getCount() != 0) {
                    if (cur.moveToFirst()) {
                        do {
                            cartgetset obj = new cartgetset();
                            String resid = cur.getString(cur.getColumnIndex("resid"));
                            String foodid = cur.getString(cur.getColumnIndex("foodname"));
                            String menuid = cur.getString(cur.getColumnIndex("menuid"));
                            String foodname = cur.getString(cur.getColumnIndex("foodname"));
                            String foodprice = cur.getString(cur.getColumnIndex("foodprice"));
                            String fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                            String restcurrency = cur.getString(cur.getColumnIndex("restcurrency"));
                            obj.setResid(resid);
                            obj.setFoodid(foodid);
                            obj.setMenuid(menuid);
                            obj.setFoodname(foodname);
                            obj.setFoodprice(foodprice);
                            obj.setFooddesc(fooddesc);
                            obj.setRestcurrency(restcurrency);
                            cartlist.add(obj);
                            try {
                                float quant = Float.parseFloat(foodprice);
                                float single = Float.parseFloat(restcurrency);
                                Log.e("12345", "" + quant + single);
                                float totalsum = quant * single;
                                total = totalsum + total;
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        } while (cur.moveToNext());
                    }
                }
                cur.close();
                db1.close();
//                myDbHelper.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {

        }
    }

    public void getDataEnvio() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String currentLocation = prefs.getString("CityName", null).replace(" ","%20");

        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "getdeliveryprice.php?res_id=" + res_id+"&lat="+ String.valueOf(latitudecur)+"&long="+ String.valueOf(longitudecur)+"&city="+currentLocation;
        Log.d("CheckUrl", "" + getString(R.string.link) + getString(R.string.servicepath) + "getdeliveryprice.php?res_id=" + res_id+"&lat="+ String.valueOf(latitudecur)+"&long="+String.valueOf(longitudecur)+"&city="+currentLocation);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);

                            String status = obj.getString("success");
                            if (Objects.equals(status, "1")) {
                                JSONArray ja_orderDetail = obj.getJSONArray("order_details");
                                Log.e("Response", obj.toString());
                                JSONObject jo_data = ja_orderDetail.getJSONObject(0);
                                price = jo_data.getString("delivery_price");
                                //Dialogo
                                total=Double.parseDouble(price)+Double.parseDouble(foodprice.replace(",","."));
                                Intent iv = new Intent(PlaceOrder.this, OrderInfo.class);
                                iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                //IntentOrderInfo
                                iv.putExtra("order_price", foodprice);
                                iv.putExtra("delivery_price", price);
                                iv.putExtra("total_price", String.format("%.2f", total));
                                iv.putExtra("address", edt_address.getText().toString());
                                iv.putExtra("latitude", String.valueOf(latitudecur));
                                iv.putExtra("longitude", String.valueOf(longitudecur));
                                iv.putExtra("description", description);
                                startActivity(iv);
                                finish();
                            } else  {
                                JSONArray ja_orderDetail = obj.getJSONArray("order_details");
                                Log.e("Response", obj.toString());
                                JSONObject jo_data = ja_orderDetail.getJSONObject(0);
                                String messages = jo_data.getString("message");

                                AlertDialog.Builder builder1 = new AlertDialog.Builder(PlaceOrder.this, R.style.MyDialogTheme);
                                builder1.setTitle("No es posible enviar tu pedido");
                                builder1.setCancelable(false);
                                builder1.setMessage(messages);
                                builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {

                                        dialog.cancel();
                                        Intent intent = new Intent(PlaceOrder.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("CheckUrl",e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //displaying the error in toast if occurrs
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(stringRequest);
    }
}
