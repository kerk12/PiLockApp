package com.kerk12.pilock;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class PINEntryActivity extends AppCompatActivity {

    private String AuthToken = null;
    private String ServerURL = null;
    private String PIN = null;



    public static final String AUTH_TOKEN_KEY = "authToken";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinentry);

        /*
         * Initialize the auth token and server url.
         */
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences authPrefs = getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);
        ServerURL = sharedPrefs.getString(SettingsActivity.SERVER_ADDRESS_KEY, "none");
        AuthToken = authPrefs.getString(AUTH_TOKEN_KEY, "None");



    }
}
