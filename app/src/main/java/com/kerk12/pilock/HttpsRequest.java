package com.kerk12.pilock;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * Superclass used for implementing GET and POST requests. Contains all the common attributes/methods used by HttpsGET and HttpsPOST.
 */
public class HttpsRequest {

    /**
     * The URL to perform the request to.
     * This is usually modified on GET request with parameters.
     */
    private URL url;
    /**
     * The parameters of the request.
     */
    private Map<String, String> params;

    /**
     * The SSL Socket Factory used when there is a self-signed certificate.
     */
    private SSLSocketFactory sslSocketFactory;

    /**
     * Instance of the {@see HttpsConnectionError} enum used for representing errors.
     */
    private HttpsConnectionError error = null;
    /**
     * Boolean indicating if the connection has errors.
     */
    private Boolean HasError = false;

    /**
     * The response code returned by the request.
     */
    protected int ResponseCode = 200;

    /**
     * The input stream of the response.
     */
    protected InputStream ResponseStream = null;
    /**
     * The error stream of the response. Used whenever the response code indicates an error.
     */
    protected InputStream ErrorStream = null;
    /**
     * The final response, as a string.
     */
    private String Response = null;

    /**
     * Constructor used when there are parameters to be passed along with the request.
     * @param url The page to send the request to.
     * @param params The request parameters.
     */
    public HttpsRequest(URL url, Map<String, String> params) {
        this.url = url;
        this.params = params;
    }

    /**
     * Default constructor.
     * @param url The URL to send the request to.
     */
    public HttpsRequest(URL url) {
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
