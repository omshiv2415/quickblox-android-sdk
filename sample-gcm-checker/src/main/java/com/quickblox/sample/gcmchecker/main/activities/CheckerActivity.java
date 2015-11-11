package com.quickblox.sample.gcmchecker.main.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.auth.QBAuth;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringUtils;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.sample.gcmchecker.QuerySendReport;
import com.quickblox.sample.gcmchecker.R;
import com.quickblox.sample.gcmchecker.main.Consts;
import com.quickblox.sample.gcmchecker.main.ReportAdapter;
import com.quickblox.sample.gcmchecker.main.helper.CredentialsDBManager;
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
import java.util.Random;

/**
 * Created by tereha on 15.09.15.
 */
public class CheckerActivity extends AppCompatActivity {
    private String TAG = CheckerActivity.class.getSimpleName();
    private Button startCheckerBtn;
    private ListView serversListView;
    private ReportAdapter reportAdapter;
    private ProgressBar checkerPB;
    private GoogleCloudMessaging googleCloudMessaging;
    private String regId;
    private Credentials currentCredentials;
    public static HashMap<String, ArrayList<ResultTests>> resultsMap;
    private ArrayList<Report> reportList;
    private int desiredPushId;
    private ArrayList<Credentials> credentialsList;
    private Handler checkServerTimerTaskHandler;
    private Runnable checkServerTimerTask;
    private int indexCurrentServer = -1;
    private String deviceId;
    private CheckServerTask checkServerTask;
    private String currentTestStep;
    public IOException exception;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_notification_checker_layout);
        initActionBar();

        credentialsList = CredentialsDBManager.getAllCredetntials(this);

        prepareData();
        initUI();

        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));

        if (checkServerTimerTaskHandler == null){
            initCheckServerTask();
        }
        startCheckServer();
    }

    private void prepareData(){
//        credentialsList = CredentialsDBManager.getAllCredetntials(this);
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
//        startCheckerBtn = (Button) findViewById(R.id.startCheckerBtn);
//        checkerPB = (ProgressBar) findViewById(R.id.startCheckerPB);
//        checkerPB.setVisibility(View.INVISIBLE);
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
        if (checkServerTimerTaskHandler == null){
            initCheckServerTask();
        }
        startCheckServer();
    }

    private void startCheckServer() {

        if (indexCurrentServer == credentialsList.size() - 1) {
            indexCurrentServer = 0;
        } else {
            indexCurrentServer++;
        }

//        if (checkerPB.getVisibility()!= View.VISIBLE){
//            checkerPB.setVisibility(View.VISIBLE);
//        }

//        startCheckerBtn.setClickable(false);
//        startCheckerBtn.setEnabled(false);
        setColorCurrentItem(Consts.TEST_ITEM_BACKGROUND_COLOR);
        setDesiredPushId(0);
        Credentials credentials = credentialsList.get(indexCurrentServer);
        setCurrentCredentials(credentials);

        startCheckTimer();
        startServerTest();
    }

    private void startServerTest() {
        initApp();
        startTestTask();
    }

    private void startTestTask() {
        if (checkServerTask != null){
            checkServerTask.cancel(true);
            checkServerTask = null;
        }
        checkServerTask = new CheckServerTask();
        checkServerTask.execute();
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
            Log.d(TAG, "desiredPushID = " + getDesiredPushId());

            if (sendDate != null && deliveryDate != null && serverTitle != null && isDesiredPush) {
                setDesiredPushId(0);
                if (serverTitle.equals(getCurrentCredentials().getTitle())) {
                    stopCheckTimer();
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

        saveTestResult("Delivery time = " + travelingDateText + " ms");

        if (!StringUtils.isEmpty(serverTitle) && !StringUtils.isEmpty(deliveryDate)) {
            sendResultToServer(serverTitle, travelingTime, null);
        }

        setColorCurrentItem(Consts.NORMAL_ITEM_BACKGROUND_COLOR);
        startCheckServer();
    }

    private void sendResultToServer(String serverTitle, long pushTime, IOException exception) {
        QuerySendReport querySendReport;

        if (exception != null){
            querySendReport = new QuerySendReport(serverTitle, exception);
        } else {
            querySendReport = new QuerySendReport(serverTitle, pushTime);
        }

        querySendReport.performAsyncWithCallback(new QBEntityCallbackImpl<Void>());
    }

    private void saveTestResult(String errorMessage){
        if (errorMessage.equals("")){
            errorMessage = "Unknown error";
        }
        long currentDateLong = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String currentDate = sdf.format(currentDateLong);

        String serverTitle = getCurrentCredentials().getTitle();

        ResultTests resultTests = new ResultTests(currentDate, errorMessage);

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

    private void setColorCurrentItem(int backgroundColor){
        int index = indexCurrentServer;

        if (backgroundColor==Consts.TEST_ITEM_BACKGROUND_COLOR) {
            reportAdapter.getItem(index).setIsCurrentItem(true);
        } else {
            reportAdapter.getItem(index).setIsCurrentItem(false);
        }

        View view = serversListView.getChildAt(index);
        if (view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                view.setBackgroundResource(backgroundColor);
            }
        }
        reportAdapter.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setColorStatusOval(int backgroundResource){
        int index = indexCurrentServer;
        reportAdapter.getItem(index).setColorStatusOval(backgroundResource);
        View view = serversListView.getChildAt(index);
        if (view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                view.findViewById(R.id.statusOvalTV).setBackgroundResource(backgroundResource);
            }
        }
        reportAdapter.notifyDataSetChanged();
    }

    private void setDeliveryTime(String time){
        int index = indexCurrentServer;
        reportAdapter.getItem(index).setDeliveryDateLastPush(time);
        View view = serversListView.getChildAt(index);
        if (view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                TextView textView = (TextView) view.findViewById(R.id.deliveryTimeTV);
                textView.setText(time);
            }
        }
        reportAdapter.notifyDataSetChanged();
    }

    synchronized private void addSendedPushToReport (){
        int index = indexCurrentServer;
        reportAdapter.getItem(index).setSendedPushes(reportAdapter.getItem(index).getSendedPushes() + 1);
        int newCount = reportAdapter.getItem(index).getSendedPushes();
        View view = serversListView.getChildAt(index);
        if (view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                TextView textView = (TextView) view.findViewById(R.id.countSendedPushes);
                textView.setText(String.valueOf(newCount));
            }
        }
        reportAdapter.notifyDataSetChanged();
    }

    synchronized private void addSuccessPushToReport (){
        int index = indexCurrentServer;
        reportAdapter.getItem(index).setSuccessPushes(reportAdapter.getItem(index).getSuccessPushes() + 1);
        int newCount = reportAdapter.getItem(index).getSuccessPushes();
        View view = serversListView.getChildAt(index);
        if (view != null) {
            ReportAdapter.ViewHolder viewHolder = (ReportAdapter.ViewHolder) view.getTag();
            if (viewHolder.getViewTag() == index) {
                TextView textView = (TextView) view.findViewById(R.id.countSuccessPushes);
                textView.setText(String.valueOf(newCount));
            }
        }
        reportAdapter.notifyDataSetChanged();
    }

    private void initCheckServerTask() {
        checkServerTimerTaskHandler = new Handler(getMainLooper());
        checkServerTimerTask = new Runnable() {
            @Override
            public void run() {
                setDesiredPushId(0);
                setColorStatusOval(Consts.STATUS_COLOR_FAIL);
                if (currentTestStep.equals(Consts.STEP_PUSH_SENDED)){
                    saveTestResult("Timeout");
                } else {
                    saveTestResult("Timeout " + currentTestStep);
                }
                sendResultToServer(getCurrentCredentials().getTitle(), -1, null);
                setColorCurrentItem(Consts.NORMAL_ITEM_BACKGROUND_COLOR);
                startCheckServer();
            }
        };
    }

    private void startCheckTimer() {
        if (checkServerTimerTaskHandler != null) {
            checkServerTimerTaskHandler.postAtTime(checkServerTimerTask, SystemClock.uptimeMillis() + Consts.PUSH_TIMEOUT);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopCheckTimer() {
        Log.d(TAG, "stopCheckTimer()");
        if (checkServerTimerTaskHandler != null) {
            checkServerTimerTaskHandler.removeCallbacks(checkServerTimerTask);
        }
    }

    private void stopTestTask() {
        Log.d(TAG, "stopTestTask()");
        if (checkServerTask != null) {
            checkServerTask.cancel(true);
            checkServerTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPushReceiver);
        super.onDestroy();
    }

    private void initGCMInstances() throws IOException {
        if (googleCloudMessaging == null) {
            googleCloudMessaging = GoogleCloudMessaging.getInstance(CheckerActivity.this);
        }

        if (regId == null) {
            regId = googleCloudMessaging.register(Consts.PROJECT_NUMBER);
        }

        if (deviceId == null) {
            final TelephonyManager mTelephony = (TelephonyManager) getSystemService(
                    Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId(); //*** use for mobiles
            } else {
                deviceId = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID); //*** use for tablets
            }
        }
    }

    private class CheckServerTask extends AsyncTask <Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                initGCMInstances();

                if (!isCancelled()) {
                    currentTestStep = Consts.STEP_CREATE_SESSION;
                    Log.d(TAG, currentTestStep);
                    QBAuth.createSession(new QBUser(getCurrentCredentials().getUserLogin(),
                            getCurrentCredentials().getUserPass()));
                }

                if (!isCancelled()) {
                    currentTestStep = Consts.STEP_SUBSCRIBE_TO_PUSH;
                    Log.d(TAG, currentTestStep);
                    QBMessages.subscribeToPushNotificationsTask(regId, deviceId, QBEnvironment.DEVELOPMENT);
                }

                if (!isCancelled()) {
                    currentTestStep = Consts.STEP_CREATE_EVENT;
                    Log.d(TAG, currentTestStep);
                    QBMessages.createEvent(createPushNotificationEvent());
                }

                if (!isCancelled()) {
                    currentTestStep = Consts.STEP_PUSH_SENDED;
                    Log.d(TAG, currentTestStep);
                    publishProgress(Consts.TASK_SUCCESS_ACTION);
                }
            } catch (QBResponseException e) {
                if (!isCancelled()) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                    saveTestResult(e.getMessage());
                    exception = e;
                    publishProgress(Consts.TASK_FAIL_ACTION);
                }
            } catch (IOException e){
                if (!isCancelled()) {
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                    saveTestResult(e.getMessage());
                    exception = e;
                    publishProgress(Consts.TASK_FAIL_ACTION);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            if (params[0].equals(Consts.TASK_SUCCESS_ACTION)) {
                addSendedPushToReport();
            } else {
                setDesiredPushId(0);
                setColorStatusOval(Consts.STATUS_COLOR_FAIL);
                sendResultToServer(getCurrentCredentials().getTitle(), -1, exception);
                exception = null;
                setColorCurrentItem(Consts.NORMAL_ITEM_BACKGROUND_COLOR);
                startCheckServer();
            }
            super.onProgressUpdate();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog(){
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle(R.string.log_out_dialog_title);
        quitDialog.setMessage(R.string.log_out_dialog_message);
        quitDialog.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopCheckTimer();
                stopTestTask();
                finish();
            }
        });
        quitDialog.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        quitDialog.show();
    }

    protected Dialog createDialog() {
        ArrayList<Credentials> allCredentials = CredentialsDBManager.getAllCredetntials(this);
        String [] dataForDialog = new String[allCredentials.size()];

        for (int i = 0; i<=allCredentials.size()-1; i++){
            dataForDialog[i] = allCredentials.get(i).getTitle();
        }
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.title_dialog_select_servers);
        adb.setMultiChoiceItems(dataForDialog, null, null);
        adb.setPositiveButton(R.string.run_test, myBtnClickListener);
        adb.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return adb.create();
    }

    DialogInterface.OnClickListener myBtnClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            SparseBooleanArray sbArray = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
            ArrayList<Credentials> allCredentials = CredentialsDBManager.getAllCredetntials(CheckerActivity.this);
            ArrayList<Credentials> newCredentialList = new ArrayList<>();
            for (int i = 0; i < sbArray.size(); i++) {
                int key = sbArray.keyAt(i);
                if (sbArray.get(key)){
                   newCredentialList.add(allCredentials.get(key));
                }
            }

            if (newCredentialList.size()>0) {
                stopCheckTimer();
                stopTestTask();
                credentialsList = newCredentialList;
                indexCurrentServer = -1;
                prepareData();
                initUI();
                if (checkServerTimerTaskHandler == null) {
                    initCheckServerTask();
                }
                startCheckServer();
            } else {
                Toast.makeText(CheckerActivity.this, getString(R.string.not_selected_server), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.select_servers:
                createDialog().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initActionBar() {
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_view, null);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
    }
}
