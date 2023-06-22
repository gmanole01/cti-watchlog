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

@SuppressLint({"InflateParams", "SetTextI18n", "ViewHolder"})
@SuppressWarnings({"ConstantConditions", "deprecation"})

public class TVShowsActivityFragmentGenres extends Fragment {

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

		View view = inflater.inflate(R.layout.activity_movies_fragment_genres, null);

		final LinearLayout content = view.findViewById(R.id.content);
		final ListView listView = view.findViewById(R.id.list_view);
		final TextView textView = view.findViewById(R.id.text_view);
		final LinearLayout loading = view.findViewById(R.id.loading);

		final ListAdapter adapter = new ListAdapter(inflater, genres);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				Intent intent = new Intent(getContext(), TVShowsByGenreActivity.class);
				intent.putExtra("genre_id", genres.get(position).getId());
				intent.putExtra("genre_name", genres.get(position).getName());
				startActivity(intent);
			}
		});

		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/shows/genres", new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				try {

					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {

						JSONArray genresJA = jsonObject.getJSONArray("genres");
						if(genresJA.length() > 0) {
							for(int i = 0; i < genresJA.length(); i++) {
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

	private class ListAdapter extends BaseAdapter {

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
			View view = this.inflater.inflate(R.layout.list_view_genres, null);
			TextView name = view.findViewById(R.id.name);
			name.setText(this.genres.get(position).getName());
			return view;
		}
	}

}
