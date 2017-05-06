package com.kerk12.pilock;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
import static com.kerk12.pilock.HttpsConnectionError.NOT_CONNECTED_TO_WIFI;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by kgiannakis on 4/5/2017.
 */

public class HttpsGET extends HttpsConnection{

    public HttpsGET(URL url, Map<String, String> params){
        super(url, params);
    }

    public HttpsGET(URL url){
        super(url);
    }

    private class GETTask extends AsyncTask<Void, Void, String>{

        private String getResponse() throws IOException, GeneralSecurityException {
            URL urlNew = getUrl();
            if (getParams() != null){
                String url_temp = getUrl().toString();
                QueryBuilder builder = new QueryBuilder(getParams());
                url_temp = url_temp + "?" + builder.getQuery();
                urlNew = new URL(url_temp);
            }
            HttpsURLConnection conn = (HttpsURLConnection) urlNew.openConnection();
            conn.setSSLSocketFactory(CustomSSLTruster.TrustCertificate().getSocketFactory());
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.d("HttpsGET","Allowed host "+hostname);
                    return true;
                }
            });
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            ResponseCode = conn.getResponseCode();
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


    }

    public void SendGET(Context context){
        if (IsConnectedToWiFi(context)) {
            GETTask get = new GETTask();
            try {
                super.setResponse(get.execute().get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            setError(NOT_CONNECTED_TO_WIFI);
        }
    }
}
