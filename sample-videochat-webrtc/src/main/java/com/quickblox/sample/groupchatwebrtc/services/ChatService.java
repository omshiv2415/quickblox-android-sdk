package com.quickblox.sample.groupchatwebrtc.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.SessionManager;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.activities.ListUsersActivity;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.sample.groupchatwebrtc.util.OpponentsManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.webrtc.VideoCapturerAndroid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tereha on 25.04.16.
 */
public class ChatService extends Service implements QBRTCClientSessionCallbacks {

    private static final String TAG = ChatService.class.getSimpleName();
    private QBChatService chatService;
    private String login;
//    private String password;
    private PendingIntent pendingIntent;
    private int startServiceVariant;
    private BroadcastReceiver wifiStateReceiver;
    private boolean needMaintainConnectivity;
    private QBRTCClient qbRTCClient;

    @Override
    public void onCreate() {
        super.onCreate();

        initWiFiManagerListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        initQBChatService();
        parseIntentExtras(intent);

        if (TextUtils.isEmpty(login)){
            getUserDataFromPreferences();
        }

        loginToChatIfNeed();

        return super.onStartCommand(intent, flags, startId);
    }

    private void initQBChatService(){
        if (!QBChatService.isInitialized()) {
            QBChatService.init(getApplicationContext());
        }

        chatService = QBChatService.getInstance();

        try {
            chatService.startAutoSendPresence(Consts.AUTO_SEND_PRESENCE_INTERVAL);
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        }
    }

    private void parseIntentExtras(Intent intent) {
        if (intent != null && intent.getExtras()!= null) {
            pendingIntent = intent.getParcelableExtra(Consts.PARAM_PINTENT);
            login = intent.getStringExtra(Consts.EXTRA_USER_LOGIN);
            startServiceVariant = intent.getIntExtra(Consts.START_SERVICE_VARIANT, Consts.AUTOSTART);
        }
    }

    private void loginToChatIfNeed() {

        if(!chatService.isLoggedIn()){
            loginToChat(new QBUser(login, Consts.DEFAULT_USER_PASSWORD));
        } else {
            startActionsOnSuccessLogin();
        }
    }

    private void loginToChat (QBUser qbUser){
        Exception exception = null;

        try {
            chatService.login(qbUser);
        } catch (XMPPException e) {
            exception = e;
        } catch (IOException e) {
            exception = e;
        } catch (SmackException e) {
            exception = e;
        }

        if (exception == null){
            startActionsOnSuccessLogin();
        } else {
            // Failed login to chat
            exception.printStackTrace();
            startActionsOnFailLogin();
        }
    }

    private void startActionsOnSuccessLogin() {
        initQBRTCClient();
        sendResultToActivity(true);
//        startOpponentsActivity();
        startForeground(Consts.NOTIFICATION_FORAGROUND, createNotification());
        saveUserDataToPreferences(login);
        needMaintainConnectivity = true;
    }

    private void initQBRTCClient() {
        Log.d(TAG, "initQBRTCClient()");

        qbRTCClient = QBRTCClient.getInstance(getApplicationContext());

        // Add signalling manager
        chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    qbRTCClient.addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });

        qbRTCClient.setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(final String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }
        });

        // Configure RTCClient
        //
        QBRTCConfig.setMaxOpponentsCount(Consts.MAX_OPPONENTS_COUNT);
        QBRTCConfig.setDisconnectTime(Consts.DISCONNECT_TIME);
        QBRTCConfig.setAnswerTimeInterval(Consts.ANSWER_TIME_INTERVAL);
        QBRTCConfig.setDebugEnabled(Consts.DEBUG_ENABLED);

        // Add service as callback to RTCClient
        qbRTCClient.addSessionCallbacksListener(this);
    }

    protected void getUserDataFromPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        login = sharedPreferences.getString(Consts.EXTRA_USER_LOGIN, null);
    }

    private void startActionsOnFailLogin(){
        sendResultToActivity(false);
        stopForeground(true);
        stopService(new Intent(getApplicationContext(), ChatService.class));
    }

    private void sendResultToActivity (boolean isSuccess){
        Log.d(TAG, "sendResultToActivity()");
        if (startServiceVariant == Consts.LOGIN) {
            try {
                Intent intent = new Intent().putExtra(Consts.LOGIN_RESULT, isSuccess);
                pendingIntent.send(ChatService.this, Consts.LOGIN_RESULT_CODE, intent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification createNotification() {
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, ListUsersActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setTicker(getResources().getString(R.string.service_launched))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.logged_in_as) + " " +
                        OpponentsManager.getUserNameByID(chatService.getUser().getId()));

        Notification notification = notificationBuilder.build();

        return notification;
    }

    private void saveUserDataToPreferences(String login){
        Log.d(TAG, "saveUserDataToPreferences()");
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(Consts.EXTRA_USER_LOGIN, login);
        ed.commit();
    }

    @Override
    public void onDestroy() {
        qbRTCClient.destroy();
        chatService.destroy();
        SessionManager.setCurrentSession(null);

        if (wifiStateReceiver != null){
            unregisterReceiver(wifiStateReceiver);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }


    private void initWiFiManagerListener() {
        wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "WIFI was changed");
                processCurrentWifiState(context);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    private void processCurrentWifiState(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            Log.d(TAG, "WIFI is turned off");
        } else {
            if (needMaintainConnectivity) {
                Log.d(TAG, "WIFI is turned on");
                reloginToChat();
            }
        }
    }

    private void reloginToChat() {
        initQBChatService();
        getUserDataFromPreferences();
        loginToChatIfNeed();
    }


    //========== Implement methods ==========//

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        if (SessionManager.getCurrentSession() == null){
            SessionManager.setCurrentSession(qbrtcSession);
            startCallActivity();
        } else if (SessionManager.getCurrentSession() != null && !qbrtcSession.equals(SessionManager.getCurrentSession())){
            qbrtcSession.rejectCall(new HashMap<String, String>());
        }

    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer userId) {

    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer userId, Map<String, String> userInfo) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer userId, Map<String, String> userInfo) {

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer userId) {

    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer userId) {

    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        if (qbrtcSession.equals(SessionManager.getCurrentSession())) {
            SessionManager.setCurrentSession(null);
        }
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {

    }

    private void startCallActivity(){
        CallActivity.start(getApplicationContext(), Consts.CALL_DIRECTION_TYPE.INCOMING);
    }
}