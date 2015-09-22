package com.quickblox.sample.gcmchecker.main.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.sample.gcmchecker.R;
import com.quickblox.sample.gcmchecker.main.Consts;
import com.quickblox.sample.gcmchecker.main.ReportAdapter;
import com.quickblox.sample.gcmchecker.main.helper.PlayServicesHelper;
import com.quickblox.sample.gcmchecker.main.models.Credentials;
import com.quickblox.sample.gcmchecker.main.models.Report;
import com.quickblox.sample.gcmchecker.main.utils.DialogUtils;
import com.quickblox.users.model.QBUser;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tereha on 15.09.15.
 */
public class CheckerActivity extends Activity {
    private String TAG = CheckerActivity.class.getSimpleName();
    private Button startCheckerBtn;
    private ListView messagesList;
    private PlayServicesHelper playServicesHelper;
    private ArrayList<Report> listReports = new ArrayList<>();
    private ReportAdapter reportAdapter;
    private ProgressBar checkerPB;
    private Thread t;
    private GoogleCloudMessaging googleCloudMessaging;
    private String regId;
    private Credentials currentCredentials;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_notification_checker_layout);
        initUI();

//        playServicesHelper = new PlayServicesHelper(this);

        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));

        Log.d(TAG, "Downloaded information about " + String.valueOf(SplashActivity.credentialsList.size()) + " servers");

    }

    private void initUI() {
        startCheckerBtn = (Button) findViewById(R.id.startCheckerBtn);
        messagesList = (ListView) findViewById(R.id.messagesList);
        checkerPB = (ProgressBar) findViewById(R.id.startCheckerPB);
        reportAdapter = new ReportAdapter(this, listReports);
        messagesList.setAdapter(reportAdapter);
    }

    public Credentials getCurrentCredentials() {
        return currentCredentials;
    }

    public void setCurrentCredentials(Credentials currentCredentials) {
        this.currentCredentials = currentCredentials;
    }

    public void startChecker(View view) {
        checkerPB.setVisibility(View.VISIBLE);
//        Credentials credentials = SplashActivity.credentialsList.get(16);
//        setCurrentCredentials(credentials);
//        Credentials currentCredentials = getCurrentCredentials();
//        initApp(currentCredentials.getAppId(),
//                currentCredentials.getAuthKey(),
//                currentCredentials.getAuthSecret(),
//                currentCredentials.getServerApiDomain());
//        createSession(Integer.parseInt(currentCredentials.getUserID()),
//                currentCredentials.getUserLogin(),
//                currentCredentials.getUserPass());

        initApp(Consts.APP_ID,
                Consts.AUTH_KEY,
                Consts.AUTH_SECRET,
                null);
        createSession(2224038,
                Consts.USER_LOGIN,
                Consts.USER_PASSWORD);
        Credentials credentials = new Credentials();
        credentials.setUserID("2224038");
        credentials.setTitle("starter");
        setCurrentCredentials(credentials);



//        final Handler handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                Bundle bundle = msg.getData();
//                QBEvent qbEvent = (QBEvent) bundle.getSerializable(Consts.QBEVENT_EXTRAS);
//                sendPushNotification(qbEvent);
//            }
//
//        };
//
//        t = new Thread(new Runnable() {
//            public void run() {
////                loadServersData();
//                Credentials credentials = SplashActivity.credentialsList.get(0);
//                initApp(credentials.getAppId(),
//                        credentials.getAuthKey(),
//                        credentials.getAuthSecret(),
//                        credentials.getServerApiDomain());
//                createSession(credentials.getUserLogin(),
//                        credentials.getUserPass());
//
////                try {
////                    do {
////                        Message msg = handler.obtainMessage();
////                        Bundle bundle = new Bundle();
////                        bundle.putSerializable(Consts.QBEVENT_EXTRAS, createPushNotificationEvent());
////                        msg.setData(bundle);
////                        handler.sendMessage(msg);
////                        TimeUnit.MILLISECONDS.sleep(Consts.PUSH_TIMEOUT);
////                    } while (Consts.PUSH_TIMEOUT > 0);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//            }
//        });
//        t.start();
    }

    public QBEvent createPushNotificationEvent(){
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);

        long currentTimeMillis = System.currentTimeMillis();

        JSONObject json = new JSONObject();
        try {
            json.put(Consts.EXTRA_MESSAGE, "");
            json.put(Consts.EXTRA_PUSH_ID, "id");
            json.put(Consts.EXTRA_SEND_DATE, String.valueOf(currentTimeMillis));
            json.put(Consts.EXTRA_SERVER_TITLE, getCurrentCredentials().getTitle());
        } catch (Exception e) {
            e.printStackTrace();
        }

        qbEvent.setMessage(json.toString());

        StringifyArrayList<Integer> userIds = new StringifyArrayList<>();
        userIds.add(Integer.parseInt(getCurrentCredentials().getUserID()));
        qbEvent.setUserIds(userIds);

        return qbEvent;
    }

    public void sendPushNotification(QBEvent qbEvent){
        QBMessages.createEvent(qbEvent, new QBEntityCallbackImpl<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                Log.d(TAG, "pushSended");
                checkerPB.setVisibility(View.GONE);

            }

            @Override
            public void onError(List<String> strings) {
                Log.d(TAG, "pushErrorSend");
                // errors
                checkerPB.setVisibility(View.GONE);
                DialogUtils.showLong(CheckerActivity.this, strings.toString());

            }
        });
    }



    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "new broadcast message ");
            Report report = new Report();
            report.setSendDate(intent.getExtras().getString(Consts.EXTRA_SEND_DATE));
            report.setDeliveryDate(intent.getExtras().getString(Consts.EXTRA_DELIVERY_DATE));
            report.setServerTitle(intent.getExtras().getString(Consts.EXTRA_SERVER_TITLE));

