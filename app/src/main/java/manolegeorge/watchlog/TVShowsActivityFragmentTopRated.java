package manolegeorge.watchlog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class TVShowsActivityFragmentTopRated extends Fragment {

	private ImageLoader imageLoader;
	private RequestQueue requestQueue;
	private SharedPreferences userSP;

	private int page = 1;

	private List<MovieInfo> tvShows = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		imageLoader = WatchLog.ImageLoader(getContext());
		requestQueue = Volley.newRequestQueue(getContext());
		userSP = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);

	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.activity_movies_fragment_latest, null);

		final LinearLayout content = view.findViewById(R.id.content);
		final ListView listView = view.findViewById(R.id.list_view);
		final TextView textView = view.findViewById(R.id.text_view);
		final LinearLayout loading = view.findViewById(R.id.loading);

		final MoviesListAdapter adapter = new MoviesListAdapter(inflater, tvShows, imageLoader);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getContext(), ViewTVShowActivity.class);
				intent.putExtra("tv_show_id", tvShows.get(position).getId());
				intent.putExtra("tv_show_name", tvShows.get(position).getTitle());
				startActivity(intent);
			}
		});

		final Button loadMore = (Button)inflater.inflate(R.layout.list_view_movies_footer, null);

		loadMore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				page = page + 1;

				StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/top_rated", new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							JSONObject jsonObject = new JSONObject(response);
							if(!jsonObject.getBoolean("error")) {

								JSONArray moviesJA = jsonObject.getJSONArray("tv_shows");
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

										manolegeorge.watchlog.info.MovieInfo newMovie = new manolegeorge.watchlog.info.MovieInfo(movieJO.getInt("id"), movieJO.getString("name"));
										newMovie.setReleaseDate(movieJO.getString("air_date"));
										newMovie.setPoster(movieJO.getString("poster"));
										newMovie.setRating(movieJO.getDouble("rating"));
										newMovie.setGenres(genres);

										tvShows.add(newMovie);

									}
									adapter.notifyDataSetChanged();
								}
							} else {
								Toast.makeText(getContext(), jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
							}
						} catch(JSONException e) {
							Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
						}
						loadMore.setClickable(true);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if(error instanceof TimeoutError) {
							Toast.makeText(getContext(), getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
						} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
							Toast.makeText(getContext(), getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
						} else {
							Toast.makeText(getContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
						}
						loadMore.setClickable(true);
					}
				}) {
					@Override
					protected Map<String, String> getParams() {
						Map<String, String> params = new HashMap<>();
						params.put("page", String.valueOf(page));
						return params;
					}
				};
				stringRequest2.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

				loadMore.setClickable(false);
				requestQueue.add(stringRequest2);

			}
		});

		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/top_rated", new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				try {

					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {
						JSONArray moviesJA = jsonObject.getJSONArray("tv_shows");
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

								manolegeorge.watchlog.info.MovieInfo newMovie = new manolegeorge.watchlog.info.MovieInfo(movieJO.getInt("id"), movieJO.getString("name"));
								newMovie.setReleaseDate(movieJO.getString("air_date"));
								newMovie.setPoster(movieJO.getString("poster"));
								newMovie.setRating(movieJO.getDouble("rating"));
								newMovie.setGenres(genres);

								tvShows.add(newMovie);

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
					e.printStackTrace();
					textView.setText("JSONException");
				}
				WatchLog.Utils.fadeOut(loading);
				WatchLog.Utils.fadeIn(content);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
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
		});
		stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(stringRequest1);

		return view;

	}

}
