package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

public class MoviesActivityFragmentLatest extends Fragment {

	private ImageLoader imageLoader;
	private RequestQueue requestQueue;
	private SharedPreferences userSP;
	
	private int page = 1;

	List<MovieInfo> movies = new ArrayList<>();

	@SuppressWarnings("ConstantConditions")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		imageLoader = WatchLog.ImageLoader(getContext());
		requestQueue = Volley.newRequestQueue(getContext());
		userSP = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);

	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.activity_movies_fragment_latest, null);

		final LinearLayout content = view.findViewById(R.id.content);
		final ListView listView = view.findViewById(R.id.list_view);
		final TextView textView = view.findViewById(R.id.text_view);
		final LinearLayout loading = view.findViewById(R.id.loading);

		final MoviesListAdapter adapter = new MoviesListAdapter(inflater, movies, imageLoader);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener((parent, view1, position, id) -> {
			Intent intent = new Intent(getContext(), ViewMovieActivity.class);
			intent.putExtra("movie_id", movies.get(position).getId());
			intent.putExtra("movie_title", movies.get(position).getTitle());
			startActivity(intent);
		});

		@SuppressLint("InflateParams")
		final Button loadMore = (Button)inflater.inflate(R.layout.list_view_movies_footer, null);

		loadMore.setOnClickListener(v -> {
			page = page + 1;
			
			StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/movies/discover", response -> {
				try {
					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {

						JSONArray moviesJA = jsonObject.getJSONArray("movies");
						if(moviesJA.length() > 0) {
							for(int i = 0; i < moviesJA.length(); i++) {
								JSONObject movieJO = moviesJA.getJSONObject(i);
								JSONArray movieGenresJA = movieJO.getJSONArray("genres");

								List<GenreInfo> genres = new ArrayList<>();

								for(int j = 0; j < movieGenresJA.length(); j++) {
									genres.add(new GenreInfo(
										movieGenresJA.getJSONObject(j).getInt("id"),
										movieGenresJA.getJSONObject(j).getString("name")
									));
								}

								MovieInfo newMovie = new MovieInfo(movieJO.getInt("id"), movieJO.getString("title"));
								newMovie.setReleaseDate(movieJO.getString("release_date"));
								newMovie.setPoster(movieJO.getString("poster"));
								newMovie.setGenres(genres);

								movies.add(newMovie);

							}
							adapter.notifyDataSetChanged();
						}
					} else {
						Toast.makeText(getContext(), jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
					}
				} catch(JSONException e) {
					Toast.makeText(getContext(), "JSONException", Toast.LENGTH_LONG).show();
					Log.e("XYZ", e.getMessage());
				}
				loadMore.setClickable(true);
			}, error -> {
				if(error instanceof TimeoutError) {
					Toast.makeText(getContext(), getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
				} else if(error instanceof NetworkError) {
					Toast.makeText(getContext(), getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
				}
				loadMore.setClickable(true);
			}) {
				@Override
				protected Map<String, String> getParams() {
					Map<String, String> params = new HashMap<>();
					params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
					params.put("email_address", userSP.getString("email_address", "undefined"));
					params.put("language", getResources().getConfiguration().getLocales().get(0).getLanguage());
					params.put("password", userSP.getString("password", "undefined"));
					params.put("page", String.valueOf(page));
					return params;
				}
			};
			stringRequest2.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			loadMore.setClickable(false);
			requestQueue.add(stringRequest2);

		});

		@SuppressLint("SetTextI18n")
		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/movies/discover", response -> {
			try {

				JSONObject jsonObject = new JSONObject(response);
				if(!jsonObject.getBoolean("error")) {

					JSONArray moviesJA = jsonObject.getJSONArray("movies");
					if(moviesJA.length() > 0) {
						for(int i = 0; i < moviesJA.length(); i++) {
							JSONObject movieJO = moviesJA.getJSONObject(i);
							JSONArray movieGenresJA = movieJO.getJSONArray("genres");

							List<GenreInfo> genres = new ArrayList<>();

							for(int j = 0; j < movieGenresJA.length(); j++) {
								genres.add(new GenreInfo(
									movieGenresJA.getJSONObject(j).getInt("id"),
									movieGenresJA.getJSONObject(j).getString("name")
								));
							}

							MovieInfo newMovie = new MovieInfo(movieJO.getInt("id"), movieJO.getString("title"));
							newMovie.setReleaseDate(movieJO.getString("release_date"));
							newMovie.setPoster(movieJO.getString("poster"));
							newMovie.setRating(movieJO.getDouble("rating"));
							newMovie.setGenres(genres);

							movies.add(newMovie);

						}
						adapter.notifyDataSetChanged();
						listView.addFooterView(loadMore);
						textView.setVisibility(View.GONE);
						listView.setVisibility(View.VISIBLE);
					}

				} else {
					textView.setText(jsonObject.getString("error_msg"));
				}

			} catch(JSONException e) {
				textView.setText("JSONException");
				e.printStackTrace();
			}
			WatchLog.Utils.fadeOut(loading);
			WatchLog.Utils.fadeIn(content);
		}, error -> {
			if(error instanceof TimeoutError) {
				textView.setText(getResources().getString(R.string.weak_internet_connection));
			} else if(error instanceof NetworkError) {
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
				params.put("password", userSP.getString("password", "undefined"));
				return params;
			}
		};
		stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(stringRequest1);

		return view;

	}

}
