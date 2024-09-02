package com.zapry.pkdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.zapry.privatekey.ZapryPrivateKeyHelper;

public class RootActivity extends AppCompatActivity {

    private EditText minRefreshTimeEditText, maxCacheEditText;

    private ZapryPrivateKeyHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        maxCacheEditText = findViewById(R.id.maxCacheEditText);
        minRefreshTimeEditText = findViewById(R.id.minRefreshTimeEditText);

        findViewById(R.id.to_live_btn).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
            }
        });
    }
}