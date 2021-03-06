package com.kerk12.pilock;

import android.app.ProgressDialog;
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

    /*
        Layout views:
     */
    EditText oldPinET, newPinET;
    Button submitButton;

    /*
        Parameters:
        Old Pin
        New Pin
        Pin Change URL
        Auth Token
     */
    String oldPin, newPin = "";
    String ChangePinURL = null;
    String AuthToken = null;
    int Device_Profile_Id;

    private void PerformAfterPostCheck(HttpsPOST post){

        if (post.hasError()){
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
        PINEntryActivity.EnableDisableControls(findViewById(R.id.change_pin_layout), true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);

        //Get the server URL, along with the AuthToken
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences authPrefs = getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);

        ChangePinURL = sharedPrefs.getString(SettingsActivity.SERVER_ADDRESS_KEY, "")+"/changepin";
        AuthToken = authPrefs.getString(getResources().getString(R.string.auth_token_params), "");
        Device_Profile_Id = authPrefs.getInt(PINEntryActivity.PROFILE_ID_KEY, -1);

        //Wire them to the layout...
        oldPinET = (EditText) findViewById(R.id.CP_old_pin);
        oldPinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                oldPin = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        newPinET = (EditText) findViewById(R.id.CP_new_pin);
        newPinET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newPin = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        submitButton = (Button) findViewById(R.id.CP_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable all the controls.
                PINEntryActivity.EnableDisableControls(findViewById(R.id.change_pin_layout), false);
                if (!(PINEntryActivity.ValidatePIN(newPin) && PINEntryActivity.ValidatePIN(oldPin)) ){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_pin_entered), Toast.LENGTH_LONG).show();
                    PINEntryActivity.EnableDisableControls(findViewById(R.id.change_pin_layout), true);
                    return;
                }
                final ProgressDialog hbdial = ProgressDialog.show(ChangePinActivity.this, getResources().getString(R.string.heartbeat), getResources().getString(R.string.heartbeat_text), true, false);

                Heartbeat hb = new Heartbeat();
                hb.setHeartbeatListener(new Heartbeat.HeartbeatListener() {
                    @Override
                    public void onHeartbeatSuccess() {
                        hbdial.dismiss();
                        final ProgressDialog cpdial = ProgressDialog.show(ChangePinActivity.this, getResources().getString(R.string.changing_pin), getResources().getString(R.string.please_wait), true, false);

                        try {
                            URL changePinURL = new URL(ChangePinURL);
                            Map<String, String> params = new HashMap<String, String>();
                            KeystoreHelper helper = new KeystoreHelper(getApplicationContext());
                            String authToken_dec = helper.Decrypt(AuthToken);
                            params.put(getResources().getString(R.string.auth_token_params), authToken_dec);
                            params.put(getResources().getString(R.string.profile_id_params), String.valueOf(Device_Profile_Id));
                            params.put("oldPin", oldPin);
                            params.put("newPin", newPin);

                            final HttpsPOST post = new HttpsPOST(changePinURL, params);
                            post.setRequestListener(() -> { cpdial.dismiss(); PerformAfterPostCheck(post); });
                            post.SendPOST(getApplicationContext());

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onHeartbeatFailure() {
                        hbdial.dismiss();
                        PINEntryActivity.EnableDisableControls(findViewById(R.id.change_pin_layout), true);
                    }

                    @Override
                    public void onHeartbeatFinished() {}
                });
                hb.SendHeartbeat(getApplicationContext());

            }
        });

    }


    @Override
    public void onBackPressed() {
        //End the activity when the back button is pressed.
        super.onBackPressed();
        finish();
    }
}
