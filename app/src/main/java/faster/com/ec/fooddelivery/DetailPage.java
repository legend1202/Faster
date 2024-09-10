package faster.com.ec.fooddelivery;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import faster.com.ec.Adapter.reviewadapter;
import faster.com.ec.Getset.CustomMarker;
import faster.com.ec.Getset.cartgetset;
import faster.com.ec.Getset.detailgetset;
import faster.com.ec.Getset.favgetset;
import faster.com.ec.Getset.reviewgetset;
import faster.com.ec.R;
import faster.com.ec.deliveryData;
import faster.com.ec.utils.GPSTracker;
import faster.com.ec.utils.RecyclerTouchListener;
import faster.com.ec.utils.sqliteHelper;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;

public class DetailPage extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MyPREFERENCES = "Fooddelivery";
    private static ArrayList<detailgetset> detaillist;
    faster.com.ec.utils.sqliteHelper sqliteHelper;
    private static ArrayList<cartgetset> cartlist;
    private ArrayList<favgetset> favlist;
    private ProgressDialog progressDialog;
    private double latitudecur;
    private double longitudecur;
    private final int start = 0;
    private String res_id;
    private ImageButton btn_fav;
    private ImageButton btn_fav1;
    private SQLiteDatabase db;
    private Cursor cur = null;
    View layout;
    private Button btn_share;
    String id;
    private String name;
    private String category;
    private String timing;
    private String rating;
    private String distance;
    private String image;
    private String restaurent_id;
    private String address;
    private String CategoryTotal = "";
    private CollapsingToolbarLayout collapsingToolbar;
    private String distancenew;
    private GoogleMap googleMap;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private final String tag = "fb";
    private HashMap<CustomMarker, Marker> markersHashMap;

    private ArrayList<reviewgetset> reviewlist;
    private String Error;
    private RatingBar rate;
    private TextView txt_ratenumber;
    private String userrate;
    private Button btn_submit;
    private EditText edt_review;
    private TextInputLayout input_layout_review;
    private String usercomment;
    private String revmsg,message,user_id;

    private static double roundMyData(double Rval, int numberOfDigitsAfterDecimal) {
        double p = (float) Math.pow(10, numberOfDigitsAfterDecimal);
        Rval = Rval * p;
        double tmp = Math.floor(Rval);
        return tmp / p;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_page);
        changeStatsBarColor(DetailPage.this);
        initializations();

        getIntents();

        gettingGPSLocation();
        getData();


        rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // Auto-generated
                Log.e("rate", "" + rating);
                userrate = String.valueOf(rate.getRating());
                txt_ratenumber.setText(userrate);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(btn_submit.getText().equals("Calificar")){
                    // TODO Auto-generated method stub
                    if (!validateRating()) {
                        return;
                    } else {
                        new getRateDetail().execute();
                    }
                }
                else{
                    edt_review.setEnabled(true);
                    btn_submit.setText("Calificar");
                    rate.setEnabled(true);
                }

            }
        });
    }

    private void gettingGPSLocation() {
        latitudecur = Double.parseDouble(deliveryData.delivery_lat);
        longitudecur = Double.parseDouble(deliveryData.delivery_lon);
    }

    private void initializations() {
        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        reviewlist = new ArrayList<>();

        //initialization
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        btn_share = findViewById(R.id.btn_share);
        btn_fav = findViewById(R.id.btn_fav);
        btn_fav1 = findViewById(R.id.btn_fav1);
        sqliteHelper = new sqliteHelper(DetailPage.this);
        rate= findViewById(R.id.rate1234);
        txt_ratenumber= findViewById(R.id.txt_ratenumber1);
        btn_submit= findViewById(R.id.btn_rating_restaurant);
        edt_review = findViewById(R.id.edt_review);
        input_layout_review= findViewById(R.id.input_layout_review);

        SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        user_id= prefs.getString("userid", null);
    }

    private void getIntents() {
        //getting intents
        Intent iv = getIntent();
        res_id = iv.getStringExtra("res_id");
        distancenew = iv.getStringExtra("distance");
    }

    private void getData() {
        //getting data
        detaillist = new ArrayList<>();
        cartlist = new ArrayList<>();
        favlist = new ArrayList<>();

        new GetDataAsyncTask().execute();


        //fav button onclick
        onClickFavourite();
        //facebook share callbacks
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(DetailPage.this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(DetailPage.this, "Compartiste una publicación", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Log.e(tag, "cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(tag, error.toString());
            }
        });


    }

    private void onClickFavourite() {
        btn_fav.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                sqliteHelper = new sqliteHelper(DetailPage.this);
                SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();
                // TODO Auto-generated method stub
                final detailgetset temp_Obj3 = detaillist.get(start);
                btn_fav1.setVisibility(View.VISIBLE);
                btn_fav.setVisibility(View.INVISIBLE);
                ContentValues values = new ContentValues();
                values.put("restaurent_id", temp_Obj3.getId());
                values.put("name", temp_Obj3.getName());
                values.put("category", temp_Obj3.getCategory());
                values.put("timing", temp_Obj3.getTime());
                values.put("rating", temp_Obj3.getRatting());
                values.put("distance", distancenew);
                values.put("image", temp_Obj3.getPhoto());
                values.put("address", temp_Obj3.getAddress());
                db1.insert("favourite", null, values);
                Log.e("inserted values", values.toString());
                db1.close();


            }
        });
        // on click of this Favourite button store will be unfavourite
        btn_fav1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                btn_fav.setVisibility(View.VISIBLE);
                btn_fav1.setVisibility(View.INVISIBLE);
                final detailgetset temp_Obj3 = detaillist.get(start);
                // remove record of store numberOfRecords database to unfavourite

                sqliteHelper = new sqliteHelper(DetailPage.this);
                SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();

                cur = db1.rawQuery("Delete from favourite where restaurent_id =" + temp_Obj3.getId() + ";", null);
                Log.e("deletedvalues", "" + ("Delete numberOfRecords Favourite where id =" + temp_Obj3.getId() + ";"));
                if (cur.getCount() != 0) {
                    if (cur.moveToFirst()) {
                        do {
                            favgetset obj = new favgetset();
                            restaurent_id = cur.getString(cur.getColumnIndex("restaurent_id"));
                            name = cur.getString(cur.getColumnIndex("name"));
                            category = cur.getString(cur.getColumnIndex("category"));
                            timing = cur.getString(cur.getColumnIndex("timing"));
                            rating = cur.getString(cur.getColumnIndex("rating"));
                            distance = cur.getString(cur.getColumnIndex("distance"));
                            image = cur.getString(cur.getColumnIndex("image"));
                            address = cur.getString(cur.getColumnIndex("address"));
                            obj.setName(name);
                            obj.setCategory((category));
                            obj.setTiming(timing);
                            obj.setRating(rating);
                            obj.setDistance(distance);
                            obj.setImage(image);
                            obj.setId(restaurent_id);
                            obj.setAddress(address);
                            favlist.add(obj);
                        } while (cur.moveToNext());
                    }
                }
                cur.close();
                db1.close();
            }
        });


    }

    private Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap numberOfRecords ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
