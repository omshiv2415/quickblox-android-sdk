package com.sdk.snippets.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.sdk.snippets.R;
import com.sdk.snippets.core.SnippetsList;
import com.sdk.snippets.modules.SnippetsAuth;
import com.sdk.snippets.utils.Utils;

import java.util.Date;

public class AuthActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snippets_list);

        SnippetsAuth snippets = new SnippetsAuth(this);
        SnippetsList list = (SnippetsList) findViewById(R.id.list);
        list.initialize(snippets);


        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("LOG", "requesting time..");

                Date dt = Utils.getUTCDate();
                Log.d("LOG", "dt: " +  dt.toString());

                long ts = Utils.getUTCTimestamp();
                Log.d("LOG", "ts: " + ts);
            }
        }).start();
    }
}