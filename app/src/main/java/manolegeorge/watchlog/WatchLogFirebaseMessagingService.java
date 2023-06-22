package manolegeorge.watchlog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

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

		SharedPreferences userSP = getSharedPreferences("user", Context.MODE_PRIVATE);

		if(userSP.contains("email_address") && !userSP.getString("email_address", "undefined").equals("undefined") &&
		userSP.contains("password") && !userSP.getString("password", "undefined").equals("undefined")) {

			final String emailAddress = userSP.getString("email_address", "undefined");
			final String password = userSP.getString("password", "undefined");

			Map<String, String> params = new HashMap<>();
			params.put("email_address", emailAddress);
			params.put("language", getResources().getConfiguration().locale.getLanguage());
			params.put("password", password);

			try {

				URL url = new URL(Constants.API_URL + "/get_user_data");

				HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
				connection.setConnectTimeout(60000);
				connection.setReadTimeout(60000);
				connection.setRequestProperty("User-Agent", "WatchLog");
				connection.setRequestMethod("POST");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(getQuery(params));
				wr.flush();
				wr.close();

				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				StringBuilder response = new StringBuilder();

				String inputLine;
				while((inputLine = in.readLine()) != null) response.append(inputLine);

				in.close();

				String result = response.toString();

				JSONObject jsonObject = new JSONObject(result);
				if(!jsonObject.getBoolean("error")) {

					Map<String, String> data = remoteMessage.getData();
					if(Integer.valueOf(data.get("to")) == jsonObject.getJSONObject("data").getInt("id")) {
						if(data.get("type").equals("notification")) {

							int icon = getResources().getIdentifier(data.get("n_icon"), "drawable", getPackageName());
							if(icon == 0) icon = R.drawable.ic_watchlog_eye;
							String title = data.get("n_title");
							String text = data.get("n_text");

							String CHANNEL_ID = "global";
							String CHANNEL_NAME = "Global";

							NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
							.setColorized(true)
							.setColor(getResources().getColor(R.color.colorAccent))
							.setSmallIcon(icon)
							.setContentTitle(title)
							.setContentText(text)
							.setPriority(Notification.PRIORITY_MAX)
							.setDefaults(Notification.DEFAULT_ALL);

							NotificationManager nManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

							if(Build.VERSION.SDK_INT >= 26) {

								NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

								nManager.createNotificationChannel(mChannel);

								notification.setChannelId(CHANNEL_ID);

							}

							nManager.notify((int)(Math.random() * 100000), notification.build());

						}
					}

				}
			} catch(Exception ignored) {
				//
			}
		}

	}

	private String getQuery(Map<String, String> params) throws UnsupportedEncodingException {

		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
		}
		return sb.substring(0, sb.length() - 1);

	}

}
