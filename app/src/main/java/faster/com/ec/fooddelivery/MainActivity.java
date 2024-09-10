package faster.com.ec.fooddelivery;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import faster.com.ec.Adapter.restaurentadapter;
import faster.com.ec.Getset.restaurentGetSet;
import faster.com.ec.R;
import faster.com.ec.deliveryData;
import faster.com.ec.helper.SessionManager;
import faster.com.ec.utils.Config;
import faster.com.ec.utils.GPSTracker;
import faster.com.ec.utils.RecyclerTouchListener;

public class MainActivity extends AppCompatActivity {
    private String DB_PATH = Environment.getDataDirectory() + "/Bhagirath/databases/";
    private static final String DB_NAME = "restaurant.sqlite";
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static ArrayList<restaurentGetSet> restaurentlist;
    private static String status_promo;
    private String Error;
    private RecyclerView recyclerView;
    private String timezoneID;
    private String search = "";
    private String res_name;
    private ProgressDialog progressDialog;
    private ProgressDialog pd;
    private TextView txt_nameuser;
    private TextView txt_profile;
    private RelativeLayout rel_main;
    private String Location;

    private ImageView img_profile;
    private String CategoryTotal = "", regId;
    private SwipyRefreshLayout mSwipeRefreshLayout;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private restaurentadapter adapter;
    private static int pageCount;
    public static int numberOfRecord;
    private String subCategoryName;
    public static Typeface tf_opensense_regular;
    public static Typeface tf_opensense_medium;
    private SharedPreferences prefs;
    private int radius;
    private final int PERMISSION_REQUEST_CODE = 1001;
    private final int PERMISSION_REQUEST_CODE1 = 10011;
    private final String[] permission_location = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private final String[] permission_location1 = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
//    AdRequest adRequest;
    String userId1, DeliveryBoyId;
//    AdView mAdView;
//    private InterstitialAd mInterstitialAd;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private EditText edit_search;
    private ImageView btn_speech;
    private boolean action_speech;
    private ImageView btn_search;
    private String ciudad;
    private String direccion_envio;
    private SessionManager session;

