package com.ndroid.ndroidclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;

import java.util.Locale;

import static com.ndroid.ndroidclient.Constants.SERVICE_READY;

public class SettingsActivity extends PreferenceActivity {

    private final static String TAG = "AT_SettingsActivity";
    private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SERVICE_READY)) {
                Log.d(TAG, "onReceive() " + SERVICE_READY);
                getFragmentManager().beginTransaction().replace(android.R.id.content,
                        new SettingsFragment()).commit();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Action Bar color
//        int primary = getResources().getColor(R.color.colorPrimaryDark);
//        String htmlColor = String.format(Locale.US, "#%06X", (0xFFFFFF & Color.argb(0,
//                Color.red(primary), Color.green(primary), Color.blue(primary))));
//        getActionBar().setTitle(Html.fromHtml("<font color=\""+htmlColor+"\">"
//                + getString(R.string.antitheft_settings_title) + "</font>"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICE_READY);
        registerReceiver(mServiceReceiver, filter);

        startService(new Intent(getApplicationContext(), AntiTheftService.class));
        AntiTheftManager manager = AntiTheftManager.getInstance(getApplicationContext());
        if (manager.isServiceAvailable()) {
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    new SettingsFragment()).commit();
        } else {
            Log.d(TAG, "Service not running");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        try {
            unregisterReceiver(mServiceReceiver);
        } catch (IllegalArgumentException exc) {
            Log.e(TAG, "Unregister Receiver Failed");
        }
    }
}
