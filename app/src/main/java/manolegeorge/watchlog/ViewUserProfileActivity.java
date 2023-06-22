package manolegeorge.watchlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ViewUserProfileActivity extends AppCompatActivity {

	private int userId;

	private ImageLoader imageLoader;
	private RequestQueue requestQueue;
	private SharedPreferences userSP;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if(!getIntent().hasExtra("user_id") || !getIntent().hasExtra("user_username"))
			finish();

		userId = getIntent().getIntExtra("user_id", 0);

		imageLoader = WatchLog.ImageLoader(this);
		requestQueue = Volley.newRequestQueue(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);

		setContentView(R.layout.activity_view_user_profile);

		getWindow().setStatusBarColor(Color.argb(0, 0, 0, 0));

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(getIntent().getStringExtra("user_username"));

		final ImageView profilePicture = findViewById(R.id.profile_picture);
		final LinearLayout content = findViewById(R.id.content);
		final LinearLayout loading = findViewById(R.id.loading);

		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/get_user_data", new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {

						imageLoader.displayImage(jsonObject.getJSONObject("data").getString("profile_picture"), profilePicture, new DisplayImageOptions.Builder()
							.showImageForEmptyUri(R.drawable.no_backdrop_16_9)
							.showImageOnLoading(R.drawable.no_backdrop_16_9)
							.showImageOnFail(R.drawable.no_backdrop_16_9)
							.displayer(new FadeInBitmapDisplayer(500))
							.cacheInMemory(false)
							.cacheOnDisk(true)
							.build());

					} else {
						Toast.makeText(getApplicationContext(), jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
					}
				} catch(JSONException e) {
					Toast.makeText(getApplicationContext(), "JSONException", Toast.LENGTH_LONG).show();
				}
				content.setVisibility(View.VISIBLE);
				WatchLog.Utils.fadeOut(loading);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				WatchLog.Utils.fadeOut(loading);
				if(error instanceof TimeoutError) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
				} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
				}
			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<>();
				params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
				params.put("email_address", userSP.getString("email_address", "undefined"));
				params.put("language", getResources().getConfiguration().locale.getLanguage());
				params.put("password", userSP.getString("password", "undefined"));
				params.put("user_id", String.valueOf(userId));
				return params;
			}
		};
		requestQueue.add(stringRequest1);

	}

}
