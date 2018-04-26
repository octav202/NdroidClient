package com.ndroid.ndroidclient;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.ndroid.ndroidclient.models.DeviceLocation;
import com.ndroid.ndroidclient.models.DeviceStatus;
import com.ndroid.ndroidclient.server.AddDeviceTask;
import com.ndroid.ndroidclient.server.GetDeviceStatusTask;
import com.ndroid.ndroidclient.server.SendDeviceStatusTask;
import com.ndroid.ndroidclient.server.SendLocationTask;
import com.ndroid.ndroidclient.server.ServerApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static android.net.wifi.WifiManager.EXTRA_WIFI_STATE;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static com.ndroid.ndroidclient.Constants.IP;
import static com.ndroid.ndroidclient.Constants.SERVER_URL;
import static com.ndroid.ndroidclient.Constants.SERVER_URL_PREFIX;
import static com.ndroid.ndroidclient.Constants.SERVER_URL_SUFFIX;

public class AntiTheftService extends Service {

    private static final String TAG = Constants.TAG + AntiTheftService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();

    private LocationManager mLocationManager;
    private AtomicInteger ANTI_THEFT_CHECK_FREQUENCY = new AtomicInteger(100000);
    private AtomicInteger LOCATION_REFRESH_FREQUENCY = new AtomicInteger(0);
    private int LOCATION_REFRESH_DISTANCE = 50;

    // Wifi
    WifiManager mWifiManager;
    private AtomicBoolean mWifiState = new AtomicBoolean();
    private AtomicBoolean mShouldGetStatus = new AtomicBoolean();