    private String filterName;
    private ImageButton drawerBtn;
    private TextView selecAddressBtn1;
    private ImageButton selecAddressBtn2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent.getSerializableExtra("filterName") != null) {
            deliveryData.filterName = (String) intent.getSerializableExtra("filterName");
        }

        FirebaseApp.initializeApp(this);
        FirebaseInstanceId.getInstance().getInstanceId();

        //generate key hash for facebook
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("MainActivity:", "hhy== " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        changeStatsBarColor(MainActivity.this);

        gettingSharedPref();

        gettingIntent();

        initializations();

        clickEvents();

        getDeliveryData();

        settingActionBar();

        if (deliveryData.delivery_id.equals("")) {
            startDeliveryAddressActivity();
        } else {
            if (checkPermission()) {
                restaurentlist = new ArrayList<>();
                if (GPSLocationisactive()) {
                    Error = null;
                    restaurentlist.clear();
                    reset();
                    pageCount = 1;
                    new GetDataAsyncTask().execute();
                    new LongOperation().execute();
                    mSwipeRefreshLayout.setRefreshing(false);
                    recyclerView.scrollToPosition(restaurentlist.size() - 1);
                }
            } else requestPermission();
            // activar y desactivar ventama de promociones o pedir por whatsapp
            showDialog1(MainActivity.this,"#SiTienesFasterTienesTodo \uD83E\uDDE1");
        }
    }

    public void startDeliveryAddressActivity() {
        Intent iv = new Intent(MainActivity.this, DeliveryAddress.class);
        startActivity(iv);
        finish();
    }

    public void getDeliveryData() {
        SharedPreferences prefs1 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        deliveryData.delivery_id = prefs1.getString("delivery_id", "");
        deliveryData.delivery_user_id = prefs1.getString("delivery_user_id", "");
        deliveryData.delivery_lat = prefs1.getString("delivery_lat", "");
        deliveryData.delivery_lon = prefs1.getString("delivery_lon", "");
        deliveryData.delivery_address = prefs1.getString("delivery_address", "");
        deliveryData.delivery_alias = prefs1.getString("delivery_alias", "");
        deliveryData.delivery_phone = prefs1.getString("delivery_phone", "");
        deliveryData.delivery_note = prefs1.getString("delivery_note", "");
        deliveryData.delivery_department_number = prefs1.getString("delivery_department_number", "");
    }

    public void showDialog1(Activity activity, String msg){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_promocion);
        ImageView img_promo = dialog.findViewById(R.id.promo);
        Picasso.get()
                .load("https://app.faster.com.ec/uploads/menu/whatsapp.png")
                .placeholder(R.drawable.whatsapp)
                .error(R.drawable.whatsapp)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(img_promo);

       /*Glide.with(this)
                .load("https://app.faster.com.ec/uploads/menu/whatsapp.png")
                .into(img_promo);*/

        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton1 = (Button) dialog.findViewById(R.id.btn1);
        ImageButton closeBtn = dialog.findViewById(R.id.close);

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialogButton1.setOnClickListener(v -> {
            //dialog.dismiss();
            String toNumber = "+593985494782";
            toNumber = toNumber.replace("+", "").replace(" ", "");
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "¡Hola! ¿Me ayudan con un pedido?, por favor.");
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setPackage("com.whatsapp");
            sendIntent.setType("text/plain");
            //startActivity(sendIntent);

            try {
                startActivity(sendIntent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "No tienes instalado WhatsApp.", Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Pronuncia el nombre de la tienda o producto que desees");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            //
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    edit_search.setText(result.get(0));

                    Error = null;
                    reset();
                    restaurentlist.clear();
                    pageCount = 1;
                    new GetDataAsyncTasksearch().execute();
                }
                break;

            case Splash.REQUEST_CHECK_SETTINGS:
                if(resultCode==RESULT_OK){

                    if (search.length() == 0) {
                        Error = null;
                        restaurentlist.clear();
                        reset();
                        pageCount = 1;
                        new GetDataAsyncTask().execute();
                        new LongOperation().execute();
                        mSwipeRefreshLayout.setRefreshing(false);
                        recyclerView.scrollToPosition(restaurentlist.size() - 1);
                    } else {
                        Error = null;
                        restaurentlist.clear();
                        reset();
                        pageCount = 1;
                        new GetDataAsyncTasksearch().execute();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                }else if(resultCode==RESULT_CANCELED){

                    createLocationRequest();
                }
                break;
        }
    }

    private void gettingIntent() {
        Intent i = getIntent();
        if (i.getStringExtra("sub_category_name") != null) {
            subCategoryName = i.getStringExtra("sub_category_name");
        }
        if (i.getStringExtra("sub_category_id") != null) {
            String subCategoryId = i.getStringExtra("sub_category_id");
        }
    }

    private void initializations() {
        displayFirebaseRegId();
        recyclerView = findViewById(R.id.listview);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        rel_main = findViewById(R.id.rel_main);
        edit_search = findViewById(R.id.edit_search);
        selecAddressBtn1 = findViewById(R.id.selecAddressBtn1);
        selecAddressBtn2 = findViewById(R.id.selecAddressBtn2);
        drawerBtn = findViewById(R.id.drawerBtn);
        btn_speech = findViewById(R.id.btn_speech);
        btn_search = findViewById(R.id.img_search);
        numberOfRecord = 10;
        pageCount = 1;
        action_speech = true;

        session = new SessionManager(getApplicationContext());

        //setting fonts
        font();
        SharedPreferences prefs1 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userId1 = prefs1.getString("userid", null);

        //getting intents numberOfRecords Location.class
        timezoneID = TimeZone.getDefault().getID();

        Intent iv = getIntent();
        String res_id = iv.getStringExtra("res_id");
        res_name = iv.getStringExtra("res_name");
        String manualadd = iv.getStringExtra("manualadd");

        Log.e("res_id", res_id + res_name);
        Log.e("manualadd", "" + manualadd);
//        AdShow();

    }

    private void gettingSharedPref() {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Location = prefs.getString("city", null);
        radius = 4;
    }

    private void displayFirebaseRegId() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", "null");
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }

    private void settingActionBar() {
        setupDrawer();
        drawer();
//        SpannableString s;
//        if(Objects.equals(prefs.getString("direccion_envio", null), "Unnamed Road, Ecuador")){
//            s = new SpannableString(prefs.getString("city", null));
//        }else{
//            s = new SpannableString(prefs.getString("direccion_envio", null));
//        }
//        s.setSpan(new RelativeSizeSpan(0.8f), 0,s.length(), 0);
//        s.setSpan(new TypefaceSpan("OpenSans-Regular.ttf"), 0, s.length(),
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        selecAddressBtn1.setText(deliveryData.delivery_alias == ""? deliveryData.delivery_address : (deliveryData.delivery_alias + " (" + deliveryData.delivery_address + ")"));
    }

    public static void showErrorDialog(Context c) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(c, R.style.MyDialogTheme);
        builder1.setTitle("Sin conexión");
        builder1.setMessage("Por favor revise su conexión a Internet");
        builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
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

    private boolean GPSLocationisactive() {
        GPSTracker gps = new GPSTracker();
        gps.init(MainActivity.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            return true;
        } else {
            createLocationRequest();
            return false;
        }
    }

    private void clickEvents() {

        drawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onBackPressed();
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    drawerBtn.setImageResource(R.drawable.ic_baseline_menu_24);

                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    drawerBtn.setImageResource(R.drawable.ic_arrow_back_black_24dp);
                }
            }
        });

        selecAddressBtn1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startDeliveryAddressActivity();
            }
        });

        selecAddressBtn2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startDeliveryAddressActivity();
            }
        });

        btn_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GPSLocationisactive()) {
                    if (action_speech) {
                        startVoiceInput();
                        action_speech = false;
                    } else {
                        edit_search.setText("");
                        action_speech = true;
                    }
                    return;
                }

            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (GPSLocationisactive()) {
                    if (edit_search.getText().length() > 1) {
                        Error = null;
                        reset();
                        restaurentlist.clear();
                        pageCount = 1;
                        new GetDataAsyncTasksearch().execute();
                    }
                }

            }
        });


        edit_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (GPSLocationisactive()) {
                        if (edit_search.getText().length() > 1) {
                            Error = null;
                            reset();
                            restaurentlist.clear();
                            pageCount = 1;
                            new GetDataAsyncTasksearch().execute();
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        // search on home page method
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) { // TODO
                search = s.toString();

                if (search.length() == 0) {
                    Error = null;
                    reset();
                    pageCount = 1;
                    restaurentlist.clear();
                    new GetDataAsyncTask().execute();
                    new LongOperation().execute();
                    btn_speech.setImageResource(R.drawable.microphone_icon);
                    action_speech = true;
                } else {
                    btn_speech.setImageResource(R.drawable.clear_icon);
                    action_speech = false;
                }
            }
        });


        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if (mSwipeRefreshLayout.isRefreshing()) { // TODO correccion SwipyRefreshLayout.java line 583

                    if (GPSLocationisactive()) {

                        if (search.length() == 0) {
                            Error = null;
                            restaurentlist.clear();
                            reset();
                            pageCount = 1;
                            new GetDataAsyncTask().execute();
                            new LongOperation().execute();
                            mSwipeRefreshLayout.setRefreshing(false);
                            recyclerView.scrollToPosition(restaurentlist.size() - 1);
                        } else {
                            Error = null;
                            restaurentlist.clear();
                            reset();
                            pageCount = 1;
                            new GetDataAsyncTasksearch().execute();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String estado = "" + adapter.moviesList.get(position).getRes_status();
                String enable = "" + adapter.moviesList.get(position).getRes_enable();
                String name = "" + adapter.moviesList.get(position).getName();
                final String distance = "" + adapter.moviesList.get(position).getDistance();

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("distance", distance);
                editor.apply();

                final String res_id = "" + adapter.moviesList.get(position).getId();
                if (estado.equals("open") && enable.equals("open")) {

                    Intent iv = new Intent(MainActivity.this, MenuList.class);
                    iv.putExtra("detail_id", res_id);
                    iv.putExtra("restaurent_name", "" + adapter.moviesList.get(position).getName());
                    iv.putExtra("distance", distance);
                    startActivity(iv);

                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                    builder1.setTitle("Tienda Cerrada");
                    builder1.setMessage(Html.fromHtml("Lo sentimos, la Tienda <b>" + name + "</b> se encuentra cerrada, pero puedes ingresar a ver su información."));
                    builder1.setPositiveButton("Ingresar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Intent iv = new Intent(MainActivity.this, DetailPage.class);
                            iv.putExtra("res_id", "" + res_id);
                            iv.putExtra("distance", "" + distance);
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
                    try {
                        alert11.show();
                    } catch(Exception e) {
                        Log.e("Error_alert12", Objects.requireNonNull(e.getMessage()));
                    }
                }

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void reset() {
        CategoryTotal = "";
    }

    private void font() {

        txt_nameuser = findViewById(R.id.txt_nameuser);
        txt_profile = findViewById(R.id.txt_profile);
        img_profile = findViewById(R.id.img_profile);

    }

    private void drawer() {
        // LinearLayout ll_fav = findViewById(R.id.ll_fav);
        //LinearLayout ll_suggested = findViewById(R.id.ll_suggested);
        // LinearLayout ll_cusine = findViewById(R.id.ll_cusine);

        final LinearLayout ll_aboutus = findViewById(R.id.ll_aboutus);
        final LinearLayout ll_terms = findViewById(R.id.ll_terms);
        final LinearLayout ll_logout = findViewById(R.id.ll_logout);
        LinearLayout ll_share = findViewById(R.id.ll_share);
        LinearLayout ll_search = findViewById(R.id.ll_search);
        LinearLayout ll_address = findViewById(R.id.ll_address);
        LinearLayout ll_advertise = findViewById(R.id.ll_advertise);
        LinearLayout ll_rated = findViewById(R.id.ll_rated);
        //LinearLayout ll_notification = findViewById(R.id.ll_notification);

        /** ll_fav.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        Intent iv = new Intent(MainActivity.this, Favourite.class);
        startActivity(iv);
        }
        });
         **/
        /*ll_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, MyOrderPage.class);
                iv.putExtra("key","main");
                startActivity(iv);
            }
        });*/

        ll_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url1 = "Llegó a *" + prefs.getString("city", null) + "*" + " FASTER - DELIVERY APP, una app que usa tecnología de punta para ordenar a domicilio, además de la posibilidad de rastrear en tiempo real, me ha gustado mucho y te la recomiendo ¿Qué esperas para usarla? \n\n _*FASTER - DELIVERY APP*_ *#SiTienesFasterTienesTodo*, DESCÁRGALA GRATIS AQUÍ: \n https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "FASTER - DELIVERY APP");
                intent.putExtra(Intent.EXTRA_TEXT, url1);

                startActivity(Intent.createChooser(intent, "Compartir FASTER - DELIVERY APP con: "));
            }
        });


        ll_aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, Aboutus.class);
                startActivity(iv);

            }
        });

        ll_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, Termcondition.class);
                startActivity(iv);
            }
        });

        ll_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(iv);
            }
        });

        ll_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDeliveryAddressActivity();
            }
        });

        ll_advertise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, AdvertiseActivity2.class);
                startActivity(iv);
            }
        });


        ll_rated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iv = new Intent(MainActivity.this, Mapactivity.class);
                startActivity(iv);
            }
        });

        ll_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(MainActivity.this, Login4.class);
                                startActivity(intent);
                                SharedPreferences settings = getSharedPreferences("Fooddelivery", Context.MODE_PRIVATE);
                                settings.edit().clear().apply();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                AlertDialog dialog = builder.setTitle("¿Estás seguro que deseas Cerrar Sesión?").setPositiveButton("SI", dialogClickListener)
                        .setNegativeButton("NO", dialogClickListener).show();
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            }
        });


        /* ll_suggested.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        Intent iv = new Intent(MainActivity.this, MostRatedRestaurant.class);
        startActivity(iv);
        }
        }); */

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if (prefs.getString("userid", null) != null){
            String userId = prefs.getString("userid", null);
            String image = prefs.getString("imagepath", null);
            String profileimage = prefs.getString("imageprofile", null);

            Log.e("image121", "" + profileimage);

            String uname = prefs.getString("username", null);

            Picasso.get().load(profileimage)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .resize(200, 200)
                        .into(img_profile);

            txt_nameuser.setText(uname);
            txt_profile.setText(R.string.txt_profile);
            txt_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent iv = new Intent(MainActivity.this, Profile.class);
                    startActivity(iv);
                }
            });
        } else {
            txt_profile.setText(R.string.txt_profile);
            txt_nameuser.setText(R.string.txt_signin);
        }

    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.setEnabled(true);
                recyclerView.setClickable(false);
                recyclerView.setFocusable(false);
            }
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                view.setEnabled(false);
                rel_main.setEnabled(true);
                rel_main.setVisibility(View.VISIBLE);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        // Activate the navigation drawer toggle
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

    class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String hp;
            hp = getString(R.string.link) + getString(R.string.servicepath) + "user_check_restaurant_location.php?";

            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.e("PlaceOrder10", "ResponseFromServer " + response + "user id: " + userId1);
                    try {
                        JSONObject responsedat = new JSONObject(response);
                        String txt_success = responsedat.getString("success");
                        //String txt_message = responsedat.getString("message");

                        /*case "1": {
                                Intent iv = new Intent(MainActivity.this, MainActivity.class);
                                iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                //IntentOrderInfo
                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString("city", responsedat.getString("city"));
                                editor.apply();
                                startActivity(iv);
                                finish();
                                break;
                            }*/
                        if ("200".equals(txt_success)) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setIcon(R.drawable.ic_launcher);
                            builder1.setCancelable(false);
                            builder1.setMessage("No encontramos ningún resultado para tu ubicación, zona sin cobertura.");
                            builder1.setPositiveButton("Cambiar dirección", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent iv = new Intent(MainActivity.this, DeliveryAddress.class);
                                    iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(iv);
                                    finish();
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alert11 = builder1.create();
                            try {
                                alert11.show();
                            } catch (Exception e) {
                                Log.e("Error_alert11", e.getMessage());
                            }
                        } /*else {
                            Toast.makeText(getApplicationContext(), "¡Ups! Algo no está funcionando como esperabámos.", Toast.LENGTH_SHORT).show();
                        }*/
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage("Por favor verifica tu conexión a Internet");
                            builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    //new MainActivity().LongOperation().execute();
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

                        } else {
                            Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Log.e("XXXXXXXXXX", deliveryData.delivery_lat);
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", userId1);
                    params.put("lat", deliveryData.delivery_lat);
                    params.put("lon", deliveryData.delivery_lon);
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
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            requestQueue.add(postRequest);

            return "whatever result you have";
        }
    }


    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            reset();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Espere, estamos buscando...");
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {

                //String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&lon=" + longitudecur + "&search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo);
                String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list_filter.php?timezone=" + timezoneID + "&" + "lat=" + deliveryData.delivery_lat + "&lon=" + deliveryData.delivery_lon + "&search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo) + "&type_store=" + deliveryData.filterName;
                Log.e("User",user);
                hp = new URL(user.replace(" ", "%20"));

                Log.e("URLs1", "" + hp);
                Log.e("X2", "-" + deliveryData.delivery_lat);
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
                        temp.setprice_delivery(Obj.getString("delivery_price"));
                        temp.setOpen_time(Obj.getString("close_time"));
                        temp.setClose_time(Obj.getString("open_time"));
                        temp.setCurrency(Obj.getString("currency"));
                        temp.setDelivery_time(Obj.getString("time_order"));
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

                            Toast.makeText(MainActivity.this, txt_message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (txt_success.equals("202")) {
                    Error = "all_load";
                }  if (txt_success.equals("2")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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
                                                Toast.makeText(MainActivity.this, "No se puede conectar intente nuevamente...",
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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

                                    Intent iv = new Intent(MainActivity.this, Login4.class);
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
                    Intent iv = new Intent(MainActivity.this, CompleteOrder.class);
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
                if (Error != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, Error, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Log.e("adapter", "" + restaurentlist.size());
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());

                    adapter = new restaurentadapter(recyclerView, MainActivity.this, restaurentlist);

                    adapter.setOnLoadMoreListener(new restaurentadapter.OnLoadMoreListener() {
                        @Override
                        public void onLoadMore() {
                            pageCount = pageCount + 1;
                            //Load more data for reyclerview
                            new LoadMoreData().execute();

                        }
                    });

                    recyclerView.setAdapter(adapter);

                }
            } catch (final IllegalArgumentException e) {
                Log.d("error_recyclerView", Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class LoadMoreData extends AsyncTask<Void, Void, Void> {
        ArrayList data;

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {
                //String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&lon=" + longitudecur + "&search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo);
                String user = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list_filter.php?timezone=" + timezoneID + "&" + "lat=" + deliveryData.delivery_lat + "&lon=" + deliveryData.delivery_lon + "&search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo) + "&type_store=" + deliveryData.filterName;
                hp = new URL(user.replace(" ", "%20"));

                Log.e("URLs1", "" + hp);
                Log.e("X3", deliveryData.delivery_lat + ", "+ deliveryData.delivery_lon);
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

                String txt_success = responsedat.getString("success");
                final String txt_message = responsedat.getString("message");
                data = new ArrayList<restaurentGetSet>();


                if (txt_success.equals("1")) {
                    JSONArray jsonArray = responsedat.getJSONArray("restaurant_list");

                    Log.d("hola1", jsonArray.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        restaurentGetSet temp = new restaurentGetSet();
                        JSONObject Obj = jsonArray.getJSONObject(i);
                        temp.setId(Obj.getString("id"));
                        temp.setName(Obj.getString("name"));
                        temp.setLat(Obj.getString("lat"));
                        temp.setLon(Obj.getString("lon"));
                        temp.setDistance(Obj.getString("distance"));
                        temp.setprice_delivery(Obj.getString("delivery_price"));
                        temp.setOpen_time(Obj.getString("close_time"));
                        temp.setClose_time(Obj.getString("open_time"));
                        temp.setCurrency(Obj.getString("currency"));
                        temp.setDelivery_time(Obj.getString("time_order"));
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
                            }
                            temp.setCategory(temprory);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        data.add(temp);

                        Error =null;
                    }
                } else if (txt_success.equals("201") || txt_success.equals("203") || txt_success.equals("204")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, txt_message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (txt_success.equals("202")) {
                    Error = "all_load";
                }  if (txt_success.equals("2")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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
                                                Toast.makeText(MainActivity.this, "No se puede conectar intente nuevamente...",
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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

                                    Intent i = new Intent(MainActivity.this, Login4.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
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
                    Intent iv = new Intent(MainActivity.this, CompleteOrder.class);
                    startActivity(iv);
                    finish();
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
            } catch (NullPointerException e) {
                // TODO: handle exception
            } catch (IOException e) {
                e.printStackTrace();
                Error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (Error != null) {
                if (!Error.equals("all_load")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "" + Error, Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            } else if (data.size() != 0) {
                Log.e("adapter", "" + data.size());
                adapter.setLoaded();
                adapter.addItem(data, restaurentlist.size());
            }
        }
    }

    class GetDataAsyncTasksearch extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            pd.setMessage("Buscando tiendas...");
            progressDialog.setCancelable(true);
            progressDialog.show();
            reset();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {
                // Log.e("X1", "-" + deliveryData.delivery_lat);
                //String Usersearch = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&" + "lon=" + longitudecur + "&" + "search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo);
                String Usersearch = getString(R.string.link) + getString(R.string.servicepath) + "restaurant_list.php?timezone=" + timezoneID + "&" + "lat=" + deliveryData.delivery_lat + "&" + "lon=" + deliveryData.delivery_lon + "&" + "search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo) + "&type_store=" + deliveryData.filterName;
                hp = new URL(Usersearch.replace(" ", "%20"));
                // Log.e("URLs1", "" + hp);

                URLConnection hpCon = hp.openConnection();
                hpCon.setRequestProperty("Authorization", "Bearer " + regId);

                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                // Log.d("input", "" + input);
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


                String txt_success = responsedat.getString("success");
                final String txt_message = responsedat.getString("message");

                if (txt_success.equals("1")) {
                    JSONArray jsonArray = responsedat.getJSONArray("restaurant_list");
                    restaurentlist.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        restaurentGetSet temp = new restaurentGetSet();
                        JSONObject Obj = jsonArray.getJSONObject(i);
                        temp.setId(Obj.getString("id"));
                        temp.setName(Obj.getString("name"));
                        temp.setLat(Obj.getString("lat"));
                        temp.setLon(Obj.getString("lon"));
                        temp.setDistance(Obj.getString("distance"));
                        temp.setprice_delivery(Obj.getString("delivery_price"));
                        temp.setOpen_time(Obj.getString("close_time"));
                        temp.setClose_time(Obj.getString("open_time"));
                        temp.setCurrency(Obj.getString("currency"));
                        temp.setDelivery_time(Obj.getString("time_order"));
                        temp.setImage(Obj.getString("photo"));
                        temp.setRatting(Obj.getString("ratting"));
                        temp.setRes_status(Obj.getString("res_status"));
                        temp.setRes_enable(Obj.getString("res_status_manual"));
                        JSONArray jCategory = Obj.getJSONArray("Category");
                        String[] temprory = new String[jCategory.length()];
                        for (int j = 0; j < jCategory.length(); j++) {
                            temprory[j] = jCategory.getString(j);
                            CategoryTotal = CategoryTotal + temprory[j];
                            Log.e("catname12121", "" + CategoryTotal);
                        }
                        temp.setCategory(temprory);
                        restaurentlist.add(temp);

                    }

                } else if (txt_success.equals("201") || txt_success.equals("203") || txt_success.equals("204")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, txt_message, Toast.LENGTH_SHORT).show();
                        }
                    });

                } else if (txt_success.equals("202")) {
                    Error = "all_load";

                } else  if (txt_success.equals("2")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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
                                                Toast.makeText(MainActivity.this, "No se puede conectar intente nuevamente...",
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
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
                                    editor2.putString("regId", null);
                                    editor2.apply();

                                    // session manager
                                    session.setLogin(false);
                                    Intent i = new Intent(MainActivity.this, Login4.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
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
                    Intent iv = new Intent(MainActivity.this, CompleteOrder.class);
                    startActivity(iv);
                    finish();
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
                Log.e("error",Error);
            } catch (NullPointerException e) {
                // TODO: handle exception
            } catch (IOException e) {
                e.printStackTrace();
                Error = e.getMessage();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (Error != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, Error, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                adapter = new restaurentadapter(recyclerView, MainActivity.this, restaurentlist);
                adapter.setOnLoadMoreListener(new restaurentadapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        pageCount = pageCount + 1;
                        new LoadMoreData().execute();
                    }
                });
                recyclerView.setAdapter(adapter);
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

    public static void changeStatsBarColor(Activity activity) {
        Window window = activity.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.my_statusbar_color));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                    finish();
                } else requestPermission();
            }
        }
        if (requestCode == PERMISSION_REQUEST_CODE1) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                    finish();
                } else requestPermission();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //replaces the default 'Back' button action
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            finish();
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else {

            if (!action_speech) {
                edit_search.setText("");
                action_speech = true;
            }
        }
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

                Toast.makeText(MainActivity.this, "GPS ya está activado",
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
                        resolvable.startResolutionForResult(MainActivity.this,
                                Splash.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

}
