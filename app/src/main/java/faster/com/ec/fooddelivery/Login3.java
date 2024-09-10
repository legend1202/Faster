package faster.com.ec.fooddelivery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import faster.com.ec.R;
import faster.com.ec.helper.CountryData;

public class Login3 extends AppCompatActivity {
    private Spinner spinner;

    private TextInputLayout inputLayoutPrefijo, inputLayoutNumero;
    private EditText editPrefijo;
    private EditText editNumero;
    private Button Siguiente3, ButtonWhatsApp;
    private String phoneNumber;
    //  private final int PERMISSION_REQUEST_CODE = 1001;
    // private final int PERMISSION_REQUEST_CODE1 = 10011;
    // private final String[] permission_location = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    // private final String[] permission_location1 = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    // private int amagador = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login3);

        initializations();
        setClickListener();
        setClickWhatsApp();

        //if (checkPermission()) { }
        //else { requestPermission(); }

        if(getIntent().getStringExtra("phonenumber")!=null){
            editNumero.setText(getIntent().getStringExtra("phonenumber"));
        }
    }

    /*private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }*/

    //private void requestPermission() {
    //    ActivityCompat.requestPermissions(this, permission_location, PERMISSION_REQUEST_CODE);
    //   ActivityCompat.requestPermissions(this, permission_location1, PERMISSION_REQUEST_CODE1);
    //}

    private void initializations() {
        editPrefijo = findViewById(R.id.editTextPrefijo);
        editNumero = findViewById(R.id.editTextNumero);
        inputLayoutPrefijo = findViewById(R.id.input_layout_prefijo);
        inputLayoutNumero = findViewById(R.id.input_layout_numero);

        Siguiente3 = findViewById(R.id.Siguiente4);
        ButtonWhatsApp = findViewById(R.id.Siguiente5);

        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        String text = spinner.getSelectedItem().toString();


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                //String selectedItem = parent.getItemAtPosition(position).toString();
                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
                editPrefijo.setText(code);
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent){

            }
        });
    }

    private void mensajel3(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(Login3.this, R.style.MyDialogTheme);
        phoneNumber = editPrefijo.getText().toString()+editNumero.getText().toString();
        //builder1.setTitle("Cuenta Bloqueada");
        builder1.setMessage(Html.fromHtml("Vamos a verificar el número celular"+
                "<b>"+phoneNumber+"</b><br><br>¿Es correcto o desea editar?"))
                .setCancelable(false)
                .setPositiveButton("Verificar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent intent = new Intent(Login3.this, Verify_phone.class);
                        intent.putExtra("phonenumber", phoneNumber);
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

    private void mensajeWhatsApp(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(Login3.this, R.style.MyDialogTheme);
        phoneNumber = editPrefijo.getText().toString()+editNumero.getText().toString();
        //builder1.setTitle("Cuenta Bloqueada");
        builder1.setMessage(Html.fromHtml("Vamos a verificar el número celular "+
                        "<b>"+phoneNumber+"</b><br><br>¿Es correcto o desea editar?"))
                .setCancelable(false)
                .setPositiveButton("Verificar", (dialog, id) -> {
                    new postingData().execute();
                    dialog.cancel();
                    Intent intent = new Intent(Login3.this, VerifyPhoneWhatsApp.class);
                    intent.putExtra("phonenumber", phoneNumber);
                    intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
        builder1.setNegativeButton("Editar", (dialog, id) -> dialog.cancel());
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void setClickListener() {
        Siguiente3.setOnClickListener(v -> {
            if(validatePrefijo() && validateNumero()){

            }
        });
    }

    private void setClickWhatsApp() {
        ButtonWhatsApp.setOnClickListener(v -> {
            if(validateNumeroWhatsApp()){

            }
        });
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validatePrefijo() {
        if (editPrefijo.getText().toString().trim().length()<1) {
            inputLayoutPrefijo.setError(getString(R.string.err_msg_prefijo));
            requestFocus(editPrefijo);
            return false;
        } else {
            inputLayoutPrefijo.setErrorEnabled(false);
        }
        return true;
    }

    class postingData extends AsyncTask<Void, Void, Void> {
        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(DeliveryOrderDetail.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }*/

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            //Log.e("sourceFile", "" + orderNo);
            String hp = "";
            hp = getString(R.string.link) + getString(R.string.servicepath) + "verification_whatsapp.php?";

            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, response -> {
                try {
                    JSONObject responsedat = new JSONObject(response);
                    String txt_success = responsedat.getString("success");
                    String txt_message = responsedat.getString("message");

                    if (txt_success.equals("1")) {
                        Toast.makeText(getApplicationContext(), txt_message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), txt_message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }
            },
                error -> {
                    // error
                    Log.d("Error.Response", error.toString());
                    String message = null;
                    if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
                        Toast.makeText(getApplicationContext(), "Por favor revisa tu conexión a Internet", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), "Por el momento no podemos procesar tu solicitud", Toast.LENGTH_SHORT).show();
                }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("phoneNumber", phoneNumber);
                    params.put("code", getString(R.string.version_app));
                    params.put("operative_system", getString(R.string.sistema_operativo));
                    return params;
                }

                @Override
                public String getBodyContentType() {
                    return super.getBodyContentType();
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(Login3.this);
            requestQueue.add(postRequest);
            return null;
        }

    }

    private boolean validateNumero() {
        String textn=editNumero.getText().toString();

        if (textn.trim().length()==9) {
            if(textn.charAt(0) == '9'){
                inputLayoutNumero.setErrorEnabled(false);
                mensajel3();
            } else{
                inputLayoutNumero.setError(getString(R.string.txt_phone_required));
                requestFocus(editNumero);
                return false;
            }

        } else if (textn.trim().length()==10 )  {
            if(textn.charAt(0) == '0'){
                inputLayoutNumero.setErrorEnabled(false);
                editNumero.setText(textn.substring(1, 10));
                    mensajel3();
            } else{
                inputLayoutNumero.setError(getString(R.string.txt_phone_required));
                requestFocus(editNumero);
                return false;
            }
        } else {
            inputLayoutNumero.setError(getString(R.string.txt_phone_required));
            requestFocus(editNumero);
            return false;
        }
        return true;
    }

    private boolean validateNumeroWhatsApp() {
        String textn=editNumero.getText().toString();

        if (textn.trim().length()==9 ) {
            if(textn.charAt(0) == '9'){
                inputLayoutNumero.setErrorEnabled(false);
                mensajeWhatsApp();
            } else{
                inputLayoutNumero.setError(getString(R.string.txt_phone_required));
                requestFocus(editNumero);
                return false;
            }

        } else  if (textn.trim().length()==10 )  {
            if(textn.charAt(0) == '0'){
                inputLayoutNumero.setErrorEnabled(false);
                editNumero.setText(textn.substring(1, 10));
                mensajeWhatsApp();
            } else{
                inputLayoutNumero.setError(getString(R.string.txt_phone_required));
                requestFocus(editNumero);
                return false;
            }
        } else {
            inputLayoutNumero.setError(getString(R.string.txt_phone_required));
            requestFocus(editNumero);
            return false;
        }
        return true;
    }

}