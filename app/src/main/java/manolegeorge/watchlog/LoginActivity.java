package manolegeorge.watchlog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")

public class LoginActivity extends AppCompatActivity {
	
	private EditText emailAddress;
	private EditText password;
	private Button forgotPassword;
	private Button login;
	private Button register;
	
	private ProgressDialog progressDialog;
	
	private RequestQueue requestQueue;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		emailAddress = findViewById(R.id.email_address);
		password = findViewById(R.id.password);
		forgotPassword = findViewById(R.id.forgot_password);
		login = findViewById(R.id.login);
		register = findViewById(R.id.register);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.please_wait));

		requestQueue = Volley.newRequestQueue(this);

		forgotPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
			}
		});

		login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoginClick();
			}
		});

		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
			}
		});

	}
	
	private void onLoginClick() {
		final String emailAddressString = emailAddress.getText().toString().trim();
		final String passwordString = password.getText().toString().trim();
		if(emailAddressString.isEmpty() || passwordString.isEmpty()) {
			Toast.makeText(LoginActivity.this, getResources().getString(R.string.empty_fields), Toast.LENGTH_LONG).show();
			return;
		}
		
		FirebaseMessaging.getInstance().getToken()
			.addOnCompleteListener(new OnCompleteListener<String>() {
				@Override
				public void onComplete(@NonNull Task<String> task) {
					if (!task.isSuccessful()) {
						return;
					}
					
					// Get new FCM registration token
					String token = task.getResult();
					
					login.setClickable(false);
					progressDialog.show();
					
					sendLoginRequest(
						emailAddressString,
						passwordString,
						token
					);
				}
			});
	}
	
	private void sendLoginRequest(
		String emailAddress,
		String password,
		String fcmToken
	) {
		StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_URL + "/login", new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
				try {
					JSONObject jsonObject = new JSONObject(response);
					if (!jsonObject.getBoolean("error")) {
						
						JSONObject dataJO = jsonObject.getJSONObject("data");
						
						SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString("auth_token", dataJO.getString("access_token"));
						editor.putString("email_address", dataJO.getString("email_address"));
						editor.putString("username", dataJO.getString("username"));
						editor.apply();
						
						Intent intent = new Intent(LoginActivity.this, MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						
					} else {
						Toast.makeText(LoginActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(LoginActivity.this, "JSONException", Toast.LENGTH_LONG).show();
				}
				login.setClickable(true);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				progressDialog.dismiss();
				if(error instanceof TimeoutError) {
					Toast.makeText(LoginActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(LoginActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
				}
				login.setClickable(true);
			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<>();
				params.put("email", emailAddress);
				params.put("fcm_token", fcmToken);
				params.put("language", getResources().getConfiguration().locale.getLanguage());
				params.put("password", password);
				return params;
			}
		};
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		
		requestQueue.add(stringRequest);
	}

}
