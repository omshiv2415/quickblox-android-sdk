package com.quickblox.sample.gcmchecker.main.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.quickblox.sample.gcmchecker.main.Consts;

/**
 * Created by tereha on 09.11.15.
 */
public class DBHelper extends SQLiteOpenHelper {

    private String TAG = DBHelper.class.getSimpleName();
    ContentValues cv = new ContentValues();

    public DBHelper(Context context) {
        super(context, Consts.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table " + Consts.DB_TABLE_NAME + " ("
                + Consts.DB_COLUMN_ID + " integer primary key autoincrement,"
                + Consts.DB_COLUMN_TITLE + " text,"
                + Consts.DB_COLUMN_APP_ID + " integer,"
                + Consts.DB_COLUMN_AUTH_KEY + " text,"
                + Consts.DB_COLUMN_AUTH_SECRET + " text,"
                + Consts.DB_COLUMN_USER_LOGIN + " text,"
                + Consts.DB_COLUMN_USER_ID + " test,"
                + Consts.DB_COLUMN_USER_PASSWORD + " text,"
                + Consts.DB_COLUMN_SERVER_API_DOMAIN + " text"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
