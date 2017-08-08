package com.kerk12.pilock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Class used for implementing the Heartbeat functionality. Checks if the server specified is alive or not.
 */
public class Heartbeat {

    public interface HeartbeatListener {
        void onHeartbeatSuccess();
        void onHeartbeatFailure();
        void onHeartbeatFinished();
    }

    private HeartbeatListener listener = null;

    /**
     * Checks if the server is alive. This method should be called before performing a request to the server.
     * @param context The app's context.
     * @return True if the server is alive, False if it cannot be found, if it's locked, or if an error occured.
     */
    public void SendHeartbeat(final Context context){
        URL url;

        //Get the server's url from the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

//        if ()

        try {
            url = new URL(sharedPreferences.getString(SettingsActivity.SERVER_ADDRESS_KEY, "none"));

            //Perform a GET request to the root page.
            final HttpsGET get = new HttpsGET(url, true);
            // Requires a rather connect and read timeout as the raspi zero is slow enough...
            get.setConnectTimeout(15000);
            get.setReadTimeout(15000);
            get.setSslSocketFactory(CustomSSLTruster.TrustCertificate().getSocketFactory());
            get.setRequestListener(new HttpsRequest.HttpsRequestListener() {
                @Override
                public void onRequestCompleted() {
                    listener.onHeartbeatFinished();
                    if (get.hasError()){
                        switch(get.getError()){
                            case INVALID_CERTIFICATE:
                                Toast.makeText(context, context.getResources().getString(R.string.invalid_cert), Toast.LENGTH_LONG).show();
                                break;
                            case NOT_CONNECTED_TO_WIFI:
                                Toast.makeText(context, context.getResources().getString(R.string.not_connected_to_wifi), Toast.LENGTH_LONG).show();
                                break;
                            case CONNECTION_ERROR:
                                Toast.makeText(context, context.getResources().getString(R.string.server_not_found), Toast.LENGTH_LONG).show();
                                break;
                        }
                        listener.onHeartbeatFailure();
                    } else {
                        // Check for an error in the response code.
                        int ResponseCode = get.getResponseCode();
                        switch (ResponseCode){
                            case HTTP_OK:
                                //The server is alive, but the JSON needs to be checked.
                                String response = null;
                                String status = null;
                                String version = null;
                                try {
                                    response = get.getResponse();
                                    JSONObject resp = new JSONObject(response);
                                    status = resp.getString("status");
                                    version = resp.getString("version");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (!status.equals("ALIVE")){
                                    //The server is locked.
                                    Toast.makeText(context, context.getResources().getString(R.string.locked), Toast.LENGTH_LONG).show();
                                    listener.onHeartbeatFailure();
                                    return;
                                }
                                if (!VersionChecker.CheckVersion(version)){
                                    Toast.makeText(context, context.getResources().getString(R.string.version_err), Toast.LENGTH_LONG).show();
                                    listener.onHeartbeatFailure();
                                    return;
                                }
                                listener.onHeartbeatSuccess();
                                break;
                            case HTTP_BAD_GATEWAY:
                                Toast.makeText(context, context.getResources().getString(R.string.internal_server_error), Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(context, context.getResources().getString(R.string.server_not_found), Toast.LENGTH_LONG).show();
                                break;
                        }
                        listener.onHeartbeatFailure();
                    }

                }
            });
            get.SendGET(context);
        } catch (Exception e) {
            //Return false on all exceptions
            e.printStackTrace();
            listener.onHeartbeatFailure();
        }
    }

    public void setHeartbeatListener(HeartbeatListener listener){
        this.listener = listener;
    }

}
