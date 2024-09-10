package faster.com.ec.fooddelivery;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import faster.com.ec.R;


public class Login4 extends AppCompatActivity {
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private CheckBox checkbox;
    private Button buttonEmpezar;
    private TextView tyc;
    private TextView pintro;
    private TextView timeView;


    private TextInputLayout inputLayoutNombre, inputLayoutApellido;
    private EditText editNombre;
    private EditText editApellido;
    private Button Siguiente3;

    public int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login4);

        initializations();
        setClickListener();

    }
    private void initializations() {

        editApellido = findViewById(R.id.editTextApellido);
        inputLayoutApellido = findViewById(R.id.input_layout_apellido);

        checkbox = findViewById(R.id.checkbox);
        buttonEmpezar= findViewById(R.id.Siguiente4);
        timeView =findViewById(R.id.textView3);
        tyc=findViewById(R.id.tyc);
        pintro=findViewById(R.id.more);

        String termsText = "Toca \"Aceptar y continuar\" para aceptar las ";
        String termsLink = " <a href=https://faster.com.ec/condiciones.php>Condiciones del servicio</a>";
        String allText = termsText + termsLink;

        String termsText2 = "¿Deseas conocer más sobre nuestro servicio? Mira nuestro ";
        String termsLink2 = "<a href=https://www.youtube.com/watch?v=DVcKGrkw4wY>Video Promocional</a>";
        String allText2 = termsText2 + termsLink2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tyc.setMovementMethod(LinkMovementMethod.getInstance());
            tyc.setText(Html.fromHtml(allText, Html.FROM_HTML_MODE_LEGACY));
            pintro.setMovementMethod(LinkMovementMethod.getInstance());
            pintro.setText(Html.fromHtml(allText2, Html.FROM_HTML_MODE_LEGACY));

        }
        else {
            tyc.setMovementMethod(LinkMovementMethod.getInstance());
            tyc.setText(Html.fromHtml(allText));
            pintro.setMovementMethod(LinkMovementMethod.getInstance());
            pintro.setText(Html.fromHtml(allText2));
        }

    }

    private void setClickListener() {
        buttonEmpezar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Login4.this, Login3.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }
        });

    }

}



