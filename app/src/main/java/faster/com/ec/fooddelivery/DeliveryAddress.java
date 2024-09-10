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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import faster.com.ec.Adapter.positionadapter;
import faster.com.ec.Getset.positionGetSet;
import faster.com.ec.R;
import faster.com.ec.deliveryData;
import faster.com.ec.utils.Config;
import faster.com.ec.utils.RecyclerTouchListener;


public class DeliveryAddress extends Activity {

    private static final String MY_PREFS_NAME = "Fooddelivery";
    private final int PERMISSION_REQUEST_CODE = 10011;
    private final int PERMISSION_REQUEST_CODE1 = 100111;
    private final String[] permission_location = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private final String[] permission_location1 = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private ProgressDialog progressDialog;

    private CardView selectPositionCardView;
    private SwipyRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private ImageButton ib_back;

    private positionadapter adapter;

    private String regId;
    private String delivery_userId;
    private String Error;
    private static ArrayList<positionGetSet> positionList;
    private String delivery_id = "";
    private String delivery_user_id = "";
    private String delivery_lat = "";
    private String delivery_lon = "";
    private String delivery_address = "";
    private String delivery_alias = "";
    private String delivery_phone = "";
    private String delivery_note = "";
    private String delivery_department_number = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveryaddress);
        changeStatsBarColor(DeliveryAddress.this);

        initializations();

        clickEvents();

        if (checkPermission()) {
            positionList = new ArrayList<>();
            Error = null;
            positionList.clear();
            new GetDataAsyncTask().execute();
            mSwipeRefreshLayout.setRefreshing(false);
            recyclerView.scrollToPosition(positionList.size() - 1);
        } else requestPermission();

    }

    private void initializations() {

        recyclerView = findViewById(R.id.listview);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        displayFirebaseRegId();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        delivery_userId = prefs.getString("userid", "");
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        delivery_id = prefs.getString("delivery_id", "");
        if (!delivery_id.isEmpty()) {
            Intent intent0 = new Intent(DeliveryAddress.this, MainActivity.class);
            startActivity(intent0);
            finish();
        }
    }

    private void clickEvents() {
        ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                delivery_id = prefs.getString("delivery_id", "");
                if (!delivery_id.isEmpty()) {
                    Intent intent0 = new Intent(DeliveryAddress.this, MainActivity.class);
                    startActivity(intent0);
                    finish();
                }
            }
        });

        selectPositionCardView = findViewById(R.id.selectPositionCardView);
        selectPositionCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeliveryAddress.this, SelectLocationActivity.class);
                intent.putExtra("delivery_id", "");
                intent.putExtra("delivery_user_id", delivery_userId);
                intent.putExtra("delivery_lat", "");
                intent.putExtra("delivery_lon", "");
                intent.putExtra("delivery_address", "");
                intent.putExtra("delivery_alias", "");
                intent.putExtra("delivery_phone", "");
                intent.putExtra("delivery_note", "");
                intent.putExtra("delivery_department_number", "");
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                new GetDataAsyncTask().execute();
                mSwipeRefreshLayout.setRefreshing(false);
                recyclerView.scrollToPosition(positionList.size() - 1);
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                delivery_id = positionList.get(position).getId();
                delivery_user_id = positionList.get(position).getUserId();
                delivery_lat = positionList.get(position).getLat();
                delivery_lon = positionList.get(position).getLon();
                delivery_address = positionList.get(position).getAddress();
                delivery_alias = positionList.get(position).getAlias();
                delivery_phone = positionList.get(position).getPhone();
                delivery_note = positionList.get(position).getDeliveryNote();
                delivery_department_number = positionList.get(position).getDepartmentNumber();

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(DeliveryAddress.this);
                builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle("Seleccione una opción");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(DeliveryAddress.this, android.R.layout.select_dialog_item);
                arrayAdapter.add("Seleccionar");
                arrayAdapter.add("Editar");
                arrayAdapter.add("Eliminar");

                builderSingle.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        switch (strName) {
                            case "Seleccionar":
                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString("delivery_id", delivery_id);
                                editor.putString("delivery_user_id", delivery_user_id);
                                editor.putString("delivery_lat", delivery_lat);
                                editor.putString("delivery_lon", delivery_lon);
                                editor.putString("delivery_address", delivery_address);
                                editor.putString("delivery_alias", delivery_alias);
                                editor.putString("delivery_phone", delivery_phone);
                                editor.putString("delivery_note", delivery_note);
                                editor.putString("delivery_department_number", delivery_department_number);
                                editor.apply();
                                Intent intent0 = new Intent(DeliveryAddress.this, MainActivity.class);
                                startActivity(intent0);
                                finish();
                                break;
                            case "Editar":
                                Intent intent = new Intent(DeliveryAddress.this, SelectLocationActivity.class);
                                intent.putExtra("delivery_id", delivery_id);
                                intent.putExtra("delivery_user_id", delivery_userId);
                                intent.putExtra("delivery_lat", delivery_lat);
                                intent.putExtra("delivery_lon", delivery_lon);
                                intent.putExtra("delivery_address", delivery_address);
                                intent.putExtra("delivery_alias", delivery_alias);
                                intent.putExtra("delivery_phone", delivery_phone);
                                intent.putExtra("delivery_note", delivery_note);
                                intent.putExtra("delivery_department_number", delivery_department_number);
                                startActivity(intent);
                                break;
                            case "Eliminar":
                                positionList.remove(position);
                                new DeleteDataAsyncTask().execute();
                                break;
                            default:
                                break;
                        }
                    }
                });
                builderSingle.show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void displayFirebaseRegId() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", "null");
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }

    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DeliveryAddress.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Procesando...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            String  hp = getString(R.string.link) + getString(R.string.servicepath) + "getdelivery_address.php?";
            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.e("PlaceOrder", "ResponseFromServer " + response);
                    positionList = new ArrayList<>();
                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            positionGetSet temp = new positionGetSet();
                            JSONObject Obj = jsonArray.getJSONObject(i);
                            temp.setId(Obj.getString("id"));
                            temp.setUserId(Obj.getString("user_id"));
                            temp.setLat(Obj.getString("latitude"));
                            temp.setLon(Obj.getString("longitude"));
                            temp.setAddress(Obj.getString("address"));
                            temp.setAlias(Obj.getString("alias"));
                            temp.setPhone(Obj.getString("phone"));
                            temp.setDeliveryNote(Obj.getString("delivery_note"));
                            temp.setDepartmentNumber(Obj.getString("department_number"));

                            positionList.add(temp);
                            Error = null;
                        } try {
                            progressDialog.cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                            progressDialog.cancel();
                        }
                        if (Error == null) {
                            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            recyclerView.setLayoutManager(mLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());

                            adapter = new positionadapter(recyclerView, DeliveryAddress.this, positionList);
                            recyclerView.setAdapter(adapter);
                        }


                    } catch (JSONException e) {
                        progressDialog.cancel();
                        e.printStackTrace();
                    }
                }
            },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        progressDialog.cancel();
                        Log.d("Error.Response", error.toString());
                    }

                }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", delivery_userId);
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
            RequestQueue requestQueue = Volley.newRequestQueue(DeliveryAddress.this);
            requestQueue.add(postRequest);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }

    class DeleteDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DeliveryAddress.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Procesando...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            String  hp = getString(R.string.link) + getString(R.string.servicepath) + "deletedelivery_address.php?";
            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.e("DeleteDataAsyncTask", "ResponseFromServer " + response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject Obj = jsonArray.getJSONObject(0);

                        String status = Obj.getString("status");
                        if (status.equals("Success")) {
                            if (delivery_id.equals(deliveryData.delivery_id)) {
                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString("delivery_id", "");
                                editor.putString("delivery_user_id", "");
                                editor.putString("delivery_lat", "");
                                editor.putString("delivery_lon", "");
                                editor.putString("delivery_address", "");
                                editor.putString("delivery_alias", "");
                                editor.putString("delivery_phone", "");
                                editor.putString("delivery_note", "");
                                editor.putString("delivery_department_number", "");
                                editor.apply();
                                deliveryData.delivery_id = "";
                            }
                            adapter = new positionadapter(recyclerView, DeliveryAddress.this, positionList);
                            recyclerView.setAdapter(adapter);
                        }

                    } catch (JSONException e) {
                        progressDialog.cancel();
                        e.printStackTrace();
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            progressDialog.cancel();
                            Log.d("Error.Response", error.toString());
                        }

                    }
            ) {

                @Override
                protected Map<String, String> getParams()
                {
                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                    Map<String, String> params = new HashMap<>();
                    params.put("id", delivery_id);
                    params.put("user_id", delivery_user_id);
                    params.put("latitude", delivery_lat);
                    params.put("longitude", delivery_lon);
                    params.put("address", delivery_address);
                    params.put("alias", delivery_alias);
                    params.put("phone", delivery_phone);
                    params.put("delivery_note", delivery_note);
                    params.put("department_number", delivery_department_number);
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
            RequestQueue requestQueue = Volley.newRequestQueue(DeliveryAddress.this);
            requestQueue.add(postRequest);
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                progressDialog.cancel();
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.cancel();
            }
        }

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, permission_location, PERMISSION_REQUEST_CODE);
        ActivityCompat.requestPermissions(this, permission_location1, PERMISSION_REQUEST_CODE1);
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

                Toast.makeText(DeliveryAddress.this, "GPS ya está activado",
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
                        resolvable.startResolutionForResult(DeliveryAddress.this,
                                Splash.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }
}