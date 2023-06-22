package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import manolegeorge.watchlog.info.FriendInfo;
import manolegeorge.watchlog.info.UserInfo;

@SuppressLint({"InflateParams", "SetTextI18n"})
@SuppressWarnings("ConstantConditions")

public class MyFriendsActivity extends AppCompatActivity {

	ProgressDialog progressDialog;

	ImageLoader imageLoader;
	SharedPreferences userSP;
	RequestQueue requestQueue;

	ListAdapter adapter;

	List<FriendInfo> friends = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_friends);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(getResources().getString(R.string.my_friends));

		LayoutInflater inflater = LayoutInflater.from(this);

		final LinearLayout content = findViewById(R.id.content);
		final ListView listView = findViewById(R.id.list_view);
		final TextView textView = findViewById(R.id.text_view);
		final LinearLayout loading = findViewById(R.id.loading);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.please_wait));

		imageLoader = WatchLog.ImageLoader(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);
		requestQueue = Volley.newRequestQueue(this);

		adapter = new ListAdapter(imageLoader, inflater, requestQueue, friends);
		listView.setAdapter(adapter);
		/*
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

				Intent intent = new Intent(MyFriendsActivity.this, ViewUserProfileActivity.class);
				intent.putExtra("user_id", friends.get(position).getUserInfo().getId());
				intent.putExtra("user_username", friends.get(position).getUserInfo().getUsername());
				startActivity(intent);

			}
		});
		*/

		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/friends/all", new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {

					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {

						JSONArray friendsJA = jsonObject.getJSONArray("friends");
						if(friendsJA.length() > 0) {
							for(int i = 0; i < friendsJA.length(); i++) {
								JSONObject friendJO = friendsJA.getJSONObject(i);
								JSONObject userDataJO = friendJO.getJSONObject("user_data");

								UserInfo userInfo = new UserInfo(userDataJO.getInt("id"));
								userInfo.setProfilePicture(userDataJO.getString("profile_picture"));
								userInfo.setUsername(userDataJO.getString("username"));

								FriendInfo newFriend = new FriendInfo(friendJO.getInt("id"), friendJO.getLong("timestamp"), userInfo);

								friends.add(newFriend);

							}
							adapter.notifyDataSetChanged();
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
				
				error.printStackTrace();
				
				WatchLog.Utils.fadeOut(loading);
				WatchLog.Utils.fadeIn(content);
			}
		}) {
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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class ListAdapter extends BaseAdapter {

	    ImageLoader imageLoader;
		LayoutInflater inflater;
		RequestQueue requestQueue;
		List<FriendInfo> friends;

		ListAdapter(ImageLoader mImageLoader, LayoutInflater mInflater, RequestQueue mRequestQueue, List<FriendInfo> mFriends) {
		    this.imageLoader = mImageLoader;
			this.inflater = mInflater;
			this.requestQueue = mRequestQueue;
			this.friends = mFriends;
		}

		@Override
		public int getCount() {
			return this.friends.size();
		}

		@Override
		public FriendInfo getItem(int position) {
			return this.friends.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null) {
				convertView = this.inflater.inflate(R.layout.list_view_my_friends, parent, false);
				holder = new ViewHolder();
				holder.profilePicture = convertView.findViewById(R.id.profile_picture);
				holder.username = convertView.findViewById(R.id.username);
				holder.date = convertView.findViewById(R.id.date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
            imageLoader.displayImage(friends.get(position).getUserInfo().getProfilePicture(), holder.profilePicture, new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).build());
			holder.username.setText(friends.get(position).getUserInfo().getUsername());
			holder.date.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(new Date(friends.get(position).getTimestamp() * 1000)));
			return convertView;
		}

		class ViewHolder {
			ImageView profilePicture;
			TextView username;
			TextView date;
		}

	}

}
