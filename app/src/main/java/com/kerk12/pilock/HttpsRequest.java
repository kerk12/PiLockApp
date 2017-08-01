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
     * Exception thrown when trying to query a request object's data before it was sent.
     */
    public class RequestNotExecutedException extends Exception{
        public RequestNotExecutedException(){
            super("The POST Request hasn't been Executed yet. Call SendPOST(Context context) first.");
        }
    }

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

    protected String ErrorStreamStr;
    /**
     * The final response, as a string.
     */
    private String Response = null;
    private boolean Executed = false;

    private boolean NeedsWifi = false;

    protected int ConnectTimeout, ReadTimeout = 5000;

    /**
     * Interface used to create callbacks, after the request has been completed.
     */
    public interface HttpsRequestListener {
        void onRequestCompleted();
    }

    protected HttpsRequestListener RequestListener = null;

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

    public HttpsRequest(URL url, boolean NeedsWifi){
        this.url = url;
        this.NeedsWifi = NeedsWifi;
    }

    public HttpsRequest(URL url, Map<String, String> params, boolean needsWifi) {
        this.url = url;
        this.params = params;
        NeedsWifi = needsWifi;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * Checks if the user's device is connected to a WiFi Network.
     * @param context The app's context.
     * @return True if the device is connected to a WiFi network, false if it's not.
     */
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

    /**
     * Checks if the user's device is connected to the internet.
     * @param context The app's context.
     * @return True if the device is connected to the internet, false if it's not.
     */
    public static boolean IsConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    public HttpsConnectionError getError() {
        return error;
    }

    public int getResponseCode() {
        return ResponseCode;
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

    public String getResponse() throws RequestNotExecutedException {
        if (!isExecuted()){
            throw new RequestNotExecutedException();
        }
        return Response;
    }

    public void setResponse(String response) {
        Response = response;
    }

    public Boolean hasError() {
        return HasError;
    }


    public boolean NeedsWifi() {
        return NeedsWifi;
    }

    public void setNeedsWifi(boolean needsWifi) {
        NeedsWifi = needsWifi;
    }

    public boolean isExecuted() {
        return Executed;
    }

    public void setExecuted(boolean executed) {
        Executed = executed;
    }

    public void setRequestListener(HttpsRequestListener requestListener) {
        this.RequestListener = requestListener;
    }

    public String getErrorStream() {
        return ErrorStreamStr;
    }

    public void setConnectTimeout(int connectTimeout) {
        ConnectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        ReadTimeout = readTimeout;
    }
}
