package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.main.Consts;
import com.quickblox.simplesample.messages.main.ReportAdapter;
import com.quickblox.simplesample.messages.main.helper.PlayServicesHelper;
import com.quickblox.simplesample.messages.main.models.Report;
import com.quickblox.simplesample.messages.main.utils.DialogUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_notification_checker_layout);
        initUI();

        playServicesHelper = new PlayServicesHelper(this);

        initUI();
        // Register to receive push notifications events
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(mPushReceiver,
                new IntentFilter(Consts.NEW_PUSH_EVENT));

    }

    private void initUI() {
        startCheckerBtn = (Button) findViewById(R.id.startCheckerBtn);
        messagesList = (ListView) findViewById(R.id.messagesList);
        checkerPB = (ProgressBar) findViewById(R.id.startCheckerPB);
        reportAdapter = new ReportAdapter(this, listReports);
        messagesList.setAdapter(reportAdapter);
    }


    public void startChecker(View view) {
        sendPushNotification();

    }

    private void sendPushNotification(){
        checkerPB.setVisibility(View.VISIBLE);

        // Send Push: create QuickBlox Push Notification Event
        QBEvent qbEvent = new QBEvent();
        qbEvent.setNotificationType(QBNotificationType.PUSH);
        qbEvent.setEnvironment(QBEnvironment.DEVELOPMENT);

        // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
        long currentTimeMillis = System.currentTimeMillis();
        qbEvent.setMessage(String.valueOf(currentTimeMillis));

        StringifyArrayList<Integer> userIds = new StringifyArrayList<>();
        userIds.add(2224038);
        qbEvent.setUserIds(userIds);


        QBMessages.createEvent(qbEvent, new QBEntityCallbackImpl<QBEvent>() {
            @Override
            public void onSuccess(QBEvent qbEvent, Bundle bundle) {

            }

            @Override
            public void onError(List<String> strings) {
                // errors
                DialogUtils.showLong(CheckerActivity.this, strings.toString());

            }
        });
    }


    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get extra data included in the Intent
            String message = intent.getStringExtra(Consts.EXTRA_MESSAGE);

            if (message != null) {
                processingMessage(message);
            }
        }
    };

    private void processingMessage(String message){
        long timeFromMessage = Long.parseLong(message);
        long currentTimeMillis = System.currentTimeMillis();
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
        Log.d(TAG, "\n dateSend = " + dateSendText
                + "\n" + "currentDateText = " + currentDateText
                + "\n" + "timeout = " + (currentTimeMillis - timeFromMessage) / 1000 + " sec");

        Report report = new Report (dateSendText, currentDateText, travelingDateText);
        listReports.add(report);
        reportAdapter.notifyDataSetChanged();
        checkerPB.setVisibility(View.GONE);
    }


    public void stopChecker(View view) {
        checkerPB.setVisibility(View.GONE);
    }
}
