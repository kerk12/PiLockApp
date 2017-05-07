package com.kerk12.pilock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Class used for implementing the Heartbeat functionality. Checks if the server specified is alive or not.
 */
public class Heartbeat {
    /**
     * Checks if the server is alive. This method should be called before performing a request to the server.
     * @param context The app's context.
     * @return True if the server is alive, False if it cannot be found, if it's locked, or if an error occured.
     */
    public static boolean isAlive(Context context){
        URL url;

        //Get the server's url from the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            url = new URL(sharedPreferences.getString(SettingsActivity.SERVER_ADDRESS_KEY, "none"));

            //Perform a GET request to the root page.
            HttpsGET get = new HttpsGET(url);
            get.setSslSocketFactory(CustomSSLTruster.TrustCertificate().getSocketFactory());
            get.SendGET(context);

            //Check for errors
            if (get.getHasError()){
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
                return false;
            } else {
                // Check for an error in the response code.
                int ResponseCode = get.getResponseCode();
                switch (ResponseCode){
                    case HTTP_OK:
                        //The server is alive, but the JSON needs to be checked.
                        String response = get.getResponse();
                        JSONObject resp = new JSONObject(response);
                        String status = resp.getString("status");
                        //TODO Check the version number.
                        if (!status.equals("ALIVE")){
                            //The server is locked.
                            Toast.makeText(context, context.getResources().getString(R.string.locked), Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case HTTP_BAD_GATEWAY:
                        Toast.makeText(context, context.getResources().getString(R.string.internal_server_error), Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(context, context.getResources().getString(R.string.server_not_found), Toast.LENGTH_LONG).show();
                        break;
                }
                return false;
            }

        } catch (Exception e) {
            //Return false on all exceptions
            e.printStackTrace();
            return false;
        }
    }

}
