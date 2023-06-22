package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_friends);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(getResources().getString(R.string.my_friends));

		LayoutInflater inflater = LayoutInflater.from(this);

		final RelativeLayout content = findViewById(R.id.content);
		final RecyclerView recyclerView = findViewById(R.id.recycler_view);
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
		
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);
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

		@SuppressLint("NotifyDataSetChanged")
		StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/friends/all", response -> {
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
						recyclerView.setVisibility(View.VISIBLE);
						content.setGravity(Gravity.TOP);
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
			
			error.printStackTrace();
			
			WatchLog.Utils.fadeOut(loading);
			WatchLog.Utils.fadeIn(content);
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
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static class VH extends RecyclerView.ViewHolder {
		private View itemView;
		private ImageView profilePicture;
		private TextView username;
		private TextView date;
		public VH(@NonNull View itemView) {
			super(itemView);
			this.itemView = itemView;
			
			this.profilePicture = itemView.findViewById(R.id.profile_picture);
			this.username = itemView.findViewById(R.id.username);
			this.date = itemView.findViewById(R.id.date);
		}
	}

	public static class ListAdapter extends RecyclerView.Adapter<VH> {

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
		
		@NonNull
		@Override
		public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View v = inflater.inflate(R.layout.list_view_my_friends, parent, false);
			return new VH(v);
		}
		
		@Override
		public void onBindViewHolder(@NonNull VH holder, int position) {
			imageLoader.displayImage(friends.get(position).getUserInfo().getProfilePicture(), holder.profilePicture, new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).build());
			holder.username.setText(friends.get(position).getUserInfo().getUsername());
			holder.date.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(new Date(friends.get(position).getTimestamp() * 1000)));
		}
		
		@Override
		public int getItemCount() {
			return friends.size();
		}
	}

}
