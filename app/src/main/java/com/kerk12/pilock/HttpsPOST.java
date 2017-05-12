package com.kerk12.pilock;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import static com.kerk12.pilock.HttpsConnectionError.NOT_CONNECTED_TO_INTERNET;
import static com.kerk12.pilock.HttpsConnectionError.NOT_CONNECTED_TO_WIFI;


/**
 * Class used to perform HTTPS POST Requests. Connects to the server, performs the request, recieves the Input or Error Stream, and ends the connection.
 * TODO: Integrate it with {@link HttpsRequest}
 */
public class HttpsPOST extends HttpsRequest{





    /**
     * Default constructor. Takes the server page's URL, along with the mapped data.
     * @param url The Page URL that the POST request will be performed to.
     * @param params The data parameters, mapped as String key=value pairs. See {@link QueryBuilder} for more.
     */
    public HttpsPOST(URL url, Map<String, String> params) {
        super(url, params);
    }

    private class POSTTask extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPreExecute() {
            setExecuted(true);
            super.onPreExecute();
        }

        /**
         * Sends the POST request and recieves a result string.
         * @return The result String if no errors occured, null if an error occured.
         * @throws IOException When a connection fails, as well as when the certificate cannot be read.
         * @throws GeneralSecurityException If a secure connection cannot be established.
         */
        private String getResult() throws IOException, GeneralSecurityException {

            // Set the SSLContext, so that it trusts our Self-Signed Certificate.
            SSLContext sslContext = CustomSSLTruster.TrustCertificate();
            HttpsURLConnection conn = (HttpsURLConnection) getUrl().openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
            // Set the hostname verifier to verify all hostnames.
            // If the hostname is an IP it doesn't get verified by default, unless this is inserted...
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.d("SSL Hostname", hostname+ ": Accepted Connection");
                    return true;
                }
            });

            // Set the connection parameters...
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            // Write the POST data...
            QueryBuilder builder = new QueryBuilder(getParams());
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(builder.getQuery());
            writer.flush();
            writer.close();

            ResponseCode = conn.getResponseCode();

            // Get the input if there was no error, else, get the ErrorStream and return null.
            if (ResponseCode == HttpsURLConnection.HTTP_OK){
                // TODO Make it more beautiful.
                InputStream is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }
                reader.close();

                return sb.toString();
            } else {
                ErrorStream = conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ErrorStream, "UTF-8"), 8);
                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }
                reader.close();

                ErrorStreamStr = sb.toString();
            }
            return null;

        }


        @Override
        protected String doInBackground(Void... params) {
            try {
                return getResult();
            } catch (SSLHandshakeException e){
                setError(HttpsConnectionError.INVALID_CERTIFICATE);
            } catch (IOException e) {
                e.printStackTrace();
                setError(HttpsConnectionError.CONNECTION_ERROR);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                setError(HttpsConnectionError.INVALID_CERTIFICATE);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (RequestListener != null) {
                setResponse(s);
                RequestListener.onRequestCompleted();
            }
            super.onPostExecute(s);
        }
    }

    /**
     * Sends the POST request.
     */
    public void SendPOST(Context context){
        if (!isExecuted()){
            if (NeedsWifi()) {
                if (IsConnectedToWiFi(context)) {
                    POSTTask t = new POSTTask();
                    t.execute();
                } else {
                    setError(NOT_CONNECTED_TO_WIFI);
                }
            } else {
                if (IsConnected(context)){
                    POSTTask t = new POSTTask();
                    t.execute();
                } else {
                    setError(NOT_CONNECTED_TO_INTERNET);
                }
            }
        }
    }

}