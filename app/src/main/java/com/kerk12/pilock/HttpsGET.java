package com.kerk12.pilock;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import static com.kerk12.pilock.HttpsConnectionError.CONNECTION_ERROR;
import static com.kerk12.pilock.HttpsConnectionError.INVALID_CERTIFICATE;
import static com.kerk12.pilock.HttpsConnectionError.NOT_CONNECTED_TO_INTERNET;
import static com.kerk12.pilock.HttpsConnectionError.NOT_CONNECTED_TO_WIFI;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Class used for performing GET requests.
 */
public class HttpsGET extends HttpsRequest {

    /**
     * Constructor used for passing in parameters to the GET request.
     */
    public HttpsGET(URL url, Map<String, String> params){
        super(url, params);
    }

    /**
     * Default constructor. Requires the server's URL.
     * @param url The URL the request will be sent to.
     */
    public HttpsGET(URL url){
        super(url);
    }

    /**
     * Requires the server's URL and whether the connection can be performed only on WiFi
     * @param url
     * @param NeedsWifi
     */
    public HttpsGET(URL url, boolean NeedsWifi) {
        super(url, NeedsWifi);
    }

    /**
     * Requires the server's URL, the request parameters and whether the connection can be performed only on WiFi
     * @param url
     * @param needsWifi
     */
    public HttpsGET(URL url, Map<String, String> params, boolean needsWifi) {
        super(url, params, needsWifi);
    }

    /**
     * AsyncTask responsible for sending the GET request.
     */
    private class GETTask extends AsyncTask<Void, Void, String>{

        private String getResponse() throws IOException, GeneralSecurityException {
            URL urlNew = getUrl();
            //If parameters are passed, add them to the end of the URL.
            if (getParams() != null){
                String url_temp = getUrl().toString();
                QueryBuilder builder = new QueryBuilder(getParams());
                url_temp = url_temp + "?" + builder.getQuery();
                urlNew = new URL(url_temp);
            }

            HttpsURLConnection conn = (HttpsURLConnection) urlNew.openConnection();
            // If a custom SSL socket factory, is set, add it to the connection.
            if (getSslSocketFactory() != null) {
                conn.setSSLSocketFactory(getSslSocketFactory());
                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        Log.d("HttpsGET", "Allowed host " + hostname);
                        return true;
                    }
                });
            }
            //Define connection parameters.
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            ResponseCode = conn.getResponseCode();
            //Check the response code, and read the stream. If the response code indicates an error, read the error stream.
            if (ResponseCode == HTTP_OK){
                ResponseStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ResponseStream, "UTF-8"));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null){
                    builder.append(line + "\n");
                }
                reader.close();
                return builder.toString();
            } else {
                ErrorStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ErrorStream, "UTF-8"));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null){
                    builder.append(line + "\n");
                }
                reader.close();
            }
            return null;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String resp = getResponse();
                return resp;
            } catch (SSLHandshakeException e){
                setError(INVALID_CERTIFICATE);
            } catch (IOException e) {
                e.printStackTrace();
                setError(CONNECTION_ERROR);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                setError(INVALID_CERTIFICATE);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (listener != null){
                setResponse(s);
                listener.onRequestCompleted();
            }
            setExecuted(true);
            super.onPostExecute(s);
        }
    }

    /**
     * Sends the GET request.
     * @param context The app's context.
     */
    public void SendGET(Context context){
        //Check if the phone is connected to WiFi
        //TODO Make it customizable.
        if (NeedsWifi()){
            if (!IsConnectedToWiFi(context)){
                setError(NOT_CONNECTED_TO_WIFI);
                return;
            }
        }
        if (IsConnected(context)) {
            GETTask get = new GETTask();
            get.execute();
            try {
                setResponse(get.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            setError(NOT_CONNECTED_TO_INTERNET);
        }
    }
}