    // Anti Theft Check
    private Handler mAntiTheftHandler;
    private HandlerThread mAntiTheftHandlerThread = null;
    private Runnable mGetDeviceStatusRunnable = new Runnable() {
        @Override
        public void run() {

            mWifiState.set(mWifiManager.isWifiEnabled());
            if (!mWifiState.get()) {
                // Turn Wifi on and get status
                mWifiManager.setWifiEnabled(true);
                Log.d(TAG, "Wifi Disabled, Enable & Postpone getDeviceStatus()");
                mShouldGetStatus.set(true);
            } else {
                // Wifi already On, Get status
                getDeviceStatus(true);
            }

            mAntiTheftHandler.postDelayed(this, ANTI_THEFT_CHECK_FREQUENCY.get());
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
            Log.d(TAG, "onLocationChanged() " + location.getLatitude() + ", " +
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

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        registerWifiReceiver();
        //Build URL server
        IP = Utils.getIpAddress(getApplicationContext());
        SERVER_URL = SERVER_URL_PREFIX + IP + SERVER_URL_SUFFIX;

        ANTI_THEFT_CHECK_FREQUENCY.set(Utils.getAtFrequency(getApplicationContext()) * 1000);

        if (canStartAntiTheft()) {
            // Enable AntiTheft periodic check
            startAntiTheftThread();
        } else {
            Log.d(TAG, "Id Or Frequency not set");
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        unregisterWifiReceiver();
        stopAntiTheftThread();
        stopLocationThread();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        /**
         * Gets service.
         *
         * @return the service
         */
        public AntiTheftService getService() {
            return AntiTheftService.this;
        }
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

        if (mLocationHandler != null) {
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
        if (mAntiTheftHandlerThread != null) {
            mAntiTheftHandlerThread.quit();
            mAntiTheftHandlerThread = null;
        }

        if (mAntiTheftHandler != null) {
            mAntiTheftHandler.removeCallbacksAndMessages(null);
            mAntiTheftHandler = null;
        }
    }

    private void sendDeviceStatus(DeviceStatus deviceStatus, final boolean previousWifiState) {
        new SendDeviceStatusTask(new SendDeviceStatusTask.SendDeviceStatusCallback() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onFinished(Boolean result) {
                // Check if wifi state needs to be reverted
                if (!previousWifiState) {
                    disableWifi();
                }
            }
        }).execute(deviceStatus);
    }

    private void disableWifi() {
        Boolean enabled = mWifiManager.isWifiEnabled();
        if (!enabled) {
            Log.d(TAG, "Wifi Already disabled");
        } else {
            Log.d(TAG, "Disabling Wifi");
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * Device Id
     */
    public Integer getDeviceId() {
        Integer id = Utils.getDeviceId(getApplicationContext());
        Log.d(TAG, "getDeviceId() " + id);
        return id;
    }

    public void setDeviceId(Integer id) {
        Log.d(TAG, "setDeviceId() " + id);
        Utils.storeDeviceId(getApplicationContext(), id);
    }

    /**
     * Device Name
     */
    public String getDeviceName() {
        String name = Utils.getDeviceName(getApplicationContext());
        Log.d(TAG, "getDeviceName() " + name);
        return name;
    }

    public void setDeviceName(String name) {
        Log.d(TAG, "setDeviceName() " + name);
        Utils.storeDeviceName(getApplicationContext(), name);
    }

    /**
     * Device Pass
     */
    public String getDevicePass() {
        String pass = Utils.getDevicePass(getApplicationContext());
        Log.d(TAG, "getDevicePass() " + pass);
        return pass;
    }

    public void setDevicePass(String pass) {
        Log.d(TAG, "setDevicePass() " + pass);
        Utils.storeDevicePass(getApplicationContext(), pass);
    }

    /**
     *  AntiTheft Status
     */

    public Boolean getAntiTheftStatus() {
        Boolean status = Utils.getAntiTheftStatus(getApplicationContext());
        Log.d(TAG, "getAntiTheftStatus() " + status);
        return status;
    }

    public void setAntiTheftStatus(Boolean status) {
        Log.d(TAG, "setAntiTheftStatus " + status);
        Utils.storeAntiTheftStatus(getApplicationContext(), status);
        if (status) {
            enableAntiTheft();
        } else {
            disableAntiTheft();
        }
    }

    /**
     *  IP Address
     */
    public String getIpAddress() {
        String ip = Utils.getIpAddress(getApplicationContext());
        Log.d(TAG, "getIpAddress() " +ip);
       return ip;
    }

    public void setIpAddress(String ip) {
        Log.d(TAG, "setIpAddress() " +ip);
        Utils.storeIpAddress(getApplicationContext(), ip);
        IP = ip;
        SERVER_URL = SERVER_URL_PREFIX + IP + SERVER_URL_SUFFIX;

        // restart service
        disableAntiTheft();
        enableAntiTheft();
    }

    /**
     *  AntiTheft Frequency
     */
    public Integer getAtFrequency() {
        Integer fr = Utils.getAtFrequency(getApplicationContext());
        Log.d(TAG, "getAtFrequency() " + fr);
        return fr;
    }

    public void setAtFrequency(Integer frequency) {
        Log.d(TAG, "setAtFrequency() " + frequency);
        Utils.storeAtFrequency(getApplicationContext(), frequency);

        ANTI_THEFT_CHECK_FREQUENCY.set(frequency * 1000);
        // Restart service
        disableAntiTheft();
        enableAntiTheft();
    }

    public void enableAntiTheft() {
        Log.d(TAG, "enableAntiTheft()");
        if (canStartAntiTheft()) {
            // Enable AntiTheft periodic check
            startAntiTheftThread();
        } else {
            Log.e(TAG,"enableAntiTheft - failed");
        }
    }

    public void disableAntiTheft() {
        Log.d(TAG, "disableAntiTheft()");
        stopLocationThread();
        stopAntiTheftThread();
    }

    private boolean canStartAntiTheft() {
        boolean enabled = Utils.getAntiTheftStatus(getApplicationContext());
        int deviceId = Utils.getDeviceId(getApplicationContext());
        return enabled && deviceId != 0 && ANTI_THEFT_CHECK_FREQUENCY.get() != 0;
    }

    /**
     * Server API
     */

    /**
     * Register Device on server
     * @param name
     * @param pass
     */
    public void registerDevice(final String name, final String pass, final AddDeviceTask.AddDeviceCallback callback) {
        new AddDeviceTask(new AddDeviceTask.AddDeviceCallback() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onFinished(int id) {
                Log.d(TAG, "Registered Id :" + id);

                if (id == 0) {
                    Log.e(TAG, "Registration failed!");
                } else {
                    setDeviceId(id);
                    setDeviceName(name);
                    setDevicePass(pass);
                    setAntiTheftStatus(true);
                }

                callback.onFinished(id);

            }
        }).execute(name, pass);
    }

    private void getDeviceStatus(final boolean previousWifiState) {
        new GetDeviceStatusTask(new GetDeviceStatusTask.GetDeviceStatusCallback() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onFinished(DeviceStatus status) {
                if (status == null) {
                    return;
                }

                // Start Location Thread - executed one time only
                if (LOCATION_REFRESH_FREQUENCY.get() == 0 && status.getLocationFrequency() != 0) {
                    Log.d(TAG, "Initiate Location Service");
                    LOCATION_REFRESH_FREQUENCY.set(status.getLocationFrequency() * 1000);
                    startLocationThread();
                }

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
                        Log.d(TAG, "Should Encrypt Storage");
                    }

                    if (status.getLock() == 1) {
                        // Lock Device
                        Log.d(TAG, "Should Lock Device");
                    }

                    if (status.getReboot() == 1) {
                        // Reboot;
                        Log.d(TAG, "Should Reboot Device");
                    }

                    if (status.getWipeData() == 1) {
                        // Wipe data;
                        Log.d(TAG, "Should Wipe Data");
                    }

                    if (status.getRing() == 1) {
                        // Ring
                        Log.d(TAG, "Should Ring");
                    }

                    // Operations triggered
                    status.setTriggered(1);

                    // Send Device Status and Revert wifi state
                    sendDeviceStatus(status, previousWifiState);
                } else {
                    if (!previousWifiState) {
                        disableWifi();
                    }
                }

            }
        }).execute(Utils.getDeviceId(getApplicationContext()));
    }

    /**
     * Wifi Receiver
     */

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            if (intent.getAction() == WIFI_STATE_CHANGED_ACTION) {
                int status = intent.getIntExtra(EXTRA_WIFI_STATE, 0);
                if (status == WIFI_STATE_ENABLED) {
                    Log.d(TAG, "on Receive WIFI_STATE_ENABLED");
                    if (mShouldGetStatus.compareAndSet(true,false)) {
                        getDeviceStatus(mWifiState.get());
                    }
                } else if (status == WIFI_STATE_DISABLED) {
                    Log.d(TAG, "on Receive WIFI_STATE_DISABLED");
                }

            }
        }
    };

    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }

    private void unregisterWifiReceiver() {
        unregisterReceiver(mWifiReceiver);
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
