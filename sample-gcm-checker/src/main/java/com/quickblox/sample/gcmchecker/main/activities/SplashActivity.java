package com.quickblox.sample.gcmchecker.main.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.qb.gson.JsonArray;
import com.qb.gson.JsonElement;
import com.qb.gson.JsonObject;
import com.qb.gson.JsonParser;

import com.qb.gson.JsonSyntaxException;
import com.quickblox.sample.gcmchecker.R;
import com.quickblox.sample.gcmchecker.main.Consts;
import com.quickblox.sample.gcmchecker.main.helper.CredentialsDBManager;
import com.quickblox.sample.gcmchecker.main.models.Credentials;
import com.quickblox.sample.gcmchecker.main.utils.DialogUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SplashActivity extends Activity{

    private ProgressBar progressBar;
    private Button tryAgainBtn, runByOldData;
    private final String TAG = SplashActivity.class.getSimpleName();
    public static ArrayList<Credentials> credentialsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initUI();
        startTaskLoadData();
    }

    private void initUI() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        tryAgainBtn = (Button) findViewById(R.id.tryAgainBtn);
        tryAgainBtn.setVisibility(View.GONE);
        runByOldData = (Button) findViewById(R.id.runByOldData);
        runByOldData.setText(getString(R.string.run_test_by_old_data) + getDateUpdate());
        runByOldData.setVisibility(View.INVISIBLE);

    }

    private void startTaskLoadData() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return loadServersData();
            }

            @Override
            protected void onPostExecute(Boolean params) {
                if (params){
                    startCheckerActivity();
                } else {
                    DialogUtils.showLong(getApplicationContext(), getString(R.string.error_loading_data));
                    progressBar.setVisibility(View.INVISIBLE);
                    tryAgainBtn.setVisibility(View.VISIBLE);

                    if (!getDateUpdate().equals(Consts.EMPTY_STRING)){
                        runByOldData.setVisibility(View.VISIBLE);
                    }
                }
            }
        }.execute(null, null, null);
    }

    public boolean loadServersData() {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(Consts.INSTANCES_WEB_RESOURCE);

        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String content = EntityUtils.toString(entity);

            JsonParser parser = new JsonParser();
            JsonArray mainObject = parser.parse(content).getAsJsonArray();

            for (JsonElement instance : mainObject) {
                JsonObject instanceObject = instance.getAsJsonObject();

                Credentials credentials = new Credentials();
                credentials.setTitle(instanceObject.get(Consts.INSTANCES_TITLE).getAsString());
                credentials.setAppId(instanceObject.get(Consts.INSTANCES_APP_ID).getAsInt());
                credentials.setAuthKey(instanceObject.get(Consts.INSTANCES_AUTH_KEY).getAsString());
                credentials.setAuthSecret(instanceObject.get(Consts.INSTANCES_AUTH_SECRET).getAsString());
                credentials.setUserLogin(instanceObject.get(Consts.INSTANCES_USER_LOGIN).getAsString());
                credentials.setUserID(instanceObject.get(Consts.INSTANCES_USER_ID).getAsString());
                credentials.setUserPass(instanceObject.get(Consts.INSTANCES_USER_PASSWORD).getAsString());
                credentials.setServerApiDomain(instanceObject.get(Consts.INSTANCES_SERVER_API_DOMAIN).getAsString());

                credentialsList.add(credentials);
            }

            if (credentialsList.size() > 0){
                CredentialsDBManager.clearDB(this);
                CredentialsDBManager.saveAllCredentials(SplashActivity.this, credentialsList);
                saveDateUpdate();
            }

            Log.d(TAG, "credentialsList.size() = " + credentialsList.size());
            return true;
        } catch (JsonSyntaxException e){
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startCheckerActivity() {
        Intent intent = new Intent(SplashActivity.this, CheckerActivity.class);
        startActivity(intent);
        finish();
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tryAgainBtn:
                tryAgainBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startTaskLoadData();
                break;
            case R.id.runByOldData:
                startCheckerActivity();
        }
    }

    private void saveDateUpdate (){
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date (currentTimeMillis);
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String currentDateText = df.format(currentDate);

        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(Consts.PREFS_DATE_UPDATE, currentDateText);
        ed.commit();
    }

    private String getDateUpdate(){
        String date;
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        date = sPref.getString(Consts.PREFS_DATE_UPDATE, Consts.EMPTY_STRING);
        return date;
    }
}