//            for (String key : intent.getExtras().keySet()){
//                Log.d(TAG, key + ": " + intent.getExtras().get(key));
//            }

            processingMessage(report);
        }

    };

    private void processingMessage(Report report){
        long timeFromMessage = Long.parseLong(report.getSendDate());
        long currentTimeMillis = Long.parseLong(report.getDeliveryDate());
        Date dateSend = new Date (timeFromMessage);
        Date currentDate = new Date (currentTimeMillis);
//                "yyyy-MM-dd",
//                "yyyy-MM-dd HH:mm",
//                "yyyy-MM-dd HH:mmZ",
//                "yyyy-MM-dd HH:mm:ss.SSSZ",
//                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss.SSS");
        String dateSendText = df2.format(dateSend);
        String currentDateText = df2.format(currentDate);
        String travelingDateText = String.valueOf((currentTimeMillis - timeFromMessage)/1000);
        Log.d(TAG, "\n"
                + "\ndateSend = " + dateSendText
                + "\n" + "currentDateText = " + currentDateText
                + "\n" + "timeout = " + (currentTimeMillis - timeFromMessage) / 1000 + " sec");

        Report report1 = new Report (dateSendText, currentDateText, travelingDateText);
        report1.setServerTitle(report.getServerTitle());
        listReports.add(report1);
        reportAdapter.notifyDataSetChanged();

    }


    public void stopChecker(View view) {
        if (t != null) {
            Thread dummy = t;
            t = null;
            dummy.interrupt();
        }

        checkerPB.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPushReceiver);

        super.onDestroy();
    }

    private void initApp (Integer appId, String authKey, String authSecret, String serverApiDomain){
        if (serverApiDomain != null) {
            if (serverApiDomain.contains("https://")){
                StringBuilder stringBuilder = new StringBuilder(serverApiDomain);
                stringBuilder.delete(0,8);
                serverApiDomain = stringBuilder.toString();
            }
            QBSettings.getInstance().setServerApiDomain(serverApiDomain);
        }
        QBSettings.getInstance().fastConfigInit(appId.toString(), authKey, authSecret);
    }

    private void createSession (final Integer userId, String userLogin, String userPass) {
        final QBUser qbUser = new QBUser();
        qbUser.setLogin(userLogin);
        qbUser.setPassword(userPass);

        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                subscribeToPushNotifications(userId);
            }

            @Override
            public void onError(List<String> strings) {
                for (String s : strings){
                    Log.d(TAG, "Error subscribing " + s);
                }

            }
        });
    }

    private void subscribeToPushNotifications(final Integer userId) {
        Log.d(TAG, "subscribing...");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (googleCloudMessaging == null) {
                        googleCloudMessaging = GoogleCloudMessaging.getInstance(CheckerActivity.this);
                    }
                    regId = googleCloudMessaging.register(Consts.PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regId;

                    Handler h = new Handler(getMainLooper());
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            String deviceId;

                            final TelephonyManager mTelephony = (TelephonyManager) getSystemService(
                                    Context.TELEPHONY_SERVICE);
                            if (mTelephony.getDeviceId() != null) {
                                deviceId = mTelephony.getDeviceId(); //*** use for mobiles
                            } else {
                                deviceId = Settings.Secure.getString(getContentResolver(),
                                        Settings.Secure.ANDROID_ID); //*** use for tablets
                            }

                            QBMessages.subscribeToPushNotificationsTask(regId, deviceId, QBEnvironment.DEVELOPMENT, new QBEntityCallbackImpl<ArrayList<QBSubscription>>() {
                                @Override
                                public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                                    Log.d(TAG, "subscribed");
                                    sendPushNotification(createPushNotificationEvent());
                                }

                                @Override
                                public void onError(List<String> errors) {
                                    for (String s : errors){
                                        Log.d(TAG, "Error subscribing " + s);
                                    }

                                }
                            });
                        }
                    });
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, msg + "\n");
            }
        }.execute(null, null, null);
    }
}
