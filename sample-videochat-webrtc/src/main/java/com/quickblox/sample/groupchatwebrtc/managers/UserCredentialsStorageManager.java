package com.quickblox.sample.groupchatwebrtc.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by igorkhomenko on 1/25/16.
 */
public class UserCredentialsStorageManager {
    public static final String LOGIN = "userLogin";
    public static final String PASSWORD = "userPassword";

    public static void saveUserCredentials(Context ctx, String login, String password){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPreferences.edit().putString(LOGIN, login).putString(PASSWORD, password).apply();
    }

    public static void clear(Context ctx){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPreferences.edit().remove(LOGIN).remove(PASSWORD).apply();
    }

    public static String[] getCredentials(Context ctx){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        if(!sharedPreferences.contains(LOGIN)){
            return null;
        }

        String []result= new String[2];
        result[0] = sharedPreferences.getString(LOGIN, null);
        result[1] = sharedPreferences.getString(PASSWORD, null);
        return result;
    }
}
