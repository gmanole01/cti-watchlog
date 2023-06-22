package manolegeorge.webfetcher;

import android.util.ArrayMap;

import java.util.Map;

public class StringRequest extends Request {

    private String method;
    private String url;
    private Response.Listener listener;
    private Response.ErrorListener errorListener;

    public StringRequest(String mMethod, String mURL, Response.Listener mListener, Response.ErrorListener mErrorListener) {
        this.method = mMethod;
        this.url = mURL;
        this.listener = mListener;
        this.errorListener = mErrorListener;
    }

    public void send() {
        new FetchTask(this.method, this.url, this.listener, this.errorListener).execute("");
    }

    protected Map<String, String> getParams() {
        return new ArrayMap<>();
    }

}
