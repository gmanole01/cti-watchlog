package manolegeorge.watchlog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class MoviesByGenreActivity extends AppCompatActivity {

	private int genreId;

	private ImageLoader imageLoader;
	private LayoutInflater inflater;
	private RequestQueue requestQueue;
	private SharedPreferences userSP;

	private int totalMovies = 0;
	private int loadedMovies = 0;
	private int moviesToLoad = 30;

	List<MovieInfo> movies = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if(!getIntent().hasExtra("genre_id") || !getIntent().hasExtra("genre_name"))
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

		final MoviesListAdapter adapter = new MoviesListAdapter(inflater, movies, imageLoader);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(MoviesByGenreActivity.this, ViewMovieActivity.class);
				intent.putExtra("movie_id", movies.get(position).getId());
				intent.putExtra("movie_title", movies.get(position).getTitle());
				startActivity(intent);
			}
		});

		final Button loadMore = (Button)inflater.inflate(R.layout.list_view_movies_footer, null);

		loadMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/get_movies_by_genre", new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							JSONObject jsonObject = new JSONObject(response);
							if(!jsonObject.getBoolean("error")) {

								totalMovies = jsonObject.getInt("total_movies");

								JSONArray moviesJA = jsonObject.getJSONArray("movies");
								if(moviesJA.length() > 0) {
									for(int i = 0; i < moviesJA.length(); i++) {

										loadedMovies++;

										JSONObject movieJO = moviesJA.getJSONObject(i);
										JSONArray movieGenresJA = movieJO.getJSONArray("genres");

										List<GenreInfo> genres = new ArrayList<>();

										for(int j = 0; j < movieGenresJA.length(); j++) {
											genres.add(new GenreInfo(
												movieGenresJA.getJSONObject(i).getInt("id"),
												movieGenresJA.getJSONObject(i).getString("name")
											));
										}

										manolegeorge.watchlog.info.MovieInfo newMovie = new manolegeorge.watchlog.info.MovieInfo(movieJO.getInt("id"), movieJO.getString("title"));
										newMovie.setReleaseDate(movieJO.getString("release_date"));
										newMovie.setPoster(movieJO.getString("poster"));
										newMovie.setRating(movieJO.getDouble("rating"));
										newMovie.setGenres(genres);

										movies.add(newMovie);

									}
									adapter.notifyDataSetChanged();
								}
								if(loadedMovies >= totalMovies) {
									listView.removeFooterView(loadMore);
								}

							} else {
								Toast.makeText(MoviesByGenreActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
							}
						} catch(JSONException e) {
							Toast.makeText(MoviesByGenreActivity.this, "JSONException", Toast.LENGTH_LONG).show();
						}
						loadMore.setClickable(true);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if(error instanceof TimeoutError) {
							Toast.makeText(MoviesByGenreActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
						} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
							Toast.makeText(MoviesByGenreActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(MoviesByGenreActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
						}
						loadMore.setClickable(true);
					}
				}) {
					@Override
					protected Map<String, String> getParams() {
						Map<String, String> params = new HashMap<>();
						params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
						params.put("email_address", userSP.getString("email_address", "undefined"));
						params.put("genre_id", String.valueOf(genreId));
						params.put("language", getResources().getConfiguration().locale.getLanguage());
						params.put("loaded_movies", String.valueOf(loadedMovies));
						params.put("movies_to_load", String.valueOf(moviesToLoad));
						params.put("password", userSP.getString("password", "undefined"));
						return params;
					}
				};
				stringRequest2.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

				loadMore.setClickable(false);
				requestQueue.add(stringRequest2);

			}
		});

		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/get_movies_by_genre", new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {

					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {

						totalMovies = jsonObject.getInt("total_movies");

						JSONArray moviesJA = jsonObject.getJSONArray("movies");
						if(moviesJA.length() > 0) {
							for(int i = 0; i < moviesJA.length(); i++) {

								loadedMovies++;

								JSONObject movieJO = moviesJA.getJSONObject(i);
								JSONArray movieGenresJA = movieJO.getJSONArray("genres");

								List<GenreInfo> genres = new ArrayList<>();

								for(int j = 0; j < movieGenresJA.length(); j++) {
									genres.add(new GenreInfo(
										movieGenresJA.getJSONObject(j).getInt("id"),
										movieGenresJA.getJSONObject(j).getString("name")
									));
								}

								manolegeorge.watchlog.info.MovieInfo newMovie = new manolegeorge.watchlog.info.MovieInfo(movieJO.getInt("id"), movieJO.getString("title"));
								newMovie.setReleaseDate(movieJO.getString("release_date"));
								newMovie.setPoster(movieJO.getString("poster"));
								newMovie.setRating(movieJO.getDouble("rating"));
								newMovie.setGenres(genres);

								movies.add(newMovie);

							}
							adapter.notifyDataSetChanged();
							if(loadedMovies < totalMovies) {
								listView.addFooterView(loadMore);
							}
							textView.setVisibility(View.GONE);
							listView.setVisibility(View.VISIBLE);
						}

					} else {
						textView.setText(jsonObject.getString("error_msg"));
					}

				} catch(JSONException e) {
					textView.setText("JSONException");
				}
				WatchLog.Utils.fadeOut(loading);
				WatchLog.Utils.fadeIn(content);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if(error instanceof TimeoutError) {
					textView.setText(getResources().getString(R.string.weak_internet_connection));
				} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
					textView.setText(getResources().getString(R.string.no_internet_connection));
				} else {
					textView.setText(getResources().getString(R.string.error));
				}
				WatchLog.Utils.fadeOut(loading);
				WatchLog.Utils.fadeIn(content);
			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<>();
				params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
				params.put("email_address", userSP.getString("email_address", "undefined"));
				params.put("genre_id", String.valueOf(genreId));
				params.put("language", getResources().getConfiguration().locale.getLanguage());
				params.put("loaded_movies", String.valueOf(loadedMovies));
				params.put("movies_to_load", String.valueOf(moviesToLoad));
				params.put("password", userSP.getString("password", "undefined"));
				return params;
			}
		};
		stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(stringRequest1);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
