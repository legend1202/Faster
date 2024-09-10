package faster.com.ec.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

import faster.com.ec.R;
import faster.com.ec.fooddelivery.CompleteOrder;


public class check_sesion extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "Fooddelivery";

    public void validate_sesion(final Context con, String sucess, String message){
       if (sucess.equals("2")) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(con, R.style.MyDialogTheme);
            builder1.setTitle("Existe nueva versión de Faster");
            builder1.setCancelable(false);
            builder1.setMessage(message);
            builder1.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        Log.e("hola","si entra");
                        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=faster.com.ec"));
                        con.startActivity(viewIntent);
                        System.exit(0);
                    } catch (Exception e) {
                        Toast.makeText(con, "No se puede conectar intente nuevamente...",
                                Toast.LENGTH_LONG).show();
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
        } else if (sucess.equals("3")) {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(con, R.style.MyDialogTheme);
            builder1.setTitle("Tu cuenta ha sido bloqueada");
            builder1.setCancelable(false);
            builder1.setMessage(message);
            builder1.setPositiveButton("Contactar a Soporte", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String toNumber = "+593969764774";
                    toNumber = toNumber.replace("+", "").replace(" ", "");
                    Intent sendIntent = new Intent("android.intent.action.MAIN");
                    // sendIntent.setComponent(new ComponentName(“com.whatsapp”, “com.whatsapp.Conversation”));
                    sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "¡Hola!, deseo recuperar mi cuenta en Faster");
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.setType("text/plain");
                    con.startActivity(sendIntent);
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
        }  else if (sucess.equals("6"))  {
           Intent iv = new Intent(con, CompleteOrder.class);
           con.startActivity(iv);
           finish();
       } else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(con, R.style.MyDialogTheme);
            builder1.setTitle("Información");
            builder1.setCancelable(false);
            builder1.setMessage(message);
            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

}
