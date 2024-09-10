package faster.com.ec.fooddelivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import faster.com.ec.R;
import faster.com.ec.helper.DatabaseHandler;
import faster.com.ec.utils.Config;

public class VerifyPhoneWhatsApp extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "Fooddelivery";
    private String verificationId;
    private Button SignIn;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private EditText editTextCode = null;
    private String regId;
    private DatabaseHandler db;
    private String phonenumber;
    private TextInputLayout  inputLayoutCode;
    private TextView txtvtitulo, txtvtexto, timeView, textviewtime;
    private int counter=0;
    private String code, otpStatus, otpRegistrationDate, otpPhone, otpCode;
    static CountDownTimer countDownTimer = null;

    Button send_otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_whatsapp);

        initializations();
        setClickListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {

        send_otp = findViewById(R.id.send_otp);

        send_otp.setOnClickListener(v -> {
            new GetDataAsyncTask().execute();
        });

    }

    private void initializations() {
        // mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        editTextCode = findViewById(R.id.editTextCode);
        inputLayoutCode = findViewById(R.id.input_layout_code);
        txtvtitulo = findViewById(R.id.txtvtitulo);
        txtvtexto = findViewById(R.id.txtvtexto);
        timeView = findViewById(R.id.textView4);
        textviewtime =findViewById(R.id.textView3);

        displayFirebaseRegId();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        counter = prefs.getInt("counter", 0);

        // create sqlite database
        // db = new DatabaseHandler(getApplicationContext());

        phonenumber = getIntent().getStringExtra("phonenumber");

        txtvtitulo.setText(Html.fromHtml("Verificar " + "<b><font color=#2abb9b> "+phonenumber+"</font></b>"));
        txtvtexto.setText(Html.fromHtml("Ingresa el código que enviamos a tu WhatsApp 👀."));
        // sendVerificationCode(phonenumber);
        textviewtime.setEnabled(false);
        textviewtime.setText("Reenviar código");
        reverseTimer(120, timeView);

    }
    public void reverseTimer(int Seconds,final TextView tv){
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(Seconds * 1000 + 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 120;
                tv.setText(String.format("%02d", seconds) + " segundos");
                // tv.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + " segundos");
            }

            public void onFinish() {
                if(counter == 0) {
                    tv.setText("");
                    textviewtime.setEnabled(true);
                    textviewtime.setTextColor(Color.parseColor("#ff9e00"));
                    counter++;
                }
            }
        }.start();

    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", "null");
    }

    class GetDataAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            String  hp = getString(R.string.link) + getString(R.string.servicepath) + "get_otp_whatsapp.php?";
            StringRequest postRequest = new StringRequest(Request.Method.POST, hp, response -> {
                // response
                // Log.e("PlaceOrder =====> ", "ResponseFromServer " + response);
                // positionList = new ArrayList<>();
                try {
                    //getting the whole json object from the response
                    JSONObject obj = new JSONObject(response);
                    String txt_success = obj.getString("success");
                    final String txt_message = obj.getString("message");
                    // otpStatus = obj.getString("assign");
                    // Log.e("ingresoforeground", orderAssign);
                    if (txt_success.equals("1")) {
                        JSONObject ja_order = obj.getJSONObject("otp");
                        otpStatus = ja_order.getString("status");
                        otpPhone = ja_order.getString("phone");
                        otpCode = ja_order.getString("otp");
                        otpRegistrationDate = ja_order.getString("registration_date");
                        code = editTextCode.getText().toString().trim();

                        if (!code.isEmpty()){
                            if (code.equals(otpCode)) {
                                //new GetDataAsyncTask().execute();
                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString("phonenumber", phonenumber);
                                editor.apply();

                                Intent intent = new Intent(VerifyPhoneWhatsApp.this, Loginphoto.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                Toast.makeText(VerifyPhoneWhatsApp.this, "Inicio de sesión con éxito.",
                                        Toast.LENGTH_LONG).show();
                                finish();
                                // Log.e("MENSAJE ==>", "INGRESA " + otpCode +" - "+ code);
                            } else if(code.length() < 6) {
                                requestFocus(editTextCode);
                                Toast.makeText(VerifyPhoneWhatsApp.this, "Faltan " + (6-code.length()) +" dígitos.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                requestFocus(editTextCode);
                                Toast.makeText(VerifyPhoneWhatsApp.this, "Ingrese el código de verificación valido.",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else if(code.length() < 6) {
                            requestFocus(editTextCode);
                            Toast.makeText(VerifyPhoneWhatsApp.this, "Faltan " + (6-code.length()) +" dígitos.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            },
                error -> {
                    // error
                    Log.d("Error.Response", error.toString());
                }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                    Map<String, String> params = new HashMap<>();
                    params.put("phoneNumber", phonenumber);
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
            RequestQueue requestQueue = Volley.newRequestQueue(VerifyPhoneWhatsApp.this);
            requestQueue.add(postRequest);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }

    private void setClickListener() {

        editTextCode.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                code = editTextCode.getText().toString().trim();

                if (code.isEmpty()){
                    inputLayoutCode.setError("Ingrese el código de 6 dígitos");
                }
                else if( code.length() < 6) {
                    inputLayoutCode.setError("Faltan "+(6-code.length())+" dígitos");
                    requestFocus(editTextCode);
                }
                else{
                    inputLayoutCode.setErrorEnabled(false);
                    // verifyCode(code);
                }
            }
        });

        textviewtime.setOnClickListener(v -> {

            Intent intent = new Intent(VerifyPhoneWhatsApp.this, Login3.class);
            intent.putExtra("phonenumber", phonenumber.replace("+593","0"));
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();
            counter++;
        });

    }

    @Override
    public void onBackPressed() {

    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}