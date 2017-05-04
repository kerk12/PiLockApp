package com.kerk12.pilock;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by kgiannakis on 4/5/2017.
 */

public class HttpsGET {
    private URL url;
    private Map<String, String> params = null;



    public HttpsGET(URL url, Map<String, String> params){
        this.url = url;
        this.params = params;
    }

    public HttpsGET(URL url){
        this.url = url;
    }

    private InputStream response_stream;
    private String response = null;
    private InputStream errorStream;
    private int ResponseCode = 200;

    private Boolean HasErrors;
    private HttpsConnectionError error = null;

    private class GETTask extends AsyncTask<Void, Void, String>{

        private String getResponse() throws IOException, GeneralSecurityException {
            URL urlNew = url;
            if (params != null){
                String url_temp = url.toString();
                QueryBuilder builder = new QueryBuilder(params);
                url_temp = url_temp + "?" + builder.getQuery();

            }
            HttpsURLConnection conn = (HttpsURLConnection) urlNew.openConnection();
            conn.setSSLSocketFactory(CustomSSLTruster.TrustCertificate().getSocketFactory());
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.d("PiLock_HB","Allowed host "+hostname);
                    return true;
                }
            });
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            ResponseCode = conn.getResponseCode();
            if (ResponseCode == HTTP_OK){
                response_stream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response_stream, "UTF-8"));
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null){
                    builder.append(line + "\n");
                }
                reader.close();
                return builder.toString();
            } else {
                errorStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response_stream, "UTF-8"));
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
                response = getResponse();
            } catch (SSLHandshakeException e){
                HasErrors = true;
                error = HttpsConnectionError.INVALID_CERTIFICATE;
            } catch (IOException e) {
                e.printStackTrace();
                HasErrors = true;
                error = HttpsConnectionError.CONNECTION_ERROR;
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                HasErrors = true;
                error = HttpsConnectionError.INVALID_CERTIFICATE;
            }
            return response;
        }
    }
}
