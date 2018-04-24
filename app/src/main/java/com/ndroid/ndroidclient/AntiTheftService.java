package com.ndroid.ndroidclient;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.ndroid.ndroidclient.models.DeviceLocation;
import com.ndroid.ndroidclient.models.DeviceStatus;
import com.ndroid.ndroidclient.server.GetDeviceStatusTask;
import com.ndroid.ndroidclient.server.SendLocationTask;
import com.ndroid.ndroidclient.server.ServerApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class AntiTheftService extends Service {

    private static final String TAG =  Constants.TAG + AntiTheftService.class.getSimpleName();

    private DeviceStatus mDeviceStatus;
    private int mDeviceId;
    private LocationManager mLocationManager;
    private AtomicInteger ANTI_THEFT_CHECK_FREQUENCY = new AtomicInteger(100000);
    private AtomicInteger LOCATION_REFRESH_FREQUENCY = new AtomicInteger(0);
    private int LOCATION_REFRESH_DISTANCE = 50;

    // Anti Theft Check
    private Handler mAntiTheftHandler;
    private HandlerThread mAntiTheftHandlerThread = null;
    private Runnable mGetDeviceStatusRunnable = new Runnable() {
        @Override
        public void run() {
            new GetDeviceStatusTask(new GetDeviceStatusTask.GetDeviceStatusCallback() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onFinished(DeviceStatus status) {
                    // Check if location frequency changed
                    if (status.getLocationFrequency() != LOCATION_REFRESH_FREQUENCY.get() / 1000) {
                        LOCATION_REFRESH_FREQUENCY.set(status.getLocationFrequency() * 1000);
                        Log.d(TAG, "Location Frequency Changed To " + LOCATION_REFRESH_FREQUENCY.get());

                        // Restart location update handler
                        stopLocationThread();
                        if (LOCATION_REFRESH_FREQUENCY.get() != 0) {
                            startLocationThread();
                        } else {
                            Log.d(TAG, "Stopping location updates..");
                        }
                    }

                    // Check for pending device operations
                    if (status.getTriggered() == 0) {
                        if (status.getEncryptStorage() == 1) {
                            // Encrypt Storage
                        }

                        if (status.getLock() == 1) {
                            // Lock Device
                        }

                        if (status.getReboot() == 1) {
                            // Reboot;
                        }

                        if (status.getWipeData() == 1) {
                            // Wipe data;
                        }

                        // Operations triggered
                        status.setTriggered(1);
                    }

                    // TODO send Device Status
                }
            }).execute(mDeviceId);


            mAntiTheftHandler.postDelayed(this, 10000);
        }
    };

    // Location
    private Handler mLocationHandler;
    private HandlerThread mLocationHandlerThread = null;
    private Runnable mLocationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(AntiTheftService.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(AntiTheftService.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // REQUEST LOCATION PERMISSION
                return;
            }

            // Get Current DeviceLocation
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // Get Current Time
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String time = sdf.format(new Date());

            // DeviceLocation to send to server
            DeviceLocation devLoc = new DeviceLocation();
            devLoc.setDeviceId(ServerApi.getCurrentDeviceId());
            devLoc.setLat(location.getLatitude());
            devLoc.setLon(location.getLongitude());
            devLoc.setTimeStamp(time);

            new SendLocationTask(new SendLocationTask.SendLocationCallback() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onFinished(Boolean result) {

                }
            }).execute(devLoc);

            mLocationHandler.postDelayed(this, LOCATION_REFRESH_FREQUENCY.get());
        }
    };


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d(TAG,"onLocationChanged() " + location.getLatitude() + ", " +
            location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        mDeviceId = Utils.getDeviceId(getApplicationContext());
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Enable AntiTheft periodic check
        startAntiTheftThread();

        // Request Location Updates
        //requestLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLocationThread() {
        Log.d(TAG, "startLocationThread()");
        mLocationHandlerThread = new HandlerThread("Location_Thread");
        mLocationHandlerThread.start();
        mLocationHandler = new Handler(mLocationHandlerThread.getLooper());
        mLocationHandler.postDelayed(mLocationUpdateRunnable, LOCATION_REFRESH_FREQUENCY.get());
    }

    private void stopLocationThread() {
        Log.d(TAG, "stopLocationThread()");
        if (mLocationHandlerThread != null) {
            mLocationHandlerThread.quit();
            mLocationHandlerThread = null;
        }

        if (mLocationHandlerThread != null) {
            mLocationHandler.removeCallbacksAndMessages(null);
            mLocationHandler = null;
        }
    }

    private void startAntiTheftThread() {
        Log.d(TAG, "startAntiTheftThread()");
        mAntiTheftHandlerThread = new HandlerThread("AT_Thread");
        mAntiTheftHandlerThread.start();
        mAntiTheftHandler = new Handler(mAntiTheftHandlerThread.getLooper());
        mAntiTheftHandler.post(mGetDeviceStatusRunnable);
    }

    private void stopAntiTheftThread() {
        Log.d(TAG, "stopAntiTheftThread()");
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_FREQUENCY.get(),
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        } else {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
        }
    }
}
