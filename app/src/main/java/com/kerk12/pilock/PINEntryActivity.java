package com.kerk12.pilock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PINEntryActivity extends AppCompatActivity {

    public static final String AUTH_TOKEN_KEY = "authToken";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinentry);
    }
}
