package manolegeorge.watchlog;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

@SuppressWarnings("ConstantConditions")

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] items = {
                    getResources().getString(R.string.change_profile_picture),
                    getResources().getString(R.string.view_profile_picture)
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(AccountSettingsActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        if(position == 0) {

                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

                        } else if(position == 1) {
                          startActivity(new Intent(AccountSettingsActivity.this, ViewProfilePictureActivity.class));
                        }
                    }
                });
                builder.create().show();

            }
        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                if(imageUri != null) {
                    final String extension = getExtension(AccountSettingsActivity.this, imageUri);
                    if(extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")) {
                        try {

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            if(bitmap.getWidth() >= 1000 && bitmap.getHeight() >= 1000) {
                                if(bitmap.getWidth() <= 3000 && bitmap.getHeight() <= 3000) {

                                    final String base64 = WatchLog.bitmapToBase64(bitmap, extension);
                                    if(base64 != null) {

                                        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/change_profile_picture", new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
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
                                                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                                            }
                                        }) {
                                            @Override
                                            protected Map<String, String> getParams() {
                                                Map<String, String> params = new HashMap<>();
                                                params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
                                                params.put("email_address", userSP.getString("email_address", "undefined"));
                                                params.put("extension", extension);
                                                params.put("language", getResources().getConfiguration().locale.getLanguage());
                                                params.put("password", userSP.getString("password", "undefined"));
                                                params.put("profile_picture", base64);
                                                return params;
                                            }
                                        };
                                        stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                        progressDialog.show();
                                        requestQueue.add(stringRequest1);

                                    } else {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.profile_picture_error_2), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.profile_picture_error_1), Toast.LENGTH_LONG).show();
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
