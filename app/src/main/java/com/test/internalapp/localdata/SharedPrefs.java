package com.test.internalapp.localdata;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Set;

public class SharedPrefs {

    private static String SHARED_PREFS_FILE_NAME = "com.test.internalapp";


    public static String START_SERVICE_ON_BOOT_COMPLETED = "START_SERVICE_ON_BOOT_COMPLETED";


    public static String START_MY_SERVICE = "START_MY_SERVICE";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }
    //Save Booleans
    public static void setStartService(Context context) {
        getPrefs(context).edit().putBoolean(START_MY_SERVICE, true).apply();
    }
    public static void setStopService(Context context) {
        getPrefs(context).edit().putBoolean(START_MY_SERVICE, false).apply();
    }
    //Get Booleans
    public static boolean getStartService(Context context) {
        return getPrefs(context).getBoolean(START_MY_SERVICE, true);
    }

  }