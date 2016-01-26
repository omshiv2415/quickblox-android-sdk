package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
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

    private static final String TAG = "ListUsersActivity";

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);

    private UsersAdapter usersListAdapter;
    private ListView usersList;
    private ProgressBar progressBar;
    private static QBChatService chatService;
    private static ArrayList<QBUser> users = new ArrayList<>();
    private volatile boolean resultReceived = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);

        initUI();

        QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);

        if (getActionBar() != null) {
            getActionBar().setTitle(getResources().getString(R.string.opponentsListActionBarTitle));
        }

        QBChatService.setDebugEnabled(true);
        if (!QBChatService.isInitialized()) {
            QBChatService.init(this);
            chatService = QBChatService.getInstance();
        }
        createAppSession();
    }

    private void createAppSession() {
        showProgress(true);
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                showProgress(false);

                loadUsers();
            }

            @Override
            public void onError(List<String> list) {
                Toast.makeText(LoginActivity.this, "Error while loading users", Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });
    }

    private void initUI() {
        usersList = (ListView) findViewById(R.id.usersListView);
        progressBar = (ProgressBar) findViewById(R.id.loginPB);
        progressBar.setVisibility(View.INVISIBLE);

    }

    private void initUsersList() {
        usersListAdapter = new UsersAdapter(this, users);
        usersList.setAdapter(usersListAdapter);
        usersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        usersList.setOnItemClickListener(clicklistener);
    }

    private void loadUsers(){
        loadUsers(getString(R.string.users_tag));
    }

    private void loadUsers(String tag){
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

                users.clear();
                users.addAll(DataHolder.createUsersList(qbUsers));
                initUsersList();

                // check if a user already logged in
                //
                String []userCredentials = UserCredentialsStorageManager.getCredentials(LoginActivity.this);
                if(userCredentials !=  null){
                    String login = userCredentials[0];
                    String password = userCredentials[1];

                    QBUser loggedInUser = new QBUser(login, password);
                    createUserSession(loggedInUser);
                }

            }

            @Override
            public void onError(List<String> strings) {
                showProgress(false);

                Toast.makeText(LoginActivity.this, "Error while loading users", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onError()");
            }
        });
    }

    private void showProgress(boolean show){
        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private long upTime = 0l;

    AdapterView.OnItemClickListener clicklistener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!resultReceived || (SystemClock.uptimeMillis() - upTime) < ON_ITEM_CLICK_DELAY){
                return;
            }
            resultReceived = false;
            upTime = SystemClock.uptimeMillis();

            QBUser selectedUser = usersListAdapter.getItem(position);
            createUserSession(selectedUser);
        }
    };


    private void createUserSession(final QBUser user) {

        showProgress(true);

        final String login = user.getLogin();
        final String password = user.getPassword();

        QBAuth.createSession(login, password, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle bundle) {
                Log.d(TAG, "onSuccess create session with params");
                user.setId(session.getUserId());

                // save current logged in user
                DataHolder.setLoggedUser(user);

                if (chatService.isLoggedIn()){
                    resultReceived = true;
                    startCallActivity(login);
                } else {
                    subscribeToPushNotifications();

                    chatService.login(user, new QBEntityCallbackImpl<QBUser>() {

                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess login to chat");
                            resultReceived = true;

                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                }
                            });

                            // save current user to preferences
                            UserCredentialsStorageManager.saveUserCredentials(LoginActivity.this, login, password);

                            // show Call activity
                            startCallActivity(login);
                        }

                        @Override
                        public void onError(List errors) {
                            resultReceived = true;

                            showProgress(false);

                            Toast.makeText(LoginActivity.this, "Error when login", Toast.LENGTH_SHORT).show();
                            for (Object error : errors) {
                                Log.d(TAG, error.toString());
                            }
                        }
                    });
                }

            }

            @Override
            public void onError(List<String> errors) {
                resultReceived = true;

                progressBar.setVisibility(View.INVISIBLE);

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
