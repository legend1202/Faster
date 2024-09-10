package faster.com.ec.fooddelivery;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import faster.com.ec.Getset.registergetset;
import faster.com.ec.R;
import faster.com.ec.helper.SessionManager;
import faster.com.ec.utils.Config;

public class Loginphoto extends AppCompatActivity {
    private static final int RESULT_cam_IMAGE = 2;
    private static final int RESULT_LOAD_IMAGE = 1;
    private Button camera;
    private Button gallery;
    private Button siguiente5;
    private ImageView img_user;
    private String picturepath = "";
    private String nombres, email;
    private ProgressDialog progressDialog,pDialog;
    private ArrayList<registergetset> reglist;
    private String response;
    String strSuccess;
    private String phonenumber;
    private String user2, nombres2,email2,imageprofile,phonenumber2;
    private static final String MY_PREFS_NAME = "Fooddelivery";
    private SessionManager session;
    private EditText  EditApellido;
    private TextInputLayout inputLayoutNombre, inputLayoutApellido;
    private String regId;
    private String  txt_message;
    private String clicoptions="";
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_CAMERA = 0;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginphoto);
        initializations();
        setClickListener();
        displayFirebaseRegId();
        VerifyUserbyPhone();
        if (progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

    private void initializations() {
        reglist = new ArrayList<>();
        //camera = findViewById(R.id.camera);
        //gallery = findViewById(R.id.gallery);
        siguiente5 = findViewById(R.id.Siguiente5);
        img_user = findViewById(R.id.img_user);
        EditApellido = findViewById(R.id.editTextApellido);
        inputLayoutApellido= findViewById(R.id.input_layout_apellido);
        // session manager
        session = new SessionManager(getApplicationContext());
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        //phonenumber
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        phonenumber = prefs.getString("phonenumber", null);

    }

    private void setClickListener() {

        /*.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicoptions="gallery";
                int permission = ActivityCompat.checkSelfPermission(Loginphoto.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    verifyStoragePermissions(Loginphoto.this);
                } else {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicoptions="camera";
                int permission = ActivityCompat.checkSelfPermission(Loginphoto.this, Manifest.permission.CAMERA);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    verifyStoragePermissions(Loginphoto.this);

                } else {
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivityForResult(intent, RESULT_cam_IMAGE);

                }
            }
        });

        siguiente5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitForm();
            }
        });*/

        siguiente5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private class GetImageToURL extends AsyncTask < String, Void, Bitmap > {

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap myBitMap) {
            img_user.setImageBitmap(myBitMap);
            Uri tempUri =getImageUri(getApplicationContext(),myBitMap);
            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(tempUri));
            picturepath = String.valueOf(finalFile);
            Log.d("picturepath", "" + picturepath);
        }
    }


    private void VerifyUserbyPhone() {
        progressDialog = new ProgressDialog(Loginphoto.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Consultando Información");
        progressDialog.show();

        String hp;
        hp = getString(R.string.link) + getString(R.string.servicepath) + "verifyuserbyphone.php?";

        StringRequest postRequest = new StringRequest(Request.Method.POST, hp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
                Log.e("PlaceOrder", "ResponseFromServer " + response);
                try {
                    JSONObject responsedat = new JSONObject(response);
                    String txt_success = responsedat.getString("success");
                    if (txt_success.equals("1")) {
                        JSONArray jA_category = responsedat.getJSONArray("user_detail");
                        JSONObject cat_detail = jA_category.getJSONObject(0);
                        EditApellido.setText(cat_detail.getString("fullname"));

                    }else if (!txt_success.equals("2"))  {
                        JSONArray jA_category = responsedat.getJSONArray("user_detail");
                        JSONObject cat_detail = jA_category.getJSONObject(0);
                        String mensaje = cat_detail.getString("message");

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Loginphoto.this, R.style.MyDialogTheme);
                        builder1.setTitle("Información");
                        builder1.setCancelable(false);
                        builder1.setMessage(mensaje);
                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
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
                Map<String, String> params = new HashMap<>();
                params.put("phone_no", phonenumber);
                return params;
            }
            @Override
            public String getBodyContentType() {
                return super.getBodyContentType();
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(Loginphoto.this);

        //adding the string request to request queue
        requestQueue.add(postRequest);

    }



    @Override
    public void onBackPressed() {

    }

    class postingData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Loginphoto.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            postdata();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }

            new getlogin().execute();

        }
    }


    private void displayFirebaseRegId() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        regId = pref.getString("regId", null);
        Log.e("fireBaseRid", "Firebase Reg id: " + regId);

        //new RegisterMobile().execute();
    }

    class getlogin extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Loginphoto.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
            //Log.e("PlaceOrder1", response);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            reglist.clear();
            try {
                JSONObject responsedat = new JSONObject(response);
                strSuccess = responsedat.getString("success");
                txt_message = responsedat.getString("message");

                if (strSuccess.equals("1")) {
                    JSONArray jA_category = responsedat.getJSONArray("user_detail");
                    JSONObject data = jA_category.getJSONObject(0);

                    user2 = data.getString("id");
                    email2 = data.getString("email");
                    phonenumber2 = data.getString("phone_no");
                    nombres2 = data.getString("fullname");
                    imageprofile = data.getString("image");
                    Log.e("image111", imageprofile);
                    Log.e("user2", "" + user2);
                    Log.e("Obj1", responsedat .toString());
                }

            } catch (JSONException e) {
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
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            Log.e("hola",strSuccess);
            if (strSuccess.equals("1")) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("userid", "" + user2);
                editor.putString("username", "" + nombres2);
                editor.putString("usermailid", "" + email2);
                editor.putString("usermobileno", "" + phonenumber2);
                imageprofile = getString(R.string.link) + getString(R.string.imagepath) + imageprofile;
                editor.putString("imageprofile", "" + imageprofile);
                editor.apply();

                Intent iv;
                session.setLogin(true);
                iv = new Intent(Loginphoto.this, Splash.class);

                iv.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                iv.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iv);
                finish();
            } else if (strSuccess.equals("203")) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(Loginphoto.this, R.style.MyDialogTheme);
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

                            Toast.makeText(Loginphoto.this, "No se puede conectar intente nuevamente...",
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            } else {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(Loginphoto.this, R.style.MyDialogTheme);
                builder1.setTitle("Información");
                builder1.setCancelable(false);
                builder1.setMessage(txt_message);
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
    }

    private void postdata() {
        // TODO Auto-generated method stub
        HttpClient httpClient = new DefaultHttpClient();
        String boundary = "-------------" + System.currentTimeMillis();
        String nombresfull=EditApellido.getText().toString();

        Log.e("Obj", nombresfull+" "+ phonenumber+" "+ picturepath);
        HttpEntity entity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .setBoundary(boundary).addTextBody("email", phonenumber, ContentType.create("text/plain", MIME.UTF8_CHARSET))
                .addTextBody("fullname", nombresfull, ContentType.create("text/plain", MIME.UTF8_CHARSET))
                .addTextBody("password", "123", ContentType.create("text/plain", MIME.UTF8_CHARSET))
                .addTextBody("phone_no", phonenumber)
                .addTextBody("operative_system", getString(R.string.sistema_operativo))
                .addTextBody("code", getString(R.string.version_app))
                .addTextBody("filename", nombresfull, ContentType.create("text/plain", MIME.UTF8_CHARSET))
                .build();
        //.addBinaryBody("file", new File(picturepath), ContentType.create("application/octet-stream"), "filename").build();

        HttpPost httpPost = new HttpPost(getString(R.string.link) + getString(R.string.servicepath) + "userregister.php");
        httpPost.setHeader("Content-type", "multipart/form-data; boundary=" + boundary);
        httpPost.setHeader(  "Authorization","Bearer "+regId);
        httpPost.setEntity(entity);
        HttpResponse response1 = null;
        try {
            response1 = httpClient.execute(httpPost);
            HttpEntity result = response1.getEntity();
            if (result != null) {
                try {
                    String responseStr = EntityUtils.toString(result).trim();
                    Log.e("PlaceOrder", responseStr);
                    response = responseStr;

                } catch (org.apache.http.ParseException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String temp = cursor.getString(idx);
        cursor.close();
        return temp;
    }

    private Bitmap decodeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            final int REQUIRED_SIZE = 70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {


            Uri selectedImage = data.getData();
            Bitmap photo;
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
                String[] filePathColumn = {MediaStore.MediaColumns.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturepath = cursor.getString(columnIndex);

                Log.d("picturepath", "" + picturepath);
                cursor.close();
                try {
                    photo = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    img_user.setImageBitmap(decodeFile(picturepath));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == RESULT_cam_IMAGE && resultCode == RESULT_OK && null != data) {
                Log.d("photo", "" + data.getExtras().get("data"));
                photo = (Bitmap) data.getExtras().get("data");
                img_user.setImageBitmap(photo);
                img_user.setImageBitmap(photo);
                Uri tempUri = getImageUri(getApplicationContext(), photo);
                // CALL THIS METHOD TO GET THE ACTUAL PATH
                File finalFile = new File(getRealPathFromURI(tempUri));
                picturepath = String.valueOf(finalFile);


            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void submitForm() {

        if (!validateApellido()) {
            return;
        } /*else if (picturepath.equals("")) {
            Toast.makeText(getApplicationContext(), R.string.choose_profile, Toast.LENGTH_LONG).show();
        }*/ else {
            //img_user.setImageBitmap(decodeFile(picturepath));
            //Log.d("picturepath", "" + picturepath);

            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("Nombres", EditApellido.getText().toString());
            editor.apply();

            new postingData().execute();
        }

    }

    private static void verifyStoragePermissions(final Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);

        } else {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)

        {
            case REQUEST_EXTERNAL_STORAGE: {
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    Log.e("hola",clicoptions);
                    if(clicoptions.equals("camera")){
                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        startActivityForResult(intent, RESULT_cam_IMAGE);

                    }else if(clicoptions.equals("gallery")){
                        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(i, RESULT_LOAD_IMAGE);
                    }
                } else
                {
                    Log.e("hola",clicoptions+" else");
                    verifyStoragePermissions(Loginphoto.this);
                }
            }
        }

    }

    //Valida Nombre y Apellido
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    private boolean validateApellido() {
        if (EditApellido.getText().toString().trim().length()<7) {
            inputLayoutApellido.setError(getString(R.string.err_msg_apellido));
            requestFocus(EditApellido);
            return false;
        } else {
            inputLayoutApellido.setErrorEnabled(false);
        }
        return true;
    }
}