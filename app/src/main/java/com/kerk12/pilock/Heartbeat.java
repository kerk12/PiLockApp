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
 * Created by kgiannakis on 6/5/2017.
 */

public class Heartbeat {
    public static boolean isAlive(Context context){
        URL url;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            url = new URL(sharedPreferences.getString(SettingsActivity.SERVER_ADDRESS_KEY, "none"));
            HttpsGET get = new HttpsGET(url);
            get.SendGET(context);

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
                int ResponseCode = get.getResponseCode();
                switch (ResponseCode){
                    case HTTP_OK:
                        String response = get.getResponse();
                        JSONObject resp = new JSONObject(response);
                        String status = resp.getString("status");
                        if (!status.equals("ALIVE")){
                            Toast.makeText(context, context.getResources().getString(R.string.locked), Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case HTTP_BAD_GATEWAY:
                        Toast.makeText(context, context.getResources().getString(R.string.locked), Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(context, context.getResources().getString(R.string.server_not_found), Toast.LENGTH_LONG).show();
                        break;
                }
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
