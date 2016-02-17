package com.quickblox.sample.groupchatwebrtc.app;

import com.quickblox.core.QBSettings;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;

/**
 * Created by igorkhomenko on 2/2/16.
 */
public class Application extends android.app.Application {

    private static Application instance;

    private ActivityLifecycleHandler activityLifecycleHandler;

    @Override
    public void onCreate() {
        super.onCreate();


        // Initialise QuickBlox SDK
        //
//        final String APP_ID = "92";
//        final String AUTH_KEY = "wJHdOcQSxXQGWx5";
//        final String AUTH_SECRET = "BTFsj7Rtt27DAmT";
        final String APP_ID = "4";
        final String AUTH_KEY = "bLEDyOzwhexJRAm";
        final String AUTH_SECRET = "AJq7CBm-4VB5ErJ";
        //
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setServerApiDomain("apitelvous.quickblox.com");
        QBSettings.getInstance().setChatServerDomain("chattelvous.quickblox.com");

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

    public static Application getInstance() {
        return instance;
    }

    private void initApplication() {
        instance = this;
    }
}
