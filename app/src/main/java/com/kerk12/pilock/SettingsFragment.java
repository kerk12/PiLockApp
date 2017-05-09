package com.kerk12.pilock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

/**
 * Created by kerk12 on 03/04/2017.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        EditTextPreference serverAddress = (EditTextPreference) getPreferenceScreen().findPreference(SettingsActivity.SERVER_ADDRESS_KEY);
        serverAddress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newValueStr = newValue.toString();
                // Check if the string entered is empty.
                if(newValueStr.length() == 0){
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_server_url_supplied), Toast.LENGTH_LONG).show();
                    return false;
                }
                //Check if the string is a URL.
                if (Patterns.WEB_URL.matcher(newValueStr).matches()){
                    //Check if there is a trailing backslash at the end.
                    if (newValueStr.charAt(newValueStr.length() - 1) == '/'){
                        Toast.makeText(getActivity(), "Please remove the trailing slash at the end (/)", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    //Check if Plain HTTP is used.
                    if (newValueStr.startsWith("http:")){
                        Toast.makeText(getActivity(), getResources().getString(R.string.http_not_supported), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return true;
                }
                Toast.makeText(getActivity(), "The Server URL you have provided is invalid.", Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

    }
}
