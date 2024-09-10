package faster.com.ec.fooddelivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bumptech.glide.load.resource.transcode.DrawableBytesTranscoder;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import faster.com.ec.Getset.cartgetset;
import faster.com.ec.Getset.myOrderGetSet;
import faster.com.ec.R;
import faster.com.ec.deliveryData;
import faster.com.ec.helper.SessionManager;
import faster.com.ec.utils.Config;
import faster.com.ec.utils.check_sesion;
import faster.com.ec.utils.sqliteHelper;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;

public class OrderInfo extends AppCompatActivity {
    //Enviar a siguiente activity

    private static final String DB_TABLE = "cart";
    //Propios
    private String order_price;
    private String delivery_price;
    private String total_price;
    private String notes;
    private String allresid;
    private String dni;
    private String regId;
    private String userid;
    private String restaurant_name;
    private TextInputLayout txt_amount_pay;
    private EditText edt_amount_pay;
    private TextView txt_direccion;
    private TextView txt_total_order;
    private String status = "";

    private myOrderGetSet data;

    private TextInputLayout txt_dni_input;
    private TextInputLayout txt_nombre_input;
    private TextInputLayout txt_direccion_input;
    private TextInputLayout txt_telefono_input;
    private TextInputLayout txt_reference_input;

    private EditText edt_dni;
    private EditText edt_nombre;
    private EditText edt_direccion;
    private EditText edt_telefono;
    private JSONObject sendDetail;

    private boolean factura = false;


    private Button btn_orderinfo;
    private RadioGroup rg;
    private RadioGroup opciones_pago;

    private static final String MY_PREFS_NAME = "Fooddelivery";
    private static ArrayList<cartgetset> cartlist;
    private ProgressDialog progressDialog;
    private String latitudecur;
    private String longitudecur;
    private String detail_id;
    private String foodprice="0.0";
    private double total = 0.0;
    private String amount_pay;
    private String address;
    private String phonenumber;
    private String nombres;

