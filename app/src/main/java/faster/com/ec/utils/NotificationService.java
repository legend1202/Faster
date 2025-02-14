package faster.com.ec.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

import faster.com.ec.R;
import faster.com.ec.fooddelivery.MainActivity;


public class NotificationService extends Service {
	private NotificationManager mManager;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		this.getApplicationContext();
		Context context = getApplicationContext();

		
		Random r = new Random();
		int i1 = r.nextInt(100 - 1) + 1;

//		int icon = getResources().getDrawable(android.R.drawable.btn_minus);
		long when = System.currentTimeMillis();
		
		Log.d("when", ""+when);
		
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		@SuppressWarnings("deprecation")
		Notification notification = new Notification();

//		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, MainActivity.class);
		// set intent so it does not start a new activity
		
		// notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
		// Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent1 = PendingIntent.getActivity(context, i1, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(context).setContentIntent(intent1).setSmallIcon(R.mipmap.ic_notification_small)
				.setContentTitle("FoodDelivery").setContentText("FoodDelivery");

		builder.setAutoCancel(true);

		notification = builder.build();


		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		notification.sound = uri;
		// notification.flags |= MyOrderPage.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
		
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// Logger.error("Alam Services Destroyed");
		super.onDestroy();
	}

}
