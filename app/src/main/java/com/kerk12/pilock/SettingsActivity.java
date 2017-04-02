package com.kerk12.pilock;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kerk12 on 03/04/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingsFragment sfNew = new SettingsFragment();
        getFragmentManager().beginTransaction().add(R.id.settings_frame, sfNew).commit();
    }
}
