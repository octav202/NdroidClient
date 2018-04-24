package com.ndroid.ndroidclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.ndroid.ndroidclient.Constants.SHARED_KEY_DEVICE_ID;

public class Utils {

    public static void storeDeviceId(Context context, Integer id) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SHARED_KEY_DEVICE_ID, id);
        editor.apply();
    }

    public static Integer getDeviceId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Integer id = preferences.getInt(SHARED_KEY_DEVICE_ID, 0);
        return id;
    }
}
