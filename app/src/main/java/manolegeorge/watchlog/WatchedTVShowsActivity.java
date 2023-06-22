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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.*;
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

@SuppressLint("SetTextI18n")
@SuppressWarnings("deprecation")

public class WatchedTVShowsActivity extends AppCompatActivity {

	ImageLoader imageLoader;
	RequestQueue requestQueue;
	SharedPreferences userSP;

	private List<TVShowInfo> tvShows = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watched_tv_shows);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(getResources().getString(R.string.watched_tv_shows));

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
				Intent intent = new Intent(WatchedTVShowsActivity.this, ViewTVShowActivity.class);
				intent.putExtra("tv_show_id", tvShows.get(position).getId());
				intent.putExtra("tv_show_name", tvShows.get(position).getName());
				startActivity(intent);
			}
		});

		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/get_watched_tv_shows", new Response.Listener<String>() {

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
								newTVShow.setEpisodesCount(tvShowJO.getInt("episodes_count"));
								newTVShow.setWatchedEpisodesCount(tvShowJO.getInt("watched_episodes_count"));

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
				params.put("language", getResources().getConfiguration().locale.getLanguage());
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

	public class GridAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private List<TVShowInfo> tvShows;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;

		GridAdapter(LayoutInflater mInflater, List<TVShowInfo> mTVShows, ImageLoader mImageLoader) {
			this.inflater = mInflater;
			this.tvShows = mTVShows;
			this.imageLoader = mImageLoader;
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
			return tvShows.size();
		}

		@Override
		public TVShowInfo getItem(int position) {
			return tvShows.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.grid_view_tv_shows, parent, false);
				holder = new ViewHolder();
				holder.poster = convertView.findViewById(R.id.poster);
				holder.progressBar = convertView.findViewById(R.id.progress_bar);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			imageLoader.displayImage(tvShows.get(position).getPoster(), holder.poster, this.options);
			holder.progressBar.setMax(tvShows.get(position).getEpisodesCount());
			holder.progressBar.setProgress(tvShows.get(position).getWatchedEpisodesCount());
			return convertView;
		}

		public class ViewHolder {
			public ImageView poster;
			public ProgressBar progressBar;
		}
	}

}
