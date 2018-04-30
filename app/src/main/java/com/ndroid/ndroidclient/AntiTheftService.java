package com.ndroid.ndroidclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.ndroid.ndroidclient.Constants.SERVICE_READY;


public class AntiTheftService extends Service {

    private static final String TAG = Constants.TAG + AntiTheftService.class.getSimpleName();

    private AntiTheftBinder mBinder;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        mBinder = new AntiTheftBinder(getApplicationContext());

        sendBroadcast(new Intent(SERVICE_READY));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinder.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
