package com.kerk12.pilock;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ResponseCache;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


/**
 * Class used to perform HTTPS POST Requests. Connects to the server, performs the request, recieves the Input or Error Stream, and ends the connection.
 */
public class HttpsPOST {
    private URL url = null;
    /**
     * The POST request data parameters.
     */
    private Map<String, String> params = null;
    /**
     * The final recieved {@link InputStream}, decoded to a String.
     */
    private String result = null;
    /**
     * Boolean indicating whether the post has been sent or not.
     */
    private boolean Executed = false;
    private int ResponseCode = -1;
    /**
     * The Error Stream decoded into a string. Used if a connection fails.
     */
    private String ErrorStream = null;
    /**
     * Boolean variable indicating whether the connection was completed successfully or not.
     */
    private boolean HasErrors = false;
    private boolean CertError = false;
    private POSTError error = null;

    /**
     * Enum used to indicate possible connection errors.
     * INVALID_CERTIFICATE: Invalid certificate. Connection aborted.
     * CONNECTION_ERROR: An error occurred while trying to reach the server.
     */
    public enum POSTError{
        INVALID_CERTIFICATE,
        CONNECTION_ERROR,
    }

    /**
     * Default constructor. Takes the server page's URL, along with the mapped data.
     * @param url The Page URL that the POST request will be performed to.
     * @param params The data parameters, mapped as String key=value pairs. See {@link QueryBuilder} for more.
     */
    public HttpsPOST(URL url, Map<String, String> params) {
        this.url = url;
        this.params = params;
    }

    private class POSTTask extends AsyncTask<Void, Void, String>{

        /**
         * Sends the POST request and recieves a result string.
         * @return The result String if no errors occured, null if an error occured.
         * @throws IOException When a connection fails, as well as when the certificate cannot be read.
         * @throws GeneralSecurityException If a secure connection cannot be established.
         */
        private String getResult() throws IOException, GeneralSecurityException {

            // Set the SSLContext, so that it trusts our Self-Signed Certificate.
            SSLContext sslContext = CustomSSLTruster.TrustCertificate();
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());

            // Set the connection parameters...
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            // Write the POST data...
            QueryBuilder builder = new QueryBuilder(params);
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
                InputStream ErrorIStream = conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ErrorIStream, "UTF-8"), 8);
                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }
                reader.close();

                ErrorStream = sb.toString();
            }
            return null;

        }


        @Override
        protected String doInBackground(Void... params) {
            try {
                return getResult();
            } catch (IOException e) {
                e.printStackTrace();
                HasErrors = true;
                CertError = true;
                error = POSTError.CONNECTION_ERROR;
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                HasErrors = true;
                CertError = true;
                error = POSTError.INVALID_CERTIFICATE;
            }
            return null;
        }
    }

    /**
     * Get the result string from the post request.
     * @return A string with the recieved data, null on connection error.
     */
    public String getResult(){
        SendPOST();
        return result;
    }

    /**
     * Sends the POST request.
     */
    public void SendPOST(){
        if (!Executed){
            POSTTask t = new POSTTask();
            try {
                result = t.execute().get();

            } catch (InterruptedException e) {
                e.printStackTrace();
                HasErrors = true;
            } catch (ExecutionException e) {
                e.printStackTrace();
                HasErrors = true;
            } finally {
                Executed = true;
            }
        }
    }

    /**
     * Returns the response code recieved from the server after executing the request.
     * @return The response code.
     */
    public int getResponseCode() {
        return ResponseCode;
    }

    public String getErrorStream() {
        return ErrorStream;
    }

    public boolean HasErrors() {
        return HasErrors;
    }

    public boolean HasCertError() {
        return CertError;
    }

    public POSTError getError() {
        return error;
    }
}