//            bmpUri = FileProvider.getUriForFile(DetailPage.this, BuildConfig.APPLICATION_ID + ".provider", file);

            bmpUri = FileProvider.getUriForFile(this,
                    "faster.com.ec.provider", // Over here
                    file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void getlist() {

        sqliteHelper = new sqliteHelper(DetailPage.this);
        SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();

        try {
            cur = db1.rawQuery("delete  from cart ;", null);
            Log.e("deletdetail_pagedata", "delete numberOfRecords cart ;");
            if (cur.getCount() != 0) {
                if (cur.moveToFirst()) {
                    do {
                        cartgetset obj = new cartgetset();
                        String resid = cur.getString(cur.getColumnIndex("resid"));
                        String menuid321 = cur.getString(cur.getColumnIndex("menuid"));
                        String foodid = cur.getString(cur.getColumnIndex("foodid"));
                        String foodname = cur.getString(cur.getColumnIndex("foodname"));
                        String foodprice321 = cur.getString(cur.getColumnIndex("foodprice"));
                        String fooddesc = cur.getString(cur.getColumnIndex("fooddesc"));
                        obj.setResid(resid);
                        obj.setFoodid(foodid);
                        obj.setMenuid(menuid321);
                        obj.setFoodname(foodname);
                        obj.setFooddesc(fooddesc);
                        Log.e("menuid321", menuid321);
                        Log.e("foodp321", "" + foodprice321);
                        cartlist.add(obj);
                    } while (cur.moveToNext());
                }
            }
            cur.close();
            db1.close();
//            myDbHelper.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        double latitude = 0, longitude = 0;
        try {
            String lat = detaillist.get(0).getLat();
            String lon = detaillist.get(0).getLon();
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lon);
        } catch (NumberFormatException e) {
            Log.e("Error", e.getMessage());
            Toast.makeText(DetailPage.this, getString(R.string.later_txt), Toast.LENGTH_SHORT).show();
            // TODO: handle exception
        }
        afterMapReady(latitude, longitude);

    }

    private void afterMapReady(double latitude, double longitude) {
        LatLng position = new LatLng(latitude, longitude);
        CustomMarker customMarkerOne = new CustomMarker("markerOne", latitude, longitude);
        try {
            MarkerOptions markerOption = new MarkerOptions().position(

                    new LatLng(customMarkerOne.getCustomMarkerLatitude(), customMarkerOne.getCustomMarkerLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    .title(detaillist.get(0).getName());

            Marker newMark = googleMap.addMarker(markerOption);

            addMarkerToHashMap(customMarkerOne, newMark);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17));
        } catch (Exception e1) {
            Toast.makeText(DetailPage.this, getString(R.string.later_txt), Toast.LENGTH_SHORT).show();

        }
    }



    private void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
        setUpMarkersHashMap();
        markersHashMap.put(customMarker, marker);
    }

    private void setUpMarkersHashMap() {
        if (markersHashMap == null) {
            markersHashMap = new HashMap<>();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }



    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressDialog = new ProgressDialog(DetailPage.this);
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setCancelable(true);
                progressDialog.show();
            } catch(Exception e) {
                Log.e("Error_progressDialog2", Objects.requireNonNull(e.getMessage()));
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp;
            String error1;
            try {
                detaillist.clear();
                hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "getrestaurantdetail.php?res_id=" + res_id + "&lat=" + latitudecur + "&" + "lon=" + longitudecur);
                Log.e("URLdetail", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                String x;
                x = r.readLine();
                StringBuilder total = new StringBuilder();
                while (x != null) {
                    total.append(x);
                    x = r.readLine();
                }
                Log.e("URL", "" + total);

                JSONArray jObject = new JSONArray(total.toString());
                Log.d("URL12", "" + jObject);
                JSONObject Obj1;
                Obj1 = jObject.getJSONObject(0);
                switch (Obj1.getString("status")) {
                    case "Success":
                        JSONObject jsonO = Obj1.getJSONObject("Restaurant_Detail");
                        detailgetset temp = new detailgetset();
                        temp.setId(jsonO.getString("id"));
                        temp.setName(jsonO.getString("name"));
                        temp.setAddress(jsonO.getString("address"));
                        temp.setTime(jsonO.getString("time"));
                        temp.setDelivery_time(jsonO.getString("delivery_time"));
                        temp.setCurrency(jsonO.getString("currency"));
                        temp.setPhoto(jsonO.getString("photo"));
                        temp.setPhone(jsonO.getString("phone"));
                        temp.setLat(jsonO.getString("lat"));
                        temp.setLon(jsonO.getString("lon"));
                        temp.setDesc(jsonO.getString("desc"));
                        temp.setEmail(jsonO.getString("email"));
                        temp.setLocation(jsonO.getString("address"));
                        temp.setRatting(jsonO.getString("ratting"));
                        temp.setRes_status(jsonO.getString("res_status"));
                        temp.setEnable(jsonO.getString("enable"));
                        temp.setDelivery_charg(jsonO.getString("delivery_charg"));
                        temp.setDistance(jsonO.getString("distance"));
                        temp.setCategory(jsonO.getString("Category"));
                        String catname = jsonO.getString("Category");
                        Log.e("catname", "" + catname);
                        CategoryTotal = CategoryTotal + catname;
                        detaillist.add(temp);
                        Log.e("detaillist", detaillist.get(0).getName());
                        break;
                    case "Failed":
                        final String error = Obj1.getString("Error");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailPage.this, error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    default:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DetailPage.this, "Please try again later!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                error1 = e.getMessage();
            } catch (NullPointerException e) {
                // TODO: handle exception
                error1 = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            collapsingToolbar.setTitle("");
            collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
            collapsingToolbar.setNestedScrollingEnabled(false);

            Typeface tf_opensense_regular = Typeface.createFromAsset(DetailPage.this.getAssets(), "fonts/OpenSans-Regular.ttf");

            //initialize
            AppBarLayout appBarLayout = findViewById(R.id.appbar);

            final TextView txt_title = findViewById(R.id.txt_title);

            final RatingBar rb = findViewById(R.id.rate);
            final TextView txt_ratenumber = findViewById(R.id.txt_ratenumber);
            final ImageView imageview = findViewById(R.id.img_detail);
            //TextView txt_description = findViewById(R.id.txt_description);

            //setting typeface

            txt_title.setTypeface(tf_opensense_regular);


            //setting data
            if (detaillist.size() > 0) {
                txt_title.setText((detaillist.get(0).getName()));
                rb.setRating(Float.parseFloat(detaillist.get(0).getRatting()));
                rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        // Auto-generated
                        Log.d("rate", "" + rating);
                    }
                });

                txt_ratenumber.setText("" + Float.parseFloat(detaillist.get(0).getRatting()));
                txt_ratenumber.setTypeface(tf_opensense_regular);
                final String image = detaillist.get(0).getPhoto().replace(" ", "%20");
                Picasso.get()
                        .load(getString(R.string.link) + getString(R.string.imagepath) + image)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .resize(640, 360)
                        .into(imageview);
                //Log.e("Image", getString(R.string.link) + getString(R.string.imagepath) + image);
                appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if (collapsingToolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapsingToolbar)) {
                            //      btn_share.animate().alpha(1).setDuration(600);
                            txt_ratenumber.animate().alpha(0).setDuration(600);
                            rb.animate().alpha(0).setDuration(600);
                            //       txt_name.animate().alpha(0).setDuration(600);
                        } else {
                            txt_ratenumber.animate().alpha(1).setDuration(600);
                            rb.animate().alpha(1).setDuration(600);
                            //      txt_name.animate().alpha(1).setDuration(600);
                            //   btn_share.animate().alpha(0).setDuration(600);
                            txt_title.animate().alpha(1).setDuration(600);
                            collapsingToolbar.setTitle("");
                        }
                    }
                });
                try {
                    double numbar = roundMyData(Double.parseDouble(distancenew), 1);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }


            }
            //adding map support


            //listeners after getting detail

            Button btn_call = findViewById(R.id.btn_call);
            btn_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String call = detaillist.get(0).getPhone();
                    String uri = "tel:" + call;
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse(uri));
                    startActivity(i);
                }
            });


            btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    AlertDialog alertDialog = new AlertDialog.Builder(DetailPage.this, R.style.MyDialogTheme).create();
                    alertDialog.setTitle(getString(R.string.share));
                    alertDialog.setMessage(getString(R.string.sharetitle));
                    // share on gmail,hike etc
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.more),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        String Name = detaillist.get(0).getName() + "\n";
                                        String address = "Dirección:" + detaillist.get(0).getAddress() + "\n";
                                        String Mobile = "Teléfono:" + detaillist.get(0).getPhone() + "\n";

                                        String sharee = Name + address + Mobile;
                                        Log.e("Dirección", address);
                                        Uri bmpUri = getLocalBitmapUri(imageview);
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("text/plain");
                                        share.setType("image/*");
                                        share.setType("image/jpeg");
                                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                        share.putExtra(Intent.EXTRA_SUBJECT, "Restaurant");
                                        share.putExtra("android.intent.extra.TEXT", sharee);
