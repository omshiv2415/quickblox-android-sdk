package com.quickblox.sample.groupchatwebrtc.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChatService;

import org.jivesoftware.smack.SmackException;

/**
 * Created by igorkhomenko on 2/2/16.
 */
public class ActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "ActivityLifecycle";

    //

    private int numberOfActivitiesInForeground;

    int getNumberOfActivitiesInForeground() {
        return numberOfActivitiesInForeground;
    }

    //

    private int numberOfCreatedActivities;

    int getNumberOfCreatedActivities() {
        return numberOfCreatedActivities;
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        ++numberOfCreatedActivities;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++numberOfActivitiesInForeground;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        --numberOfActivitiesInForeground;

        if(numberOfActivitiesInForeground == 0){
            try {
                QBChatService.getInstance().logout();
                Log.d(TAG, "chat logout done");
            } catch (SmackException.NotConnectedException e) {
                Log.d(TAG, "chat logout error:");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        --numberOfCreatedActivities;
    }
}

