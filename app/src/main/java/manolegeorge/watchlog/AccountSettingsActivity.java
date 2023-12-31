package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("deprecation")

public class AccountSettingsActivity extends AppCompatActivity {

    CircleImageView profilePicture;
    ImageAware profilePictureIA;

    ProgressDialog progressDialog;

    ImageLoader imageLoader;
    SharedPreferences userSP;
    RequestQueue requestQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setTitle(getResources().getString(R.string.account_settings));

        profilePicture = findViewById(R.id.profile_picture);
        TextView username = findViewById(R.id.username);
        TextView emailAddress = findViewById(R.id.email_address);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));

        getFragmentManager().beginTransaction().replace(R.id.frame_layout, new SettingsFragment()).commit();

        imageLoader = WatchLog.ImageLoader(this);
        userSP = getSharedPreferences("user", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        Bitmap profilePictureBitmap = WatchLog.Utils.getProfilePictureRounded(userSP);

        profilePictureIA = new ImageViewAware(profilePicture, false);
        imageLoader.displayImage(WatchLog.Utils.getProfilePicture(userSP), profilePictureIA, WatchLog.getImageLoaderOptions());
        username.setText(userSP.getString("username", "undefined"));
        emailAddress.setText(userSP.getString("email_address", "undefined"));

        profilePicture.setOnClickListener(view -> {

			String[] items = {
				getResources().getString(R.string.change_profile_picture),
				getResources().getString(R.string.view_profile_picture)
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(AccountSettingsActivity.this);
			builder.setItems(items, (dialogInterface, position) -> {
				if(position == 0) {

					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

				} else if(position == 1) {
				  startActivity(new Intent(AccountSettingsActivity.this, ViewProfilePictureActivity.class));
				}
			});
			builder.create().show();

		});

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingSuperCall")
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                if(imageUri != null) {
                    final String extension = getExtension(AccountSettingsActivity.this, imageUri);
                    if(extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")) {
                        try {

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
    
                            final String base64 = WatchLog.bitmapToBase64(bitmap, extension);
                            if(base64 != null) {
        
                                StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/account/picture/new", response -> {
									progressDialog.dismiss();
									try {
										JSONObject jsonObject = new JSONObject(response);
										if(!jsonObject.getBoolean("error")) {
					
											String nProfilePicture = jsonObject.getJSONObject("data").getString("url");
					
											SharedPreferences.Editor userSPEditor = userSP.edit();
											userSPEditor.putString("profile_picture", nProfilePicture);
											userSPEditor.apply();
					
											imageLoader.displayImage(WatchLog.Utils.getProfilePicture(userSP), profilePictureIA, WatchLog.getImageLoaderOptions());
					
										} else {
											Toast.makeText(getApplicationContext(), jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
										}
									} catch(JSONException e) {
										e.printStackTrace();
										Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
									}
								}, error -> {
									error.printStackTrace();
									progressDialog.dismiss();
									Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
								}) {
                                    @Override
                                    protected Map<String, String> getParams() {
                                        Map<String, String> params = new HashMap<>();
                                        params.put("extension", extension);
                                        params.put("profile_picture", base64);
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
                                stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        
                                progressDialog.show();
                                requestQueue.add(stringRequest1);
        
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                            }

                        } catch(IOException e) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public static String getExtension(Context context, Uri uri) {
        if(uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.account_settings);

            SharedPreferences userSP = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);

            Preference emailAddress = findPreference("dp_email_address");
            Preference username = findPreference("dp_username");

            emailAddress.setSummary(userSP.getString("email_address", "undefined"));
            username.setSummary(userSP.getString("username", "undefined"));

        }


    }

}
