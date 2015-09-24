package com.quickblox.sample.gcmchecker.main.activities;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
    private Thread t;
    private GoogleCloudMessaging googleCloudMessaging;
    private String regId;
    private Credentials currentCredentials;
    private ArrayList<Credentials> testServersList;
    public static HashMap<String, ArrayList<ResultTests>> resultsMap;
    private ArrayList<Report> reportList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_notification_checker_layout);
        prepareData();
        initUI();

        createTestServersList();
        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));

        Log.d(TAG, "Downloaded information about " + String.valueOf(SplashActivity.credentialsList.size()) + " servers");

    }

    private void createTestServersList() {
        testServersList = new ArrayList<>();

        Credentials credentials_1 = new Credentials();
        credentials_1.setAppId(Consts.APP_ID);
        credentials_1.setAuthKey(Consts.AUTH_KEY);
        credentials_1.setAuthSecret(Consts.AUTH_SECRET);
        credentials_1.setUserID(String.valueOf(2224038));
        credentials_1.setUserLogin(Consts.USER_LOGIN);
        credentials_1.setUserPass(Consts.USER_PASSWORD);
        credentials_1.setTitle("starter");

        Credentials credentials_2 = SplashActivity.credentialsList.get(SplashActivity.credentialsList.size() - 1);
        Credentials credentials_3 = SplashActivity.credentialsList.get(7);

//        testServersList.add(credentials_1);
        testServersList.add(credentials_2);
        testServersList.add(credentials_3);

        reportAdapter = new ReportAdapter(this, reportList);
        serversListView.setAdapter(reportAdapter);
    }

    private void prepareData(){
        reportList = new ArrayList<>();
        for (Credentials credentials : SplashActivity.credentialsList){
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

//        reportAdapter = new ReportAdapter(this, testServersList);
//        serversListView.setAdapter(reportAdapter);
        serversListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedServerTitle = SplashActivity.credentialsList.get(position).getTitle();
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

    public void startCheckerClick(View view) {
        checkerPB.setVisibility(View.VISIBLE);

//        Credentials credentials = testServersList.get(1);
        Credentials credentials = SplashActivity.credentialsList.get(10);
        setCurrentCredentials(credentials);
//        Report report1 = new Report (null, null, null);
//        report1.setServerTitle(getCurrentCredentials().getTitle());
//        listReports.add(report1);
//        reportAdapter.notifyDataSetChanged();
        initApp();
        createSession();
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
            saveTestResult(e.getMessage());
        }

        qbEvent.setMessage(json.toString());

        StringifyArrayList<Integer> userIds = new StringifyArrayList<>();
        userIds.add(Integer.parseInt(getCurrentCredentials().getUserID()));
        qbEvent.setUserIds(userIds);

        return qbEvent;
    }

    public void sendPushNotification(QBEvent qbEvent){
        addSendedPushToReport(10);
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
                setColorStatusOval(10, Consts.STATUS_COLOR_FAIL);
                for (String s : strings) {
                    saveTestResult(s);
                }
            }
        });
    }



    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "new broadcast message ");
            String sendDate = intent.getExtras().getString(Consts.EXTRA_SEND_DATE);
            String deliveryDate = intent.getExtras().getString(Consts.EXTRA_DELIVERY_DATE);
            String serverTitle = intent.getExtras().getString(Consts.EXTRA_SERVER_TITLE);

            if (sendDate != null && deliveryDate != null && serverTitle != null) {
                if (serverTitle.equals(getCurrentCredentials().getTitle())) {
//                    Report report = new Report();
//                    report.setSendDate(sendDate);
//                    report.setDeliveryDate(deliveryDate);
//                    report.setServerTitle(serverTitle);

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
        String travelingDateText = String.valueOf(currentTimeMillis - timeFromMessage);
        Log.d(TAG, "see report: \n"
                + "\ndateSend = " + dateSendText
                + "\n" + "currentDateText = " + currentDateText
                + "\n" + "timeout = " + (currentTimeMillis - timeFromMessage) + " miliSec");

//        Report report1 = new Report (dateSendText, currentDateText, travelingDateText);
//        report1.setServerTitle(report.getServerTitle());
//        listReports.add(report1);
//        reportAdapter.notifyDataSetChanged();

        setColorStatusOval(10, Consts.STATUS_COLOR_SUCCESS);
        setDeliveryTime(10, currentDateText);
        addSuccessPushToReport(10);


        saveTestResult("Timeout = " + travelingDateText + " ms");

        if (!StringUtils.isEmpty(serverTitle) && !StringUtils.isEmpty(deliveryDate)) {

            QuerySendReport querySendReport = new QuerySendReport(
                    serverTitle,
                    deliveryDate,
                    travelingDateText);
            querySendReport.performAsyncWithCallback(new QBEntityCallbackImpl<Void>() {
                                                         @Override
                                                         public void onSuccess(Void result, Bundle params) {
                                                             Log.d(TAG, "send report result - onSuccess(Object result, Bundle params) " + result.toString());
                                                         }

                                                         @Override
                                                         public void onSuccess() {
                                                             Log.d(TAG, "send report result - onSuccess() ");
                                                             saveTestResult("The result has been successfully sent to the server");
                                                         }

                                                         @Override
                                                         public void onError(List errors) {
                                                             Log.d(TAG, "send report result - onError(List errors) " + errors.toString());
                                                            saveTestResult(errors.toString());
                                                         }
                                                     }

            );
        }
    }


    public void stopCheckerClick(View view) {
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
                Log.d(TAG, "URL host =  " + serverApiDomain);
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
                for (String s : strings) {
                    Log.d(TAG, "Error subscribing " + s);
                    saveTestResult(s);
                }
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
                                    for (String s : errors){
                                        Log.d(TAG, "Error subscribing " + s);
                                        saveTestResult(s);
                                    }
                                }
                            });
                        }
                    });
                } catch (IOException ex) {
                    saveTestResult(ex.getMessage());
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private void saveTestResult(String errorMessage){
        long currentDateLong = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String currentDate = sdf.format(currentDateLong);

        ResultTests resultTests = new ResultTests(currentDate, errorMessage);

        String serverTitle = getCurrentCredentials().getTitle();

        if (resultsMap == null) {
            resultsMap = new HashMap<>();
        }

        ArrayList<ResultTests> errorItem = resultsMap.get(serverTitle);

        if (errorItem == null){
            resultsMap.put(serverTitle, new ArrayList<ResultTests>());
            errorItem = resultsMap.get(serverTitle);
        }

        errorItem.add(resultTests);
    }

    private void updateReportUI(){

    }

    private View getViewByServerTitle(String serverTitle){


        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setColorStatusOval(int index, int backgroundResource){
        View view = serversListView.getChildAt(index);
        view.findViewById(R.id.statusOvalTV).setBackgroundResource(backgroundResource);
        reportAdapter.getItem(index).setColorStatusOval(backgroundResource);
    }

    private void setDeliveryTime(int index, String time){
        View view = serversListView.getChildAt(index);
        TextView textView = (TextView) view.findViewById(R.id.deliveryTimeTV);
        textView.setText(time);
        reportAdapter.getItem(index).setDeliveryDateLastPush(time);

    }

    private void addSendedPushToReport (int index){
        reportAdapter.getItem(index).setSendedPushes(reportAdapter.getItem(index).getSendedPushes() + 1);
        int newCount = reportAdapter.getItem(index).getSendedPushes();
        View view = serversListView.getChildAt(index);
        TextView textView = (TextView) view.findViewById(R.id.sendResultTV);
        textView.setText(reportAdapter.getItem(index).getSuccessPushes() + "/" + newCount);

    }

    private void addSuccessPushToReport (int index){
        reportAdapter.getItem(index).setSuccessPushes(reportAdapter.getItem(index).getSuccessPushes() + 1);
        int newCount = reportAdapter.getItem(index).getSuccessPushes();
        View view = serversListView.getChildAt(index);
        TextView textView = (TextView) view.findViewById(R.id.sendResultTV);
        textView.setText(newCount + "/" + reportAdapter.getItem(index).getSendedPushes());
    }

}
