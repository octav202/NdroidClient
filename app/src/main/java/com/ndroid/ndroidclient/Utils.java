package com.ndroid.ndroidclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.ndroid.ndroidclient.Constants.SHARED_KEY_AT_FREQUENCY;
import static com.ndroid.ndroidclient.Constants.SHARED_KEY_AT_STATUS;
import static com.ndroid.ndroidclient.Constants.SHARED_KEY_DEVICE_ID;
import static com.ndroid.ndroidclient.Constants.SHARED_KEY_DEVICE_NAME;
import static com.ndroid.ndroidclient.Constants.SHARED_KEY_DEVICE_PASS;
import static com.ndroid.ndroidclient.Constants.SHARED_KEY_IP_ADDRESS;

public class Utils {

    // Device ID
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

    // Device Name
    public static void storeDeviceName(Context context, String name) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_KEY_DEVICE_NAME, name);
        editor.apply();
    }

    public static String getDeviceName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = preferences.getString(SHARED_KEY_DEVICE_NAME, "");
        return id;
    }

    // Device Pass
    public static void storeDevicePass(Context context, String pass) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_KEY_DEVICE_PASS, pass);
        editor.apply();
    }

    public static String getDevicePass(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pass = preferences.getString(SHARED_KEY_DEVICE_PASS, "");
        return pass;
    }

    // AntiTheft Status
    public static void storeAntiTheftStatus(Context context, boolean status) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SHARED_KEY_AT_STATUS, status);
        editor.apply();
    }

    public static boolean getAntiTheftStatus(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean status = preferences.getBoolean(SHARED_KEY_AT_STATUS, false);
        return status;
    }

    // Frequency
    public static void storeAtFrequency(Context context, Integer secs) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SHARED_KEY_AT_FREQUENCY, secs);
        editor.apply();
    }

    public static Integer getAtFrequency(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Integer id = preferences.getInt(SHARED_KEY_AT_FREQUENCY, 0);
        return id;
    }

    // IP Address
    public static void storeIpAddress(Context context, String ip) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SHARED_KEY_IP_ADDRESS, ip);
        editor.apply();
    }

    public static String getIpAddress(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = preferences.getString(SHARED_KEY_IP_ADDRESS, "");
        return ip;
    }
}
