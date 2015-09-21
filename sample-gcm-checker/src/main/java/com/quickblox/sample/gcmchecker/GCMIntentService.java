package com.quickblox.sample.gcmchecker;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.sample.gcmchecker.main.Consts;

public class GCMIntentService extends IntentService {

    private static final String TAG = GCMIntentService.class.getSimpleName();

    public GCMIntentService() {
        super(Consts.GCM_INTENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "new push");

        intent.setAction(Consts.NEW_PUSH_EVENT);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

}