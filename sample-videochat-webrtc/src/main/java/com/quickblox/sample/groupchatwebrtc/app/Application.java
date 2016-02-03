package com.quickblox.sample.groupchatwebrtc.app;

/**
 * Created by igorkhomenko on 2/2/16.
 */
public class Application extends android.app.Application {

    private static Application instance;

    private ActivityLifecycleHandler activityLifecycleHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        initApplication();

        activityLifecycleHandler = new ActivityLifecycleHandler();
        registerActivityLifecycleCallbacks(activityLifecycleHandler);
    }

    public int getNumberOfActivitiesInForeground() {
        return activityLifecycleHandler.getNumberOfActivitiesInForeground();
    }

    public int getNumberOfCreatedActivities() {
        return activityLifecycleHandler.getNumberOfCreatedActivities();
    }

    public boolean isLatestStoppedActivityIsCallOrSettings(){
        return activityLifecycleHandler.isLatestStoppedActivityIsCallOrSettings();
    }

    public static Application getInstance() {
        return instance;
    }

    private void initApplication() {
        instance = this;
    }
}
