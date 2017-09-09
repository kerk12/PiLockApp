package com.kerk12.pilock;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class UnlockActivity extends Activity {

    private TextView mTextView;
    private ImageView unlock_icon;

    GoogleApiClient mClient = null;

    boolean isConnected = false;

    private String WearToken;
    Node mNode = null;

    public static final String START_ACTIVITY = "/start_activity";
    private static final String UNLOCK = "/unlock";

    private void sendMessage(final String action, final String message){
        Wearable.MessageApi.sendMessage(mClient, mNode.getId(), action, message.getBytes()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                unlock_icon = (ImageView) stub.findViewById(R.id.unlock_icon);

                SharedPreferences authPrefs = getSharedPreferences(getResources().getString(R.string.auth_prefs), MODE_PRIVATE);
                WearToken = authPrefs.getString("wearToken", "None");



                mClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(@Nullable Bundle bundle) {
                                Log.d("CONN", "Connection success");
                                isConnected = true;
                                Wearable.NodeApi.getConnectedNodes(mClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                    @Override
                                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                                        for (Node node : getConnectedNodesResult.getNodes()){
                                            if (node.isNearby()){
                                                mNode = node;
                                            }
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                Log.d("CONN", "Connection suspended");

                            }
                        })
                        .addApi(Wearable.API).build();
                mClient.connect();

                unlock_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (WearToken.equals("None")){
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_synced), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (isConnected && mNode != null){
                            sendMessage(UNLOCK, WearToken);
                            Toast.makeText(getApplicationContext(), getString(R.string.unlock_request_sent), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.could_not_connect_to_phone), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnected){
            mClient.disconnect();
        }
    }
}
