package faster.com.ec.fooddelivery;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import faster.com.ec.slider.IndicatorView.animation.type.IndicatorAnimationType;
import faster.com.ec.slider.IndicatorView.draw.controller.DrawController;
import faster.com.ec.slider.SliderAnimations;
import faster.com.ec.slider.SliderView;

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
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import faster.com.ec.Adapter.SliderAdapter;
import faster.com.ec.Adapter.SliderItem;
import faster.com.ec.Adapter.advertiseadapter;
import faster.com.ec.Getset.advertiseGetSet;
import faster.com.ec.R;
import faster.com.ec.helper.SessionManager;
import faster.com.ec.utils.Config;
import faster.com.ec.utils.GPSTracker;
import faster.com.ec.utils.RecyclerTouchListener;

public class AdvertiseActivity2 extends AppCompatActivity {
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static ArrayList<advertiseGetSet> advertiseList;
    private String Error;
    private RecyclerView recyclerView;
    private String timezoneID;
    private String search = "";
    private String res_name;
    private ProgressDialog progressDialog;
    private ProgressDialog pd;
    private double latitudecur = 0;
    private double longitudecur = 0;
    private TextView txt_nameuser;
    private TextView txt_profile;
    private RelativeLayout rel_main;
    private String Location;
    private ImageView img_profile;
    private String CategoryTotal = "", regId;
    private SwipyRefreshLayout mSwipeRefreshLayout;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private advertiseadapter adapter;
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
    private String call_phone_number="";

    private String filterName;
    ImageButton ib_back;

