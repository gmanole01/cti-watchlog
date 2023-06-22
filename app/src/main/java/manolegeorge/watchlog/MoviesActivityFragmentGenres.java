package manolegeorge.watchlog;

import android.annotation.SuppressLint;
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

import manolegeorge.watchlog.info.GenreInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ConstantConditions")

public class MoviesActivityFragmentGenres extends Fragment {
	
	RequestQueue requestQueue;
	SharedPreferences userSP;
	
	List<GenreInfo> genres = new ArrayList<>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		requestQueue = Volley.newRequestQueue(getContext());
		userSP = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
		
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		@SuppressLint("InflateParams") View view = inflater.inflate(R.layout.activity_movies_fragment_genres, null);
		
		final LinearLayout content = view.findViewById(R.id.content);
		final ListView listView = view.findViewById(R.id.list_view);
		final TextView textView = view.findViewById(R.id.text_view);
		final LinearLayout loading = view.findViewById(R.id.loading);
		
		final ListAdapter adapter = new ListAdapter(inflater, genres);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener((adapterView, view1, position, l) -> {
			Intent intent = new Intent(getContext(), MoviesByGenreActivity.class);
			intent.putExtra("genre_id", genres.get(position).getId());
			intent.putExtra("genre_name", genres.get(position).getName());
			startActivity(intent);
		});
		
		@SuppressLint("SetTextI18n")
		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/movies/genres", response -> {
			try {
				
				JSONObject jsonObject = new JSONObject(response);
				if (!jsonObject.getBoolean("error")) {
					
					JSONArray genresJA = jsonObject.getJSONArray("genres");
					if (genresJA.length() > 0) {
						for (int i = 0; i < genresJA.length(); i++) {
							genres.add(new GenreInfo(
									genresJA.getJSONObject(i).getInt("id"),
									genresJA.getJSONObject(i).getString("name")
							));
						}
						adapter.notifyDataSetChanged();
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
				params.put("language", getContext().getResources().getConfiguration().getLocales().get(0).getLanguage());
				params.put("password", userSP.getString("password", "undefined"));
				return params;
			}
		};
		stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		
		requestQueue.add(stringRequest1);
		
		return view;
		
	}
	
	private static class ListAdapter extends BaseAdapter {
		
		LayoutInflater inflater;
		List<GenreInfo> genres;
		
		ListAdapter(LayoutInflater mInflater, List<GenreInfo> mGenres) {
			this.inflater = mInflater;
			this.genres = mGenres;
		}
		
		@Override
		public int getCount() {
			return this.genres.size();
		}
		
		@Override
		public GenreInfo getItem(int i) {
			return this.genres.get(i);
		}
		
		@Override
		public long getItemId(int i) {
			return i;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			@SuppressLint({"ViewHolder", "InflateParams"})
			View view = this.inflater.inflate(R.layout.list_view_genres, null);
			TextView name = view.findViewById(R.id.name);
			name.setText(this.genres.get(position).getName());
			return view;
		}
	}
	
}
