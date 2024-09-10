package faster.com.ec.fooddelivery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import faster.com.ec.Adapter.ListViewHolderCart;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;

public class Cart extends AppCompatActivity {
    private static ArrayList<cartgetset> cartlist;
    private ProgressDialog progressDialog;
    private String menuid;
    private String s1;
    private double total = 0.00;
    private TextView txt_finalans;
    private int quantity;
    private EditText edt_review, edt_notes;
    ImageButton ib_back;
    faster.com.ec.utils.sqliteHelper sqliteHelper;
    private String detail_id;
    private String restaurent_name;
    private String notes;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private ListView list_cart;
    private double latitudecur;
    private double longitudecur;
    private String delivery_price;
    private String foodprice;
    private Double total_price=0.00;
    private String direccion_envio;

    private TextView txt_delivery_price;
    private TextView txt_tota;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String[] permission_location = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        changeStatsBarColor(Cart.this);

        initialization();
        gettingIntent();
        ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getData();

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);

        if (result != PackageManager.PERMISSION_GRANTED) {
            verifyPermissions(Cart.this);
        } else {
            gettingGPSLocation();
            if(latitudecur!=0){
                getDataEnvio();
            }
        }

    }


    private void gettingIntent() {
        Intent iv = getIntent();
        detail_id = iv.getStringExtra("detail_id");
        restaurent_name = iv.getStringExtra("restaurent_name");

        Log.e("detail_id", "" + detail_id);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);


    }

    private static void verifyPermissions ( final Activity activity){
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, permission_location, PERMISSION_REQUEST_CODE);

        } else {
            ActivityCompat.requestPermissions(activity, permission_location, PERMISSION_REQUEST_CODE);
        }
    }

    private void checkPermissions () {
        int permissionLocation = ContextCompat.checkSelfPermission(Cart.this,
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

    private void gettingGPSLocation () {
        latitudecur = Double.parseDouble(deliveryData.delivery_lat);
        longitudecur = Double.parseDouble(deliveryData.delivery_lon);
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

                Toast.makeText(Cart.this, "GPS ya está activado",
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
                        resolvable.startResolutionForResult(Cart.this,
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
                    getDataEnvio();
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
        AlertDialog.Builder builder1 = new AlertDialog.Builder(Cart.this, R.style.MyDialogTheme);
        builder1.setTitle("GPS ACTIVADO");
        builder1.setCancelable(false);
        builder1.setMessage("Tu Gps ya se encuentra activado, clic en Calcular envío para continuar");
        builder1.setPositiveButton("Calcular Envío", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                gettingGPSLocation();
                if (direccion_envio != null) {
                    getDataEnvio();
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
            Log.e("Error_alert11", Objects.requireNonNull(e.getMessage()));
        }
    }

    private List<Address> getAddress () {
        if (latitudecur != 0 && longitudecur != 0) {
            try {
                Geocoder geocoder = new Geocoder(Cart.this);
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


    private void initialization() {
        edt_notes= findViewById(R.id.edt_notes);
        txt_finalans = findViewById(R.id.txt_finalans);
        //((TextView) findViewById(R.id.txt_title)).setTypeface(MainActivity.tf_opensense_medium);
        list_cart = findViewById(R.id.list_cart);
        txt_delivery_price= findViewById(R.id.txt_delivery_price);
        txt_tota= findViewById(R.id.txt_tota);

        edt_notes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (null != edt_notes.getLayout() && edt_notes.getLayout().getLineCount() > 2) {
                    edt_notes.getText().delete(edt_notes.getText().length() - 1, edt_notes.getText().length());
                }
            }
        });

    }

    private void getData() {
        cartlist = new ArrayList<>();
        new getList().execute();
        Log.d("hola","ex1 "+cartlist.toString());
    }

    private class getList extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(Cart.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            cartlist.clear();
            sqliteHelper = new sqliteHelper(Cart.this);
            SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();

            try {
                Cursor cur = db1.rawQuery("select * from cart where foodprice >0 and resid="+detail_id+";", null);
                // Log.e("cartlisting", "" + ("select * numberOfRecords cart where foodprice <=0;"+detail_id));
                // Log.d("SIZWA", "" + cur.getCount());
                if (cur.getCount() != 0) {
                    if (cur.moveToFirst()) {
                        do {
                            cartgetset obj = new cartgetset();
                            String resid = cur.getString(cur.getColumnIndex("resid"));
                            String foodid = cur.getString(cur.getColumnIndex("foodname"));
                            menuid = cur.getString(cur.getColumnIndex("menuid"));
                            String foodname = cur.getString(cur.getColumnIndex("foodname"));
                            String foodprice = cur.getString(cur.getColumnIndex("foodprice"));
                            String fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                            String restcurrency = cur.getString(cur.getColumnIndex("restcurrency"));
                            restcurrency = restcurrency.replace("$", "");
                            obj.setResid(resid);
                            obj.setFoodid(foodid);
                            obj.setMenuid(menuid);
                            obj.setFoodname(foodname);
                            obj.setFoodprice(foodprice);
                            obj.setFooddesc(fooddesc);
                            obj.setRestcurrency(restcurrency);
                            cartlist.add(obj);
                            try {
                                double quant = Double.parseDouble(foodprice);
                                double single = Double.parseDouble(restcurrency);
                                Log.e("12345", "" + quant + single);
                                double totalsum = quant * single;
                                total = totalsum + total;
                            } catch (NumberFormatException e) {
                                Log.e("Error", e.getMessage());
                            }
                        } while (cur.moveToNext());
                    }
                }
                cur.close();
                db1.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }


            if (cartlist.size() == 0) {
                Log.d("file", "" + cartlist.size());
                AlertDialog.Builder builder1 = new AlertDialog.Builder(Cart.this, R.style.MyDialogTheme);
                builder1.setTitle("Canasta vacía");
                builder1.setMessage("Aún no tienes productos en tu canasta.");
                builder1.setCancelable(false);
                builder1.setPositiveButton("Entiendo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent iv = new Intent(Cart.this, MenuList.class);
                        iv.putExtra("detail_id", detail_id);
                        iv.putExtra("restaurent_name", restaurent_name);
                        finish();
                        //startActivity(iv);
                    }
                });
                AlertDialog alert11 = builder1.create();
                try {
                    alert11.show();
                } catch(Exception e) {
                    Log.e("Error_alert11", e.getMessage());
                }

            } else {
                list_cart = findViewById(R.id.list_cart);
                Button btncheckout = findViewById(R.id.btn_checkout);
                //btncheckout.setTypeface(MainActivity.tf_opensense_regular);
                btncheckout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Double double_finalans = Double.valueOf(txt_finalans.getText().toString().replace(",", ".").replace("$ ", ""));
                        //if(!txt_finalans.getText().equals("$ 0,00") && !txt_finalans.getText().equals("$ 0.00")) {
                        if(double_finalans > 0.00) {
                            Intent iv = new Intent(Cart.this, OrderInfo.class);
                            iv.putExtra("order_price", "" + txt_finalans.getText().toString().replace("$ ",""));
                            iv.putExtra("detail_id", detail_id);
                            iv.putExtra("deliveryprice", delivery_price.replace(".",","));
                            iv.putExtra("totalprice", String.format("%.2f",total_price));
                            iv.putExtra("detail_id", detail_id);

                            if(edt_notes.getText().toString().length()>1){
                                notes=edt_notes.getText().toString();
                            }else{
                                notes="";
                            }
                            iv.putExtra("notes", notes);
                            startActivity(iv);

                        }else{
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(Cart.this, R.style.MyDialogTheme);
                            builder1.setTitle("Canasta vacía");
                            builder1.setMessage("Aún no tienes productos en tu canasta.");
                            builder1.setCancelable(true);
                            builder1.setPositiveButton("Entiendo", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent iv = new Intent(Cart.this, MenuList.class);
                                    iv.putExtra("restaurent_name", "" + restaurent_name);
                                    iv.putExtra("detail_id", detail_id);
                                    finish();
                                    //startActivity(iv);
                                }
                            });
                            builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
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
                });
                cartadapter1 adapter = new cartadapter1(Cart.this, cartlist, menuid, quantity);
                list_cart.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                list_cart.getAdapter().getCount();
                String totalcartitem = String.valueOf(list_cart.getAdapter().getCount());
                if (totalcartitem.equals("0")) {
                    btncheckout.setEnabled(false);
                    Toast.makeText(getApplicationContext(), R.string.cart_empty, Toast.LENGTH_SHORT).show();
                } else {
                    btncheckout.setEnabled(true);
                }
                list_cart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    }
                });
                double roundOff = total;
                txt_finalans.setText(getString(R.string.currency) + " " + String.format ("%.2f", roundOff).replace(".",","));
                Button btn_cart = findViewById(R.id.btn_cart);
                btn_cart.setText(totalcartitem);
            }
        }
    }




    @Override
    public void onBackPressed() {
        Intent iv = new Intent(Cart.this, MenuList.class);
        iv.putExtra("detail_id", detail_id);
        iv.putExtra("restaurent_name", restaurent_name);
        startActivity(iv);
        this.finish();
    }


    class cartadapter1 extends BaseAdapter {
        final ArrayList<Integer> quantity = new ArrayList<>();
        SQLiteDatabase db;
        Cursor cur = null;
        final String menuid1;
        String menuid321, foodprice321, restcurrency321;
        final int quen;
        String foodid, foodname, fooddesc;
        int val1;
        double add, sub;
        private final ArrayList<cartgetset> data1;
        private final Activity activity;
        private LayoutInflater inflater = null;

        cartadapter1(Activity a, ArrayList<cartgetset> str, String menuid, int quantity) {
            activity = a;
            data1 = str;
            menuid1 = menuid;
            quen = quantity;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (int i = 0; i < data1.size(); i++) {
                this.quantity.add(i);
            }
        }

        @Override
        public int getCount() {
            return data1.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row;
            final ListViewHolderCart listViewHolder;
            Typeface opensansregular = Typeface.createFromAsset(activity.getAssets(), "fonts/OpenSans-Regular.ttf");
            if (convertView == null) {
                row = inflater.inflate(R.layout.cell_cart, parent, false);
                listViewHolder = new ListViewHolderCart();
                listViewHolder.txt_name1 = row.findViewById(R.id.txt_name1);
                listViewHolder.txt_name1.setTypeface(opensansregular);
                listViewHolder.txt_desc = row.findViewById(R.id.txt_desc);
                listViewHolder.txt_desc.setTypeface(opensansregular);
                listViewHolder.txt_totalprice = row.findViewById(R.id.txt_totalprice);
                listViewHolder.txt_basic_price = row.findViewById(R.id.txt_basic_price);
                listViewHolder.txt_totalprice.setTypeface(MainActivity.tf_opensense_medium);
                listViewHolder.txt_basic_price.setTypeface(opensansregular);
                listViewHolder.txt_quantity = row.findViewById(R.id.txt_quantity);
                listViewHolder.txt_quantity.setTypeface(opensansregular);

                listViewHolder.btn_plus = row.findViewById(R.id.btn_plus);
                listViewHolder.btn_minus1 = row.findViewById(R.id.btn_minus1);
                listViewHolder.edTextQuantity = row.findViewById(R.id.edTextQuantity);
                listViewHolder.edTextQuantity.setTypeface(opensansregular);
                listViewHolder.btn_plus.setTag(listViewHolder);
                listViewHolder.btn_minus1.setTag(listViewHolder);
                row.setTag(listViewHolder);
            } else {
                row = convertView;
                listViewHolder = (ListViewHolderCart) row.getTag();
            }
            listViewHolder.txt_name1.setText((data1.get(position).getFoodname()));
            listViewHolder.txt_desc.setText(data1.get(position).getFooddesc());
            listViewHolder.txt_totalprice.setText(getString(R.string.currency) + " " + (String.format ("%.2f", Double.parseDouble(data1.get(position).getRestcurrency()))).replace(".",","));

            listViewHolder.edTextQuantity.setText(data1.get(position).getFoodprice());
            val1 = Integer.parseInt(data1.get(position).getFoodprice());
            listViewHolder.btn_plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View p = (View) v.getParent();
                    ListViewHolderCart holder1 = (ListViewHolderCart) (v.getTag());
                    val1 = Integer.valueOf(holder1.edTextQuantity.getText().toString());
                    String add_price = holder1.txt_totalprice.getText().toString().trim().replace("$", "");
                    add_price=add_price.replace(",", ".");
                    add = Double.parseDouble(add_price);
                    total = total + add;
                    Log.e("totalansadd ", "" + total);
                    double roundOff = total;
                    // here first one by yejun
                    total_price = total + Double.parseDouble(delivery_price);

                    txt_finalans.setText(getString(R.string.currency) + " " + String.format ("%.2f", roundOff).replace(".",","));
                    txt_tota.setText(getString(R.string.currency) + " " + String.format ("%.2f", total_price).replace(".",","));
                    val1++;
                    holder1.edTextQuantity.setText(String.valueOf(val1));
                    //DBAdapter myDbHelper;
                    //myDbHelper = new DBAdapter(activity);
                    sqliteHelper = new sqliteHelper(Cart.this);
                    SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();
                    /*try {
                        myDbHelper.createDataBase();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        myDbHelper.openDataBase();
                    } catch (SQLException sqle) {
                        sqle.printStackTrace();
                    }
                    db = myDbHelper.getReadableDatabase();*/
                    try {
                        cur = db1.rawQuery("UPDATE cart SET foodprice ='" + val1 + "' Where menuid ='" + data1.get(position).getMenuid() + "';", null);
                        Log.e("updatequeryplus", "" + "UPDATE cart SET foodprice ='" + val1 + "' Where menuid ='" + data1.get(position).getMenuid() + "';");
                        Log.e("SIZWA", "" + cur.getCount());
                        if (cur.getCount() != 0) {
                            if (cur.moveToFirst()) {
                                do {
                                    cartgetset obj = new cartgetset();
                                    menuid321 = cur.getString(cur.getColumnIndex("menuid"));
                                    foodid = cur.getString(cur.getColumnIndex("foodid"));
                                    foodname = cur.getString(cur.getColumnIndex("foodname"));
                                    foodprice321 = cur.getString(cur.getColumnIndex("foodprice"));
                                    fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                                    restcurrency321 = cur.getString(cur.getColumnIndex("restcurrency"));
                                    obj.setFoodid(foodid);
                                    obj.setMenuid(menuid321);
                                    obj.setFoodname(foodname);
                                    obj.setFoodprice(foodprice321);
                                    obj.setFooddesc(fooddesc);
                                    obj.setRestcurrency(restcurrency321);
                                    Log.e("menuid321updatedcart", "" + menuid321);
                                    Log.e("foodp321updatedcart", "" + foodprice321);
                                    data1.add(obj);
                                    new getList().execute();
                                } while (cur.moveToNext());
                            }
                        }
                        cur.close();
                        db1.close();
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());

                    }
                }
            });
            listViewHolder.btn_minus1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListViewHolderCart holder1 = (ListViewHolderCart) (v.getTag());
                    String sub_price = holder1.txt_totalprice.getText().toString().trim().replace("$", "");
                    sub_price=sub_price.replace(",", ".");
                    Log.e("subprice", sub_price);
                    sub = Double.parseDouble(sub_price);
                    val1=Integer.valueOf(holder1.edTextQuantity.getText().toString());
                    if (val1 > 0) {
                        val1 = Integer.valueOf(holder1.edTextQuantity.getText().toString());
                        val1--;
                        total = total - sub;
                        Log.e("totalanssub ", "" + total);
                        double roundOff = total;
                        // here second one by yejun
                        total_price = total + Double.parseDouble(delivery_price);
                        txt_finalans.setText(getString(R.string.currency) + " " + String.format ("%.2f", roundOff));
                        txt_tota.setText(getString(R.string.currency) + " " + String.format ("%.2f", total_price).replace(".",","));

                        holder1.edTextQuantity.setText(String.valueOf(val1));

                        sqliteHelper = new sqliteHelper(Cart.this);
                        SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();

                        try {
                            cur = db1.rawQuery("UPDATE cart SET foodprice ='" + val1 + "' Where menuid ='" + data1.get(position).getMenuid() + "';", null);
                            Log.e("updatequeryminus", "" + "UPDATE cart SET foodprice ='" + val1 + "' Where menuid ='" + data1.get(position).getMenuid() + "';");
                            Log.e("SIZWA", "" + cur.getCount());
                            if (cur.getCount() != 0) {
                                if (cur.moveToFirst()) {
                                    do {
                                        cartgetset obj = new cartgetset();
                                        menuid321 = cur.getString(cur.getColumnIndex("menuid"));
                                        foodid = cur.getString(cur.getColumnIndex("foodid"));
                                        foodname = cur.getString(cur.getColumnIndex("foodname"));
                                        foodprice321 = cur.getString(cur.getColumnIndex("foodprice"));
                                        fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                                        restcurrency321 = cur.getString(cur.getColumnIndex("restcurrency"));
                                        obj.setFoodid(foodid);
                                        obj.setMenuid(menuid321);
                                        obj.setFoodname(foodname);
                                        obj.setFoodprice(foodprice321);
                                        obj.setFooddesc(fooddesc);
                                        obj.setRestcurrency(restcurrency321);
                                        Log.e("menuid321updatedcart", "" + menuid321);
                                        Log.e("foodp321updatedcart", "" + foodprice321);
                                        data1.add(obj);
                                        new getList().execute();
                                    } while (cur.moveToNext());
                                }
                            }
                            cur.close();
                            db1.close();
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    }
                }
            });
            return row;
        }
    }

    public void getDataEnvio() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String currentLocation = prefs.getString("city", null).replace(" ","%20");
        //creating a string request to send request to the url
        String hp = getString(R.string.link) + getString(R.string.servicepath) + "getdeliveryprice.php?res_id=" + detail_id+"&lat="+ latitudecur+"&long="+ longitudecur+"&city="+currentLocation;
        //Log.d("CheckUrl", "" + getString(R.string.link) + getString(R.string.servicepath) + "getdeliveryprice.php?res_id=" + detail_id+"&lat="+ latitudecur+"&long="+longitudecur+"&city="+currentLocation);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, hp,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);
                            JSONArray ja_orderDetail = obj.getJSONArray("order_details");
                            Log.e("Response", obj.toString());
                            JSONObject jo_data = ja_orderDetail.getJSONObject(0);
                            String status = obj.getString("success");
                            if (Objects.equals(status, "1")) {
                                delivery_price = jo_data.getString("delivery_price");
                                foodprice = txt_finalans.getText().toString().replace("$ ","");
                                total_price=Double.parseDouble(delivery_price)+Double.parseDouble(foodprice.replace(",","."));
                                txt_delivery_price.setText(getString(R.string.currency) + " " + delivery_price.replace(".",","));
                                txt_tota.setText(getString(R.string.currency) + " " + String.format("%.2f", total_price).replace(".",","));
                            }
                            else{
                                String messages = jo_data.getString("message");
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(Cart.this, R.style.MyDialogTheme);
                                builder1.setTitle("No es posible enviar tu pedido");
                                builder1.setCancelable(false);
                                builder1.setMessage(messages);
                                builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(Cart.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                AlertDialog alert11 = builder1.create();
                                try {
                                    alert11.show();
                                } catch(Exception e) {
                                    Log.e("Error_alert11", Objects.requireNonNull(e.getMessage()));
                                }
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("CheckUrl", Objects.requireNonNull(e.getMessage()));
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