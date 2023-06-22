package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

import manolegeorge.watchlog.info.TVShowInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint({"InflateParams", "SetTextI18n"})
@SuppressWarnings({"ConstantConditions", "deprecation"})

public class FavouriteTVShowsActivity extends AppCompatActivity {

	private List<TVShowInfo> tvShows = new ArrayList<>();

	SharedPreferences userSP;
	RequestQueue requestQueue;
	ImageLoader imageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favourite_tv_shows);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(getResources().getString(R.string.favourite_tv_shows));

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

		final GridAdapter adapter = new GridAdapter(inflater, tvShows, imageLoader);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(FavouriteTVShowsActivity.this, ViewTVShowActivity.class);
				intent.putExtra("tv_show_id", tvShows.get(position).getId());
				intent.putExtra("tv_show_name", tvShows.get(position).getName());
				startActivity(intent);
			}
		});
		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

				CharSequence[] menuItems = new CharSequence[] {
					getResources().getString(R.string.delete),
					getResources().getString(R.string.details)
				};

				AlertDialog.Builder builder1 = new AlertDialog.Builder(FavouriteTVShowsActivity.this);
				builder1.setTitle(tvShows.get(position).getName());
				builder1.setItems(menuItems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == 0) {

							StringRequest stringRequest3 = new StringRequest(Request.Method.POST, Constants.API_URL + "/remove_favourite_movie", new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									try {
										JSONObject jsonObject = new JSONObject(response);
										if(!jsonObject.getBoolean("error")) {
											tvShows.remove(position);
											adapter.notifyDataSetChanged();
										} else {
											Toast.makeText(FavouriteTVShowsActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
										}
									} catch(JSONException e) {
										Toast.makeText(FavouriteTVShowsActivity.this, "JSONException", Toast.LENGTH_LONG).show();
									}
									progressDialog.dismiss();
								}
							}, new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									progressDialog.dismiss();
									if(error instanceof TimeoutError) {
										Toast.makeText(FavouriteTVShowsActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
									} else if(error instanceof NoConnectionError || error instanceof NetworkError) {
										Toast.makeText(FavouriteTVShowsActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
									} else {
										Toast.makeText(FavouriteTVShowsActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
									}
								}
							}) {
								@Override
								protected Map<String, String> getParams() {
									Map<String, String> params = new HashMap<>();
									params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
									params.put("email_address", userSP.getString("email_address", "undefined"));
									params.put("language", getResources().getConfiguration().locale.getLanguage());
									params.put("tv_show_id", String.valueOf(tvShows.get(position).getId()));
									params.put("password", userSP.getString("password", "undefined"));
									return params;
								}
							};
							stringRequest3.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

							progressDialog.show();
							requestQueue.add(stringRequest3);

						} else if(which == 1) {
							/*
							View watchedMovieDetailsDialogView = inflater.inflate(R.layout.dialog_favourite_movie_details, null);

							TextView watchedDate = watchedMovieDetailsDialogView.findViewById(R.id.watched_date);
							watchedDate.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(new Date(tvShows.get(position).getTimestamp() * 1000)));

							AlertDialog.Builder builder2 = new AlertDialog.Builder(FavouriteTVShowsActivity.this);
							builder2.setCancelable(false);
							builder2.setView(watchedMovieDetailsDialogView);
							builder2.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {}
							});
							builder2.show();
							*/
						}
					}
				});
				builder1.show();
				return true;
			}
		});

		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/favourites/all", new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {

						JSONArray tvShowsJA = jsonObject.getJSONArray("tv_shows");
						if(tvShowsJA.length() > 0) {
							for(int i = 0; i < tvShowsJA.length(); i++) {

								JSONObject tvShowJO = tvShowsJA.getJSONObject(i);

								TVShowInfo newTVShow = new TVShowInfo(tvShowJO.getInt("id"), tvShowJO.getString("name"));
								newTVShow.setPoster(tvShowJO.getString("poster"));

								tvShows.add(newTVShow);

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
		switch(item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class GridAdapter extends BaseAdapter {

		private final LayoutInflater inflater;
		private final List<TVShowInfo> tvShows;
		private final ImageLoader imageLoader;
		private final DisplayImageOptions options;

		GridAdapter(LayoutInflater inflater, List<TVShowInfo> mTVShows, ImageLoader imageLoader) {
			this.inflater = inflater;
			this.tvShows = mTVShows;
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
			return this.tvShows.size();
		}

		@Override
		public TVShowInfo getItem(int position) {
			return this.tvShows.get(position);
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
			imageLoader.displayImage(tvShows.get(position).getPoster(), holder.poster, this.options);
			return convertView;
		}

		public class ViewHolder {
			public ImageView poster;
		}

	}

}
