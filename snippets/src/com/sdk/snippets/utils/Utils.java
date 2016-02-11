package com.sdk.snippets.utils;

import android.content.Context;
import android.text.format.DateUtils;

import com.quickblox.core.helper.FileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by igorkhomenko on 10/22/15.
 */
public class Utils {
    public static String getContentFromFile(InputStream is) {
        char[] buffer = new char[1024];
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while (bufferedReader.read(buffer, 0, 1024) != -1) {
                stringBuilder.append(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static File getFileFromRawResource(int fileId, Context context) {
        InputStream is = context.getResources().openRawResource(fileId);
        File file = FileHelper.getFileInputStream(is, "sample" + fileId + ".txt", "qb_snippets12");
        return file;
    }

    public static Date getUTCDate(){
        long nowAsPerDeviceTimeZone = getUTCTimestamp();
        return new Date(nowAsPerDeviceTimeZone);
    }

    public static long getUTCTimestamp(){
        long nowAsPerDeviceTimeZone = 0;
        SntpClient sntpClient = new SntpClient();

        boolean success = sntpClient.requestTime("time.apple.com", 30000);

        if (success) {
            nowAsPerDeviceTimeZone = sntpClient.getNtpTime();
            Calendar cal = Calendar.getInstance();
            TimeZone timeZoneInDevice = cal.getTimeZone();
            int differentialOfTimeZones = timeZoneInDevice.getOffset(System.currentTimeMillis());
            nowAsPerDeviceTimeZone -= differentialOfTimeZones;
        }

        return nowAsPerDeviceTimeZone;
    }
}
