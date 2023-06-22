package manolegeorge.watchlog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import manolegeorge.watchlog.info.MovieInfo;

@SuppressLint("InflateParams")
@SuppressWarnings({"ConstantConditions", "deprecation"})

public class SearchMoviesActivity extends AppCompatActivity {

    private String searchQuery = "";

    private int totalMovies = 0;
    private int loadedMovies = 0;
    private int moviesToLoad = 50;

    List<MovieInfo> movies = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movies);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText editText = findViewById(R.id.edit_text);
        final ListView listView = findViewById(R.id.list_view);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));

        final SharedPreferences userSP = getSharedPreferences("user", Context.MODE_PRIVATE);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final ImageLoader imageLoader = WatchLog.ImageLoader(this);

        LayoutInflater inflater = LayoutInflater.from(this);

        final ListAdapter adapter = new ListAdapter(inflater, movies, imageLoader);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SearchMoviesActivity.this, ViewMovieActivity.class);
                intent.putExtra("movie_id", movies.get(position).getId());
                intent.putExtra("movie_title", movies.get(position).getTitle());
                startActivity(intent);
            }
        });

        final Button loadMore = (Button)inflater.inflate(R.layout.list_view_movies_footer, null);

        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StringRequest stringRequest2 = new StringRequest(Request.Method.POST, Constants.API_URL + "/search_movies", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(!jsonObject.getBoolean("error")) {

                                totalMovies = jsonObject.getInt("total_movies");

                                JSONArray moviesJA = jsonObject.getJSONArray("movies");
                                if(moviesJA.length() > 0) {
                                    for(int i = 0; i < moviesJA.length(); i++) {

                                        loadedMovies++;

                                        JSONObject movieJO = moviesJA.getJSONObject(i);

                                        manolegeorge.watchlog.info.MovieInfo newMovie = new manolegeorge.watchlog.info.MovieInfo(movieJO.getInt("id"), movieJO.getString("title"));
                                        newMovie.setReleaseDate(movieJO.getString("release_date"));
                                        newMovie.setPoster(movieJO.getString("poster"));

                                        movies.add(newMovie);

                                    }
                                } else {
                                    Toast.makeText(SearchMoviesActivity.this, getResources().getString(R.string.no_movies_found), Toast.LENGTH_LONG).show();
                                }

                                if(loadedMovies >= totalMovies) {
                                    listView.removeFooterView(loadMore);
                                }

                                adapter.notifyDataSetChanged();

                            } else {
                                Toast.makeText(SearchMoviesActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
                            }
                        } catch(JSONException e) {
                            Toast.makeText(SearchMoviesActivity.this, "JSONException", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        if(error instanceof TimeoutError) {
                            Toast.makeText(SearchMoviesActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
                        } else if(error instanceof NoConnectionError || error instanceof NetworkError) {
                            Toast.makeText(SearchMoviesActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SearchMoviesActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
                        params.put("email_address", userSP.getString("email_address", "undefined"));
                        params.put("language", getResources().getConfiguration().locale.getLanguage());
                        params.put("loaded_movies", String.valueOf(loadedMovies));
                        params.put("movies_to_load", String.valueOf(moviesToLoad));
                        params.put("password", userSP.getString("password", "undefined"));
                        params.put("search_query", searchQuery);
                        return params;
                    }
                };
                stringRequest2.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                progressDialog.show();
                requestQueue.add(stringRequest2);

            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {

                    searchQuery = editText.getText().toString().trim();
                    StringRequest stringRequest1 = new StringRequest(Request.Method.POST, Constants.API_URL + "/search_movies", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if(!jsonObject.getBoolean("error")) {

                                    listView.removeFooterView(loadMore);
                                    movies.clear();
                                    adapter.notifyDataSetChanged();

                                    totalMovies = jsonObject.getInt("total_movies");
                                    loadedMovies = 0;

                                    JSONArray moviesJA = jsonObject.getJSONArray("movies");
                                    if(moviesJA.length() > 0) {
                                        for(int i = 0; i < moviesJA.length(); i++) {

                                            loadedMovies++;

                                            JSONObject movieJO = moviesJA.getJSONObject(i);

                                            manolegeorge.watchlog.info.MovieInfo newMovie = new manolegeorge.watchlog.info.MovieInfo(movieJO.getInt("id"), movieJO.getString("title"));
                                            newMovie.setReleaseDate(movieJO.getString("release_date"));
                                            newMovie.setPoster(movieJO.getString("poster"));

                                            movies.add(newMovie);

                                        }
                                    } else {
                                        Toast.makeText(SearchMoviesActivity.this, getResources().getString(R.string.no_movies_found), Toast.LENGTH_LONG).show();
                                    }

                                    adapter.notifyDataSetChanged();

                                    if(loadedMovies < totalMovies) {
                                        listView.addFooterView(loadMore);
                                    }

                                } else {
                                    Toast.makeText(SearchMoviesActivity.this, jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
                                }
                            } catch(JSONException e) {
                                Toast.makeText(SearchMoviesActivity.this, "JSONException", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            if(error instanceof TimeoutError) {
                                Toast.makeText(SearchMoviesActivity.this, getResources().getString(R.string.weak_internet_connection), Toast.LENGTH_LONG).show();
                            } else if(error instanceof NoConnectionError || error instanceof NetworkError) {
                                Toast.makeText(SearchMoviesActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SearchMoviesActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                            }
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("app_versionCode", String.valueOf(BuildConfig.VERSION_CODE));
                            params.put("email_address", userSP.getString("email_address", "undefined"));
                            params.put("language", getResources().getConfiguration().locale.getLanguage());
                            params.put("loaded_movies", String.valueOf(loadedMovies));
                            params.put("movies_to_load", String.valueOf(moviesToLoad));
                            params.put("password", userSP.getString("password", "undefined"));
                            params.put("search_query", searchQuery);
                            return params;
                        }
                    };
                    stringRequest1.setRetryPolicy(new DefaultRetryPolicy(0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    progressDialog.show();
                    requestQueue.add(stringRequest1);

                    return true;
                }
                return false;
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

    public static class ListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<MovieInfo> movies;
        private ImageLoader imageLoader;
        private DisplayImageOptions options;

        ListAdapter(LayoutInflater inflater, List<MovieInfo> movies, ImageLoader imageLoader) {
            this.inflater = inflater;
            this.movies = movies;
            this.imageLoader = imageLoader;
            this.options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.no_poster)
                    .showImageForEmptyUri(R.drawable.no_poster)
                    .showImageOnFail(R.drawable.no_poster)
                    .cacheInMemory(false)
                    .cacheOnDisk(true)
                    .build();
        }

        @Override
        public int getCount() {
            return movies.size();
        }

        @Override
        public MovieInfo getItem(int position) {
            return movies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = inflater.inflate(R.layout.list_view_search_movies, parent, false);

            ImageView poster = convertView.findViewById(R.id.poster);
            TextView title = convertView.findViewById(R.id.title);
            TextView releaseDate = convertView.findViewById(R.id.release_date);

            imageLoader.displayImage(movies.get(position).getPoster(), poster, this.options);
            title.setText(movies.get(position).getTitle());
            releaseDate.setText(movies.get(position).getReleaseDate());

            return convertView;

        }

    }

}
