package faster.com.ec.fooddelivery;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import faster.com.ec.R;
import faster.com.ec.deliveryData;
import faster.com.ec.helper.CountryData;
import faster.com.ec.utils.Config;

public class EditLocation extends AppCompatActivity {
    private static final String MY_PREFS_NAME = "Fooddelivery";

    private ProgressDialog progressDialog;

    private Spinner spinner;

    private EditText input_address;
    private TextInputLayout inputLayoutAddress;
    private EditText input_flat_apartment;
    private EditText input_delivery_note;
    private TextInputLayout inputLayoutNote;
    private TextInputLayout inputLayoutPrefix;
    private TextInputLayout inputAlias;
    private EditText input_prefix;
    private TextInputLayout inputLayoutNumber;
    private EditText input_number;
    private EditText input_alias;
    private Button keepButton;

    private String delivery_id;
    private String delivery_user_id;
    private String delivery_latitude;
    private String delivery_longitude;
    private String delivery_address;
    private String delivery_department_number;
    private String delivery_note;
    private String delivery_phone;
    private String delivery_alias;
    private String regId;
    private String error_message;
    private Integer NumRandom;
    private CharSequence CurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editlocation);
        changeStatsBarColor(EditLocation.this);
        initView();

        displayFirebaseRegId();
        setClickListener();
        random();

        if(getIntent().getStringExtra("phonenumber")!=null){
            input_number.setText(getIntent().getStringExtra("phonenumber"));
        }
    }

    private void initView() {
        input_address = findViewById(R.id.address);
        inputLayoutAddress = findViewById(R.id.rel_address);
        input_flat_apartment = findViewById(R.id.flat_apartment);
        input_delivery_note = findViewById(R.id.delivery_note);
        inputLayoutNote = findViewById(R.id.rel_delivery_note);
        input_prefix = findViewById(R.id.editTextPrefijo);
        inputLayoutPrefix = findViewById(R.id.rel_prefix);
        input_number = findViewById(R.id.editTextNumero);
        inputLayoutNumber = findViewById(R.id.rel_number);
        input_alias = findViewById(R.id.alias);
        inputAlias = findViewById(R.id.rel_alias);

        Intent intent = getIntent();

        delivery_id = (String)intent.getStringExtra("delivery_id");
        delivery_user_id = (String)intent.getStringExtra("delivery_user_id");
        delivery_latitude = (String)intent.getStringExtra("delivery_lat");
        delivery_longitude = (String)intent.getStringExtra("delivery_lon");
        delivery_address = (String)intent.getStringExtra("delivery_address");
        delivery_alias = (String)intent.getStringExtra("delivery_alias");
        delivery_phone = (String)intent.getStringExtra("delivery_phone");
        delivery_note = (String)intent.getStringExtra("delivery_note");
        delivery_department_number = (String)intent.getStringExtra("delivery_department_number");

        input_address.setText(delivery_address);
        input_alias.setText(delivery_alias);
        input_delivery_note.setText(delivery_note);
        input_flat_apartment.setText(delivery_department_number);
        input_number.setText(delivery_phone.trim().length() > 4 ? delivery_phone.substring(4) : "");

        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                //String selectedItem = parent.getItemAtPosition(position).toString();
                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
                input_prefix.setText(code);
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        keepButton = findViewById(R.id.btn_keep);
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);
    }

    private void message3(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(EditLocation.this, R.style.MyDialogTheme);
        delivery_phone = input_prefix.getText().toString()+input_number.getText().toString();
        //builder1.setTitle("Cuenta Bloqueada");
        builder1.setMessage(Html.fromHtml("Vamos a verificar el número celular"+
                "<b> "+delivery_phone+"</b> <br><br>¿Es correcto o desea editar?"))
                .setCancelable(false)
                .setPositiveButton("Verificar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent intent = new Intent(EditLocation.this, Verify_phone.class);
                        intent.putExtra("phonenumber", delivery_phone);
                        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
        builder1.setNegativeButton("Editar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();

            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    private void random(){
        final int min = 1;
        final int max = 999;
        NumRandom = new Random().nextInt((max - min) + 1) + min;
        Date d = new Date();
        CurrentDate = DateFormat.format("mmss", d.getTime());
    }

    private void setClickListener() {
        keepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateAddress() && validateNote() && validateAlias() && validatePrefix() && validateNumber() ){
                    delivery_address = input_address.getText().toString();
                    delivery_department_number = input_flat_apartment.getText().toString();
                    delivery_note = input_delivery_note.getText().toString();
                    delivery_phone = input_prefix.getText().toString()+input_number.getText().toString();
                    delivery_alias = input_alias.getText().toString();
                    new PostData().execute();
                }

            }
        });
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validateAddress() {
        if (input_address.getText().toString().trim().length()<4) {
            inputLayoutAddress.setError(getString(R.string.err_msg_address));
            requestFocus(input_address);
            return false;
        } else {
            inputLayoutAddress.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateNote() {
        if (input_delivery_note.getText().toString().trim().length()<2) {
            inputLayoutNote.setError(getString(R.string.err_msg_note));
            requestFocus(input_delivery_note);
            return false;
        } else {
            inputLayoutNote.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateAlias() {
        if (input_alias.getText().toString().trim().length()<2) {
            inputAlias.setError(getString(R.string.err_msg_alias));
            requestFocus(input_alias);
            return false;
        } else {
            inputAlias.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePrefix() {
        if (input_prefix.getText().toString().trim().length()<4) {
            inputLayoutPrefix.setError(getString(R.string.err_msg_prefijo));
            requestFocus(input_prefix);
            return false;
        } else {
            inputLayoutPrefix.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateNumber() {
        String textn = input_number.getText().toString();

        if (textn.trim().length()==9 ) {
            if(textn.substring(0, 1).equals("9")){
                inputLayoutNumber.setErrorEnabled(false);
                return true;
            }
            else{
                inputLayoutNumber.setError(getString(R.string.txt_phone_required));
                requestFocus(input_number);
                return false;
            }

        } else  if (textn.trim().length()==10 )  {
            if(textn.substring(0, 1).equals("0")){
                inputLayoutNumber.setErrorEnabled(false);
                input_number.setText(textn.substring(1, 10));
                return true;
            }
            else{
                inputLayoutNumber.setError(getString(R.string.txt_phone_required));
                requestFocus(input_number);
                return false;
            }
        }
        else {
            inputLayoutNumber.setError(getString(R.string.txt_phone_required));
            requestFocus(input_number);
            return false;
        }
    }

    class PostData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditLocation.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Guardando nueva dirección");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            String  hp = getString(R.string.link) + getString(R.string.servicepath) + (delivery_id.isEmpty()? "add" : "update") + "delivery_address.php?";
            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // response
                    Log.e("Add/Update Address", "ResponseFromServer " + response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject Obj = jsonArray.getJSONObject(0);

                        String status = Obj.getString("status");

                        if (!status.equals("Success")) {
                            error_message = Obj.getString("error_message");
                            //Log.e("statusXXX", error_message);
                        }

                        if (status.equals("Success")) {
                            if (delivery_id == deliveryData.delivery_id) {
                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString("delivery_id", delivery_id);
                                editor.putString("delivery_user_id", delivery_user_id);
                                editor.putString("delivery_lat", delivery_latitude);
                                editor.putString("delivery_lon", delivery_longitude);
                                editor.putString("delivery_address", delivery_address);
                                editor.putString("delivery_alias", delivery_alias);
                                editor.putString("delivery_phone", delivery_phone);
                                editor.putString("delivery_note", delivery_note);
                                editor.putString("delivery_department_number", delivery_department_number);
                                editor.apply();
                                deliveryData.delivery_id = delivery_id;
                                deliveryData.delivery_user_id = delivery_user_id;
                                deliveryData.delivery_lat= delivery_latitude;
                                deliveryData.delivery_lon = delivery_longitude;
                                deliveryData.delivery_address = delivery_address;
                                deliveryData.delivery_alias = delivery_alias;
                                deliveryData.delivery_phone = delivery_phone;
                                deliveryData.delivery_note = delivery_note;
                                deliveryData.delivery_department_number = delivery_department_number;

                            }
                            Intent intent = new Intent(EditLocation.this, DeliveryAddress.class);
                            startActivity(intent);
                            finish();
                        } else if (status.equals("Failed")) {
                            progressDialog.hide();
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(EditLocation.this, R.style.MyDialogTheme);
                            builder1.setTitle("Información");
                            builder1.setCancelable(false);
                            builder1.setMessage(error_message);
                            builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    onBackPressed();
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
                        }

                    }
            ) {

                @Override
                protected Map<String, String> getParams() {
                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                    Map<String, String> params = new HashMap<>();
                    params.put("id", delivery_id);
                    params.put("user_id", delivery_user_id);
                    params.put("latitude", delivery_latitude);
                    params.put("longitude", delivery_longitude);
                    params.put("address", delivery_address);
                    params.put("alias", delivery_alias);
                    params.put("phone", delivery_phone);
                    params.put("delivery_note", delivery_note);
                    params.put("department_number", delivery_department_number);
                    params.put("token", delivery_user_id + NumRandom + CurrentDate);
                    params.put("code", getString(R.string.version_app));
                    params.put("operative_system", getString(R.string.sistema_operativo));
                    //Log.e("~~~~~~~~~~~", params.toString());
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
            RequestQueue requestQueue = Volley.newRequestQueue(EditLocation.this);
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

}
