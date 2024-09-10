package faster.com.ec.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import faster.com.ec.R;
import faster.com.ec.fooddelivery.CompleteOrder;
import faster.com.ec.fooddelivery.MainActivity;
import faster.com.ec.fooddelivery.MenuActivity;


/**
 * Created by Ravi Tamada on 08/08/16.
 * www.androidhive.info
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final String CHANNEL_ID = "12345";
    private static final String MY_PREFS_ACTIVITY = "RestaurantActivity";
    private NotificationUtils notificationUtils;
    private String title,message,click_action, activity_dialog,button_dialog;
    private Boolean show_push,show_dialog;
    sqliteHelper sqliteHelper;
    long[] VIBRATE_PATTERN = new long[]{0,400,800,600,800,800,800,1000};

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        storeRegIdInPref(token);
    }

    private void storeRegIdInPref(String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        Log.e(TAG, "sendRegistrationToServer11: " + token);
        Log.e(TAG, "token: " + token);
        editor.putString("regId", token);
        editor.apply();
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "From: " + remoteMessage.getFrom());


        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "MyOrderPage Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                //show_notification=json.optBoolean("show_notification");
                // socket_action=json.optString("socket_action");
                // important=json.optBoolean("important");

                title=json.optString("title");
                message=json.optString("message");
                click_action=json.optString("click_action");
                activity_dialog=json.optString("activity_dialog");
                button_dialog=json.optString("button_dialog");
                show_push=json.optBoolean("show_push");
                show_dialog=json.optBoolean("show_dialog");

                SharedPreferences prefsDeliveryBoyId = getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE);
                Boolean MainActivity = prefsDeliveryBoyId.getBoolean("Main",false);
                String Activity = prefsDeliveryBoyId.getString("Activity","");

                Log.d("response1", json+MainActivity.toString()+Activity);

                if(title.equals("pedido_puerta")){
                    Intent dialog=new Intent(activity_dialog);
                    dialog.putExtra("title","Tu pedido ha llegado");
                    dialog.putExtra("message",message);
                    dialog.putExtra("button_dialog",button_dialog);
                    dialog.putExtra("click_action",click_action);

                    dialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent=PendingIntent.getActivity(this,0,dialog,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
                    pendingIntent.send();
                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());

                    notificationUtils.playNotificationSound();
                }

                if(show_push){
                    if (!NotificationUtils.isAppIsInBackground(getApplicationContext())){
                        if(MainActivity){
                           // RestaurantStatus.getInstance().settingData();
                            // play notification sound
                            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                            notificationUtils.playNotificationSound();
                        }else
                        {handleDataMessage(json);}
                    }else
                    {handleDataMessage(json);}
                }
                if(show_dialog){
                    if (!NotificationUtils.isAppIsInBackground(getApplicationContext())){
                        CompleteOrder.getInstance().getDataprogress();

                        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                        notificationUtils.playNotificationSound();
                    } else {
                        if(activity_dialog.length()>0){
                            Intent dialog=new Intent(activity_dialog);
                            dialog.putExtra("title",title);
                            dialog.putExtra("message",message);
                            dialog.putExtra("button_dialog",button_dialog);
                            dialog.putExtra("click_action",click_action);

                            dialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent=PendingIntent.getActivity(this,0,dialog,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
                            pendingIntent.send();
                            // play notification sound
                            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                            notificationUtils.playNotificationSound();
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        } /*else {
            // If the app is in background, firebase itself handles the notification
        }*/


    }
    private void handleDataMessage(JSONObject json) {
//        Log.e(TAG, "push json: " + json.toString());

        try {
            JSONObject data = json;//.getJSONObject("data");

            title = data.optString("title");
            message = data.optString("message");
            click_action = data.optString("click_action");
            boolean isBackground = data.optBoolean("is_background");
            String imageUrl = data.optString("image");
            String timestamp = data.optString("timestamp");
            String isSaveinDatabase = data.optString("isSaveDatabase");

            Log.e(TAG, "title: " + title);
            Log.e(TAG, "message: " + message);
            Log.e(TAG, "isBackground: " + isBackground);
            Log.e(TAG, "imageUrl: " + imageUrl);
            Log.e(TAG, "timestamp: " + timestamp);
            Calendar calander = Calendar.getInstance();
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String Date = simpledateformat.format(calander.getTime());
            timestamp = Date;

            //if (isSaveinDatabase.equals("true"))
                //saveindatabase();
            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", message);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message,click_action, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                resultIntent.putExtra("message", message);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), title, message, click_action, timestamp, resultIntent);


                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
                }
            }


        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }


    /*private void saveindatabase() {
        sqliteHelper = new sqliteHelper(getApplicationContext());
        SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();
            DBAdapter myDbHelpel = new DBAdapter(getApplicationContext());
        try {
            myDbHelpel.createDataBase();
        } catch (IOException io) {
            throw new Error("Unable TO Create DataBase");
        }
        try {
            myDbHelpel.openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SQLiteDatabase db = myDbHelpel.getWritableDatabase();
        ContentValues values = new ContentValues();
        Log.e("fsddf", title + "::" + message);
        values.put("title", title);
        values.put("message", message);
        values.put("bool", "from");
        db1.insert("notification", null, values);
        myDbHelpel.close();
    }/*

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String click_action, String timeStamp, Intent intent) {
        //Intent intent1=new Intent(click_action);
        //intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent1,PendingIntent.FLAG_ONE_SHOT);

        Intent notificationIntent = new Intent(this, MenuActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent mIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            //mChannel.setVibrationPattern(VIBRATE_PATTERN);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }

        NotificationCompat.Builder mBuilder1 = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_notification_small)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(getResources().getColor(R.color.colorPrimary))
                //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        //R.mipmap.ic_notification_small))
                .setContentIntent(mIntent)
                .setAutoCancel(true)
                .setVibrate(VIBRATE_PATTERN)
                .setOngoing(false) //true desactivar al delizar la notificacion se cierra
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(123, mBuilder1.build());
        playSound();
        this.Viber(context);

        PowerManager power = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = power.isInteractive();

        if (!isScreenOn) { //¿La pantalla esta apagada?
            //La pantalla esta apagada!, se enciende.
            PowerManager.WakeLock wl = power.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wl.acquire(10000);
            wl.release();
        }/*else{
            //La pantalla esta encendida!
        }*/
    }

    public void Viber(Context cn) {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) cn.getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 300 milliseconds
        v.vibrate(50000);
    }

    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
        playSound();
    }

    private void playSound() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }
}
