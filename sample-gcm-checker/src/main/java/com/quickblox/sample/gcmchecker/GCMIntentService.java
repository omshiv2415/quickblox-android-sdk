package com.quickblox.sample.gcmchecker;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.sample.gcmchecker.main.Consts;

public class GCMIntentService extends IntentService {

    private static final String TAG = GCMIntentService.class.getSimpleName();

    public GCMIntentService() {
        super(Consts.GCM_INTENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "new push");

        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
        String messageType = googleCloudMessaging.getMessageType(intent);
        Log.i(TAG, "new push type - " + messageType);
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            long currentTimeMillis = System.currentTimeMillis();

            intent.putExtra(Consts.EXTRA_DELIVERY_DATE, String.valueOf(currentTimeMillis));
            intent.setAction(Consts.NEW_PUSH_EVENT);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}