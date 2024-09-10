package faster.com.ec.fooddelivery;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import faster.com.ec.R;
import faster.com.ec.helper.SessionManager;
import faster.com.ec.utils.Config;
import faster.com.ec.utils.check_sesion;

public class RatingDelivery extends AppCompatActivity {
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private String deliveryboy_name;
    private String  deliveryboy_vehicle_type;
    private String  deliveryboy_vehicle_no;
    private String delivery_phone;
    private String order_price;
    private String delivery_price;
    private String total;
    private String image;
    private String orderId;
    private String userId;
    private String regId;
    private String userrate;
    private String usercomment;
    private String ally_name;

    private TextView txt_name;
    private TextView txt_address;
    private TextView txt_contact;
    private TextView txt_order_price;
    private TextView txt_delivery_price;
    private TextView txt_total;
    private TextView txt_ratenumber;
    private TextView txt_name2;
    private ImageView rest_image;
    ProgressDialog progressDialog;
    private SessionManager session;
    private Button btn_rating_deliveryboy;
    private RatingBar rate;
    private EditText edt_review;
    private TextInputLayout input_layout_review;
    private RelativeLayout rel_call;
    private RelativeLayout rel_order;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_rating_delivery);
        changeStatsBarColor(RatingDelivery.this);
        gettingIntents();
        initialization();
        displayFirebaseRegId();
        getSharedPref();
        setClickListener();
    }
    private void gettingIntents() {
        Intent i = getIntent();

        SharedPreferences prefs1 = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        deliveryboy_name = prefs1.getString("deliveryboy_name", null);
        deliveryboy_vehicle_type = prefs1.getString("deliveryboy_vehicle_type", null);
        deliveryboy_vehicle_no = prefs1.getString("deliveryboy_vehicle_no", null);
        delivery_phone= prefs1.getString("deliveryboy_phone", null);
        order_price= prefs1.getString("order_price", null);
        delivery_price= prefs1.getString("delivery_price", null);
        total= prefs1.getString("total", null);
        image= prefs1.getString("image", null);
        orderId= prefs1.getString("orderId", null);
        ally_name= prefs1.getString("ally_name", null);

    }

    private void initialization() {
        txt_name = findViewById(R.id.txt_name);
        txt_address= findViewById(R.id.txt_address);
        txt_contact= findViewById(R.id.txt_contact);
        txt_order_price= findViewById(R.id.txt_order_price);
        txt_delivery_price= findViewById(R.id.txt_delivery_price);
        txt_total= findViewById(R.id.txt_total);
        rest_image= findViewById(R.id.image);
        txt_name2= findViewById(R.id. txt_name2);
        btn_rating_deliveryboy= findViewById(R.id.btn_rating_deliveryboy);
        rate= findViewById(R.id.rate1234);
        txt_ratenumber= findViewById(R.id.txt_ratenumber1);
        edt_review = findViewById(R.id.edt_review);
        input_layout_review= findViewById(R.id.input_layout_review);
        rel_call= findViewById(R.id.rel_call);
        rel_order= findViewById(R.id.rel_order);

        session = new SessionManager(getApplicationContext());

        //seteo variables

        txt_name.setText(deliveryboy_name);
        txt_address.setText(deliveryboy_vehicle_type);
        txt_contact.setText("Placa: "+deliveryboy_vehicle_no);
        Picasso.get().load(getResources().getString(R.string.link) + "uploads/deliveryboys/" + image)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .resize(200, 200)
                .into(rest_image);
        txt_name2.setText("Repartidor de "+ally_name);
        txt_order_price.setText("$ "+order_price.replace(".",","));
        txt_delivery_price.setText("$ "+delivery_price.replace(".",","));
        txt_total.setText("$ "+total.replace(".",","));
    }

    private void setClickListener() {
        btn_rating_deliveryboy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateRating()) {
                    return;
                } else {
                    new user_check_restaurant_location().execute();
                }

            }
        });

        rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                // Auto-generated
                Log.e("rate", "" + rating);
                userrate = String.valueOf(rate.getRating());
                txt_ratenumber.setText(userrate);
            }
        });

        rel_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + delivery_phone;
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse(uri));
                startActivity(i);
            }
        });

        rel_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RatingDelivery.this, OrderSpecification.class);
                i.putExtra("OrderId", orderId);
                startActivity(i);
            }
        });
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }
    private void getSharedPref() {
        SharedPreferences sp = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userId = sp.getString("userid", "");

    }

    class user_check_restaurant_location extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(RatingDelivery.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Buscando tiendas cercanas");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String hp;
            hp = getString(R.string.link) + getString(R.string.servicepath) + "order_ratting_deliveryboy.php?";

            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.e("PlaceOrder5", "ResponseFromServer " + response+"......... user id: "+userId);
                    try {
                        JSONObject responsedat = new JSONObject(response);
                        String txt_success = responsedat.getString("success");
                        String txt_message = responsedat.getString("message");

                        if (txt_success.equals("1")) {

                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString("orderId", null);
                            editor.apply();

                            Intent i = new Intent(RatingDelivery.this, Splash.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();


                        }
                        else  if (txt_success.equals("200") ||txt_success.equals("201") ||txt_success.equals("202")) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(RatingDelivery.this, R.style.MyDialogTheme);
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
                            } catch(Exception e) {
                                Log.e("Error_alert11", e.getMessage());
                            }

                        }else if (txt_success.equals("4")||txt_success.equals("5"))  {

                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString("phonenumber",null);
                            editor.apply();

                            SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0).edit();
                            editor2.putString("regId",null);
                            editor2.apply();

                            // session manager
                            session.setLogin(false);

                            Intent i = new Intent(RatingDelivery.this, Login4.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();

                        }
                        else{
                            check_sesion se=new check_sesion();
                            se.validate_sesion(RatingDelivery.this,txt_success,txt_message);
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
                            if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(RatingDelivery.this, R.style.MyDialogTheme);
                                builder1.setTitle("Información");
                                builder1.setCancelable(false);
                                builder1.setMessage("Por favor verifica tu conexión a Internet");
                                builder1.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        new user_check_restaurant_location().execute();
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


                            } else
                                Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                        }

                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id", userId);
                    params.put("order_id", orderId);
                    params.put("ratting_deliveryboy",  userrate.replace(".0", ""));
                    params.put("review_deliveryboy", usercomment.replace("%20", " "));
                    params.put("code", getString(R.string.version_app));
                    params.put("operative_system",  getString(R.string.sistema_operativo));
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
            RequestQueue requestQueue = Volley.newRequestQueue(RatingDelivery.this);
            requestQueue.add(postRequest);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
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

        if ( userrate.equals("0.0")) {
            input_layout_review.setError("Por favor califique al repartidor");
            requestFocus(edt_review);
            return false;
        } else {
            input_layout_review.setErrorEnabled(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(RatingDelivery.this, R.style.MyDialogTheme);
        builder1.setTitle("Calificación del Repartidor");
        builder1.setMessage("Nos encantaría conocer la experiencia obtenida con el repartidor, ¿Deseas calificar al repartidor?");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Calificar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder1.setNegativeButton("Salir sin calificar", new DialogInterface.OnClickListener() {
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

    }

}

