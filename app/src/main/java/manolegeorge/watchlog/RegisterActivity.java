package manolegeorge.watchlog;

import android.app.ProgressDialog;
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
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
	
	private EditText emailAddress;
	private EditText username;
	private EditText password;
	private EditText repeatPassword;
	
	private RequestQueue requestQueue;
	private Button register;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		emailAddress = findViewById(R.id.email_address);
		username = findViewById(R.id.username);
		password = findViewById(R.id.password);
		repeatPassword = findViewById(R.id.repeat_password);
		register = findViewById(R.id.register);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.please_wait));

		requestQueue = Volley.newRequestQueue(this);

		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onRegisterClick();
			}
		});

	}
	
	public void onRegisterClick() {
		final String emailAddressString = emailAddress.getText().toString().trim();
		final String usernameString = username.getText().toString().trim();
		final String passwordString = password.getText().toString().trim();
		final String repeatPasswordString = repeatPassword.getText().toString().trim();
		
		if(
			emailAddressString.isEmpty() ||
			usernameString.isEmpty() ||
			passwordString.isEmpty() ||
			repeatPasswordString.isEmpty()
		) {
			Toast.makeText(
				getApplicationContext(),
				getResources().getString(R.string.empty_fields),
				Toast.LENGTH_LONG
			).show();
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
					
					register.setClickable(false);
					progressDialog.show();
					
					sendRegisterRequest(
						emailAddressString,
						usernameString,
						passwordString,
						repeatPasswordString,
						token
					);
				}
			});
	}
	
	private void sendRegisterRequest(
		String emailAddress,
		String username,
		String password,
		String repeatPassword,
		String fcmToken
	) {
		StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_URL + "/register", new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				progressDialog.dismiss();
				try {
					JSONObject jsonObject = new JSONObject(response);
					if(!jsonObject.getBoolean("error")) {
						Toast.makeText(
							RegisterActivity.this,
							"Contul tau a fost creat! Te poti autentifica acum!",
							Toast.LENGTH_LONG
						).show();
						finish();
					} else {
						Toast.makeText(RegisterActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					Toast.makeText(RegisterActivity.this, "JSONException", Toast.LENGTH_LONG).show();
				}
				register.setClickable(true);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				progressDialog.dismiss();
				if(error instanceof TimeoutError) {
					Toast.makeText(RegisterActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
						RegisterActivity.this,
						getResources().getString(R.string.error),
						Toast.LENGTH_LONG
					).show();
					error.printStackTrace();
				}
				register.setClickable(true);
			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> params = new HashMap<>();
				params.put("email_address", emailAddress);
				params.put("fcm_token", fcmToken);
				params.put("language", getResources().getConfiguration().getLocales().get(0).getLanguage());
				params.put("password", password);
				params.put("repeat_password", repeatPassword);
				params.put("username", username);
				return params;
			}
		};
		stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		
		requestQueue.add(stringRequest);
	}

}