    SliderView sliderView;
    private SliderAdapter slideAdapter;
    List<SliderItem> sliderItemList;
    CardView cv_slider_mainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_advertise2);

        Intent intent = getIntent();
        filterName = (String) intent.getSerializableExtra("filterName");

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

        changeStatsBarColor(AdvertiseActivity2.this);

        gettingSharedPref();

        gettingIntent();

        initializations();

        settingActionBar();

        clickEvents();

        if (checkPermission()) {
            advertiseList = new ArrayList<>();
            if (GPSLocationisactive()) {
                Error = null;
                advertiseList.clear();
                reset();
                pageCount = 1;
                new GetDataAsyncTask().execute();
                mSwipeRefreshLayout.setRefreshing(false);
                recyclerView.scrollToPosition(advertiseList.size() - 1);
            }
        } else requestPermission();
        // showDialog1(MainActivity.this,"Solicita tu bebida gratis en cada Pedido");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Pronuncia el nombre de empresas, servicios o negocios que desees");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

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
                    advertiseList.clear();
                    pageCount = 1;
                    new GetDataAsyncTask().execute();
                }
                break;

            case Splash.REQUEST_CHECK_SETTINGS:
                if(resultCode==RESULT_OK){

                    if (search.length() == 0) {
                        Error = null;
                        advertiseList.clear();
                        reset();
                        pageCount = 1;
                        new GetDataAsyncTask().execute();
                        mSwipeRefreshLayout.setRefreshing(false);
                        recyclerView.scrollToPosition(advertiseList.size() - 1);
                    } else {
                        Error = null;
                        advertiseList.clear();
                        reset();
                        pageCount = 1;
                        new GetDataAsyncTask().execute();
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
        btn_speech = findViewById(R.id.btn_speech);
        btn_search = findViewById(R.id.img_search);
        numberOfRecord = getResources().getInteger(R.integer.numberOfRecords);
        pageCount = 1;
        action_speech = true;

        cv_slider_mainActivity = findViewById(R.id.cv_slider_mainActivity);
        sliderView = findViewById(R.id.imageSlider);

        slideAdapter = new SliderAdapter(this);
        sliderView.setSliderAdapter(slideAdapter);

        sliderView.setIndicatorAnimation(IndicatorAnimationType.THIN_WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.setIndicatorSelectedColor(Color.WHITE);
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.setScrollTimeInSec(5);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();

        sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
            @Override
            public void onIndicatorClicked(int position) {
                sliderView.setCurrentPagePosition(position);
            }
        });

        ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ((TextView) findViewById(R.id.txt_title)).setTypeface(MainActivity.tf_opensense_medium);

        session = new SessionManager(getApplicationContext());

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
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }

    private void settingActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            SpannableString s;

            if(prefs.getString("direccion_envio", null).equals("Unnamed Road, Ecuador")){
                s = new SpannableString(prefs.getString("city", null));
            }
            else{
                s = new SpannableString(prefs.getString("direccion_envio", null));
            }
            s.setSpan(new RelativeSizeSpan(0.8f), 0,s.length(), 0);
            s.setSpan(new TypefaceSpan("OpenSans-Regular.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);

        }

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
        alert11.show();
    }

    private List<Address> getAddress () {
        if (latitudecur != 0 && longitudecur != 0) {
            try {
                Geocoder geocoder = new Geocoder(AdvertiseActivity2.this);
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


    private boolean GPSLocationisactive() {
        GPSTracker gps = new GPSTracker();
        gps.init(AdvertiseActivity2.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            try {
                latitudecur = gps.getLatitude();
                longitudecur = gps.getLongitude();
                //latitudecur = -0.1267201;
                //longitudecur = -78.4636728;
                direccion_envio = getAddress().get(0).getAddressLine(0);

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("latitud", String.valueOf(latitudecur));
                editor.putString("longitud", String.valueOf(longitudecur));
                editor.putString("direccion_envio", String.valueOf(direccion_envio));
                editor.apply();

                settingActionBar();

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

    private void clickEvents() {
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
                        advertiseList.clear();
                        pageCount = 1;
                        new GetDataAsyncTask().execute();
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
                            advertiseList.clear();
                            pageCount = 1;
                            new GetDataAsyncTask().execute();
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
                    advertiseList.clear();
                    new GetDataAsyncTask().execute();
                    btn_speech.setImageResource(R.drawable.microphone_service);
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
                if (GPSLocationisactive()) {

                    if (search.length() == 0) {
                        Error = null;
                        advertiseList.clear();
                        reset();
                        pageCount = 1;
                        new GetDataAsyncTask().execute();
                        mSwipeRefreshLayout.setRefreshing(false);
                        recyclerView.scrollToPosition(advertiseList.size() - 1);
                    } else {
                        Error = null;
                        advertiseList.clear();
                        reset();
                        pageCount = 1;
                        new GetDataAsyncTask().execute();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }

            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                call_phone_number = advertiseList.get(position).getPhone();
                callPhoneNumber();
            }
        }));
    }
    public void callPhoneNumber()
    {
        try
        {
            if(Build.VERSION.SDK_INT > 22)
            {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AdvertiseActivity2.this, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + call_phone_number));
                startActivity(callIntent);

            }
            else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + call_phone_number));
                startActivity(callIntent);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    private void reset() {
        CategoryTotal = "";
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

    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AdvertiseActivity2.this);
            pd.setMessage("Buscando empresas, servicios o negocios...");
            pd.setCancelable(true);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            try {
                String Usersearch = getString(R.string.link) + getString(R.string.servicepath) + "get_advertising_list.php?timezone=" + timezoneID + "&" + "lat=" + latitudecur + "&" + "lon=" + longitudecur + "&" + "search=" + search + "&noofrecords=" + numberOfRecord + "&pageno=" + pageCount + "&radius=" + radius + "&city=" + Location + "&user_id=" + userId1 + "&code=" + getString(R.string.version_app) + "&operative_system=" + getString(R.string.sistema_operativo) + "&type_store=" + filterName;

                hp = new URL(Usersearch.replace(" ", "%20"));
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
                String txt_message = responsedat.getString("message");

                advertiseList = new ArrayList<>();
                sliderItemList = new ArrayList<>();
                if (txt_success.equals("1")) {
                    JSONArray jsonArray = responsedat.getJSONArray("restaurant_list");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        advertiseGetSet temp = new advertiseGetSet();
                        JSONObject Obj = jsonArray.getJSONObject(i);
                        temp.setAddress(Obj.getString("address"));
                        temp.setOpen_time(Obj.getString("open_time") + " - " + Obj.getString("close_time"));
                        //temp.setClose_time(Obj.getString("close_time"));
                        temp.setImage(Obj.getString("photo"));
                        temp.setPhone(Obj.getString("phone"));
                        advertiseList.add(temp);

                        SliderItem sliderItem = new SliderItem();
                        sliderItem.setRestaurantName(Obj.getString("name"));
                        sliderItem.setImageUrl(getApplication().getResources().getString(R.string.link) + getApplication().getString(R.string.imagepath) +
                                Obj.getString("photo"));
                        sliderItemList.add(sliderItem);
                        Error = null;
                    }
                } if (txt_success.equals("2")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(AdvertiseActivity2.this, R.style.MyDialogTheme);
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
                                                Toast.makeText(AdvertiseActivity2.this, "No se puede conectar intente nuevamente...",
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(AdvertiseActivity2.this, R.style.MyDialogTheme);
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
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(AdvertiseActivity2.this, R.style.MyDialogTheme);
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

                                    Intent iv = new Intent(AdvertiseActivity2.this, Login4.class);
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
                    Intent iv = new Intent(AdvertiseActivity2.this, CompleteOrder.class);
                    startActivity(iv);
                    finish();
                }



            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Error = e.getMessage();
                Toast.makeText(AdvertiseActivity2.this, "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
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
            if (pd.isShowing()) {
                pd.dismiss();
            }
            if (Error != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdvertiseActivity2.this, Error, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Log.e("adapter", "" + advertiseList.size());
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());

                adapter = new advertiseadapter(recyclerView, AdvertiseActivity2.this, advertiseList);
                recyclerView.setAdapter(adapter);

                slideAdapter.renewItems(sliderItemList);
                if (sliderItemList.size() > 0) {
                    cv_slider_mainActivity.setVisibility(View.VISIBLE);
                } else {
                    cv_slider_mainActivity.setVisibility(View.INVISIBLE);
                }
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
                    Intent i = new Intent(AdvertiseActivity2.this, AdvertiseActivity2.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                    finish();
                } else requestPermission();
            }
        }
        if (requestCode == PERMISSION_REQUEST_CODE1) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(AdvertiseActivity2.this, AdvertiseActivity2.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(i);
                    finish();
                } else requestPermission();
            }
        }

        if(requestCode == 101)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                callPhoneNumber();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!action_speech) {
            edit_search.setText("");
            action_speech = true;
        } else {
            Intent i = new Intent(AdvertiseActivity2.this, MenuActivity.class);
            startActivity(i);
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

                Toast.makeText(AdvertiseActivity2.this, "GPS ya está activado",
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
                        resolvable.startResolutionForResult(AdvertiseActivity2.this,
                                Splash.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

}
