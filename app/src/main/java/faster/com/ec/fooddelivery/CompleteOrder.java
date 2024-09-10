package faster.com.ec.fooddelivery;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;
import static faster.com.ec.fooddelivery.MainActivity.tf_opensense_regular;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import faster.com.ec.Getset.orderTimelineGetSet;
import faster.com.ec.R;
import faster.com.ec.timeline.OrderStatus;
import faster.com.ec.timeline.TimeLineAdapter;
import faster.com.ec.timeline.TimeLineModel;
import faster.com.ec.tracking.collection.MarkerCollection;
import faster.com.ec.tracking.helpers.FirebaseEventListenerHelper;
import faster.com.ec.tracking.helpers.GoogleMapHelper;
import faster.com.ec.tracking.helpers.MarkerAnimationHelper;
import faster.com.ec.tracking.helpers.UiHelper;
import faster.com.ec.tracking.interfaces.FirebaseDriverListener;
import faster.com.ec.tracking.interfaces.LatLngInterpolator;
import faster.com.ec.tracking.model.Driver;


public class CompleteOrder extends AppCompatActivity implements FirebaseDriverListener {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2161;
    private static final String ONLINE_DRIVERS = "online_drivers";

    private final GoogleMapHelper googleMapHelper = new GoogleMapHelper();
    private DatabaseReference databaseReference;
    private GoogleMap googleMap;
    private LocationRequest locationRequest;
    private UiHelper uiHelper;
    private FirebaseEventListenerHelper firebaseEventListenerHelper;
    private FusedLocationProviderClient locationProviderClient;

    private Marker currentLocationMarker;

    private TextView totalOnlineDrivers;

    private boolean locationFlag = true;

    private RecyclerView mRecyclerView;
    private final List<TimeLineModel> mDataList = new ArrayList<>();
    private String orderId;
    private String statusOrder;
    private String[] ordertxt;
    private String[] rejecttxt;
    private TextView txt_name;
    private TextView txt_contact;
    private TextView txt_address;
    private TextView txt_order_amount;
    private TextView txt_order_EstimatedTime;
    private TextView txt_order_time;
    private TextView txt_delivery_price;
    private TextView txt_total_price;
    private TextView txt_order_timer;
    private TextView txtestado;
    private TextView txt_order_title;
    private TextView txt_descripcion;
    private TextView txt_name2;
    private ImageView rest_image;
    private Button btn_call;
    private Button btn_call_rider;
    private ImageView btn_call_icono;
    static CountDownTimer countDownTimer = null;
    private RelativeLayout rel_bottom;
    private ProgressDialog progressDialog;
    Button txt_orderno;
    private ArrayList<orderTimelineGetSet> orderGetSet;
    private orderTimelineGetSet oData;
    private String key;
    String user_id;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static CompleteOrder instance;
    private String status1;
    private int counter;
    private String telefono;
    private String telefonorider;
    private static Double lonStart = null;
    private static Double latStart = null;
    private static Double orderLat = null;
    private static Double orderLon = null;
    private static Boolean markerRestaurant = false;
    private static Boolean markerCliente = false;

