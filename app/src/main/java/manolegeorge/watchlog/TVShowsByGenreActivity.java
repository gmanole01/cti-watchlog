package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;

import manolegeorge.watchlog.adapters.MoviesListAdapter;
import manolegeorge.watchlog.info.GenreInfo;
import manolegeorge.watchlog.info.MovieInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("InflateParams")
@SuppressWarnings({"ConstantConditions", "deprecation"})

public class TVShowsByGenreActivity extends AppCompatActivity {
	
	private int genreId;
	
	private ImageLoader imageLoader;
	private LayoutInflater inflater;
	private RequestQueue requestQueue;
	private SharedPreferences userSP;
	
	private int totalTVShows = 0;
	private int loadedTVShows = 0;
	private int tvShowsToLoad = 30;
	
	private List<MovieInfo> tvShows = new ArrayList<>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		if (!getIntent().hasExtra("genre_id") || !getIntent().hasExtra("genre_name"))
			finish();
		
		genreId = getIntent().getIntExtra("genre_id", 0);
		
		imageLoader = WatchLog.ImageLoader(this);
		inflater = LayoutInflater.from(this);
		requestQueue = Volley.newRequestQueue(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);
		
		setContentView(R.layout.activity_movies_by_genre);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		setTitle(getIntent().getStringExtra("genre_name"));
		
		final LinearLayout content = findViewById(R.id.content);
		final ListView listView = findViewById(R.id.list_view);
		final TextView textView = findViewById(R.id.text_view);
		final LinearLayout loading = findViewById(R.id.loading);
		
		final MoviesListAdapter adapter = new MoviesListAdapter(inflater, tvShows, imageLoader);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			Intent intent = new Intent(TVShowsByGenreActivity.this, ViewTVShowActivity.class);
			intent.putExtra("tv_show_id", tvShows.get(position).getId());
			intent.putExtra("tv_show_name", tvShows.get(position).getTitle());
			startActivity(intent);
		});
		
		final Button loadMore = (Button) inflater.inflate(R.layout.list_view_movies_footer, null);
		
		loadMore.setOnClickListener(v -> {
			
			StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/get_tv_shows_by_genre", response -> {
				try {
					JSONObject jsonObject = new JSONObject(response);
					if (!jsonObject.getBoolean("error")) {
						
						totalTVShows = jsonObject.getInt("total_tv_shows");
						
						JSONArray moviesJA = jsonObject.getJSONArray("tv_shows");
						if (moviesJA.length() > 0) {
							for (int i = 0; i < moviesJA.length(); i++) {
								
								loadedTVShows++;
								
								JSONObject movieJO = moviesJA.getJSONObject(i);
								JSONArray movieGenresJA = movieJO.getJSONArray("genres");
								
								List<GenreInfo> genres = new ArrayList<>();
								
								for (int j = 0; j < movieGenresJA.length(); j++) {
									genres.add(new GenreInfo(
											movieGenresJA.getJSONObject(j).getInt("id"),
											movieGenresJA.getJSONObject(j).getString("name")
									));
								}
								
								MovieInfo newMovie = new MovieInfo(movieJO.getInt("id"), movieJO.getString("name"));
								newMovie.setReleaseDate(movieJO.getString("air_date"));
								newMovie.setPoster(movieJO.getString("poster"));
								newMovie.setRating(movieJO.getDouble("rating"));
								newMovie.setGenres(genres);
								
								tvShows.add(newMovie);
								
							}
							adapter.notifyDataSetChanged();
						}
						if (loadedTVShows >= totalTVShows) {
							listView.removeFooterView(loadMore);
						}
						
					} else {
						Toast.makeText(TVShowsByGenreActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(TVShowsByGenreActivity.this, e.toString(), Toast.LENGTH_LONG).show();
				}
				loadMore.setClickable(true);
			}, error -> {
				if (error instanceof TimeoutError) {
					Toast.makeText(TVShowsByGenreActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
				} else if (error instanceof NetworkError) {
					Toast.makeText(TVShowsByGenreActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(TVShowsByGenreActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
				}
				loadMore.setClickable(true);
			}) {
				@Override
				protected Map<String, String> getParams() {
					Map<String, String> params = new HashMap<>();
					params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
					params.put("email_address", userSP.getString("email_address", "undefined"));
					params.put("genre_id", String.valueOf(genreId));
					params.put("language", getResources().getConfiguration().locale.getLanguage());
					params.put("loaded_tv_shows", String.valueOf(loadedTVShows));
					params.put("tv_shows_to_load", String.valueOf(tvShowsToLoad));
					params.put("password", userSP.getString("password", "undefined"));
					return params;
				}
			};
			stringRequest2.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			
			loadMore.setClickable(false);
			requestQueue.add(stringRequest2);
			
		});
		
		@SuppressLint("SetTextI18n")
		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/get_tv_shows_by_genre", response -> {
			try {
				
				JSONObject jsonObject = new JSONObject(response);
				if (!jsonObject.getBoolean("error")) {
					
					totalTVShows = jsonObject.getInt("total_tv_shows");
					
					JSONArray moviesJA = jsonObject.getJSONArray("tv_shows");
					if (moviesJA.length() > 0) {
						for (int i = 0; i < moviesJA.length(); i++) {
							
							loadedTVShows++;
							
							JSONObject movieJO = moviesJA.getJSONObject(i);
							JSONArray movieGenresJA = movieJO.getJSONArray("genres");
							
							List<GenreInfo> genres = new ArrayList<>();
							
							for (int j = 0; j < movieGenresJA.length(); j++) {
								genres.add(new GenreInfo(
										movieGenresJA.getJSONObject(j).getInt("id"),
										movieGenresJA.getJSONObject(j).getString("name")
								));
							}
							
							MovieInfo newMovie = new MovieInfo(movieJO.getInt("id"), movieJO.getString("name"));
							newMovie.setReleaseDate(movieJO.getString("air_date"));
							newMovie.setPoster(movieJO.getString("poster"));
							newMovie.setRating(movieJO.getDouble("rating"));
							newMovie.setGenres(genres);
							
							tvShows.add(newMovie);
							
						}
						adapter.notifyDataSetChanged();
						if (loadedTVShows < totalTVShows) {
							listView.addFooterView(loadMore);
						}
						textView.setVisibility(View.GONE);
						listView.setVisibility(View.VISIBLE);
					}
					
				} else {
					textView.setText(jsonObject.getString("error_msg"));
				}
				
			} catch (JSONException e) {
				textView.setText("JSONException");
			}
			WatchLog.Utils.fadeOut(loading);
			WatchLog.Utils.fadeIn(content);
		}, error -> {
			if (error instanceof TimeoutError) {
				textView.setText(getResources().getString(R.string.weak_internet_connection));
			} else if (error instanceof NetworkError) {
				textView.setText(getResources().getString(R.string.no_internet_connection));
			} else {
				textView.setText(getResources().getString(R.string.error));
			}
			WatchLog.Utils.fadeOut(loading);
			WatchLog.Utils.fadeIn(content);
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<>();
				params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
				params.put("email_address", userSP.getString("email_address", "undefined"));
				params.put("genre_id", String.valueOf(genreId));
				params.put("loaded_tv_shows", String.valueOf(loadedTVShows));
				params.put("tv_shows_to_load", String.valueOf(tvShowsToLoad));
				params.put("password", userSP.getString("password", "undefined"));
				return params;
			}
		};
		stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		
		requestQueue.add(stringRequest1);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
