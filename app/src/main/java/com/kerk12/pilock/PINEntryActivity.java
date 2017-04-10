package com.kerk12.pilock;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class PINEntryActivity extends AppCompatActivity {

    private String AuthToken = null;
    private String ServerURL = null;
    private String PIN = null;

    private void AnalyzeResult(String s) {
        JSONObject response = null;
        try {
            response = new JSONObject(s);
            if (response.getString("message") == "SUCCESS"){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.access_granted), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    }

    private void PerformAfterPOSTCheck(HttpsPOST post){
        int ResponseCode = post.getResponseCode();
        if (post.HasErrors()){
            HttpsPOST.POSTError error = post.getError();
            switch (error){
                case INVALID_CERTIFICATE:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_cert), Toast.LENGTH_LONG).show();
                    break;
                case CONNECTION_ERROR:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.server_not_found), Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            switch (ResponseCode) {
                case HTTP_OK:
                    String result = post.getResult();
                    AnalyzeResult(result);
                    break;
                case HTTP_UNAUTHORIZED:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
                    break;
            }
        }
        unlockButton.setEnabled(true);
    }

    EditText pinET;
    Button unlockButton;

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

        pinET = (EditText) findViewById(R.id.pin);
        pinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PIN = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        unlockButton = (Button) findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                unlockButton.setEnabled(false);
                //TODO Validate the input.
                URL unlockURL = null;
                try {
                    unlockURL = new URL(ServerURL+"/authentication");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }


                Map<String, String> params = new HashMap<String, String>();
                params.put(getResources().getString(R.string.auth_token_params), AuthToken);
                params.put(getResources().getString(R.string.pin_params), PIN);

                HttpsPOST post = new HttpsPOST(unlockURL, params);
                post.SendPOST();
                PerformAfterPOSTCheck(post);

            }
        });

    }
}
