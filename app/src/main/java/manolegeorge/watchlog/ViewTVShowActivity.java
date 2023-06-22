package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import manolegeorge.watchlog.ui.widget.WLCheckBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint({"InflateParams","SetTextI18n"})
@SuppressWarnings({"ConstantConditions", "deprecation", "unused"})

public class ViewTVShowActivity extends AppCompatActivity {

	private int tvShowId = 0;
	private String tvShowName;

	ProgressDialog progressDialog;

	private ImageLoader imageLoader;
	private LayoutInflater inflater;
	private SharedPreferences userSP;
	private RequestQueue requestQueue;

	private boolean isSendingRating = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if(
			!getIntent().hasExtra("tv_show_id") ||
			!getIntent().hasExtra("tv_show_name")
		) finish();

		tvShowId = getIntent().getIntExtra("tv_show_id", 0);
		tvShowName = getIntent().getStringExtra("tv_show_name");

		setContentView(R.layout.activity_view_tv_show);
		setTitle(tvShowName);

		getWindow().setStatusBarColor(Color.argb(0, 0, 0, 0));

		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		imageLoader = WatchLog.ImageLoader(this);
		inflater = LayoutInflater.from(this);
		requestQueue = Volley.newRequestQueue(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.please_wait));

		final LinearLayout content = findViewById(R.id.content);
		final LinearLayout loading = findViewById(R.id.loading);
		final ImageView backdrop = findViewById(R.id.backdrop);
		final LinearLayout seasonsContainer = findViewById(R.id.seasons_container);
		final TextView overview = findViewById(R.id.overview);
		final RatingBar communityRating = findViewById(R.id.community_rating);
		final RatingBar myRating = findViewById(R.id.my_rating);
		final ImageView poster = findViewById(R.id.poster);
		final TextView title = findViewById(R.id.title);

		StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/get/" + tvShowId, new Response.Listener<String>() {

			@Override
			public void onResponse(String response1) {
				try {
					JSONObject jsonObject1 = new JSONObject(response1);
					if(!jsonObject1.getBoolean("error")) {

						JSONObject dataJO = jsonObject1.getJSONObject("data");
						imageLoader.displayImage(dataJO.getString("backdrop"), backdrop, new DisplayImageOptions.Builder()
							.showImageOnLoading(R.drawable.no_backdrop_16_9)
							.showImageOnFail(R.drawable.no_backdrop_16_9)
							.displayer(new FadeInBitmapDisplayer(500))
							.cacheInMemory(false)
							.cacheOnDisk(true)
							.build());
						imageLoader.displayImage(dataJO.getString("poster_small"), poster, new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).build());

						final JSONArray seasons = dataJO.getJSONArray("seasons");
						for(int i = 0; i < seasons.length(); i++) {

							final int seasonNumber = seasons.getJSONObject(i).getInt("season_number");
							final String seasonName = seasons.getJSONObject(i).getString("name");
							final int seasonId = seasons.getJSONObject(i).getInt("id");

							View seasonView = inflater.inflate(R.layout.tv_show_list_item_season, null);
							final WLCheckBox checkBox = seasonView.findViewById(R.id.check_box);
							TextView name = seasonView.findViewById(R.id.name);
							TextView textView = seasonView.findViewById(R.id.text_view);
							checkBox.setChecked(seasons.getJSONObject(i).getBoolean("is_watched"));
							checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
								@Override
								public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
									if(checked) {

										StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/mark_season_as_watched", new Response.Listener<String>() {
											@Override
											public void onResponse(String response2) {
												try {
													JSONObject jsonObject2 = new JSONObject(response2);
													if(jsonObject2.getBoolean("error")) {
														checkBox.setChecked(false, false);
														Toast.makeText(ViewTVShowActivity.this, jsonObject2.getString("error_msg"), Toast.LENGTH_LONG).show();
													}
												} catch(JSONException e) {
													checkBox.setChecked(false, false);
													Toast.makeText(ViewTVShowActivity.this, "JSONException", Toast.LENGTH_LONG).show();
												}
											}
										}, new Response.ErrorListener() {
											@Override
											public void onErrorResponse(VolleyError error) {
												checkBox.setChecked(false, false);
												if(error instanceof TimeoutError) {
													Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
												} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
													Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
												} else {
													Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
												}
											}
										}) {
											@Override
											protected Map<String, String> getParams() {
												Map<String, String> params = new HashMap<>();
												params.put("id", String.valueOf(tvShowId));
												return params;
											}
											
											@Override
											public Map<String, String> getHeaders() throws AuthFailureError {
												Map<String, String> headers = new HashMap<>();
												headers.put("Accept", "application/json");
												headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
												return headers;
											}
										};
										requestQueue.add(stringRequest2);

									} else {

										StringRequest stringRequest3 = new StringRequest(Request.Method.POST, Constants.API_URL + "/remove_watched_season", new Response.Listener<String>() {
											@Override
											public void onResponse(String response3) {
												try {
													JSONObject jsonObject3 = new JSONObject(response3);
													if(jsonObject3.getBoolean("error")) {
														checkBox.setChecked(true, false);
														Toast.makeText(ViewTVShowActivity.this, jsonObject3.getString("error_msg"), Toast.LENGTH_LONG).show();
													}
												} catch(JSONException e) {
													checkBox.setChecked(true, false);
													Toast.makeText(ViewTVShowActivity.this, "JSONException", Toast.LENGTH_LONG).show();
												}
											}
										}, new Response.ErrorListener() {
											@Override
											public void onErrorResponse(VolleyError error) {
												checkBox.setChecked(true, false);
												if(error instanceof TimeoutError) {
													Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
												} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
													Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
												} else {
													Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
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
												params.put("season_number", String.valueOf(seasonNumber));
												params.put("tv_show_id", String.valueOf(tvShowId));
												return params;
											}
										};
										requestQueue.add(stringRequest3);

									}
								}
							});
							name.setText(seasonName);
							textView.setText(seasons.getJSONObject(i).getInt("watched_episodes") + "/" + seasons.getJSONObject(i).getInt("episode_count"));
							seasonView.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									Intent intent = new Intent(ViewTVShowActivity.this, ViewTVShowSeasonActivity.class);
									intent.putExtra("tv_show_id", tvShowId);
									intent.putExtra("tv_show_name", tvShowName);
									intent.putExtra("season_name", seasonName);
									intent.putExtra("season_number", seasonNumber);
									intent.putExtra("season_id", seasonId);
									startActivity(intent);
								}
							});
							seasonsContainer.addView(seasonView);

						}

						overview.setText(dataJO.getString("overview"));

						communityRating.setRating((float)dataJO.getDouble("rating"));

					} else {
						if(jsonObject1.getString("error_id").equals("movie_not_found"))
							finish();
						Toast.makeText(ViewTVShowActivity.this, jsonObject1.getString("error_msg"), Toast.LENGTH_LONG).show();
					}
				} catch(JSONException e) {
					e.printStackTrace();
					Toast.makeText(ViewTVShowActivity.this, "JSONException", Toast.LENGTH_LONG).show();
				}
				WatchLog.Utils.fadeOut(loading);
				WatchLog.Utils.fadeIn(content);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				//WatchLog.Utils.fadeOut(loading, 200);
				error.printStackTrace();
				Toast.makeText(ViewTVShowActivity.this, error.toString(), Toast.LENGTH_LONG).show();
			}
		}) {
			
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String, String> headers = new HashMap<>();
				headers.put("Accept", "application/json");
				headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
				return headers;
			}
		};
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(stringRequest);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_view_tv_show, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.add_to_favourites:
				StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/favourites/add", new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						progressDialog.dismiss();
						try {
							JSONObject jsonObject = new JSONObject(response);
							if(!jsonObject.getBoolean("error")) {
								Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.tv_show_added_to_favourites), Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(ViewTVShowActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
							}
						} catch(JSONException e) {
							Toast.makeText(ViewTVShowActivity.this, "JSONException", Toast.LENGTH_LONG).show();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						progressDialog.dismiss();
						if(error instanceof TimeoutError) {
							Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
						} else if(
							error instanceof NoConnectionError ||
								error instanceof NetworkError
						) {
							Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(ViewTVShowActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
						}
					}
				}) {
					@Override
					protected Map<String, String> getParams() {
						Map<String, String> params = new HashMap<>();
						params.put("id", String.valueOf(tvShowId));
						return params;
					}
					
					@Override
					public Map<String, String> getHeaders() {
						Map<String, String> headers = new HashMap<>();
						headers.put("Accept", "application/json");
						headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
						return headers;
					}
				};
				stringRequest2.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

				progressDialog.show();
				requestQueue.add(stringRequest2);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
