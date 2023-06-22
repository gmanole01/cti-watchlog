package manolegeorge.webfetcher;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

class FetchTask extends AsyncTask<String, String, String> {

    private String method;
    private String url;
    private Response.Listener listener;
    private Response.ErrorListener errorListener;
    private Map<String, String> params;

    private int statusCode;
    private String response;
    
    FetchTask(String mMethod, String mURL, Response.Listener mListener, Response.ErrorListener mErrorListener) {
        this(
            mMethod,
            mURL,
            mListener,
            mErrorListener,
            new HashMap<>()
        );
    }

    FetchTask(String mMethod, String mURL, Response.Listener mListener, Response.ErrorListener mErrorListener, Map<String, String> mParams) {
        this.method = mMethod;
        this.url = mURL;
        this.listener = mListener;
        this.errorListener = mErrorListener;
        this.params = mParams;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL pURL = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection)pURL.openConnection();
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("User-Agent", "WatchLog");
            connection.setRequestMethod(this.method);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            this.statusCode = connection.getResponseCode();

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(getQuery(this.params));
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();

            String inputLine;
            while((inputLine = in.readLine()) != null) response.append(inputLine);

            in.close();

            this.response = response.toString();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onPostExecute(String result) {
        if(this.statusCode == 200) {
            this.listener.onResponse(this.response);
        } else {
            this.errorListener.onErrorResponse(this.statusCode);
        }
    }

    private String getQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for(Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            if(pos < params.size() - 1) sb.append("&");
            pos++;
        }
        return sb.toString();
    }

}
