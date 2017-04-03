package com.kerk12.pilock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    String username = null;
    String password = null;

    String AuthToken = null;

    EditText usernameET, passwordET;
    Button loginButton;
    private class LoginTask extends AsyncTask<String, Void, String>{

        boolean failed = false;
        int failure = 0;

        private String getResult(URL serverURL) throws IOException {
            InputStream is = null;

            HttpsURLConnection conn = (HttpsURLConnection) serverURL.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            Map params = new HashMap<String, String>();
            params.put("username", username);
            params.put("password", password);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            QueryBuilder builder = new QueryBuilder(params);
            writer.write(builder.getQuery());
            writer.flush();
            writer.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                //Read the result (will be processed later...)
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }
                is.close();
                String content = sb.toString();

                return content;
            } else if (conn.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN){
                failure = 403;
                failed = true;
            }
            return null;
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream in;

            URL serverLoginURL = null;
            try {
               serverLoginURL = new URL(params[0]+"/login");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String res = null;
            try {
                res = getResult(serverLoginURL);
            } catch (IOException e) {
                e.printStackTrace();
            }


            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (failed){
                if (failure == 403){
                    Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_LONG).show();
                }
                return;
            }
            AuthToken = s;

        }
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

        usernameET = (EditText) findViewById(R.id.login_username);
        passwordET = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                username = usernameET.getText().toString();
                password = passwordET.getText().toString();

                LoginTask t = new LoginTask();
                String[] url = {sharedPrefs.getString(SettingsActivity.SERVER_ADDRESS_KEY, "none")};
                t.execute(url);
                String result;
                try {
                    result = t.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
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
