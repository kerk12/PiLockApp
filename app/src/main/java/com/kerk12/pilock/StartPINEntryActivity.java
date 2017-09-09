package com.kerk12.pilock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class StartPINEntryActivity extends WearableListenerService{

    private static final String START_ACTIVITY = "/start_activity";
    private static final String UNLOCK = "/unlock";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(UNLOCK)){
            String wearToken = new String(messageEvent.getData());

            Intent i = new Intent(this, PINEntryActivity.class);
            //TODO Implement more checks...
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(PINEntryActivity.HEADLESS_EXTRA, true);
            i.putExtra(PINEntryActivity.WEAR_TOKEN, wearToken);
            startActivity(i);
        }
    }
}
