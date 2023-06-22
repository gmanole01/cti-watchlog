package manolegeorge.watchlog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ConstantConditions")

public class ResetPasswordActivity extends AppCompatActivity {

	RequestQueue requestQueue;
	SharedPreferences userSP;

	EditText emailAddress;

	ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(getResources().getString(R.string.reset_password));

		requestQueue = Volley.newRequestQueue(this);
		userSP = getSharedPreferences("user", Context.MODE_PRIVATE);

		emailAddress = findViewById(R.id.email_address);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getResources().getString(R.string.please_wait));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_reset_password, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

			case android.R.id.home:
				finish();
				return true;
			case R.id.done:

				final String sEmailAddress = emailAddress.getText().toString().trim();
				if(sEmailAddress.length() > 0) {

					StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_URL + "/reset_password", new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							progressDialog.dismiss();
							try {
								JSONObject jsonObject = new JSONObject(response);
								if(!jsonObject.getBoolean("error")) {
									finish();
									Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.reset_password_success), Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(ResetPasswordActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
								}
							} catch(JSONException e) {
								Toast.makeText(ResetPasswordActivity.this, "JSONException", Toast.LENGTH_LONG).show();
							}
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							progressDialog.dismiss();
							Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
						}
					}) {
						@Override
						protected Map<String, String> getParams() {
							Map<String, String> params = new HashMap<>();
							params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
							params.put("email_address", sEmailAddress);
							params.put("language", getResources().getConfiguration().locale.getLanguage());
							return params;
						}
					};
					stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

					progressDialog.show();
					requestQueue.add(stringRequest);

				} else {
					Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.empty_field), Toast.LENGTH_LONG).show();
				}

				return true;

		}
		return super.onOptionsItemSelected(item);
	}

}
