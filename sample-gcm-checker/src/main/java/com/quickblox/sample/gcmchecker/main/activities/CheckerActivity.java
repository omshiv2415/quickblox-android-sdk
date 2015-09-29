package com.quickblox.sample.gcmchecker.main.activities;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.helper.StringUtils;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.sample.gcmchecker.QuerySendReport;
import com.quickblox.sample.gcmchecker.R;
import com.quickblox.sample.gcmchecker.main.Consts;
import com.quickblox.sample.gcmchecker.main.ReportAdapter;
import com.quickblox.sample.gcmchecker.main.models.Credentials;
import com.quickblox.sample.gcmchecker.main.models.Report;
import com.quickblox.sample.gcmchecker.main.models.ResultTests;
import com.quickblox.sample.gcmchecker.main.utils.DialogUtils;
import com.quickblox.users.model.QBUser;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by tereha on 15.09.15.
 */
public class CheckerActivity extends AppCompatActivity {
    private String TAG = CheckerActivity.class.getSimpleName();
    private Button startCheckerBtn;
    private ListView serversListView;
    private ArrayList<Report> listReports = new ArrayList<>();
    private ReportAdapter reportAdapter;
    private ProgressBar checkerPB;
    private GoogleCloudMessaging googleCloudMessaging;
    private String regId;
    private Credentials currentCredentials;
    public static HashMap<String, ArrayList<ResultTests>> resultsMap;
    private ArrayList<Report> reportList;
    private int desiredPushId;
    private ArrayList<Credentials> credentialsList;
    private Handler checkServerTaskHandler;
    private Runnable checkServerTask;
    private int i = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_notification_checker_layout);
        prepareData();
        initUI();

        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));

        Log.d(TAG, "Downloaded information about " + String.valueOf(SplashActivity.credentialsList.size()) + " servers");

    }

    private void prepareData(){
        credentialsList = SplashActivity.credentialsList;
        reportList = new ArrayList<>();
        for (Credentials credentials : credentialsList){
            Report report = new Report();
            report.setSendedPushes(0);
            report.setSuccessPushes(0);
            report.setColorStatusOval(Consts.STATUS_COLOR_IN_PROGRESS);
            report.setServerTitle(credentials.getTitle());
            reportList.add(report);
        }
    }

    private void initUI() {
        serversListView = (ListView) findViewById(R.id.serversList);
        reportAdapter = new ReportAdapter(this, reportList);
        serversListView.setAdapter(reportAdapter);
        serversListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedServerTitle = credentialsList.get(position).getTitle();
                if (resultsMap != null) {
                    if (resultsMap.containsKey(selectedServerTitle)) {
                        Intent intent = new Intent(CheckerActivity.this, ResultTestsActivity.class);
                        intent.putExtra(Consts.EXTRA_SERVER_TITLE, selectedServerTitle);
                        startActivity(intent);
                    } else {
                        DialogUtils.show(CheckerActivity.this, getString(R.string.no_information));
                    }
                } else {
                    DialogUtils.show(CheckerActivity.this, getString(R.string.no_information));
                }
            }
        });
        startCheckerBtn = (Button) findViewById(R.id.startCheckerBtn);
        checkerPB = (ProgressBar) findViewById(R.id.startCheckerPB);
    }

    public Credentials getCurrentCredentials() {
        return currentCredentials;
    }

    public void setCurrentCredentials(Credentials currentCredentials) {
        this.currentCredentials = currentCredentials;
    }

    public int getDesiredPushId() {
        return desiredPushId;
    }

    public void setDesiredPushId(int desiredPushId) {
        this.desiredPushId = desiredPushId;
    }

    public void startCheckerClick(View view) {
        if (checkServerTaskHandler == null){
            initCheckServerTask();
        }

        startCheckServerByIndex(0);
    }

    private void startCheckServerByIndex(int index) {
        checkerPB.setVisibility(View.VISIBLE);
        setDesiredPushId(0);
        Credentials credentials = credentialsList.get(index);
        setCurrentCredentials(credentials);

        if (i == credentialsList.size() - 1) {
            i = 0;
        } else {
            i++;
        }

        startCheckTimer();
        initApp();
        createSession();
    }

    private void initApp (){
        Integer appId = getCurrentCredentials().getAppId();
        String authKey = getCurrentCredentials().getAuthKey();
        String authSecret = getCurrentCredentials().getAuthSecret();
        String urlServerApiDomain = getCurrentCredentials().getServerApiDomain();

        if (urlServerApiDomain != null) {
            String serverApiDomain = null;
            try {
                URL url = new URL(urlServerApiDomain);
                serverApiDomain = url.getHost();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            QBSettings.getInstance().setServerApiDomain(serverApiDomain);
        }
        QBSettings.getInstance().fastConfigInit(appId.toString(), authKey, authSecret);
    }

    private void createSession () {
        String userLogin = getCurrentCredentials().getUserLogin();
        String userPass = getCurrentCredentials().getUserPass();

        final QBUser qbUser = new QBUser();
        qbUser.setLogin(userLogin);
        qbUser.setPassword(userPass);

        QBAuth.createSession(qbUser, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                subscribeToPushNotifications();
            }

            @Override
            public void onError(List<String> strings) {
                stopCheckTimer();
                setColorStatusOval(Consts.STATUS_COLOR_FAIL);
                for (String s : strings) {
                    Log.d(TAG, "Error subscribing " + s);
                    saveTestResult(s);
                }
                sendResultToServer(getCurrentCredentials().getTitle(), null, -1);
                startCheckServerByIndex(i);
            }
        });
    }

    private void subscribeToPushNotifications() {
        Log.d(TAG, "subscribing...");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (googleCloudMessaging == null) {
                        googleCloudMessaging = GoogleCloudMessaging.getInstance(CheckerActivity.this);
                    }
                    regId = googleCloudMessaging.register(Consts.PROJECT_NUMBER);

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
                                    stopCheckTimer();
                                    setColorStatusOval(Consts.STATUS_COLOR_FAIL);
                                    for (String s : errors){
                                        Log.d(TAG, "Error subscribing " + s);
                                        saveTestResult(s);
                                    }
                                    sendResultToServer(getCurrentCredentials().getTitle(), null, -1);
                                    startCheckServerByIndex(i);
                                }
                            });
                        }
                    });
                } catch (IOException ex) {
                    stopCheckTimer();
                    saveTestResult(ex.getMessage());
                    sendResultToServer(getCurrentCredentials().getTitle(), null, -1);
                    startCheckServerByIndex(i);
                }
                return null;
            }
        }.execute(null, null, null);
    }

    public void sendPushNotification(QBEvent qbEvent){
        addSendedPushToReport();
        QBMessages.createEvent(qbEvent, new QBEntityCallbackImpl<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {
                Log.d(TAG, "pushSended");
                checkerPB.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> strings) {
                stopCheckTimer();
                Log.d(TAG, "pushErrorSend" + strings.toString());
                checkerPB.setVisibility(View.GONE);
                DialogUtils.show(CheckerActivity.this, strings.toString());
                setColorStatusOval(Consts.STATUS_COLOR_FAIL);
                for (String s : strings) {
                    saveTestResult("Error" + s);
                }
                sendResultToServer(getCurrentCredentials().getTitle(), null, -1);
                startCheckServerByIndex(i);
            }
        });
    }

    public QBEvent createPushNotificationEvent(){
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);

        int pushId = new Random(System.currentTimeMillis()).nextInt(999999 - 100000) + 100000;
        setDesiredPushId(pushId);
        long currentTimeMillis = System.currentTimeMillis();

        JSONObject json = new JSONObject();
        try {
            json.put(Consts.EXTRA_MESSAGE, "");
            json.put(Consts.EXTRA_PUSH_ID, pushId);
            json.put(Consts.EXTRA_SEND_DATE, String.valueOf(currentTimeMillis));
            json.put(Consts.EXTRA_SERVER_TITLE, getCurrentCredentials().getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            saveTestResult(e.getMessage());
        }

        qbEvent.setMessage(json.toString());

        StringifyArrayList<Integer> userIds = new StringifyArrayList<>();
        userIds.add(Integer.parseInt(getCurrentCredentials().getUserID()));
        qbEvent.setUserIds(userIds);

        return qbEvent;
    }

    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "new broadcast message ");
            String sendDate = intent.getExtras().getString(Consts.EXTRA_SEND_DATE);
            String deliveryDate = intent.getExtras().getString(Consts.EXTRA_DELIVERY_DATE);
            String serverTitle = intent.getExtras().getString(Consts.EXTRA_SERVER_TITLE);
            String pushId = intent.getExtras().getString(Consts.EXTRA_PUSH_ID);

            boolean isDesiredPush = false;
            if (pushId != null) {
                isDesiredPush = Integer.parseInt(pushId) == getDesiredPushId();
            }
            Log.d(TAG, "pushId = " + pushId);

            if (sendDate != null && deliveryDate != null && serverTitle != null && isDesiredPush) {
                if (serverTitle.equals(getCurrentCredentials().getTitle())) {
                    processingMessage(serverTitle, sendDate, deliveryDate);
                }
            }
        }

    };

    private void processingMessage(String serverTitle,String sendDate, String deliveryDate) {
        long timeFromMessage = Long.parseLong(sendDate);
        long currentTimeMillis = Long.parseLong(deliveryDate);
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
        long travelingTime = currentTimeMillis - timeFromMessage;
        String travelingDateText = String.valueOf(currentTimeMillis - timeFromMessage);
        Log.d(TAG, "see report: \n"
                + "\ndateSend = " + dateSendText
                + "\n" + "currentDateText = " + currentDateText
                + "\n" + "timeout = " + (currentTimeMillis - timeFromMessage) + " ms");

        setColorStatusOval(Consts.STATUS_COLOR_SUCCESS);
        setDeliveryTime(currentDateText);
        addSuccessPushToReport();


        saveTestResult("Timeout = " + travelingDateText + " ms");

        stopCheckTimer();

        if (!StringUtils.isEmpty(serverTitle) && !StringUtils.isEmpty(deliveryDate)) {
            sendResultToServer(serverTitle, deliveryDate, travelingTime);
        }

        startCheckServerByIndex(i);
    }

    private void sendResultToServer(String serverTitle, String deliveryDate, long travelingTime) {
        if (deliveryDate == null){
            deliveryDate = String.valueOf(System.currentTimeMillis());
        }

        QuerySendReport querySendReport = new QuerySendReport(
                serverTitle,
                deliveryDate,
                travelingTime);
        querySendReport.performAsyncWithCallback(new QBEntityCallbackImpl<Void>() {
                                                     @Override
                                                     public void onSuccess(Void result, Bundle params) {
                                                         Log.d(TAG, "send report result - onSuccess(Object result, Bundle params) " + result.toString());
                                                     }

                                                     @Override
                                                     public void onSuccess() {
                                                         Log.d(TAG, "send report result - onSuccess() ");
//                                                         saveTestResult("The result has been successfully sent to the server");
                                                     }

                                                     @Override
                                                     public void onError(List errors) {
                                                         Log.d(TAG, "send report result - onError(List errors) " + errors.toString());
//                                                         saveTestResult("Error sending data to the server" + errors.toString());
                                                     }
                                                 }

        );
    }


    public void stopCheckerClick(View view) {
        checkerPB.setVisibility(View.GONE);
        stopCheckTimer();
        checkServerTaskHandler = null;
    }



    private void saveTestResult(String errorMessage){
        if (errorMessage.equals("")){
            errorMessage = "unknown error";
        }
        long currentDateLong = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String currentDate = sdf.format(currentDateLong);

        String serverTitle = getCurrentCredentials().getTitle();

        ResultTests resultTests = new ResultTests(serverTitle + " " + currentDate, errorMessage);

        if (resultsMap == null) {
            resultsMap = new HashMap<>();
        }

        ArrayList<ResultTests> errorItem = resultsMap.get(serverTitle);

        if (errorItem == null){
            resultsMap.put(serverTitle, new ArrayList<ResultTests>());
            errorItem = resultsMap.get(serverTitle);
        }

        errorItem.add(0, resultTests);
    }

    private View getViewByServerTitle(String serverTitle){

        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setColorStatusOval(int backgroundResource){
        int index = i - 1;
        reportAdapter.getItem(index).setColorStatusOval(backgroundResource);
        View view = serversListView.getChildAt(index);
        if (/*needUpdateUi(index) && */view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                view.findViewById(R.id.statusOvalTV).setBackgroundResource(backgroundResource);
            }
        }
    }

    private void setDeliveryTime(String time){
        int index = i - 1;
        reportAdapter.getItem(index).setDeliveryDateLastPush(time);
        View view = serversListView.getChildAt(index);
        if (/*needUpdateUi(index) && */view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                TextView textView = (TextView) view.findViewById(R.id.deliveryTimeTV);
                textView.setText(time);
            }
        }
    }

    private void addSendedPushToReport (){
        int index = i - 1;
        reportAdapter.getItem(index).setSendedPushes(reportAdapter.getItem(index).getSendedPushes() + 1);
        int newCount = reportAdapter.getItem(index).getSendedPushes();
        View view = serversListView.getChildAt(index);
        if (/*needUpdateUi(index) &&*/ view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                TextView textView = (TextView) view.findViewById(R.id.sendResultTV);
                textView.setText(reportAdapter.getItem(index).getSuccessPushes() + "/" + newCount);
            }
        }
    }

    private void addSuccessPushToReport (){
        int index = i - 1;
        reportAdapter.getItem(index).setSuccessPushes(reportAdapter.getItem(index).getSuccessPushes() + 1);
        int newCount = reportAdapter.getItem(index).getSuccessPushes();
        View view = serversListView.getChildAt(index);
        if (/*needUpdateUi(index) && */view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                TextView textView = (TextView) view.findViewById(R.id.sendResultTV);
                textView.setText(newCount + "/" + reportAdapter.getItem(index).getSendedPushes());
            }
        }
    }

    private boolean needUpdateUi(int index){
        return reportAdapter.getItem(index).getServerTitle().equals(getCurrentCredentials().getTitle());
    }

    private void initCheckServerTask() {
        checkServerTaskHandler = new Handler(Looper.myLooper());
        checkServerTask = new Runnable() {
            @Override
            public void run() {
                setColorStatusOval(Consts.STATUS_COLOR_FAIL);
                saveTestResult("Push timeout");
                sendResultToServer(getCurrentCredentials().getTitle(), null, -1);
                startCheckServerByIndex(i);
            }
        };
    }

    private void startCheckTimer() {
        Log.d(TAG, "");
        if (checkServerTaskHandler != null) {
            checkServerTaskHandler.postAtTime(checkServerTask, SystemClock.uptimeMillis() + Consts.PUSH_TIMEOUT);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopCheckTimer() {
        Log.d(TAG, "");
        if (checkServerTaskHandler != null) {
            checkServerTaskHandler.removeCallbacks(checkServerTask);
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPushReceiver);

        super.onDestroy();
    }

}
