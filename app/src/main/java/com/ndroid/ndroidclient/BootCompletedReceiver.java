package com.ndroid.ndroidclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        Log.d("AT_BootReceiver", "Start AntiTheftService");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, AntiTheftService.class));
        } else {
            context.startService(new Intent(context, AntiTheftService.class));
        }
    }
}