    private SlidingUpPanelLayout sliding_layout2;
    private ImageButton btn_menu;
    private String ruta = "";

//    public static MapRipple mapRipple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.supportMap);
        uiHelper = new UiHelper(this);
        //assert mapFragment != null;
        //mapFragment.getMapAsync(googleMap -> this.googleMap = googleMap);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;

            }
        });
        changeStatsBarColor(CompleteOrder.this);
        gettingIntents();
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        totalOnlineDrivers = findViewById(R.id.totalOnlineDrivers);
        firebaseEventListenerHelper = new FirebaseEventListenerHelper(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child(ONLINE_DRIVERS);
        /*Query query = databaseReference.orderByChild("driverId").equalTo(orderId);
        query.addChildEventListener(firebaseEventListenerHelper);*/
        ordertxt = new String[]{getString(R.string.timelineo1), getString(R.string.timelineo2), getString(R.string.timelineo3), getString(R.string.timelineo4), getString(R.string.timelineo5)};
        rejecttxt = new String[]{getString(R.string.timeline6), getString(R.string.timeline7), getString(R.string.timeline8)};
        initView();
        instance = this;

        //   if (key.equals("orderplace")) {
        //     sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        // }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ejemplo1", "11");
        /*if (mapRipple != null && mapRipple.isAnimationRunning()) {
            mapRipple.stopRippleMapAnimation();
        }*/
        new getData().execute();

    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (mapRipple != null && mapRipple.isAnimationRunning()) {
            mapRipple.stopRippleMapAnimation();
        }*/
    }

    public static CompleteOrder getInstance() {
        return instance;
    }

    private void gettingIntents() {

        SharedPreferences prefs1 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        user_id = prefs1.getString("userid", null);
        status1 = prefs1.getString("status_order", null);
        orderId = prefs1.getString("orderId", null);
        key = prefs1.getString("key", null);

        Log.e("Order status tag","order status: " + key);

    }

    private void initView() {

        //settine time line adapter for order status
        mRecyclerView = findViewById(R.id.time_line);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.getRecycledViewPool().clear();


        Objects.requireNonNull(getSupportActionBar()).hide();

        //initialization

        txt_orderno = findViewById(R.id.txt_orderno);
        //txt_orderno.setTypeface(tf_opensense_regular);
        txt_orderno.setText("Ver Orden # " + orderId);

        rest_image = findViewById(R.id.image);

        txt_name = findViewById(R.id.txt_name);
        //txt_name.setTypeface(tf_opensense_regular);

        btn_call = findViewById(R.id.btn_call);
        btn_call_rider = findViewById(R.id.btn_call_rider);
        btn_call_icono = findViewById(R.id.btn_call_icono);

        txt_delivery_price = findViewById(R.id.txt_delivery_price);
        txt_delivery_price.setTypeface(tf_opensense_regular);

        txt_total_price = findViewById(R.id.txt_total_price);
        txt_total_price.setTypeface(tf_opensense_regular);

        txt_order_title = findViewById(R.id.txt_order_title);
        txt_order_title.setTypeface(tf_opensense_regular);

        txt_descripcion = findViewById(R.id.txt_descripcion);
        txt_descripcion.setTypeface(tf_opensense_regular);

        //TextView txt_order = findViewById(R.id.txt_order);
        //txt_order.setTypeface(tf_opensense_regular);
        // txt_order.setText(orderId);

        txtestado = findViewById(R.id.txtestado);
        txt_name2 = findViewById(R.id.txt_name2);

        TextView txt_order_amount_title = findViewById(R.id.txt_order_amount_title);
        txt_order_amount_title.setTypeface(tf_opensense_regular);

        txt_order_amount = findViewById(R.id.txt_order_amount);
        txt_order_amount.setTypeface(tf_opensense_regular);

        TextView txt_order_time_title = findViewById(R.id.txt_order_time_title);
        txt_order_time_title.setTypeface(tf_opensense_regular);

        txt_order_time = findViewById(R.id.txt_order_time);
        txt_order_time.setTypeface(tf_opensense_regular);

        txt_order_timer = findViewById(R.id.txt_order_timer);
        txt_order_timer.setTypeface(tf_opensense_regular);

        rel_bottom = findViewById(R.id.rel_bottom);
        //on click listeners

        sliding_layout2 = findViewById(R.id.sliding_layout2);
        btn_menu = findViewById(R.id.btn_menu);

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sliding_layout2.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED && !("orderplace".equals(key))) {
                    sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    if ("orderplace".equals(key)) {
                        Intent i = new Intent(CompleteOrder.this, MyOrderPage.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        startActivity(i);
                    } else {
                        onBackPressed();
                    }
                }

            }
        });

        RelativeLayout rel_order = findViewById(R.id.rel_order);
        rel_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CompleteOrder.this, OrderSpecification.class);
                i.putExtra("OrderId", orderId);
                startActivity(i);
            }
        });

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + telefono;
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse(uri));
                startActivity(i);
            }
        });

        btn_call_rider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri;
                uri = "tel:" + telefonorider;
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse(uri));
                startActivity(i);
            }
        });

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sliding_layout2.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }

            }
        });
        rest_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(CompleteOrder.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_custom_layout, null);
                PhotoView photoView = mView.findViewById(R.id.imageView77);
                Picasso.get().load(ruta)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .into(photoView);
                mBuilder.setView(mView);
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();

            }
        });

    }


    private void updateTimeline() {

        if (Objects.equals(orderGetSet.get(0).getStatus(), "Activate")) {
            mDataList.add(new TimeLineModel(ordertxt[1], orderGetSet.get(0).getOrder_date_time(), OrderStatus.ACTIVE));

            if (Objects.equals(orderGetSet.get(1).getStatus(), "Activate")) {
                mDataList.add(new TimeLineModel(ordertxt[2], orderGetSet.get(1).getOrder_date_time(), OrderStatus.ACTIVE));

                if (Objects.equals(orderGetSet.get(2).getStatus(), "Activate")) {
                    mDataList.add(new TimeLineModel(ordertxt[3], orderGetSet.get(2).getOrder_date_time(), OrderStatus.ACTIVE));

                    if (Objects.equals(orderGetSet.get(3).getStatus(), "Activate")) {
                        mDataList.add(new TimeLineModel(ordertxt[4], orderGetSet.get(3).getOrder_date_time(), OrderStatus.ACTIVE));
                    } else {
                        mDataList.add(new TimeLineModel(ordertxt[4], "", OrderStatus.INACTIVE));
                    }
                } else {
                    mDataList.add(new TimeLineModel(ordertxt[3], "", OrderStatus.INACTIVE));
                    mDataList.add(new TimeLineModel(ordertxt[4], "", OrderStatus.INACTIVE));
                }
            } else {
                mDataList.add(new TimeLineModel(ordertxt[2], "", OrderStatus.INACTIVE));
                mDataList.add(new TimeLineModel(ordertxt[3], "", OrderStatus.INACTIVE));
                mDataList.add(new TimeLineModel(ordertxt[4], "", OrderStatus.INACTIVE));
            }
        } else {
            mDataList.add(new TimeLineModel(ordertxt[1], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[2], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[3], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[4], "", OrderStatus.INACTIVE));
        }
        boolean mWithLinePadding = false;
        TimeLineAdapter mTimeLineAdapter = new TimeLineAdapter(mDataList, mWithLinePadding);
        mRecyclerView.setAdapter(mTimeLineAdapter);
        mRecyclerView.getRecycledViewPool().clear();

    }

    private void updateRejectTimeLine(String val) {
        if (Objects.equals(orderGetSet.get(0).getStatus(), "Activate")) {
            Log.e("ejemplo", "reject ");
            mDataList.add(new TimeLineModel(ordertxt[1], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[2], "", OrderStatus.INACTIVE));
            mDataList.add(new TimeLineModel(ordertxt[3], "", OrderStatus.INACTIVE));
            if (val.equals("reject")) {
                mDataList.add(new TimeLineModel(rejecttxt[0], orderGetSet.get(0).getOrder_date_time(), OrderStatus.COMPLETED));
            } else if (val.equals("autorest")) {
                mDataList.add(new TimeLineModel(rejecttxt[1], orderGetSet.get(0).getOrder_date_time(), OrderStatus.COMPLETED));
            } else if (val.equals("autodeli")) {
                mDataList.add(new TimeLineModel(rejecttxt[2], orderGetSet.get(0).getOrder_date_time(), OrderStatus.COMPLETED));
            }

        }
        boolean mWithLinePadding = false;
        TimeLineAdapter mTimeLineAdapter = new TimeLineAdapter(mDataList, mWithLinePadding);
        mRecyclerView.setAdapter(mTimeLineAdapter);
        mRecyclerView.getRecycledViewPool().clear();
    }

    public void getDataprogress() {
        new getData().execute();
    }

    class getData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            mDataList.clear();
            /*if (mapRipple != null && mapRipple.isAnimationRunning()) {
                mapRipple.stopRippleMapAnimation();
            }*/
            //creating a string request to send request to the url
            String hp = getString(R.string.link) + getString(R.string.servicepath) + "order_details.php?order_id=" + orderId + "&user_id=" + user_id;
            // Log.d("CheckUrl", "" + getString(R.string.link) + getString(R.string.servicepath) + "order_details.php?order_id=" + orderId + "&user_id=" + user_id);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, hp,
                    response -> {
                        try {
                            //getting the whole json object from the response
                            JSONObject obj = new JSONObject(response);

                            String status = obj.getString("success");
                            statusOrder = obj.getString("success");
                            if (Objects.equals(status, "1")) {
                                orderGetSet = new ArrayList<>();

                                JSONArray ja_orderDetail = obj.getJSONArray("order_details");
                                Log.e("Response", obj.toString());
                                JSONObject jo_data = ja_orderDetail.getJSONObject(0);
                                String txt_restName = jo_data.getString("restaurant_name");
                                String estado = jo_data.getString("status");
                                if (!markerCliente) {
                                    markerCliente = true;
                                }
                                if (!markerRestaurant) {
                                    markerRestaurant = true;
                                }

                                latStart = Double.valueOf(jo_data.getString("restaurant_lat"));
                                lonStart = Double.valueOf(jo_data.getString("restaurant_lon"));
                                orderLat = Double.valueOf(jo_data.getString("order_lat"));
                                orderLon = Double.valueOf(jo_data.getString("order_lon"));
                                Marker mPerth = googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latStart, lonStart))
                                        .title(txt_restName)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.res_icon)
                                        )
                                );
                                mPerth.setTag("restaurant_" + jo_data.getString("order_id"));

                                Marker mPerth2 = googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(orderLat, orderLon))
                                        .title("Punto de Entrega")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.cli_icon)));
                                mPerth2.setTag("order_" + jo_data.getString("order_id"));

                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(mPerth2.getPosition());
                                builder.include(mPerth.getPosition());

                                LatLngBounds bounds = builder.build();
                                int padding = 30; // offset from edges of the map in pixels
                                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                    @Override
                                    public void onMapLoaded() {

                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                                        LatLng center_map = googleMap.getCameraPosition().target;
                                        if (estado.equals("0")) {
                                            Marker markerDeliveryBoy = null;
                                            markerDeliveryBoy = MarkerCollection.getMarker(orderId+"_deliveryboy");
                                            if (markerDeliveryBoy != null) {
                                                //markerDeliveryBoy.remove();
                                            }else{
                                                // MarkerOptions markerOptions = googleMapHelper.getDriverMarkerOptions(new LatLng(driver.getLat(), driver.getLng()));
                                                //  Marker marker = googleMap.addMarker(markerOptions);
                                                //  marker.setTag(driver.getDriverId());
                                            }
                                            /*
                                            if (mapRipple == null) {
                                                mapRipple = new MapRipple(googleMap, center_map, CompleteOrder.this);
                                                mapRipple.withDistance(500);
                                                mapRipple.withRippleDuration(1000);
                                                mapRipple.withTransparency(0.5f);
                                                mapRipple.withStrokeColor(Color.CYAN);

                                            }
                                            if (!mapRipple.isAnimationRunning()) {
                                                mapRipple.startRippleMapAnimation();
                                            }*/

                                        } else if ((estado.equals("4") || estado.equals("5") || estado.equals("1") || estado.equals("3"))) {
                                            if (estado.equals("5")) {
                                               /* if (mapRipple == null) {
                                                    mapRipple = new MapRipple(googleMap, center_map, CompleteOrder.this);
                                                    mapRipple.withDistance(500);
                                                    mapRipple.withRippleDuration(1000);
                                                    mapRipple.withTransparency(0.5f);
                                                    mapRipple.withStrokeColor(Color.CYAN);

                                                }
                                                if (!mapRipple.isAnimationRunning()) {
                                                    mapRipple.startRippleMapAnimation();
                                                }*/
                                            } else if (estado.equals("4")) {
                                                /*if (mapRipple != null && mapRipple.isAnimationRunning()) {
                                                    mapRipple.stopRippleMapAnimation();
                                                }*/
                                                totalOnlineDrivers.setText("Pedido entregado con éxito");

                                                Marker marker1 = null;
                                                marker1 = MarkerCollection.getMarker(orderId);
                                                if (marker1 != null) {
                                                    marker1.remove();
                                                }
                                                MarkerCollection.clearMarkers();
                                                currentLocationMarker = null;
                                                mPerth2.showInfoWindow();
                                                animateCamera(new LatLng(orderLat, orderLon));
                                            } else {
                                                mPerth.showInfoWindow();
                                                animateCamera(new LatLng(latStart, lonStart));
                                            }
                                        } else {
                                          /*  if (mapRipple != null && mapRipple.isAnimationRunning()) {
                                                mapRipple.stopRippleMapAnimation();
                                            }*/
                                            mPerth.showInfoWindow();
                                            animateCamera(new LatLng(latStart, lonStart));
                                        }
                                    }
                                });

                                String txt_restaurant_address = jo_data.getString("restaurant_address");
                                String txt_restaurant_contact = jo_data.getString("restaurant_contact");
                                String txt_order_amount = jo_data.getString("order_amount");
                                String txt_order_time = jo_data.getString("order_time");
                                String txt_delivery_time = jo_data.getString("delivery_time");
                                String txt_order_id = jo_data.getString("order_id");
                                String txt_delivery_price2 = jo_data.getString("delivery_price");
                                String txt_total_price2 = jo_data.getString("total_price");
                                String restaurant_image = jo_data.getString("restaurant_image");
                                //String status = jo_data.getString("status");

                                String time_rest = jo_data.getString("time_rest");

                                String deliveryboy_name = jo_data.getString("deliveryboy_name");
                                String ally_name = jo_data.getString("ally_name");
                                String deliveryboy_phone = jo_data.getString("deliveryboy_phone");
                                String deliveryboy_image = jo_data.getString("deliveryboy_image");
                                String deliveryboy_vehicle_no = jo_data.getString("deliveryboy_vehicle_no");
                                String deliveryboy_vehicle_type = jo_data.getString("deliveryboy_vehicle_type");
                                String ratting = jo_data.getString("ratting");
                                Log.d("ejemplo2", time_rest);

                                status1 = jo_data.getString("order_status");

                                orderId = jo_data.getString("order_id");
                                txt_orderno.setText("Ver Orden # " + orderId);
                                if ("inactive".equals(status1)) {
                                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                    editor.putString("orderId", null);
                                    editor.apply();
                                }

                                // Log.e("Response1", jo_data.getString("reject_date_time") + " " + jo_data.getString("reject_auto_restaurant_date_time") + " " + jo_data.getString("reject_auto_deliveryboy_date_time"));

                                updateUI(ratting, estado, time_rest, deliveryboy_name, ally_name, deliveryboy_phone, deliveryboy_image, deliveryboy_vehicle_no, deliveryboy_vehicle_type, txt_restName, txt_restaurant_address, txt_restaurant_contact, restaurant_image, txt_order_amount, txt_order_time, txt_delivery_time, txt_order_id, txt_delivery_price2, txt_total_price2, status1);
                                if (jo_data.getString("reject_date_time").equals("null") && jo_data.getString("reject_auto_restaurant_date_time").equals("null") && jo_data.getString("reject_auto_deliveryboy_date_time").equals("null")) {
                                    Log.e("ejemplo", "no reject");
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("assign_date_time"));
                                    oData.setStatus(jo_data.getString("assign_status"));
                                    orderGetSet.add(oData);
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("order_verified_date"));
                                    oData.setStatus(jo_data.getString("order_verified"));
                                    orderGetSet.add(oData);
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("delivery_date_time"));
                                    oData.setStatus(jo_data.getString("delivery_status"));
                                    orderGetSet.add(oData);
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("delivered_date_time"));
                                    oData.setStatus(jo_data.getString("delivered_status"));
                                    orderGetSet.add(oData);
                                    updateTimeline();
                                } else if (!jo_data.getString("reject_date_time").equals("null")) {
                                    Log.e("ejemplo", "reject");
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("reject_date_time"));
                                    oData.setStatus(jo_data.getString("reject_status"));
                                    orderGetSet.add(oData);
                                    updateRejectTimeLine("reject");
                                } else if (!jo_data.getString("reject_auto_restaurant_date_time").equals("null")) {
                                    Log.e("ejemplo", "reject auto res");
                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("reject_auto_restaurant_date_time"));
                                    oData.setStatus(jo_data.getString("reject_auto_restaurant_status"));
                                    orderGetSet.add(oData);
                                    updateRejectTimeLine("autorest");
                                } else if (!jo_data.getString("reject_auto_deliveryboy_date_time").equals("null")) {

                                    oData = new orderTimelineGetSet();
                                    oData.setOrder_date_time(jo_data.getString("reject_auto_deliveryboy_date_time"));
                                    oData.setStatus(jo_data.getString("reject_auto_deliveryboy_status"));
                                    orderGetSet.add(oData);
                                    updateRejectTimeLine("autodeli");
                                }

                            } /*else{
                                if (mapRipple != null && mapRipple.isAnimationRunning()) {
                                    mapRipple.stopRippleMapAnimation();
                                }
                            }*/
                        } catch (JSONException e) {
                            /*if (mapRipple != null && mapRipple.isAnimationRunning()) {
                                mapRipple.stopRippleMapAnimation();
                            }*/
                            e.printStackTrace();
                        }
                    },
                    error -> {
                       /* if (mapRipple != null && mapRipple.isAnimationRunning()) {
                            mapRipple.stopRippleMapAnimation();
                        }*/
                        Log.d("Error.Response", error.toString());
                        String message = null;
                        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(CompleteOrder.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage("Por favor verifica tu conexión a Internet");
                            builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    new getData().execute();
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
                                Log.e("Error_alert11", Objects.requireNonNull(e.getMessage()));
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                        }
                    });

            //creating a request queue
            RequestQueue requestQueue = Volley.newRequestQueue(CompleteOrder.this);

            //adding the string request to request queue
            requestQueue.add(stringRequest);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }

    private void updateUI(String ratting, String estado, String time_rest, String deliveryboy_name, String ally_name, String deliveryboy_phone, String deliveryboy_image, String deliveryboy_vehicle_no, String deliveryboy_vehicle_type, String txt_restName, String txt_restaurant_address, String txt_restaurant_contact, String restaurant_image, String txt_order_amo, String txt_order_tim, String txt_delivery_time, String txt_order_id, String txt_delivery_price1, String txt_total_price1, String order_status) {
        txt_order_amount.setText(getString(R.string.currency) + " " + txt_order_amo.replace(".", ","));
        txt_delivery_price.setText(getString(R.string.currency) + " " + txt_delivery_price1.replace(".", ","));
        txt_total_price.setText(getString(R.string.currency) + " " + txt_total_price1.replace(".", ","));
        txt_order_time.setText(txt_order_tim);

        if (estado.equals("3")) {
            btn_call_rider.setText("Llamar al Repartidor");
            btn_call_icono.setVisibility(View.VISIBLE);
            btn_call_rider.setEnabled(true);
            telefonorider = deliveryboy_phone;
            //  txt_name2.setText("Repartidor " + ally_name);
            txt_name.setText(deliveryboy_name);
            //  txt_address.setText("Descripción: " + deliveryboy_vehicle_type);
            txt_descripcion.setText(deliveryboy_vehicle_type + "\nPlaca: " + deliveryboy_vehicle_no);
            //   txt_contact.setText("Placa: " + deliveryboy_vehicle_no);
            txtestado.setText("El repartidor llegará pronto");
            if (Integer.parseInt(time_rest) != 0) {
                reverseTimer(Integer.parseInt(time_rest), totalOnlineDrivers, "El repartidor llegará pronto");
            } else {
                totalOnlineDrivers.setText(Html.fromHtml("¡El repartidor <b>llegará</b> pronto!"));
            }
            btn_call.setText("Llamar a la Tienda");
            telefono = txt_restaurant_contact;

            ruta = getResources().getString(R.string.link) + "uploads/deliveryboys/" + deliveryboy_image;

            Query query = databaseReference.orderByChild("driverId").equalTo(orderId);
            query.addChildEventListener(firebaseEventListenerHelper);
            Log.v("borja1","orderId="+orderId);
        } else {
            btn_call.setText("Llamar a la Tienda");
            telefono = txt_restaurant_contact;
            // txt_name2.setText("Restaurante");
            txt_name.setText(txt_restName);
            txt_descripcion.setText(txt_restaurant_address);
            //   txt_address.setText(txt_restaurant_address);
            ruta = getResources().getString(R.string.link) + getString(R.string.imagepath) + restaurant_image;

            if (estado.equals("0")) {
                reverseTimer(Integer.parseInt(time_rest), totalOnlineDrivers, "¡Estamos buscando el repartidor perfecto!");
                txtestado.setText("Pedido Enviado");
                btn_call_rider.setText("Asignando un Faster");
                btn_call_rider.setEnabled(false);
                btn_call_icono.setVisibility(View.GONE);

            } else if (estado.equals("5")) {
                btn_call.setText("Llamar a la Tienda");
                telefono = txt_restaurant_contact;

                btn_call_rider.setText("Llamar al Repartidor");
                btn_call_icono.setVisibility(View.VISIBLE);
                btn_call_rider.setEnabled(true);
                telefonorider = deliveryboy_phone;
                if (Integer.parseInt(time_rest) != 0) {
                    reverseTimer(Integer.parseInt(time_rest), totalOnlineDrivers, "Esperando a la tienda");
                } else {
                    totalOnlineDrivers.setText(Html.fromHtml("¡El repartidor <b>llegará</b> pronto!"));
                }
                txtestado.setText("Esperando a la tienda");

                Query query = databaseReference.orderByChild("driverId").equalTo(orderId);
                query.addChildEventListener(firebaseEventListenerHelper);
                Log.v("borja2","orderId="+orderId);
            } else if (estado.equals("1")) {
                btn_call_rider.setText("Llamar al Repartidor");
                btn_call_icono.setVisibility(View.VISIBLE);
                btn_call_rider.setEnabled(true);
                telefonorider = deliveryboy_phone;

                txtestado.setText("Pedido en proceso");
                if (Integer.parseInt(time_rest) != 0) {
                    reverseTimer(Integer.parseInt(time_rest), totalOnlineDrivers, "Repartidor en camino a recoger la orden");
                } else {
                    totalOnlineDrivers.setText(Html.fromHtml("¡La tienda está <b>preparando</b> tu orden!"));
                }

                btn_call.setText("Llamar a la Tienda");
                telefono = txt_restaurant_contact;

                Query query = databaseReference.orderByChild("driverId").equalTo(orderId);
                query.addChildEventListener(firebaseEventListenerHelper);
                Log.v("borja3","orderId="+orderId);
            } else if (estado.equals("6") || estado.equals("7")) {
                sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                if (estado.equals("6")) {
                    totalOnlineDrivers.setText("Repartidores ocupados, intente nuevamente");

                } else if (estado.equals("7")) {
                    totalOnlineDrivers.setText("Tienda ocupada, intente nuevamente");
                }


                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                txt_order_title.setText("");
                txt_order_timer.setText("");

            } else if (estado.equals("2")) {
                totalOnlineDrivers.setText("La tienda ha rechazado tu pedido");
                sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                btn_call_rider.setText("Llamar al Repartidor");
                btn_call_icono.setVisibility(View.VISIBLE);
                btn_call_rider.setEnabled(true);
                telefonorider = deliveryboy_phone;

                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                txt_order_title.setText("");
                txt_order_timer.setText("");

                btn_call.setText("Llamar a la Tienda");
                telefono = txt_restaurant_contact;
            } else if (estado.equals("4")) {
                txtestado.setText("Pedido entregado con éxito");
                sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                btn_call_rider.setText("Llamar al Repartidor");
                btn_call_icono.setVisibility(View.VISIBLE);
                btn_call_rider.setEnabled(true);
                telefonorider = deliveryboy_phone;
                totalOnlineDrivers.setText("Pedido entregado con éxito");
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                txt_order_title.setText("");
                txt_order_timer.setText("");

                if (ratting.equals("0")) {
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("deliveryboy_name", deliveryboy_name);
                    editor.putString("deliveryboy_vehicle_type", deliveryboy_vehicle_type);
                    editor.putString("deliveryboy_vehicle_no", deliveryboy_vehicle_no);
                    editor.putString("deliveryboy_phone", deliveryboy_phone);
                    editor.putString("order_price", txt_order_amo);
                    editor.putString("delivery_price", txt_delivery_price1);
                    editor.putString("total", txt_total_price1);
                    editor.putString("image", deliveryboy_image);
                    editor.putString("orderId", txt_order_id);
                    editor.putString("ally_name", ally_name);
                    editor.apply();

                    Intent i = new Intent(CompleteOrder.this, RatingDelivery.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }

            }

        }
        Picasso.get().load(ruta)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .resize(640, 360)
                .into(rest_image);

        mDataList.add(new TimeLineModel(ordertxt[0], txt_order_tim, OrderStatus.ACTIVE));
    }


    public void reverseTimer(int Seconds, final TextView tv, String estado) {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(Seconds * 1000 + 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                tv.setText(estado + " " + String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));
            }

            public void onFinish() {
                tv.setText(estado);

            }
        }.start();
    }

    @Override
    public void onBackPressed() {

        if ("inactive".equals(status1)) {
            if (sliding_layout2.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED && !("orderplace".equals(key))) {
                sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            } else {
                Intent i;
                if ("orderplace".equals(key)) {
                    i = new Intent(CompleteOrder.this, MyOrderPage.class);
                    i.putExtra("key", "main");
                } else {
                    //i = new Intent(CompleteOrder.this, MenuActivity.class);
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(CompleteOrder.this, R.style.MyDialogTheme);
                    builder1.setTitle("Pedido en proceso");
                    builder1.setMessage("Al salir se te notificará cada estado del pedido. ¿Deseas salir de la aplicación?");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finishAffinity();
                        }
                    });
                    builder1.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert11 = builder1.create();
                    try {
                        alert11.show();
                    } catch(Exception e) {
                        Log.e("Error_alert11", Objects.requireNonNull(e.getMessage()));
                    }
                }
                //i.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                //startActivity(i);
            }

        } else {
            if (sliding_layout2.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                sliding_layout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            } else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CompleteOrder.this, R.style.MyDialogTheme);
                builder1.setTitle("Pedido en proceso");
                builder1.setMessage("Al salir se te notificará cada estado del pedido. ¿Deseas salir de la aplicación?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();
                    }
                });
                builder1.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert11 = builder1.create();
                try {
                    alert11.show();
                } catch(Exception e) {
                    Log.e("Error_alert11", Objects.requireNonNull(e.getMessage()));
                }
            }
        }
    }


    private void animateCamera(LatLng latLng) {

        // CameraUpdate cameraUpdate = googleMapHelper.buildCameraUpdate(latLng);
        // googleMap.animateCamera(cameraUpdate);
    }

    private void showOrAnimateUserMarker(LatLng latLng) {
        if (currentLocationMarker == null)
            currentLocationMarker = googleMap.addMarker(googleMapHelper.getCurrentLocationMarker(latLng));
        else
            MarkerAnimationHelper.animateMarkerToGB(
                    currentLocationMarker,
                    new LatLng(latLng.latitude,
                            latLng.longitude),
                    new LatLngInterpolator.Spherical());
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        if (!uiHelper.isHaveLocationPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        if (uiHelper.isLocationProviderEnabled())
            uiHelper.showPositiveDialogWithListener(this, getResources().getString(R.string.need_location), getResources().getString(R.string.location_content), () -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)), "Turn On", false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            int value = grantResults[0];
            if (value == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            } else if (value == PackageManager.PERMISSION_GRANTED) requestLocationUpdates();
        }
    }

    @Override
    public void onDriverAdded(Driver driver) {

        if (driver != null && driver.getDriverId().equals(orderId)) {
            Marker marker1 = null;
            marker1 = MarkerCollection.getMarker(driver.getDriverId());
            if (marker1 != null) {
                marker1.remove();
            }

            MarkerCollection.clearMarkers();
            currentLocationMarker = null;
            MarkerOptions markerOptions = googleMapHelper.getDriverMarkerOptions(new LatLng(driver.getLat(), driver.getLng()));
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(driver.getDriverId());
            MarkerCollection.insertMarker(marker);
        }

    }

    @Override
    public void onDriverRemoved(Driver driver) {
        if (driver != null && driver.getDriverId() == orderId) {
            MarkerCollection.removeMarker(driver.getDriverId());
        }
    }

    @Override
    public void onDriverUpdated(Driver driver) {
        Log.i("marcadores", MarkerCollection.allMarkers().size() + "");

        //MarkerCollection.clearMarkers();
        Marker marker = null;
        marker = MarkerCollection.getMarker(driver.getDriverId());
        if (lonStart != null && latStart != null) {
            try {
                //assert marker != null;
                marker.setPosition(new LatLng(latStart, lonStart));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (driver == null) return;

        if (latStart == null || lonStart == null) {
            latStart = driver.getLat();
            lonStart = driver.getLng();
        }
        LatLng oldLocation, newLocaation;
        oldLocation = new LatLng(latStart, lonStart);
        newLocaation = new LatLng(driver.getLat(), driver.getLng());
        float bearing = (float) bearingBetweenLocations(oldLocation, newLocaation);
        MarkerAnimationHelper.rotateMarker(marker, bearing);


        LatLng latLng = new LatLng(driver.getLat(), driver.getLng());
        latStart = driver.getLat();
        lonStart = driver.getLng();
        animateCamera(latLng);

        // showOrAnimateUserMarker(latLng);
    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(firebaseEventListenerHelper);
        MarkerCollection.clearMarkers();
        currentLocationMarker = null;
    }


}


