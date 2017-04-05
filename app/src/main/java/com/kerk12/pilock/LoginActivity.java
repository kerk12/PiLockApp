package com.kerk12.pilock;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

public class LoginActivity extends AppCompatActivity {

    private final int STORAGE_PERM = 1;

    String username = null;
    String password = null;

    String AuthToken = null;
    String tempPIN = null;

    EditText usernameET, passwordET;
    Button loginButton;
    // Old Login Task. Will be replaced.
//    private class LoginTask extends AsyncTask<String, Void, String>{
//
//        boolean failed = false;
//        int failure = 0;
//        boolean CertFailure = false;
//        String CertfailMsg = null;
//
//        private String getResult(URL serverURL) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
//            InputStream is = null;
//
//
//            SSLContext context = null;
//            try {
//                context = CustomSSLTruster.TrustCertificate();
//            } catch (GeneralSecurityException e) {
//                CertFailure = true;
//                CertfailMsg = "Could not read Certificate file.";
//            }
//
//            HttpsURLConnection conn = (HttpsURLConnection) serverURL.openConnection();
//            conn.setSSLSocketFactory(context.getSocketFactory());
//            conn.setConnectTimeout(5000);
//            conn.setReadTimeout(10000);
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//            conn.setUseCaches(false);
//
//            Map params = new HashMap<String, String>();
//            params.put("username", username);
//            params.put("password", password);
//
//            OutputStream os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//            QueryBuilder builder = new QueryBuilder(params);
//            writer.write(builder.getQuery());
//            writer.flush();
//            writer.close();
//
//            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
//                //Read the result (will be processed later...)
//                is = conn.getInputStream();
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
//                StringBuilder sb = new StringBuilder();
//                String line = null;
//                while ((line = reader.readLine()) != null){
//                    sb.append(line + "\n");
//                }
//                is.close();
//                String content = sb.toString();
//
//                return content;
//            } else if (conn.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN){
//                failure = 403;
//                failed = true;
//            }
//            return null;
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            InputStream in;
//
//            URL serverLoginURL = null;
//            try {
//               serverLoginURL = new URL(params[0]+"/login");
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//            String res = null;
//            try {
//                res = getResult(serverLoginURL);
//            }catch (IOException | CertificateException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
//                e.printStackTrace();
//            }
//
//
//            return res;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            if (failed){
//                if (failure == 403){
//                    Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_LONG).show();
//                }
//                return;
//            }
//            AuthToken = s;
//
//        }
//    }



    private class LoginTask extends AsyncTask<Void, Void, String>{

        private boolean HasErrors = false;
        private int ResponseCode = 200;
        private boolean CertError = false;

        private String getResult() throws GeneralSecurityException, IOException {
            InputStream is = null;

            String serverURL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(SettingsActivity.SERVER_ADDRESS_KEY, "none");
            URL loginURL = new URL(serverURL + "/login");

            HttpsURLConnection conn = (HttpsURLConnection) loginURL.openConnection();
            conn.setSSLSocketFactory(CustomSSLTruster.TrustCertificate().getSocketFactory());
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(10000);

            Map<String, String> loginParams = new HashMap<String, String>();
            loginParams.put("username", username);
            loginParams.put("password", password);

            QueryBuilder builder = new QueryBuilder(loginParams);
            OutputStream os = conn.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(builder.getQuery());

            writer.flush();
            writer.close();

            ResponseCode = conn.getResponseCode();
            if (ResponseCode == HttpURLConnection.HTTP_ACCEPTED) {
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);

                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();
                return sb.toString();
            }
            HasErrors = true;
            return null;


        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            try {
                result = getResult();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                CertError = true;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (HasErrors){
                if (CertError) {
                    Toast.makeText(getApplicationContext(), "Certificate validation error...", Toast.LENGTH_LONG);
                    return;
                }
                switch (ResponseCode){
                    case 401:
                        Toast.makeText(getApplicationContext(), "Invalid Login Credentials", Toast.LENGTH_LONG).show();
                        break;
                }
                return;
            }
            try {
                JSONObject resp = new JSONObject(s);
                if (resp.getString("message").equals("PROFILE_REGISTERED")){
                    AlertDialog.Builder bob = new AlertDialog.Builder(getApplicationContext());
                    bob.setMessage(getResources().getString(R.string.profile_already_registered))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                } else if (resp.getString("message").equals("CREATED")){
                    AuthToken = resp.getString("auth_token");
                    tempPIN = resp.getString("pin");

                    SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(PINEntryActivity.AUTH_TOKEN_KEY, AuthToken);
                    editor.commit();

                    AlertDialog.Builder bob = new AlertDialog.Builder(getApplicationContext());
                    LayoutInflater inflater = getLayoutInflater();
                    View v = inflater.inflate(R.layout.pin_dialog, null);
                    TextView pin_view = (TextView) v.findViewById(R.id.dialog_PIN);

                    pin_view.setText(tempPIN);
                    bob.setView(v).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO Launch the other activity
                        }
                    }).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean CheckForExtStorageReadPerm(Context context){
        int permCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permCheck == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void reqPerms(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERM);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPrefs.getString(SettingsActivity.SERVER_ADDRESS_KEY, "none").equals("none")){
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }
        if (!CheckForExtStorageReadPerm(getApplicationContext())){
            reqPerms();
        }
        usernameET = (EditText) findViewById(R.id.login_username);
        passwordET = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!CheckForExtStorageReadPerm(getApplicationContext())) {
                    reqPerms();
                    return;
                }
                username = usernameET.getText().toString();
                password = passwordET.getText().toString();
                LoginTask t = new LoginTask();
                t.execute();

            }
        });
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
