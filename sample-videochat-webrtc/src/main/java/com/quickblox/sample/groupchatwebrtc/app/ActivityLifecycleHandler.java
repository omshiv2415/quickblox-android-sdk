package com.quickblox.sample.groupchatwebrtc.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.sample.groupchatwebrtc.activities.LoginActivity;

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

    //

    private boolean isLatestStoppedActivityIsCallOrSettings;

    boolean isLatestStoppedActivityIsCallOrSettings(){
        return isLatestStoppedActivityIsCallOrSettings;
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

        if (activity instanceof LoginActivity) {
            Log.d(TAG, "stopping LoginActivity");
            isLatestStoppedActivityIsCallOrSettings = false;
        }else{
            Log.d(TAG, "stopping CallActivity or SettingsActivity");
            isLatestStoppedActivityIsCallOrSettings = true;
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

