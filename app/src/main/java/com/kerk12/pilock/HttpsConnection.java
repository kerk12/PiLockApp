package com.kerk12.pilock;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by kerk12 on 05/05/2017.
 */

public class HttpsConnection {
    private URL url;
    private Map<String, String> params;

    private SSLSocketFactory sslSocketFactory;

    private HttpsConnectionError error = null;
    private Boolean HasError = false;

    protected int ResponseCode = 200;
    protected InputStream ResponseStream = null;
    protected InputStream ErrorStream = null;
    private String Response = null;

    public HttpsConnection(URL url, Map<String, String> params) {
        this.url = url;
        this.params = params;
    }

    public HttpsConnection(URL url) {
        this.url = url;
    }



    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public static boolean IsConnectedToWiFi(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()){
            if (info.getType() == ConnectivityManager.TYPE_WIFI){
                return true;
            }
        }
        return false;
    }

    public HttpsConnectionError getError() {
        return error;
    }

    public int getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(int responseCode) {
        ResponseCode = responseCode;
    }

    public InputStream getResponseStream() {
        return ResponseStream;
    }

    public void setResponseStream(InputStream responseStream) {
        ResponseStream = responseStream;
    }

    public InputStream getErrorStream() {
        return ErrorStream;
    }

    public void setErrorStream(InputStream errorStream) {
        ErrorStream = errorStream;
    }

    public void setError(HttpsConnectionError error) {
        HasError = true;
        this.error = error;
    }

    public URL getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getResponse() {
        return Response;
    }

    public void setResponse(String response) {
        Response = response;
    }

    public Boolean getHasError() {
        return HasError;
    }

    public void setHasError(Boolean hasError) {
        HasError = hasError;
    }
}
