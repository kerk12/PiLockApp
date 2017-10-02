package com.kerk12.pilock;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class PINEntryActivity extends AppCompatActivity implements MessageApi.MessageListener{

    private String AuthToken = null;
    private int Device_Profile_Id;
    private String ServerURL = null;
    private String PIN = "";
    private boolean passwordless_enabled = false;
    private boolean isHeadless = false;

    private GoogleApiClient mClient = null;
    private boolean isConnected = false;

    private Node mNode = null;

    private String wearToken = null;

    private static final String START_ACTIVITY = "/start_activity";

    private void getBestNode(){
        Wearable.NodeApi.getConnectedNodes(mClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node : getConnectedNodesResult.getNodes()){
                    if (node.isNearby()){
                        mNode = node;
                        Toast.makeText(getApplicationContext(), getString(R.string.connected_to_wear), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void sendMessage(final String action, final String message){
        Wearable.MessageApi.sendMessage(mClient, mNode.getId(), action, message.getBytes()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
            }
        });
    }

    public static final String HEADLESS_EXTRA = "com.kerk12.pilock.unlock.headless";
    public static final String WEAR_TOKEN = "com.kerk12.pilock.unlock.wear_token";


    private void AnalyzeResult(String s) {


        try {
            JSONObject response = new JSONObject(s);
            if (response.getString("message").equals("SUCCESS")){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.access_granted), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    }

    private String getWearToken(String s){
        try {
            JSONObject response = new JSONObject(s);
            if (response.getString("message").equals("SUCCESS")) {
                String wearToken = response.getString("wearToken");
                return wearToken;
            }
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private void sendUnlock(final boolean headless){
        unlockButton.setEnabled(false);
        pinET.setEnabled(false);
        if (!isHeadless) {
            if (!passwordless_enabled && !ValidatePIN(PIN)) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_pin_entered), Toast.LENGTH_LONG).show();
                unlockButton.setEnabled(true);
                pinET.setEnabled(true);
                return;
            }
        }
        final ProgressDialog hbdial = ProgressDialog.show(PINEntryActivity.this, getResources().getString(R.string.heartbeat), getResources().getString(R.string.heartbeat_text), true, false);
        Heartbeat hb = new Heartbeat();
        hb.setHeartbeatListener(new Heartbeat.HeartbeatListener() {
            @Override
            public void onHeartbeatSuccess() {
                URL unlockURL = null;
                try {
                    unlockURL = new URL(ServerURL+"/authentication");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }


                Map<String, String> params = new HashMap<String, String>();
                params.put(getResources().getString(R.string.auth_token_params), AuthToken);
                params.put(getResources().getString(R.string.profile_id_params), String.valueOf(Device_Profile_Id));
                if (!passwordless_enabled && isHeadless){
                    params.put("wearToken", wearToken);
                } else if (!passwordless_enabled) params.put(getResources().getString(R.string.pin_params), PIN);

                final ProgressDialog dialog = ProgressDialog.show(PINEntryActivity.this, getResources().getString(R.string.wait_dialog_title), getResources().getString(R.string.wait_dialog), true, false);
                final HttpsPOST post = new HttpsPOST(unlockURL, params);
                post.setRequestListener(new HttpsPOST.HttpsRequestListener() {
                    @Override
                    public void onRequestCompleted() {
                        PerformAfterPOSTCheck(post);
                        dialog.dismiss();
                        unlockButton.setEnabled(true);
                        pinET.setEnabled(true);
                        pinET.setText("");
                        if (headless){
                            finish();
                            moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }
                    }
                });
                post.SendPOST(getApplicationContext());
            }

            @Override
            public void onHeartbeatFailure() {
                hbdial.dismiss();
                unlockButton.setEnabled(true);
                pinET.setEnabled(true);
            }

            @Override
            public void onHeartbeatFinished() {

            }
        });
        hb.SendHeartbeat(getApplicationContext());

    }

    private void PerformAfterPOSTCheck(HttpsPOST post){
        int ResponseCode = post.getResponseCode();
        if (post.hasError()){
            HttpsConnectionError error = post.getError();
            switch (error){
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
            switch (ResponseCode) {
                case HTTP_OK:
                    String result = null;
                    try {
                        result = post.getResponse();
                        AnalyzeResult(result);
                    } catch (HttpsRequest.RequestNotExecutedException e) {
                        e.printStackTrace();
                    }
                    break;
                case HTTP_UNAUTHORIZED:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private void PerformWearAfterPOSTCheck(HttpsPOST post){
        int ResponseCode = post.getResponseCode();
        if (post.hasError()){
            HttpsConnectionError error = post.getError();
            switch (error){
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
            switch (ResponseCode) {
                case HTTP_OK:
                    String result = null;
                    try {
                        result = post.getResponse();
                        getWearToken(result);
                    } catch (HttpsRequest.RequestNotExecutedException e) {
                        e.printStackTrace();
                    }
                    break;
                case HTTP_UNAUTHORIZED:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    EditText pinET;
    Button unlockButton;
    TextView PinEntryHeader, PINCap;


    /**
     * Validates the given pin. Checks if the pin is of the right size and if it contains non numeric characters.
     * @param pin The pin.
     * @return True if the pin is valid, false if it's not.
     */
    public static boolean ValidatePIN(String pin){
        //Check the length. It needs to be exactly 6 characters long.
        if(pin.length() != 6){
            return false;
        }
        //Check for non-numeric characters.
        char[] pinChars = pin.toCharArray();
        for (char c : pinChars){
            if (!Character.isDigit(c)){
                return false;
            }
        }
        return true;
    }

    public static final String AUTH_TOKEN_KEY = "authToken";
    public static final String PROFILE_ID_KEY = "device_profile_id";
    public static final String PASSWORDLESS_KEY = "passwordless";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the SECURE flag, to prevent screenshots and background snapshots.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_pinentry);

        mClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        isConnected = true;
                        getBestNode();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addApi(Wearable.API).build();
        mClient.connect();
        isHeadless = getIntent().getBooleanExtra(HEADLESS_EXTRA, false);

        if (isHeadless){
            wearToken = getIntent().getStringExtra(WEAR_TOKEN);
        }

        /*
         * Initialize the auth token and server url.
         */
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences authPrefs = getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);

        ServerURL = sharedPrefs.getString(SettingsActivity.SERVER_ADDRESS_KEY, "none");
        Device_Profile_Id = sharedPrefs.getInt(PROFILE_ID_KEY, -1);
        AuthToken = authPrefs.getString(AUTH_TOKEN_KEY, "None");
        if (AuthToken.equals("None")){
            finish();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        passwordless_enabled = authPrefs.getBoolean(PASSWORDLESS_KEY, false);

        // Set up the layout.
        PinEntryHeader = (TextView) findViewById(R.id.pin_entry_header);
        PINCap = (TextView) findViewById(R.id.pin_entry_capital);



        pinET = (EditText) findViewById(R.id.pin);
        if (!passwordless_enabled) {
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
        } else {
            pinET.setVisibility(View.GONE);
            PINCap.setVisibility(View.GONE);
            PinEntryHeader.setText(getResources().getString(R.string.passwordless_pin_entry_header));
        }

        unlockButton = (Button) findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUnlock(false);
            }
        });

        if (isHeadless){
            sendUnlock(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pin_entry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.change_pin_menu_choice:
                if (!passwordless_enabled) {
                    Intent i = new Intent(this, ChangePinActivity.class);
                    startActivity(i);
                } else
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.change_pin_not_allowed), Toast.LENGTH_LONG).show();
                return true;
            case R.id.sync_watch_choice:
                if (isConnected && mNode != null){
                    if (!ValidatePIN(PIN)){
                        Toast.makeText(getApplicationContext(), "Please enter your PIN.", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    Heartbeat hb = new Heartbeat();
                    hb.setHeartbeatListener(new Heartbeat.HeartbeatListener() {
                        @Override
                        public void onHeartbeatSuccess() {
                            URL wearTokenURL = null;
                            try {
                                wearTokenURL = new URL(ServerURL+"/weartoken");
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(getResources().getString(R.string.auth_token_params), AuthToken);
                            params.put(getResources().getString(R.string.profile_id_params), String.valueOf(Device_Profile_Id));
                            params.put(getResources().getString(R.string.pin_params), PIN);

                            final HttpsPOST post = new HttpsPOST(wearTokenURL, params, false);
                            post.setRequestListener(new HttpsRequest.HttpsRequestListener() {
                                @Override
                                public void onRequestCompleted() {
                                    PerformWearAfterPOSTCheck(post);
                                    try {
                                        if (post.getResponse() == null){
                                            Toast.makeText(getApplicationContext(), getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        String wearToken = getWearToken(post.getResponse());
                                        if (wearToken != null) {
                                            if (!mNode.isNearby() || mNode == null ){
                                                Toast.makeText(getApplicationContext(), getString(R.string.no_nearby_nodes), Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            sendMessage(START_ACTIVITY, wearToken);
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.synced_successfully), Toast.LENGTH_LONG).show();
                                        } else return;
                                    } catch (HttpsRequest.RequestNotExecutedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            post.SendPOST(getApplicationContext());
                        }

                        @Override
                        public void onHeartbeatFailure() {

                        }

                        @Override
                        public void onHeartbeatFinished() {

                        }
                    });
                    hb.SendHeartbeat(getApplicationContext());
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_connected_to_wear), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.change_pin_about:
                LoginActivity.ShowAboutDialog(PINEntryActivity.this);
                return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (mClient.isConnected()){
            mClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }
}
