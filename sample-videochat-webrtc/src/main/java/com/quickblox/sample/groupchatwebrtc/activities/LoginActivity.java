package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.adapters.UsersAdapter;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.sample.groupchatwebrtc.managers.UserCredentialsStorageManager;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;
import com.quickblox.sample.groupchatwebrtc.pushnotifications.CheckPlayService;
import com.quickblox.sample.groupchatwebrtc.pushnotifications.RegistrationIntentService;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

/**
 * QuickBlox team
 */
public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);

    private UsersAdapter usersListAdapter;
    private ListView usersList;
    private ProgressBar progressBar;
    private static QBChatService chatService;
    private static ArrayList<QBUser> users = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Fabric
        Fabric.with(this, new Crashlytics());

        initUI();

        // Initialise Chat
        //
        QBChatService.setDebugEnabled(true);
        chatService = QBChatService.getInstance();

        // check if a user already logged in
        //
        String []userCredentials = UserCredentialsStorageManager.getCredentials(LoginActivity.this);
        if(userCredentials != null){
            String login = userCredentials[0];
            String password = userCredentials[1];

            QBUser loggedInUser = new QBUser(login, password);
            createUserSession(loggedInUser, true);
        }else{
            createAppSession();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onAttachedToWindow() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void createAppSession() {
        showProgress(true);

        QBAuth.createSession(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                showProgress(false);

                Log.d(TAG, "loading users");
                loadUsers(null);
            }

            @Override
            public void onError(QBResponseException list) {
                Toast.makeText(LoginActivity.this, "Error while loading users", Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });
    }

    private void initUI() {
        setContentView(R.layout.activity_login);

        usersList = (ListView) findViewById(R.id.usersListView);
        progressBar = (ProgressBar) findViewById(R.id.loginPB);
        progressBar.setVisibility(View.INVISIBLE);

        if (getActionBar() != null) {
            getActionBar().setTitle(getResources().getString(R.string.opponentsListActionBarTitle));
        }
    }

    private void initUsersList() {
        usersListAdapter = new UsersAdapter(this, users);
        usersList.setAdapter(usersListAdapter);
        usersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        usersList.setOnItemClickListener(clicklistener);
    }

    private void loadUsers(QBEntityCallback callback){
        loadUsers(getString(R.string.users_tag), callback);
    }

    private void loadUsers(String tag, final QBEntityCallback callback){
        showProgress(true);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPerPage(getResources().getInteger(R.integer.users_count));
        List<String> tags = new LinkedList<>();
        tags.add(tag);
        //
        QBUsers.getUsersByTags(tags, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                showProgress(false);

                Log.d(TAG, "load users done");

                users.clear();
                users.addAll(DataHolder.createUsersList(qbUsers));
                initUsersList();

                if(callback != null) {
                    callback.onSuccess(qbUsers, bundle);
                }
            }

            @Override
            public void onError(QBResponseException strings) {
                showProgress(false);

                Toast.makeText(LoginActivity.this, "Error while loading users", Toast.LENGTH_SHORT).show();

                if(callback != null) {
                    callback.onError(strings);
                }
            }
        });
    }

    private void showProgress(boolean show){
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private long upTime = 0l;

    AdapterView.OnItemClickListener clicklistener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if ((SystemClock.uptimeMillis() - upTime) < ON_ITEM_CLICK_DELAY){
                return;
            }
            upTime = SystemClock.uptimeMillis();

            QBUser selectedUser = usersListAdapter.getItem(position);
            createUserSession(selectedUser, false);
        }
    };


    private void createUserSession(final QBUser user, final boolean restoreSession) {

        Log.d(TAG, "creating a session");

        showProgress(true);

        final String login = user.getLogin();
        final String password = user.getPassword();

        Log.d(TAG, "creating a session2");

        QBAuth.createSession(login, password, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle bundle) {
                Log.d(TAG, "onSuccess create session with params");
                user.setId(session.getUserId());

                // save current logged in user
                DataHolder.setLoggedUser(user);

                subscribeToPushNotifications();

                chatService.login(user, new QBEntityCallback<Void>() {

                    @Override
                    public void onSuccess(Void result, Bundle bundle) {
                        Log.d(TAG, "onSuccess login to chat");

                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);

                                // save current user to preferences
                                UserCredentialsStorageManager.saveUserCredentials(LoginActivity.this, login, password);

                                if(restoreSession){
                                    loadUsers(new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                                        @Override
                                        public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                                            // show Call activity
                                            startCallActivity(login);
                                        }

                                        @Override
                                        public void onError(QBResponseException list) {

                                        }
                                    });
                                }else{
                                    // show Call activity
                                    startCallActivity(login);
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        showProgress(false);
                        Toast.makeText(LoginActivity.this, "Error when login", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(QBResponseException errors) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, "Error when login, check test users login and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subscribeToPushNotifications(){
        boolean gcmSupported = CheckPlayService.checkPlayServices(this);

        Log.d(TAG, "gcmSupported: " + gcmSupported);

        if(!gcmSupported){
            // do something
        }else{
            // Start IntentService to register this application with GCM.
            Log.d(TAG, "Start IntentService to register this application with GCM");
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void startCallActivity(String login) {
        Log.d(TAG, "startCallActivity, login: " + login);

        Intent intent = new Intent(LoginActivity.this, CallActivity.class);
        intent.putExtra("login", login);
        startActivityForResult(intent, Consts.CALL_ACTIVITY_CLOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Consts.CALL_ACTIVITY_CLOSE){
            if (resultCode == Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED) {
                Toast.makeText(this, getString(R.string.WIFI_DISABLED),Toast.LENGTH_LONG).show();
            }
        }
    }
}
