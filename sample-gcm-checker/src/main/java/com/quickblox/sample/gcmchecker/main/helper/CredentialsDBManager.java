package com.quickblox.sample.gcmchecker.main.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.quickblox.sample.gcmchecker.main.Consts;
import com.quickblox.sample.gcmchecker.main.models.Credentials;

import java.util.ArrayList;

/**
 * Created by tereha on 09.11.15.
 */
public class CredentialsDBManager {

    private static String TAG = CredentialsDBManager.class.getSimpleName();

    public static ArrayList <Credentials> getAllCredetntials(Context context){
        ArrayList<Credentials> allCredentials = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(Consts.DB_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int titleColIndex = c.getColumnIndex(Consts.DB_COLUMN_TITLE);
            int appIdColIndex = c.getColumnIndex(Consts.DB_COLUMN_APP_ID);
            int authKeyColIndex = c.getColumnIndex(Consts.DB_COLUMN_AUTH_KEY);
            int authSecretColIndex = c.getColumnIndex(Consts.DB_COLUMN_AUTH_SECRET);
            int userLoginColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_LOGIN);
            int userIdColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_ID);
            int userPassColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_PASSWORD);
            int serverApiDomainColIndex = c.getColumnIndex(Consts.DB_COLUMN_SERVER_API_DOMAIN);

            do {
                Credentials credentials = new Credentials();

                credentials.setTitle(c.getString(titleColIndex));
                credentials.setAppId(c.getInt(appIdColIndex));
                credentials.setAuthKey(c.getString(authKeyColIndex));
                credentials.setAuthSecret(c.getString(authSecretColIndex));
                credentials.setUserLogin(c.getString(userLoginColIndex));
                credentials.setUserID(c.getString(userIdColIndex));
                credentials.setUserPass(c.getString(userPassColIndex));
                credentials.setServerApiDomain(c.getString(serverApiDomainColIndex));

                allCredentials.add(credentials);
            } while (c.moveToNext());
        }

        if (c != null) {
            c.close();
        }
        dbHelper.close();

        return allCredentials;
    }

    public static Credentials getCredentialsByTitle (Context context, String title){
        Credentials credentials = new Credentials();
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query(Consts.DB_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int titleColIndex = c.getColumnIndex(Consts.DB_COLUMN_TITLE);
            int appIdColIndex = c.getColumnIndex(Consts.DB_COLUMN_APP_ID);
            int authKeyColIndex = c.getColumnIndex(Consts.DB_COLUMN_AUTH_KEY);
            int authSecretColIndex = c.getColumnIndex(Consts.DB_COLUMN_AUTH_SECRET);
            int userLoginColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_LOGIN);
            int userIdColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_ID);
            int userPassColIndex = c.getColumnIndex(Consts.DB_COLUMN_USER_PASSWORD);
            int serverApiDomainColIndex = c.getColumnIndex(Consts.DB_COLUMN_SERVER_API_DOMAIN);

            do {
                if (c.getString(titleColIndex).equals(title)) {
                    credentials.setTitle(c.getString(titleColIndex));
                    credentials.setAppId(c.getInt(appIdColIndex));
                    credentials.setAuthKey(c.getString(authKeyColIndex));
                    credentials.setAuthSecret(c.getString(authSecretColIndex));
                    credentials.setUserLogin(c.getString(userLoginColIndex));
                    credentials.setUserID(c.getString(userIdColIndex));
                    credentials.setUserPass(c.getString(userPassColIndex));
                    credentials.setServerApiDomain(c.getString(serverApiDomainColIndex));
                    break;
                }
            } while (c.moveToNext());
        }

        if (c != null) {
            c.close();
        }
        dbHelper.close();

        return credentials;
    }

    public static void saveAllCredentials (Context context, ArrayList<Credentials> allCredentials){
        for (Credentials credentials : allCredentials){
            saveCredentials(context, credentials);
        }
        Log.d(TAG, "saveAllCredentials");
    }

    public static void saveCredentials(Context context, Credentials credentials){
        ContentValues cv = new ContentValues();
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        cv.put(Consts.DB_COLUMN_TITLE, credentials.getTitle());
        cv.put(Consts.DB_COLUMN_APP_ID, credentials.getAppId());
        cv.put(Consts.DB_COLUMN_AUTH_KEY, credentials.getAuthKey());
        cv.put(Consts.DB_COLUMN_AUTH_SECRET, credentials.getAuthSecret());
        cv.put(Consts.DB_COLUMN_USER_LOGIN, credentials.getUserLogin());
        cv.put(Consts.DB_COLUMN_USER_ID, credentials.getUserID());
        cv.put(Consts.DB_COLUMN_USER_PASSWORD, credentials.getUserPass());
        cv.put(Consts.DB_COLUMN_SERVER_API_DOMAIN, credentials.getServerApiDomain());

        db.insert(Consts.DB_TABLE_NAME, null, cv);
        dbHelper.close();
    }

    public static void clearDB (Context context){
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Consts.DB_TABLE_NAME, null, null);
        dbHelper.close();
    }
}
