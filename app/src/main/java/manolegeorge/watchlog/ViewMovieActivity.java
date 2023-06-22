package manolegeorge.watchlog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"ConstantConditions", "deprecation", "FieldCanBeLocal"})

public class ViewMovieActivity extends AppCompatActivity {

	private int movieId;
	private String movieTitle;

	ProgressDialog progressDialog;

	private ImageLoader imageLoader;
	private RequestQueue requestQueue;
	private SharedPreferences userSP;

	private boolean isSendingRating = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if(
			!getIntent().hasExtra("movie_id") ||
				!getIntent().hasExtra("movie_title")
		) finish();

		movieId = getIntent().getIntExtra("movie_id", 0);
		movieTitle = getIntent().getStringExtra("movie_title");

		final DisplayImageOptions backdropOptions = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.no_backdrop_16_9)
			.showImageOnLoading(R.drawable.no_backdrop_16_9)
			.showImageOnFail(R.drawable.no_backdrop_16_9)
			.displayer(new FadeInBitmapDisplayer(500))
			.cacheInMemory(false)
			.cacheOnDisk(true)
			.build();
		imageLoader = WatchLog.ImageLoader(this);
		final DisplayImageOptions posterOptions = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.no_poster)
			.showImageOnLoading(R.drawable.no_poster)
			.showImageOnFail(R.drawable.no_poster)
			.displayer(new FadeInBitmapDisplayer(500))
			.cacheInMemory(false)
			.cacheOnDisk(true)
			.build();
		requestQueue = Volley.newRequestQueue(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);

		setContentView(R.layout.activity_view_movie);
		setTitle(movieTitle);

		getWindow().setStatusBarColor(Color.argb(0, 0, 0, 0));

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.please_wait));

		final ImageView backdrop = findViewById(R.id.backdrop);
		final LinearLayout content = findViewById(R.id.content);
		final LinearLayout loading = findViewById(R.id.loading);
		final TextView title = findViewById(R.id.title);
		final TextView runtime = findViewById(R.id.runtime);
		final RatingBar communityRating = findViewById(R.id.community_rating);
		final RatingBar myRating = findViewById(R.id.my_rating);
		final TextView budget = findViewById(R.id.budget);
		final TextView revenue = findViewById(R.id.revenue);
		final TextView overview = findViewById(R.id.overview);
		final ImageView poster = findViewById(R.id.poster);

		myRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar ratingBar, final float rating, boolean fromUser) {
				if(!isSendingRating) {

					StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/rate_movie", new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							Log.d("TWAYS", response);
							try {
								JSONObject jsonObject = new JSONObject(response);
								if(jsonObject.getBoolean("error")) {
									Toast.makeText(getApplicationContext(), jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
								}
							} catch(JSONException e) {
								Toast.makeText(getApplicationContext(), "JSONException", Toast.LENGTH_LONG).show();
							}
							isSendingRating = false;
							myRating.setEnabled(true);
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							if(error instanceof TimeoutError) {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
							} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
							}
							isSendingRating = false;
							myRating.setEnabled(true);
						}
					}) {
						@Override
						protected Map<String, String> getParams() {
							Map<String, String> params = new HashMap<>();
							params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
							params.put("email_address", userSP.getString("email_address", "undefined"));
							params.put("movie_id", String.valueOf(movieId));
							params.put("password", userSP.getString("password", "undefined"));
							params.put("rating", String.valueOf(rating));
							return params;
						}
					};

					myRating.setEnabled(false);
					isSendingRating = true;
					requestQueue.add(stringRequest2);

				}
			}
		});

		StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_URL + "/movies/get/" + movieId, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {

						JSONObject dataJO = jsonObject.getJSONObject("data");
						imageLoader.displayImage(dataJO.getString("backdrop"), backdrop, backdropOptions);
						imageLoader.displayImage(dataJO.getString("poster_small"), poster, posterOptions);
						title.setText(Html.fromHtml(getResources().getString(R.string.movie_info_1, dataJO.getString("title"))));
						if(dataJO.getInt("runtime") == 0) {
							runtime.setText(Html.fromHtml(getResources().getString(R.string.movie_info_2, "N/A")));
						} else {
							runtime.setText(Html.fromHtml(getResources().getString(R.string.movie_info_2, formatRuntime(dataJO.getInt("runtime")))));
						}
						if(dataJO.getInt("budget") == 0) {
							budget.setText(Html.fromHtml(getResources().getString(R.string.movie_info_3, "N/A")));
						} else {
							budget.setText(Html.fromHtml(getResources().getString(R.string.movie_info_3, NumberFormat.getNumberInstance(Locale.US).format(dataJO.getInt("budget")) + "$")));
						}
						if(dataJO.getInt("revenue") == 0) {
							revenue.setText(Html.fromHtml(getResources().getString(R.string.movie_info_4, "N/A")));
						} else {
							revenue.setText(Html.fromHtml(getResources().getString(R.string.movie_info_4, NumberFormat.getNumberInstance(Locale.US).format(dataJO.getInt("revenue")) + "$")));
						}
						overview.setText(dataJO.getString("overview"));

						//myRating.setRating((float)dataJO.getDouble("my_rating"));
						communityRating.setRating((float)dataJO.getDouble("rating"));

					} else {
						if(jsonObject.getString("error_id").equals("movie_not_found"))
							finish();
						Toast.makeText(ViewMovieActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
					}
				} catch(JSONException e) {
					Log.d("TWAYS", response);
					Log.d("TWAYS", e.toString());
					Toast.makeText(ViewMovieActivity.this, "JSONException", Toast.LENGTH_LONG).show();
				}
				WatchLog.Utils.fadeOut(loading);
				WatchLog.Utils.fadeIn(content);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				WatchLog.Utils.fadeOut(loading, 200);
				Toast.makeText(ViewMovieActivity.this, error.toString(), Toast.LENGTH_LONG).show();
			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<>();
				params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
				params.put("email_address", userSP.getString("email_address", "undefined"));
				params.put("movie_id", String.valueOf(movieId));
				params.put("password", userSP.getString("password", "undefined"));
				return params;
			}
		};
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(stringRequest);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_view_movie, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

			case android.R.id.home:
				finish();
				return true;

			case R.id.mark_as_watched:
				progressDialog.show();
				StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/movies/watched/add", new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						progressDialog.dismiss();
						try {
							JSONObject jsonObject = new JSONObject(response);
							if(!jsonObject.getBoolean("error")) {
								Toast.makeText(ViewMovieActivity.this, getResources().getString(R.string.movie_marked_as_watched), Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(ViewMovieActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
							}
						} catch(JSONException e) {
							Toast.makeText(ViewMovieActivity.this, "JSONException", Toast.LENGTH_LONG).show();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						progressDialog.dismiss();
						error.printStackTrace();
						if(error instanceof TimeoutError) {
							Toast.makeText(ViewMovieActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
						} else if(
							error instanceof NoConnectionError ||
								error instanceof NetworkError
						) {
							Toast.makeText(ViewMovieActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(ViewMovieActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
						}
					}
				}) {
					@Override
					protected Map<String, String> getParams() {
						Map<String, String> params = new HashMap<>();
						params.put("id", String.valueOf(movieId));
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
				stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
				requestQueue.add(stringRequest1);
				return true;

			case R.id.add_to_favourites:

				StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/movies/favourites/add", new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						progressDialog.dismiss();
						try {
							JSONObject jsonObject = new JSONObject(response);
							if(!jsonObject.getBoolean("error")) {
								Toast.makeText(ViewMovieActivity.this, getResources().getString(R.string.movie_added_to_favourites), Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(ViewMovieActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
							}
						} catch(JSONException e) {
							Toast.makeText(ViewMovieActivity.this, "JSONException", Toast.LENGTH_LONG).show();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						progressDialog.dismiss();
						error.printStackTrace();
						if(error instanceof TimeoutError) {
							Toast.makeText(ViewMovieActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
						} else if(
							error instanceof NoConnectionError ||
								error instanceof NetworkError
						) {
							Toast.makeText(ViewMovieActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(ViewMovieActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
						}
					}
				}) {
					@Override
					protected Map<String, String> getParams() {
						Map<String, String> params = new HashMap<>();
						params.put("id", String.valueOf(movieId));
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
				stringRequest2.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

				progressDialog.show();
				requestQueue.add(stringRequest2);

				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private String formatRuntime(int time) {
		if(time < 60) {
			return time + "m";
		} else if(time == 60) {
			return "1h";
		} else {
			int hours = time / 60;
			int minutes = time - hours * 60;
			return hours + "h " + minutes + "m";
		}
	}

}
