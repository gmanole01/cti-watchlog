package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import manolegeorge.watchlog.info.MovieInfo;
import manolegeorge.watchlog.info.WatchedMovieInfo;

@SuppressLint({"InflateParams", "SetTextI18n"})

public class HiddenWatchedMoviesActivity extends AppCompatActivity {

	int totalMovies = 0;
	int loadedMovies = 0;
	int moviesToLoad = 30;

	boolean isLoadingMoreItems = false;

	List<WatchedMovieInfo> movies = new ArrayList<>();

	SharedPreferences userSP;
	RequestQueue requestQueue;
	ImageLoader imageLoader;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hidden_watched_movies);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		setTitle(getResources().getString(R.string.hidden_movies));

		final LinearLayout content = findViewById(R.id.content);
		final GridView gridView = findViewById(R.id.grid_view);
		final TextView textView = findViewById(R.id.text_view);
		final LinearLayout loading = findViewById(R.id.loading);

		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.please_wait));

		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);
		requestQueue = Volley.newRequestQueue(this);
		imageLoader = WatchLog.ImageLoader(this);

		final LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final GridAdapter adapter = new GridAdapter(inflater, movies, imageLoader);
		gridView.setAdapter(adapter);
		gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					if((firstVisibleItem + visibleItemCount) == totalItemCount) {
						if(loadedMovies < totalMovies && !isLoadingMoreItems) {

							StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/get_hidden_watched_movies", response -> {
								try {
									JSONObject jsonObject = new JSONObject(response);
									if(!jsonObject.getBoolean("error")) {

										totalMovies = jsonObject.getInt("total_movies");

										JSONArray moviesJA = jsonObject.getJSONArray("movies");
										if(moviesJA.length() > 0) {
											for(int i = 0; i < moviesJA.length(); i++) {

												loadedMovies++;

												JSONObject movieJO = moviesJA.getJSONObject(i);
												JSONObject movieDataJO = movieJO.getJSONObject("movie_data");

												MovieInfo newMovie = new MovieInfo(movieDataJO.getInt("id"), movieDataJO.getString("title"));
												newMovie.setReleaseDate(movieDataJO.getString("release_date"));
												newMovie.setPoster(movieDataJO.getString("poster"));

												movies.add(new WatchedMovieInfo(movieJO.getLong("timestamp"), newMovie));

											}
											adapter.notifyDataSetChanged();
										}

									} else {
										Toast.makeText(HiddenWatchedMoviesActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
									}
								} catch(JSONException e) {
									Toast.makeText(HiddenWatchedMoviesActivity.this, "JSONException", Toast.LENGTH_LONG).show();
								}
								isLoadingMoreItems = false;
							}, error -> {
								if(error instanceof TimeoutError) {
									Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
								} else if(error instanceof NetworkError) {
									Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
								}
								isLoadingMoreItems = false;
							}) {
								@Override
								protected Map<String, String> getParams() {
									Map<String, String> params = new HashMap<>();
									params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
									params.put("email_address", userSP.getString("email_address", "undefined"));
									params.put("language", getResources().getConfiguration().locale.getLanguage());
									params.put("loaded_movies", String.valueOf(loadedMovies));
									params.put("movies_to_load", String.valueOf(moviesToLoad));
									params.put("password", userSP.getString("password", "undefined"));
									return params;
								}
							};
							stringRequest2.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

							isLoadingMoreItems = true;
							requestQueue.add(stringRequest2);

						}
					}
				}

			});
			gridView.setOnItemClickListener((parent, view, position, id) -> {
				Intent intent = new Intent(HiddenWatchedMoviesActivity.this, ViewMovieActivity.class);
				intent.putExtra("movie_id", movies.get(position).getMovieInfo().getId());
				intent.putExtra("movie_title", movies.get(position).getMovieInfo().getTitle());
				startActivity(intent);
			});
			gridView.setOnItemLongClickListener((parent, view, position, id) -> {

				CharSequence[] menuItems = new CharSequence[] {
					getResources().getString(R.string.display),
					getResources().getString(R.string.delete),
					getResources().getString(R.string.details)
				};

				AlertDialog.Builder builder1 = new AlertDialog.Builder(HiddenWatchedMoviesActivity.this);
				builder1.setTitle(movies.get(position).getMovieInfo().getTitle());
				builder1.setItems(menuItems, (dialog, which) -> {
					if(which == 0) {

						StringRequest stringRequest4 = new StringRequest(Request.Method.POST, Constants.API_URL + "/remove_hidden_watched_movie", response -> {
							try {
								JSONObject jsonObject = new JSONObject(response);
								if(!jsonObject.getBoolean("error")) {
									movies.remove(position);
									totalMovies--;
									loadedMovies--;
									adapter.notifyDataSetChanged();
								} else {
									Toast.makeText(HiddenWatchedMoviesActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
								}
							} catch(JSONException e) {
								Toast.makeText(HiddenWatchedMoviesActivity.this, "JSONException", Toast.LENGTH_LONG).show();
							}
							progressDialog.dismiss();
						}, error -> {
							progressDialog.dismiss();
							if(error instanceof TimeoutError) {
								Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
							} else if(error instanceof NetworkError) {
								Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
							}
						}) {
							@Override
							protected Map<String, String> getParams() {
								Map<String, String> params = new HashMap<>();
								params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
								params.put("email_address", userSP.getString("email_address", "undefined"));
								params.put("language", getResources().getConfiguration().locale.getLanguage());
								params.put("movie_id", String.valueOf(movies.get(position).getMovieInfo().getId()));
								params.put("password", userSP.getString("password", "undefined"));
								return params;
							}
						};
						stringRequest4.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

						progressDialog.show();
						requestQueue.add(stringRequest4);

					} else if(which == 1) {

						StringRequest stringRequest3 = new StringRequest(Request.Method.POST, Constants.API_URL + "/remove_watched_movie", response -> {
							try {
								JSONObject jsonObject = new JSONObject(response);
								if(!jsonObject.getBoolean("error")) {
									movies.remove(position);
									totalMovies--;
									loadedMovies--;
									adapter.notifyDataSetChanged();
								} else {
									Toast.makeText(HiddenWatchedMoviesActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
								}
							} catch(JSONException e) {
								Toast.makeText(HiddenWatchedMoviesActivity.this, "JSONException", Toast.LENGTH_LONG).show();
							}
							progressDialog.dismiss();
						}, error -> {
							progressDialog.dismiss();
							if(error instanceof TimeoutError) {
								Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
							} else if(error instanceof NetworkError) {
								Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(HiddenWatchedMoviesActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
							}
						}) {
							@Override
							protected Map<String, String> getParams() {
								Map<String, String> params = new HashMap<>();
								params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
								params.put("email_address", userSP.getString("email_address", "undefined"));
								params.put("language", getResources().getConfiguration().locale.getLanguage());
								params.put("movie_id", String.valueOf(movies.get(position).getMovieInfo().getId()));
								params.put("password", userSP.getString("password", "undefined"));
								return params;
							}
						};
						stringRequest3.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

						progressDialog.show();
						requestQueue.add(stringRequest3);

					} else if(which == 2) {

						View watchedMovieDetailsDialogView = inflater.inflate(R.layout.dialog_watched_movie_details, null);

						TextView watchedDate = watchedMovieDetailsDialogView.findViewById(R.id.watched_date);
						watchedDate.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(new Date(movies.get(position).getTimestamp() * 1000)));

						AlertDialog.Builder builder2 = new AlertDialog.Builder(HiddenWatchedMoviesActivity.this);
						builder2.setCancelable(false);
						builder2.setView(watchedMovieDetailsDialogView);
						builder2.setPositiveButton(getResources().getString(R.string.ok), (dialog1, which1) -> {});
							builder2.show();

						}
					});
					builder1.show();
					return true;
				});

				StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/get_hidden_watched_movies", response -> {
					try {
						JSONObject jsonObject = new JSONObject(response);
						if(!jsonObject.getBoolean("error")) {

							totalMovies = jsonObject.getInt("total_movies");

							JSONArray moviesJA = jsonObject.getJSONArray("movies");
							if(moviesJA.length() > 0) {
								for(int i = 0; i < moviesJA.length(); i++) {

									loadedMovies++;

									JSONObject movieJO = moviesJA.getJSONObject(i);
									JSONObject movieDataJO = movieJO.getJSONObject("movie_data");

									MovieInfo newMovie = new MovieInfo(movieDataJO.getInt("id"), movieDataJO.getString("title"));
									newMovie.setReleaseDate(movieDataJO.getString("release_date"));
									newMovie.setPoster(movieDataJO.getString("poster"));

									movies.add(new WatchedMovieInfo(movieJO.getLong("timestamp"), newMovie));

								}
								adapter.notifyDataSetChanged();
								textView.setVisibility(View.GONE);
								gridView.setVisibility(View.VISIBLE);
							}

						} else {
							textView.setText(jsonObject.getString("error_msg"));
						}
					} catch(JSONException e) {
						textView.setText("JSONException");
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
				if (item.getItemId() == android.R.id.home) {
					finish();
					return true;
				}
				return super.onOptionsItemSelected(item);
			}

			public static class GridAdapter extends BaseAdapter {

				private final LayoutInflater inflater;
				private final List<WatchedMovieInfo> movies;
				private final ImageLoader imageLoader;
				private final DisplayImageOptions options;

				GridAdapter(LayoutInflater inflater, List<WatchedMovieInfo> movies, ImageLoader imageLoader) {
					this.inflater = inflater;
					this.movies = movies;
					this.imageLoader = imageLoader;
					this.options = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.no_poster)
					.showImageForEmptyUri(R.drawable.no_poster)
					.showImageOnFail(R.drawable.no_poster)
					.cacheInMemory(false)
					.cacheOnDisk(true)
					.build();
				}

				@Override
				public int getCount() {
					return movies.size();
				}

				@Override
				public WatchedMovieInfo getItem(int position) {
					return movies.get(position);
				}

				@Override
				public long getItemId(int position) {
					return position;
				}

				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					ViewHolder holder;
					if(convertView == null) {
						convertView = inflater.inflate(R.layout.grid_view_movies, parent, false);
						holder = new ViewHolder();
						holder.poster = convertView.findViewById(R.id.poster);
						convertView.setTag(holder);
					} else {
						holder = (ViewHolder)convertView.getTag();
					}
					imageLoader.displayImage(movies.get(position).getMovieInfo().getPoster(), holder.poster, this.options);
					return convertView;
				}

				public static class ViewHolder {
					public ImageView poster;
				}

			}

		}