//                                        share.putExtra("android.intent.extra.TEXT", address);
                                        share.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                        startActivity(Intent.createChooser(share, "Compartir \uD83E\uDDE1"));
                                    } catch (NullPointerException e) {
                                        // TODO: handle exception
                                    }
                                }
                            });

                    // share on whatsapp

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.whatsapp),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) {

                                    String Name = "*" + detaillist.get(0).getName() + "*\n";
                                    String address = "*Dirección:* " + detaillist.get(0).getAddress() + "\n";
                                    String Mobile = "*Teléfono:* " + detaillist.get(0).getPhone() + "\n" + "\n";
                                    String Link = "Encuentra los productos de *" + detaillist.get(0).getName() + "* en _*Faster - Delivery App*_, disponible en Google Play Store https://play.google.com/store/apps/details?id=faster.com.ec" + "\n";

                                    String sharee = Name + address + Mobile + Link;
                                    Uri bmpUri = getLocalBitmapUri(imageview);
                                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                                    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, sharee);
                                    whatsappIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                    whatsappIntent.setType("text/plain");
                                    whatsappIntent.setType("image/*");
                                    whatsappIntent.setPackage("com.whatsapp");
                                    //startActivity(whatsappIntent);
                                    try {
                                        startActivity(whatsappIntent);
                                        // Detailpage.this.startActivity(whatsappIntent);

                                    } catch (android.content.ActivityNotFoundException ex) {
                                        Toast.makeText(DetailPage.this, "No tienes instalado WhatsApp.", Toast.LENGTH_LONG)
                                                .show();
                                    }

                                }
                            });

                    // share on facebook

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.facebook),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    //fb share integrate
                                    String imageurl = getString(R.string.link) + getString(R.string.imagepath) + image;
                                    String url1 = "https://www.google.com";
                                    String dist = detaillist.get(0).getDesc();
                                    String Name = detaillist.get(0).getName() + "\n";
                                    String address = "Dirección:" + detaillist.get(0).getAddress() + "\n";
                                    String Mobile = "Teléfono:" + detaillist.get(0).getPhone() + "\n";

                                    String urlToShare = dist + Name + address + Mobile;

                                    ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                                            .putString("fb:app_id", getString(R.string.facebook_app_id))
                                            .putString("og:type", "article")
                                            .putString("og:url", url1)
                                            .putString("og:title", detaillist.get(0).getName())
                                            .putString("og:image", imageurl)
                                            .putString("og:description", urlToShare).build();
                                    // Create an action
                                    ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                                            .setActionType("news.publishes")
                                            .putObject("article", object)
                                            .build();

                                    // Create the content
                                    ShareOpenGraphContent contentn = new ShareOpenGraphContent.Builder()
                                            .setPreviewPropertyName("article").setAction(action)
                                            .build();

                                    Log.e("check", object.getBundle().toString() + " " + action.getBundle().toString());
                                    shareDialog.show(contentn, ShareDialog.Mode.AUTOMATIC);

                                }
                            });
                    alertDialog.show();
                }
            });

            //checking Favourite
            new getfavlist().execute();
        }

    }

    private void getdetailforNearMe() {
        // TODO Auto-generated method stub
        URL hp = null;
        try {
            reviewlist.clear();

            hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "getrestaurant_review.php?res_id=" + res_id);

            Log.d("URLrev", "" + hp);
            URLConnection hpCon = hp.openConnection();
            hpCon.connect();
            InputStream input = hpCon.getInputStream();
            Log.d("input", "" + input);
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            String x = "";
            x = r.readLine();
            StringBuilder total = new StringBuilder();

            while (x != null) {
                total.append(x);
                x = r.readLine();
            }
            Log.e("URL", "" + total);
            JSONArray jsonArray = new JSONArray(total.toString());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.getString("status").equals("Success")) {
                JSONArray data = jsonObject.getJSONArray("Reviews");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject rev_detail = data.getJSONObject(i);
                    reviewgetset temp = new reviewgetset();
                    temp.setId(rev_detail.getString("id"));
                    temp.setUsername(rev_detail.getString("username"));
                    temp.setImage(rev_detail.getString("image"));
                    temp.setReview_text(rev_detail.getString("review_text"));
                    temp.setRatting(rev_detail.getString("ratting"));
                    Log.e("id",data.toString());
                    reviewlist.add(temp);
                }
            }

        } catch (JSONException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Error = e.getMessage();
        } catch (NullPointerException e) {
            // TODO: handle exception
            Error = e.getMessage();
        }
    }

    class getRateDetail extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            URL hp = null;
            try {
                hp = new URL(getString(R.string.link) + getString(R.string.servicepath) + "restaurant_review_restaurant.php?" + "res_id=" + res_id + "&ratting=" + userrate.replace(".0", "") + "&user_id=" + user_id+ "&review_text=" + usercomment);
                Log.e("userurl", "" + hp);
                URLConnection hpCon = hp.openConnection();
                hpCon.connect();
                InputStream input = hpCon.getInputStream();
                Log.d("input", "" + input);
                BufferedReader r = new BufferedReader(new InputStreamReader(input));
                String x = "";
                // x = r.readLine();
                StringBuilder total = new StringBuilder();
                while (x != null) {
                    total.append(x);
                    x = r.readLine();
                }
                // Log.d("totalid", "" + total);
                JSONArray response = new JSONArray(total.toString());
                JSONObject stat = response.getJSONObject(0);
                Log.d("totalid", "" + stat);
                revmsg = stat.getString("success");
                message = stat.getString("message");
                //Log.d("totalid",   revmsg);
            } catch (JSONException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (revmsg.equals("1")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailPage.this);
                builder.setMessage("Se ha guardado su calificación con éxito")
                        .setTitle("Gracias");
                builder.setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                new getfavlist().execute();
                                edt_review.setEnabled(false);
                                btn_submit.setText("Edita tu calificación");
                                rate.setEnabled(false);

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();


            } else {
                Toast.makeText(DetailPage.this, message, Toast.LENGTH_LONG).show();
            }



        }

    }


    private class getfavlist extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            try {
                progressDialog = new ProgressDialog(DetailPage.this);
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setCancelable(true);
                progressDialog.show();
            } catch(Exception e) {
                Log.e("Error_progressDialog", Objects.requireNonNull(e.getMessage()));
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            favlist.clear();
            getdetailforNearMe();

            sqliteHelper = new sqliteHelper(DetailPage.this);
            SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();
            try {
                cur = db1.rawQuery("select * from Favourite where restaurent_id=" + detaillist.get(0).getId() + ";", null);

                Log.e("alreadyfav", "select * numberOfRecords Favourite where restaurent_id=" + detaillist.get(0).getId() + ";");
                if (cur.getCount() != 0) {
                    if (cur.moveToFirst()) {
                        do {
                            favgetset obj = new favgetset();
                            restaurent_id = cur.getString(cur.getColumnIndex("restaurent_id"));
                            name = cur.getString(cur.getColumnIndex("name"));
                            category = cur.getString(cur.getColumnIndex("category"));
                            timing = cur.getString(cur.getColumnIndex("timing"));
                            rating = cur.getString(cur.getColumnIndex("rating"));
                            distance = cur.getString(cur.getColumnIndex("distance"));
                            image = cur.getString(cur.getColumnIndex("image"));
                            address = cur.getString(cur.getColumnIndex("address"));
                            obj.setName(name);
                            obj.setCategory((category));
                            obj.setTiming(timing);
                            obj.setRating(rating);
                            obj.setDistance(distance);
                            obj.setImage(image);
                            obj.setId(restaurent_id);
                            obj.setAddress(address);
                            favlist.add(obj);
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

            if (favlist.size() == 0) {
                btn_fav.setVisibility(View.VISIBLE);
                btn_fav1.setVisibility(View.INVISIBLE);
            } else {

                btn_fav1.setVisibility(View.VISIBLE);
                btn_fav.setVisibility(View.INVISIBLE);
            }


            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                if (Error != null) {
                    Snackbar.make(findViewById(R.id.coordinator), R.string.no_review, Snackbar.LENGTH_LONG).show();
                } else {
                    RecyclerView listview = findViewById(R.id.listview);
                    reviewadapter adapter = new reviewadapter(DetailPage.this, reviewlist);
                    RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
                    listview.setLayoutManager(mLayoutManager);
                    listview.setItemAnimator(new DefaultItemAnimator());
                    listview.setAdapter(adapter);
                    listview.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), listview, new RecyclerTouchListener.ClickListener() {

                        @Override
                        public void onClick(View view, int position) {

                        }

                        @Override
                        public void onLongClick(View view, int position) {

                        }
                    }));

                }
            }

        }
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateRating() {

        try {
            usercomment = edt_review.getText().toString().replace(" ", "%20");
            userrate = String.valueOf(rate.getRating());
            if (usercomment.equals(null)) {
                usercomment = "";
            }
        } catch (NullPointerException e) {
            // TODO: handle exception
        }

        if (usercomment.equals("") || userrate.equals("0.0")) {
            input_layout_review.setError("Por favor califique a la tienda");
            requestFocus(edt_review);
            return false;
        } else {
            input_layout_review.setErrorEnabled(false);
        }
        return true;
    }
}
