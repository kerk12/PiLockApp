package com.kerk12.pilock;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class LoginActivity extends AppCompatActivity {

    private final int STORAGE_PERM = 1;


    String username = null;
    String password = null;

    String AuthToken = null;
    String tempPIN = null;

    EditText usernameET, passwordET;
    Button loginButton;

    /**
     * Launches {@link PINEntryActivity}
     */
    private void LaunchPINEntryActivity(){
        Intent i = new Intent(this, PINEntryActivity.class);
        startActivity(i);
        finish();
    }


    /**
     * Check for the READ_EXTERNAL_STORAGE permission.
     * @param context The context of the application.
     * @return True if the permission is granted, false if not.
     */
    public static boolean CheckForExtStorageReadPerm(Context context){
        int permCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permCheck == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }


    private void PerformAfterPOSTCheck(HttpsPOST post){
        int ResponseCode = post.getResponseCode();
        if (post.HasErrors()){
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
                        result = post.getResult();
                        AnalyzeJSONResponse(result);
                    } catch (HttpsPOST.POSTNotExecutedException e) {
                        e.printStackTrace();
                    }
                    break;
                case HTTP_UNAUTHORIZED:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_login), Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.server_not_found), Toast.LENGTH_LONG).show();
            }
        }



    }

    /**
     * Request the READ_EXTERNAL_STORAGE permission.
     */
    private void reqPerms(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERM);
    }

    private void AnalyzeJSONResponse(String s){
        try {
            JSONObject resp = new JSONObject(s);
            //If another device has registered a profile.
            if (resp.getString("message").equals("PROFILE_REGISTERED")){
                AlertDialog.Builder bob = new AlertDialog.Builder(LoginActivity.this);
                bob.setMessage(getResources().getString(R.string.profile_already_registered))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            } else if (resp.getString("message").equals("CREATED")){
                //If the profile was created successfully, get the returned AuthToken and the PIN
                AuthToken = resp.getString(getResources().getString(R.string.auth_token_params));
                tempPIN = resp.getString("pin");

                //Store the AuthToken...
                SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PINEntryActivity.AUTH_TOKEN_KEY, AuthToken);
                editor.commit();

                //Show the PIN to the user.
                AlertDialog.Builder bob = new AlertDialog.Builder(LoginActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View v = inflater.inflate(R.layout.pin_dialog, null);
                TextView pin_view = (TextView) v.findViewById(R.id.dialog_PIN);

                pin_view.setText(tempPIN);
                bob.setView(v).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LaunchPINEntryActivity();
                    }
                }).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_login);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //If the Server URL is "none", then the URL hasn't been set. Launch {@link SettingsActivity}.
        if (sharedPrefs.getString(SettingsActivity.SERVER_ADDRESS_KEY, "none").equals("none")){
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            finish();
        }

        //Make sure permission is granted to read external storage.
        if (!CheckForExtStorageReadPerm(getApplicationContext())){
            reqPerms();
        }

        //Check if the AuthToken is stored in the device. If it is, launch the PINEntryActivity.
        SharedPreferences authPrefs = getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);
        if (!authPrefs.getString(PINEntryActivity.AUTH_TOKEN_KEY, "").equals("")){
            LaunchPINEntryActivity();
        }

        usernameET = (EditText) findViewById(R.id.login_username);
        passwordET = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_button);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginButton.setEnabled(false);
                if (!CheckForExtStorageReadPerm(getApplicationContext())) {
                    reqPerms();
                    loginButton.setEnabled(true);
                    return;
                }
                if (!Heartbeat.isAlive(getApplicationContext())){
                    loginButton.setEnabled(true);
                    return;
                }
                username = usernameET.getText().toString();
                password = passwordET.getText().toString();

                if (!ValidateCredentials(username, password)){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_enter_username_and_password), Toast.LENGTH_LONG).show();
                    loginButton.setEnabled(true);
                    return;
                }


                try {

                    String serverURL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(SettingsActivity.SERVER_ADDRESS_KEY, "none");
                    URL LoginURL = new URL(serverURL+"/login");
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(getResources().getString(R.string.username_params), username);
                    params.put(getResources().getString(R.string.password_params), password);
                    HttpsPOST post = new HttpsPOST(LoginURL, params);
                    post.SendPOST(getApplicationContext());
                    PerformAfterPOSTCheck(post);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } finally {
                    loginButton.setEnabled(true);
                }



            }
        });
    }

    /**
     * Check if the supplied Username and Password are not empty
     * @param username The username
     * @param password The password
     * @return True if both aren't empty, false if at least one of them is.
     */
    private boolean ValidateCredentials(String username, String password) {
        return !(username.length() == 0 || password.length() == 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings_menu_choice:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                finish();
                return true;
            case R.id.settings_about_choice:
                ShowAboutDialog(LoginActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void ShowAboutDialog(Context context){
        AlertDialog.Builder bob = new AlertDialog.Builder(context);
        bob.setTitle("About PiLock")
                .setMessage(context.getResources().getString(R.string.about_message))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
