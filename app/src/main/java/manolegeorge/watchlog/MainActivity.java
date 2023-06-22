package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.navigation.NavigationView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("InflateParams")
@SuppressWarnings({"deprecation"})

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private CircleImageView profilePicture;
	private ImageAware profilePictureIA;

	ImageLoader imageLoader;
	RequestQueue requestQueue;
	SharedPreferences userSP;

	private EditText addFriendUsername;

	private AlertDialog addFriendDialog;

	ProgressDialog progressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		getWindow().setStatusBarColor(Color.argb(0, 0, 0, 0));

		setContentView(R.layout.activity_main);

		setTitle(getResources().getString(R.string.dashboard));

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		
		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		imageLoader = WatchLog.ImageLoader(this);
		requestQueue = Volley.newRequestQueue(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);

		View navHeaderView = navigationView.getHeaderView(0);
		profilePicture = navHeaderView.findViewById(R.id.profile_picture);
		TextView username = navHeaderView.findViewById(R.id.username);
		TextView emailAddress = navHeaderView.findViewById(R.id.email_address);

		profilePictureIA = new ImageViewAware(profilePicture, false);
		imageLoader.displayImage(WatchLog.Utils.getProfilePicture(userSP), profilePictureIA, WatchLog.getImageLoaderOptions());
		username.setText(userSP.getString("username", "undefined"));
		emailAddress.setText(userSP.getString("email_address", "undefined"));

		navigationView.getMenu().getItem(0).setChecked(true);

		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View addFriendDialogView = inflater.inflate(R.layout.dialog_add_friend, null);
		addFriendUsername = addFriendDialogView.findViewById(R.id.username);

		addFriendDialog = new AlertDialog.Builder(this)
			.setCancelable(false)
			.setMessage(getResources().getString(R.string.add_friend_dialog_message))
			.setPositiveButton(getResources().getString(R.string.ok), null)
			.setNegativeButton(getResources().getString(R.string.cancel), null)
			.setView(addFriendDialogView).create();

		addFriendDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialog) {

				Button positive = addFriendDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				Button negative = addFriendDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

				positive.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						final String username = addFriendUsername.getText().toString().trim();
						if(username.length() > 0) {

							StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_URL + "/friends/add", new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									progressDialog.dismiss();
									try {
										JSONObject jsonObject = new JSONObject(response);
										if(!jsonObject.getBoolean("error")) {
											addFriendUsername.setText("");
											dialog.dismiss();
											Toast.makeText(MainActivity.this, getResources().getString(R.string.add_friend_success), Toast.LENGTH_LONG).show();
										} else {
											Toast.makeText(MainActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
										}
									} catch(JSONException e) {
										Toast.makeText(MainActivity.this, "JSONException", Toast.LENGTH_LONG).show();
									}
								}
							}, new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									progressDialog.dismiss();
									dialog.dismiss();
									error.printStackTrace();
									Toast.makeText(MainActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
								}
							}) {
								@Override
								protected Map<String, String> getParams() {
									Map<String, String> params = new HashMap<>();
									params.put("username", username);
									return params;
								}
								
								@Override
								public Map<String, String> getHeaders() {
									Map<String, String> headers = new HashMap<>();
									headers.put("Accept", "application/json");
									headers.put("Authorization", "Bearer " + userSP.getString("auth_token", ""));
									return headers;
								}
							};
							stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

							progressDialog.show();
							requestQueue.add(stringRequest);

						} else {
							Toast.makeText(MainActivity.this, getResources().getString(R.string.empty_field), Toast.LENGTH_LONG).show();
						}
					}
				});

				negative.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						addFriendUsername.setText("");
						dialog.dismiss();
					}
				});

			}
		});

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.please_wait));
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if(drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		imageLoader.displayImage(WatchLog.Utils.getProfilePicture(userSP), profilePictureIA, WatchLog.getImageLoaderOptions());
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);

		switch(item.getItemId()) {

			case R.id.watched_movies:
				startActivity(new Intent(MainActivity.this, WatchedMoviesActivity.class));
				break;
			case R.id.favourite_movies:
				startActivity(new Intent(MainActivity.this, FavouriteMoviesActivity.class));
				return true;
			case R.id.watched_tv_shows:
				startActivity(new Intent(MainActivity.this, WatchedTVShowsActivity.class));
				return true;
			case R.id.favourite_tv_shows:
				startActivity(new Intent(MainActivity.this, FavouriteTVShowsActivity.class));
				return true;
			case R.id.movies:
				startActivity(new Intent(MainActivity.this, MoviesActivity.class));
				return true;
			case R.id.tv_shows:
				startActivity(new Intent(MainActivity.this, TVShowsActivity.class));
				return true;
			case R.id.my_friends:
				startActivity(new Intent(MainActivity.this, MyFriendsActivity.class));
				return true;
			case R.id.friend_requests:
				startActivity(new Intent(MainActivity.this, FriendRequestsActivity.class));
				return true;
			case R.id.add_friend:
				addFriendDialog.show();
				return true;
			case R.id.account_settings:
				startActivity(new Intent(MainActivity.this, AccountSettingsActivity.class));
				return true;
			case R.id.logout:

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(true);
				builder.setMessage(getResources().getString(R.string.log_out_confirmation));
				builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.clear();
						editor.apply();

						Intent intent = new Intent(MainActivity.this, LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);

					}
				});
				builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
				builder.create().show();

				return true;

		}

		return false;

	}

}
