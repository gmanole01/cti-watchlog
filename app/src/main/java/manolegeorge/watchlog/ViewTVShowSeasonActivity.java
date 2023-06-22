package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import manolegeorge.watchlog.ui.widget.WLCheckBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint({"InflateParams", "SetTextI18n"})
@SuppressWarnings({"FieldCanBeLocal", "ConstantConditions", "deprecation"})

public class ViewTVShowSeasonActivity extends AppCompatActivity {

	private int tvShowId;
	private String tvShowName;
	private int seasonNumber;
	private String seasonName;

	private LayoutInflater inflater;
	private RequestQueue requestQueue;
	private SharedPreferences userSP;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if(
			!getIntent().hasExtra("tv_show_id") ||
			!getIntent().hasExtra("tv_show_name") ||
			!getIntent().hasExtra("season_name") ||
			!getIntent().hasExtra("season_number")
		) {
			finish();
		}

		tvShowId = getIntent().getIntExtra("tv_show_id", 0);
		tvShowName = getIntent().getStringExtra("tv_show_name");
		seasonName = getIntent().getStringExtra("season_name");
		seasonNumber = getIntent().getIntExtra("season_number", 0);

		inflater = LayoutInflater.from(this);
		requestQueue = Volley.newRequestQueue(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);

		setContentView(R.layout.activity_view_tv_show_season);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(tvShowName);
		getSupportActionBar().setSubtitle(seasonName);

		final ScrollView content = findViewById(R.id.content);
		final LinearLayout linearLayout = findViewById(R.id.linear_layout);
		final TextView textView = findViewById(R.id.text_view);
		final LinearLayout loading = findViewById(R.id.loading);

		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/get/" + tvShowId + "/season/" + seasonNumber, new Response.Listener<String>() {

			@Override
			public void onResponse(String response1) {
				try {
					final JSONObject jsonObject1 = new JSONObject(response1);
					if(!jsonObject1.getBoolean("error")) {

						JSONArray episodesJA = jsonObject1.getJSONArray("episodes");
						if(episodesJA.length() > 0) {
							for(int i = 0; i < episodesJA.length(); i++) {

								final int episodeNumber = episodesJA.getJSONObject(i).getInt("episode_number");

								View view = inflater.inflate(R.layout.tv_show_list_item_episode, null);
								TextView number = view.findViewById(R.id.number);
								TextView name = view.findViewById(R.id.name);
								final WLCheckBox checkBox = view.findViewById(R.id.check_box);
								number.setText(((episodeNumber < 10) ? "0" : "" ) + episodeNumber);
								name.setText(episodesJA.getJSONObject(i).getString("name"));
								checkBox.setChecked(episodesJA.getJSONObject(i).getBoolean("is_watched"));
								checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
									@Override
									public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
										if(checked) {

											StringRequest stringRequest3 = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/get/" + tvShowId + "/season/" + seasonNumber + "/episode/" + episodeNumber + "/watched", new Response.Listener<String>() {
												@Override
												public void onResponse(String response3) {
													try {
														JSONObject jsonObject3 = new JSONObject(response3);
														if(jsonObject3.getBoolean("error")) {
															checkBox.setChecked(false, false);
															Toast.makeText(ViewTVShowSeasonActivity.this, jsonObject3.getString("error_msg"), Toast.LENGTH_LONG).show();
														}
													} catch(JSONException e) {
														checkBox.setChecked(false, false);
														Toast.makeText(ViewTVShowSeasonActivity.this, "JSONException", Toast.LENGTH_LONG).show();
													}
												}
											}, new Response.ErrorListener() {
												@Override
												public void onErrorResponse(VolleyError error) {
													error.printStackTrace();
													checkBox.setChecked(false, false);
													if(error instanceof TimeoutError) {
														Toast.makeText(ViewTVShowSeasonActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
													} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
														Toast.makeText(ViewTVShowSeasonActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
													} else {
														Toast.makeText(ViewTVShowSeasonActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
													}
												}
											}) {
												@Override
												public Map<String, String> getHeaders() {
													Map<String, String> headers = new HashMap<>();
													headers.put("Accept", "application/json");
													headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
													return headers;
												}
											};
											requestQueue.add(stringRequest3);

										} else {

											StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/get/" + tvShowId + "/season/" + seasonNumber + "/episode/" + episodeNumber + "/not_watched", new Response.Listener<String>() {
												@Override
												public void onResponse(String response2) {
													try {
														JSONObject jsonObject2 = new JSONObject(response2);
														if(jsonObject2.getBoolean("error")) {
															checkBox.setChecked(true, false);
															Toast.makeText(ViewTVShowSeasonActivity.this, jsonObject2.getString("error_msg"), Toast.LENGTH_LONG).show();
														}
													} catch(JSONException e) {
														checkBox.setChecked(true, false);
														Toast.makeText(ViewTVShowSeasonActivity.this, "JSONException", Toast.LENGTH_LONG).show();
													}
												}
											}, new Response.ErrorListener() {
												@Override
												public void onErrorResponse(VolleyError error) {
													error.printStackTrace();
													checkBox.setChecked(true, false);
													if(error instanceof TimeoutError) {
														Toast.makeText(ViewTVShowSeasonActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
													} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
														Toast.makeText(ViewTVShowSeasonActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
													} else {
														Toast.makeText(ViewTVShowSeasonActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
													}
												}
											}) {
												@Override
												public Map<String, String> getHeaders() {
													Map<String, String> headers = new HashMap<>();
													headers.put("Accept", "application/json");
													headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
													return headers;
												}
											};
											requestQueue.add(stringRequest2);
										}
									}
								});
								linearLayout.addView(view);

							}
						}

					} else {
						textView.setText(jsonObject1.getString("error_msg"));
					}
				} catch(JSONException e) {
					e.printStackTrace();
					//textView.setText("JSONException");
				}
				WatchLog.Utils.fadeOut(loading);
				WatchLog.Utils.fadeIn(content);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
			}
		}) {
			@Override
			public Map<String, String> getHeaders() {
				Map<String, String> headers = new HashMap<>();
				headers.put("Accept", "application/json");
				headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
				return headers;
			}
		};
		stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(stringRequest1);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
