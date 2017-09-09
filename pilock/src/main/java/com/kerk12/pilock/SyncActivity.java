package com.kerk12.pilock;

import android.content.SharedPreferences;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by kerk12 on 9/9/17.
 */

public class SyncActivity extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(UnlockActivity.START_ACTIVITY)){
            String wearToken = new String(messageEvent.getData());
            SharedPreferences authPrefs = getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);

            SharedPreferences.Editor editor = authPrefs.edit();
            editor.putString("wearToken", wearToken);
            editor.commit();


        }
        super.onMessageReceived(messageEvent);
    }
}