    faster.com.ec.utils.sqliteHelper sqliteHelper;
    private SessionManager session;
    private boolean vuelto=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);
        changeStatsBarColor(OrderInfo.this);
        displayFirebaseRegId();
        getSharedPref();
        gettingIntent();
        initialization();
        addListenerOnButton();
        clickEvents();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //getting Data
        cartlist = new ArrayList<>();
        new getList().execute();

    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }

    private void getSharedPref() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        userid = prefs.getString("userid", "");
        phonenumber = prefs.getString("phonenumber", null);
        nombres = prefs.getString("Nombres", null);
        address = prefs.getString("direccion_envio", null);
    }

    private void gettingIntent() {
        Intent iv = getIntent();
        detail_id = iv.getStringExtra("detail_id");
        foodprice = iv.getStringExtra("order_price");
        notes = iv.getStringExtra("notes");
        delivery_price = iv.getStringExtra("deliveryprice");
        total_price = iv.getStringExtra("totalprice");
    }

    private void initialization() {
        txt_total_order = findViewById(R.id.txt_total_order);
        txt_amount_pay = findViewById(R.id.txt_amount_pay);
        edt_amount_pay = findViewById(R.id.edt_amount_pay);
        btn_orderinfo = findViewById(R.id.btn_orderinfo);
        txt_direccion = findViewById(R.id.txt_direccion);
        rg = findViewById(R.id.opciones_pago);
        opciones_pago = findViewById(R.id.opciones_fact);

        txt_dni_input = findViewById(R.id.txt_dni_input);
        txt_nombre_input = findViewById(R.id.txt_nombre_input);
        txt_telefono_input = findViewById(R.id.txt_telefono_input);

        edt_dni = findViewById(R.id.edt_dni);
        edt_nombre = findViewById(R.id.edt_nombre);
        edt_telefono = findViewById(R.id.edt_telefono);

        //txt_direccion.setText(address);
        txt_direccion.setText(deliveryData.delivery_alias +" - "+ deliveryData.delivery_address);
        txt_total_order.setText("Total a pagar "+getString(R.string.currency)+total_price);
        session = new SessionManager(getApplicationContext());
    }

    private void clickEvents() {
        btn_orderinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateAmount()){  return;}
                else if (!validateDni()) {
                    return;
                } else if (!validateNombres()) {
                    return;
                } else {
                    enviarpedido();
                    //btn_orderinfo.setEnabled(false);
                }
            }
        });
    }

    // este code es para eliminar los datos del db sqlite
    // items en 0
    private void deleteOrderToDatabase() {
        sqliteHelper = new sqliteHelper(OrderInfo.this);
        SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();
        db1.execSQL("delete from " + DB_TABLE);
        db1.close();
    }

    private void enviarpedido() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderInfo.this, R.style.MyDialogTheme);
        builder1.setTitle(Html.fromHtml("<font color='#ff9e00'>Confirmación</font>"));
        builder1.setCancelable(false);
        builder1.setMessage(Html.fromHtml("Tu pedido se entregará en la dirección seleccionada <font color='#ff9e00'><b>"
                +deliveryData.delivery_alias +" - "+ deliveryData.delivery_address
                +"</b></font>." + "<p>Te recordamos que una vez hecho el pedido no podrás cancelarlo. <font color='#2abb9b'><b>¿Estás seguro que deseas pedir?</b></font>"));
        builder1.setPositiveButton(Html.fromHtml("<font color=#48dd79>Si, estoy seguro</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                new PostData().execute();
            }

        });
        builder1.setNegativeButton(Html.fromHtml("<font color='#F27466'>Cancelar</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                //btn_orderinfo.setEnabled(true);
            }
        });

        AlertDialog alert11 = builder1.create();
        try {
            alert11.show();
        } catch (Exception e) {
            //
        }
    }


    class PostData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(OrderInfo.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Espere, estamos procesando el pedido 🤩");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // verfica aqui
            String  hp = getString(R.string.link) + getString(R.string.servicepath) + "bookorder.php?";
            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.e("PlaceOrder", "ResponseFromServer " + response);
                    try {
                        data = new myOrderGetSet();
                        JSONObject responsedat = new JSONObject(response);
                        String txt_success = responsedat.getString("success");
                        final String txt_message = responsedat.getString("message");

                        Log.d("res", txt_success);
                        if (txt_success.equals("1")){
                            status = txt_success;
                            JSONArray jA_category = responsedat.getJSONArray("order_details");
                            JSONObject cat_detail = jA_category.getJSONObject(0);

                            restaurant_name = cat_detail.getString("restaurant_name");
                            data.setResName(cat_detail.getString("restaurant_name"));
                            data.setResAddress(cat_detail.getString("restaurant_address"));
                            data.setOrder_total(cat_detail.getString("order_amount").replace(".",","));
                            data.setDelivery_price(cat_detail.getString("delivery_price").replace(".",","));
                            data.setOrder_dateTime(cat_detail.getString("order_date"));
                            data.setOrder_id(cat_detail.getString("order_id"));
                            data.setTotal_price(cat_detail.getString("total_general").replace(".",","));
                            deleteOrderToDatabase();

                        } else if (txt_success.equals("20") || txt_success.equals("-2") || txt_success.equals("0")) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderInfo.this, R.style.MyDialogTheme);
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
                                //
                            }
                        } else if (txt_success.equals("-3")) {

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderInfo.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage(txt_message);
                            builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                                    String distance = prefs.getString("distance", null);

                                    Intent iv = new Intent(OrderInfo.this, Cart.class);
                                    iv.putExtra("detail_id", detail_id);
                                    iv.putExtra("restaurent_name", "" + restaurant_name);
                                    iv.putExtra("distance", distance);
                                    startActivity(iv);
                                    finish();
                                }
                            });
                            AlertDialog alert11 = builder1.create();
                            try {
                                alert11.show();
                            } catch (Exception e) {
                                //
                            }

                        } else if (txt_success.equals("500")) {
                            Log.e("commit_rollback", txt_success);
                            Intent iv = new Intent(OrderInfo.this, MainActivity.class);
                            iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderInfo.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage(txt_message);
                            builder1.setPositiveButton("Aceptar", (dialog, id) -> {
                                startActivity(iv);
                                progressDialog.setCancelable(true);
                                progressDialog.hide();
                                dialog.cancel();
                                finish();
                            });
                            AlertDialog alert11 = builder1.create();
                            try {
                                alert11.show();
                            } catch (Exception e) {
                                //
                            }
                        }else if (txt_success.equals("250")) {
                            Log.e("duplicate_order", txt_success);
                            Intent iv = new Intent(OrderInfo.this, CompleteOrder.class);
                            iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(iv);
                            progressDialog.setCancelable(true);
                            progressDialog.hide();
                            finish();
                        } else if (txt_success.equals("4") || txt_success.equals("5")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderInfo.this, R.style.MyDialogTheme);
                                    builder1.setTitle("Información");
                                    builder1.setCancelable(false);
                                    builder1.setMessage(txt_message);
                                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {

                                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                            editor.putString("phonenumber", null);
                                            editor.apply();

                                            SharedPreferences.Editor editor2 = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0).edit();
                                            editor2.putString("regId", null);
                                            editor2.apply();

                                            // session manager
                                            session.setLogin(false);

                                            Intent iv = new Intent(OrderInfo.this, Login4.class);
                                            iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(iv);
                                            finish();

                                        }
                                    });
                                    AlertDialog alert11 = builder1.create();
                                    try {
                                        alert11.show();
                                    } catch (Exception e) {
                                        //
                                    }
                                }
                            });

                        } else {
                            check_sesion se = new check_sesion();
                            se.validate_sesion(OrderInfo.this, txt_success, txt_message);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updateUI();

                }
            },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        String message = null;
                        if (error instanceof TimeoutError || error instanceof NetworkError) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderInfo.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage("Estamos procesando tu pedido 🤩");
                            builder1.setPositiveButton(Html.fromHtml("<font color=#48dd79>Ver pedido 🧡</font>"), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    new PostData().execute();
                                }
                            });
                            /*builder1.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    finishAffinity();
                                }
                            });*/

                            AlertDialog alert11 = builder1.create();
                            try {
                                alert11.show();
                            } catch (Exception e) {
                                //
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                        }
                        //btn_orderinfo.setEnabled(true);
                    }

                }
            ) {

                @Override
                protected Map<String, String> getParams() {
                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                    Map<String, String> params = new HashMap<>();
                    params.put("total_general", total_price.replace(",","."));
                    params.put("order_dni", dni);
                    params.put("order_name", nombres);
                    params.put("order_phone", phonenumber);
                    params.put("amount_pay", amount_pay.replace(",","."));
                    params.put("user_id", userid);
                    params.put("res_id", detail_id);
                    params.put("order_note", notes);
                    params.put("address_id", deliveryData.delivery_id);
                    params.put("address", deliveryData.delivery_address);
                    params.put("lat", deliveryData.delivery_lat);
                    params.put("long", deliveryData.delivery_lon);
                    params.put("food_desc", sendDetail.toString());
                    params.put("notes", "");
                    params.put("delivery_price", delivery_price.replace(",","."));
                    params.put("total_price", foodprice.replace(",","."));
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
            RequestQueue requestQueue = Volley.newRequestQueue(OrderInfo.this);
            requestQueue.add(postRequest);
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                progressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.cancel();
            }
        }

    }


    private void updateUI() {
        Log.d("second", status);
        if (status.equals("1")) {
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("orderId", data.getOrder_id());
            editor.putString("key", "notification");
            editor.apply();

            Intent iv = new Intent(OrderInfo.this, CompleteOrder.class);
            iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            iv.putExtra("orderid", data.getOrder_id());
            iv.putExtra("restName", data.getResName());
            iv.putExtra("resAddress", data.getResAddress());
            iv.putExtra("ordertime", data.getOrder_dateTime());
            iv.putExtra("order_amount", data.getOrder_total());

            Log.d("Info Tag","order id prefs  " + data.getOrder_id());

            //IntentOrderInfo
            iv.putExtra("order_price", foodprice);
            iv.putExtra("delivery_price", delivery_price);
            iv.putExtra("total_price", total_price);

            saveOrderToDatabase();
            ClearOrder();
            startActivity(iv);
            finish();

        }
    }

    private void saveOrderToDatabase() {
        sqliteHelper = new sqliteHelper(OrderInfo.this);
        SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("restaurantAddress", data.getResAddress());
        values.put("restaurantName", data.getResName());
        values.put("orderAmount", data.getOrder_total());
        values.put("orderId", data.getOrder_id());
        values.put("orderTime", data.getOrder_dateTime());
        db1.insert("order_detail", null, values);
        db1.close();
    }
    private void ClearOrder() {
        sqliteHelper = new sqliteHelper(OrderInfo.this);
        SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("foodprice", "0");
        db1.update("cart", values, null,null);
        db1.close();
    }
    private boolean validateAmount() {
        if(!vuelto){
            amount_pay=total_price;
        }
        else{

            if (edt_amount_pay.getText().toString().trim().length()<1) {
                txt_amount_pay.setError("Ingrese");
                requestFocus(edt_amount_pay);
                return false;
            } else {

                if(Double.parseDouble(edt_amount_pay.getText().toString()) > Double.parseDouble(total_price.replace(",",".")))
                {
                    txt_amount_pay.setErrorEnabled(false);
                    amount_pay=String.format("%.2f", Double.parseDouble(edt_amount_pay.getText().toString()));
                    return true;
                }
                else {
                    txt_amount_pay.setError("Ingrese");
                    requestFocus(edt_amount_pay);
                    return false;
                }

            }}
        return true;
    }

    public void addListenerOnButton() {

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radio1:
                        txt_amount_pay.setVisibility(View.INVISIBLE);
                        vuelto=false;
                        break;
                    case R.id.radio2:
                        txt_amount_pay.setVisibility(View.VISIBLE);
                        edt_amount_pay.setFocusableInTouchMode(true);
                        edt_amount_pay.requestFocus();
                        vuelto=true;
                        break;
                }
            }
        });

        opciones_pago.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radiofc1:
                        txt_dni_input.setVisibility(View.INVISIBLE);
                        txt_nombre_input.setVisibility(View.INVISIBLE);
                        txt_telefono_input.setVisibility(View.INVISIBLE);
                        txt_dni_input.setFocusableInTouchMode(false);
                        factura = false;
                        break;
                    case R.id.radiofc2:
                        txt_dni_input.setVisibility(View.VISIBLE);
                        txt_nombre_input.setVisibility(View.VISIBLE);
                        txt_telefono_input.setVisibility(View.VISIBLE);
                        txt_dni_input.setFocusableInTouchMode(true);


                        edt_nombre.setText(nombres);
                        edt_telefono.setText(phonenumber.replace("+593", "0"));
                        edt_telefono.setKeyListener(null);

                        txt_dni_input.requestFocus();
                        factura = true;
                        break;
                }
            }
        });


        edt_amount_pay.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.equals("") ) txt_amount_pay.setErrorEnabled(false);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            public void afterTextChanged(Editable s) {

            }
        });

        edt_amount_pay.setFilters(new InputFilter[] {
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 3, afterDecimal = 2;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        String temp = edt_amount_pay.getText() + source.toString();

                        if (temp.equals(".")) {
                            return "0.";
                        }
                        else if (temp.toString().indexOf(".") == -1) {
                            // no decimal point placed yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }

                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });



    }
    //Valida Monto
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void initViews() {
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private class getList extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(OrderInfo.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Cargando");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            sqliteHelper = new sqliteHelper(OrderInfo.this);
            SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();

            try {
                Log.d("hoka", detail_id);
                Cursor cur = db1.rawQuery("select * from cart where foodprice >=1 and resid=" + detail_id + ";", null);
                Log.e("cartlistingplaceorder", "" + ("select * numberOfRecords cart where foodprice >=1 ;"));
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
                            Log.d("checking", ""+cartlist.size());
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
                // myDbHelper.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            JSONObject jo_itemId = new JSONObject();
            JSONObject jo_item;
            JSONObject jo_itemQuantity = new JSONObject();
            JSONObject jo_itemPrice = new JSONObject();
            JSONArray jo_detail = null;
            jo_detail = new JSONArray();


            StringBuilder totalOrderInfo = new StringBuilder();
            StringBuilder totalvalue = new StringBuilder();
            StringBuilder totalDetailOrder = new StringBuilder();


            for (int i = 0; i < cartlist.size(); i++) {
                jo_item = new JSONObject();
                allresid = cartlist.get(i).getResid();

                String tempItemName = cartlist.get(i).getFoodname();
                totalvalue.append(tempItemName).append(",");

                String menu_id = "ItemId=" + cartlist.get(i).getMenuid();

                String qty = "/ItemQty=" + cartlist.get(i).getFoodprice();


                float ans1 = Float.parseFloat(cartlist.get(i).getFoodprice());
                float ans2 = Float.valueOf(cartlist.get(i).getRestcurrency().replace("$ ", ""));
                float ans3 = ans1 * ans2;

                String sprice = "/ItemAmt=" + String.valueOf(ans3);


                String SingleOrderInfo = menu_id + qty + sprice;
                totalDetailOrder.append(" Item Name: ").append(tempItemName).append(" Item Price: ").append(ans1).append(" Item Quantity: ").append(ans2).append(" Total: ").append(ans3).append(";");

                totalOrderInfo.append(SingleOrderInfo).append(",");

                try {
                    jo_itemId.put("ItemId", cartlist.get(i).getMenuid());
                    jo_itemQuantity.put("ItemQty", cartlist.get(i).getFoodprice());
                    jo_itemPrice.put("ItemAmt", String.valueOf(ans3));
                    jo_item.put("ItemId", cartlist.get(i).getMenuid());
                    jo_item.put("ItemQty", cartlist.get(i).getFoodprice());
                    jo_item.put("ItemAmt", String.valueOf(ans3));
                    jo_detail.put(jo_item);


                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }

            sendDetail = new JSONObject();
            try {
                sendDetail.put("Order", jo_detail);
                Log.d("checking", sendDetail.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                progressDialog.cancel();
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.cancel();
            }

        }
    }

    private boolean validateDni() {
        if (!factura) {
            dni = "9999999999";
        }
        else{
            if (edt_dni.getText().toString().trim().length() < 4) {
                txt_dni_input.setError("Se requiere identificación");
                requestFocus(edt_dni);
                return false;
            } else {
                txt_dni_input.setErrorEnabled(false);
                dni = edt_dni.getText().toString();
            }}
        return true;
    }

    private boolean validateNombres() {
        if (!factura) {
            dni = "9999999999";
        }
        else{
            if (edt_nombre.getText().toString().trim().length() < 7) {
                txt_nombre_input.setError("Se requiere nombre y apellido");
                requestFocus(edt_nombre);
                return false;
            }
            else{
                txt_nombre_input.setErrorEnabled(false);
                nombres = edt_nombre.getText().toString();
            }}
        return true;
    }

}
