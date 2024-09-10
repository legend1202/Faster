package faster.com.ec.fooddelivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import faster.com.ec.R;
import faster.com.ec.helper.DatabaseHandler;

public class Verify_phone extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "Fooddelivery";
    private String verificationId;
    private Button SignIn;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private EditText editTextCode;
    private DatabaseHandler db;
    private String phonenumber;
    private TextInputLayout  inputLayoutCode;
    private TextView txtvtitulo, txtvtexto, timeView, textviewtime;
    private int counter=0;
    private String code;
    static CountDownTimer countDownTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        initializations();
        setClickListener();

    }

    private void initializations() {
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);
        editTextCode = findViewById(R.id.editTextCode);
        inputLayoutCode = findViewById(R.id.input_layout_code);
        txtvtitulo = findViewById(R.id.txtvtitulo);
        txtvtexto = findViewById(R.id.txtvtexto);
        timeView =findViewById(R.id.textView4);
        textviewtime =findViewById(R.id.textView3);


        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        counter = prefs.getInt("counter", 0);

        // create sqlite database
        db = new DatabaseHandler(getApplicationContext());

        phonenumber = getIntent().getStringExtra("phonenumber");

        txtvtitulo.setText("Verificar " +phonenumber);
        txtvtexto.setText(Html.fromHtml("Esperando para detectar automáticamente el SMS enviado al: "+ "<b><font color=#091020> "+phonenumber+"</font></b>"));
        sendVerificationCode(phonenumber);
        textviewtime.setEnabled(false);
        textviewtime.setText("Volver a enviar SMS");
        reverseTimer(60,timeView);

    }
    public void reverseTimer(int Seconds,final TextView tv){
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(Seconds* 1000+1000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                tv.setText(String.format("%02d", minutes)
                        + ":" + String.format("%02d", seconds));
            }

            public void onFinish() {
                if(counter==0) {
                    tv.setText("");
                    textviewtime.setEnabled(true);
                    textviewtime.setTextColor(Color.parseColor("#ff9e00"));
                    counter++;
                }
            }
        }.start();

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
                    verifyCode(code);
                }
            }
        });

        textviewtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Verify_phone.this, Login3.class);
                intent.putExtra("phonenumber", phonenumber.replace("+593","0"));
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                counter++;

            }
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
    private void verifyCode(String code) {

        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        } catch (Exception e) {
            //String test=e.toString();
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //db.addUser("dsadsad", "dfsdf", "sdadasd", "25/02/2019");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.delete().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {

                                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString("phonenumber", phonenumber);
                                editor.apply();

                                Intent intent = new Intent(Verify_phone.this, Loginphoto.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });

                    } else {
                        Toast.makeText(Verify_phone.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationCode(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                editTextCode.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(Verify_phone.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

}

