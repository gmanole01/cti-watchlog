package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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

import manolegeorge.watchlog.info.FriendRequestInfo;
import manolegeorge.watchlog.info.UserInfo;

@SuppressLint({"InflateParams", "SetTextI18n"})
@SuppressWarnings("ConstantConditions")

public class FriendRequestsActivity extends AppCompatActivity {

    ProgressDialog progressDialog;

    ImageLoader imageLoader;
    SharedPreferences userSP;
    RequestQueue requestQueue;

    ListAdapter adapter;

    List<FriendRequestInfo> friendRequests = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getResources().getString(R.string.friend_requests));

        LayoutInflater inflater = LayoutInflater.from(this);

        final LinearLayout content = findViewById(R.id.content);
        final ListView listView = findViewById(R.id.list_view);
        final TextView textView = findViewById(R.id.text_view);
        final LinearLayout loading = findViewById(R.id.loading);

        final Button loadMore = (Button)inflater.inflate(R.layout.list_view_friend_requests_received_footer, null);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));

        imageLoader = WatchLog.ImageLoader(this);
        userSP = getSharedPreferences("user", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        adapter = new ListAdapter(imageLoader, inflater, requestQueue, friendRequests);
        listView.setAdapter(adapter);

        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/friends/requests", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    if(!jsonObject.getBoolean("error")) {

                        JSONArray friendRequestsJA = jsonObject.getJSONArray("friend_requests");
                        if(friendRequestsJA.length() > 0) {
                            for(int i = 0; i < friendRequestsJA.length(); i++) {

                                JSONObject friendRequestJO = friendRequestsJA.getJSONObject(i);
                                JSONObject userDataJO = friendRequestJO.getJSONObject("user_data");

                                UserInfo userInfo = new UserInfo(userDataJO.getInt("id"));
                                userInfo.setProfilePicture(userDataJO.getString("profile_picture"));
                                userInfo.setUsername(userDataJO.getString("username"));

                                FriendRequestInfo newFriendRequest = new FriendRequestInfo(friendRequestJO.getInt("id"), friendRequestJO.getLong("timestamp"), userInfo);

                                friendRequests.add(newFriendRequest);

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
                params.put("password", userSP.getString("password", "undefined"));
                return params;
            }
    
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

    public class ListAdapter extends BaseAdapter {

        ImageLoader imageLoader;
        LayoutInflater inflater;
        RequestQueue requestQueue;
        List<FriendRequestInfo> friendRequests;

        ListAdapter(ImageLoader mImageLoader, LayoutInflater mInflater, RequestQueue mRequestQueue, List<FriendRequestInfo> mFriendRequests) {
            this.imageLoader = mImageLoader;
            this.inflater = mInflater;
            this.requestQueue = mRequestQueue;
            this.friendRequests = mFriendRequests;
        }

        @Override
        public int getCount() {
            return this.friendRequests.size();
        }

        @Override
        public FriendRequestInfo getItem(int position) {
            return this.friendRequests.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                convertView = this.inflater.inflate(R.layout.list_view_friend_requests_received, parent, false);
                holder = new ViewHolder();
                holder.profilePicture = convertView.findViewById(R.id.profile_picture);
                holder.username = convertView.findViewById(R.id.username);
                holder.date = convertView.findViewById(R.id.date);
                holder.accept = convertView.findViewById(R.id.accept);
                holder.refuse = convertView.findViewById(R.id.refuse);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            imageLoader.displayImage(friendRequests.get(position).getUserInfo().getProfilePicture(), holder.profilePicture, new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).build());
            holder.username.setText(friendRequests.get(position).getUserInfo().getUsername());
            holder.date.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(new Date(friendRequests.get(position).getTimestamp() * 1000)));
            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.show();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_URL + "/friends/accept", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if(!jsonObject.getBoolean("error")) {
                                    friendRequests.remove(position);
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(FriendRequestsActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
                                }
                            } catch(JSONException e) {
                                Toast.makeText(FriendRequestsActivity.this, "JSONException", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(FriendRequestsActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("id", String.valueOf(friendRequests.get(position).getId()));
                            return params;
                        }
    
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Accept", "application/json");
                            headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
                            return headers;
                        }
                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                }
            });
            holder.refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.show();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_URL + "/friends/refuse", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if(!jsonObject.getBoolean("error")) {
                                    friendRequests.remove(position);
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(FriendRequestsActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
                                }
                            } catch(JSONException e) {
                                Toast.makeText(FriendRequestsActivity.this, "JSONException", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(FriendRequestsActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("id", String.valueOf(friendRequests.get(position).getId()));
                            return params;
                        }
    
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Accept", "application/json");
                            headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
                            return headers;
                        }
                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView profilePicture;
            TextView username;
            TextView date;
            ImageButton accept;
            ImageButton refuse;
        }

    }

}
