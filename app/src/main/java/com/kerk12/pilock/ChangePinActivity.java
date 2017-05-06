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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class ChangePinActivity extends AppCompatActivity {

    EditText oldPinET, newPinET;
    Button submitButton;

    String oldPin, newPin = "";
    String ChangePinURL = null;
    String AuthToken = null;

    private void PerformAfterPostCheck(HttpsPOST post){
        if (post.HasErrors()){
            switch (post.getError()){
                case INVALID_CERTIFICATE:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_cert), Toast.LENGTH_LONG).show();
                    break;
                case CONNECTION_ERROR:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.server_not_found), Toast.LENGTH_LONG).show();
                    break;
                case NOT_CONNECTED_TO_WIFI:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_connected_to_wifi), Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            int responsecode = post.getResponseCode();
            switch (responsecode){
                case HttpsURLConnection.HTTP_OK:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.pin_change_successful), Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case HttpsURLConnection.HTTP_UNAUTHORIZED:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.server_not_found), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences authPrefs = getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);

        ChangePinURL = sharedPrefs.getString(SettingsActivity.SERVER_ADDRESS_KEY, "")+"/changepin";
        AuthToken = authPrefs.getString(getResources().getString(R.string.auth_token_params), "");

        oldPinET = (EditText) findViewById(R.id.CP_old_pin);
        oldPinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                oldPin = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        newPinET = (EditText) findViewById(R.id.CP_new_pin);
        newPinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newPin = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        submitButton = (Button) findViewById(R.id.CP_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitButton.setEnabled(false);
                if (!(PINEntryActivity.ValidatePIN(newPin) && PINEntryActivity.ValidatePIN(oldPin)) ){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_pin_entered), Toast.LENGTH_LONG).show();
                    submitButton.setEnabled(true);
                    return;
                }
                if (!Heartbeat.isAlive(getApplicationContext())){
                    submitButton.setEnabled(true);
                    return;
                }

                try {
                    URL changePinURL = new URL(ChangePinURL);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(getResources().getString(R.string.auth_token_params), AuthToken);
                    params.put("oldPin", oldPin);
                    params.put("newPin", newPin);

                    HttpsPOST post = new HttpsPOST(changePinURL, params);
                    post.SendPOST(getApplicationContext());
                    PerformAfterPostCheck(post);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
