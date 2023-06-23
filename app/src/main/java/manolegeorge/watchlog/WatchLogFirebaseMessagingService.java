package manolegeorge.watchlog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings({"ConstantConditions"})

public class WatchLogFirebaseMessagingService extends FirebaseMessagingService {

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		
		Log.e("XYZ", "NEW MESSAGE");
		
		RemoteMessage.Notification fcmNotification = remoteMessage.getNotification();
		
		String CHANNEL_ID = "global";
		String CHANNEL_NAME = "Global";
		
		Intent i = new Intent(this, MyFriendsActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent pendingIntent = PendingIntent.getActivity(
			this, 0, i,
			PendingIntent.FLAG_IMMUTABLE
		);
		
		NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
			.setColorized(true)
			.setColor(getResources().getColor(R.color.colorAccent))
			.setSmallIcon(R.mipmap.ic_launcher)
			.setContentTitle(fcmNotification.getTitle())
			.setContentText(fcmNotification.getBody())
			.setPriority(Notification.PRIORITY_MAX)
			.setContentIntent(pendingIntent)
			.setDefaults(Notification.DEFAULT_ALL);
		
		NotificationManager nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		if(Build.VERSION.SDK_INT >= 26) {
			
			NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
			
			nManager.createNotificationChannel(mChannel);
			
			notification.setChannelId(CHANNEL_ID);
			
		}
		
		nManager.notify((int)(Math.random() * 100000), notification.build());

	}

	private String getQuery(Map<String, String> params) throws UnsupportedEncodingException {

		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
		}
		return sb.substring(0, sb.length() - 1);

	}

}
