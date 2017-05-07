package com.kerk12.pilock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by kerk12 on 03/04/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String SERVER_ADDRESS_KEY = "server_address";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingsFragment sfNew = new SettingsFragment();
        getFragmentManager().beginTransaction().add(R.id.settings_frame, sfNew).commit();
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPrefs.getString(SERVER_ADDRESS_KEY, "none").equals("none")){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_server_url_detected), Toast.LENGTH_LONG).show();
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        } else {
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
