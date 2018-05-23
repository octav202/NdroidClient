package com.ndroid.ndroidclient;

import android.Manifest;
import android.app.NotificationManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ndroid.ndroidclient.models.DeviceAlert;
import com.ndroid.ndroidclient.models.DeviceLocation;
import com.ndroid.ndroidclient.models.DeviceStatus;
import com.ndroid.ndroidclient.server.AddDeviceTask;
import com.ndroid.ndroidclient.server.GetDeviceAlertTask;
import com.ndroid.ndroidclient.server.GetDeviceStatusTask;
import com.ndroid.ndroidclient.server.SendDeviceStatusTask;
import com.ndroid.ndroidclient.server.SendLocationTask;
import com.ndroid.ndroidclient.server.ServerApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;
import static android.net.wifi.WifiManager.EXTRA_WIFI_STATE;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static com.ndroid.ndroidclient.Constants.DEVICE_ADMIN;
import static com.ndroid.ndroidclient.Constants.DEVICE_REGISTERED;
import static com.ndroid.ndroidclient.Constants.DEVICE_REGISTERED_EXTRA_KEY;
import static com.ndroid.ndroidclient.Constants.IP;
import static com.ndroid.ndroidclient.Constants.LOCATION;
import static com.ndroid.ndroidclient.Constants.RING_TIMEOUT;
import static com.ndroid.ndroidclient.Constants.SERVER_URL;
import static com.ndroid.ndroidclient.Constants.SERVER_URL_PREFIX;
import static com.ndroid.ndroidclient.Constants.SERVER_URL_SUFFIX;

public class AntiTheftBinder extends IAntiTheftService.Stub {
    public static final String TAG = "AT_AntiTheftBinder";

    private Context mContext;
    private LocationManager mLocationManager;
    private AtomicInteger ANTI_THEFT_CHECK_FREQUENCY = new AtomicInteger(100000);
    private AtomicInteger LOCATION_REFRESH_FREQUENCY = new AtomicInteger(0);
    private int LOCATION_REFRESH_DISTANCE = 50;
    private AtomicInteger mDeviceId = new AtomicInteger();

    private DevicePolicyManager mDeviceManager ;

    // Wifi
    WifiManager mWifiManager;
    private AtomicBoolean mWifiState = new AtomicBoolean();
    private AtomicBoolean mShouldGetStatus = new AtomicBoolean();

    // Window Manager
    private WindowManager mWindowManager;

    // Freeze
    private RelativeLayout mDummyView;
    private WindowManager.LayoutParams mWindowParams;
    private AtomicBoolean mFroze = new AtomicBoolean();

    // Alert
    private RelativeLayout mAlertView;
    private WindowManager.LayoutParams mAlertParams;
    private AtomicBoolean mAlert = new AtomicBoolean();
    private DeviceAlert mDeviceAlert;

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
            if (ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Location Permission not Requested");

                requestLocationPermissions();
                return;
            }

            // Get Current DeviceLocation
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // Get Current Time
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String time = sdf.format(new Date());

                // DeviceLocation to send to server
                DeviceLocation devLoc = new DeviceLocation();
                devLoc.setDeviceId(mDeviceId.get());
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
            } else {
                Log.d(TAG, "Location is Null");
            }

            if (mLocationHandler != null) {
                mLocationHandler.postDelayed(this, LOCATION_REFRESH_FREQUENCY.get());
            }
        }
    };

    public AntiTheftBinder(Context c) {
        Log.d(TAG, "AntiTheftBinder created");
        mContext = c;

        AntiTheftManager.getInstance(mContext);

        mDeviceManager = (DevicePolicyManager)mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mLocationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);

        // Request all necessary permissions
        requestLocationPermissions();
        requestAdminPermissions();
        requestDisturbPermissions();
        requestOverlayPermissions();

        registerWifiReceiver();
        //Build URL server
        IP = Utils.getIpAddress(mContext);
        SERVER_URL = SERVER_URL_PREFIX + IP + SERVER_URL_SUFFIX;

        ANTI_THEFT_CHECK_FREQUENCY.set(Utils.getAtFrequency(mContext) * 1000);

        if (canStartAntiTheft()) {
            // Enable AntiTheft periodic check
            startAntiTheftThread();
        } else {
            Log.d(TAG, "Id Or Frequency not set");
        }
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        unregisterWifiReceiver();
        stopAntiTheftThread();
        stopLocationThread();
    }

    private void startLocationThread() {
        Log.d(TAG, "startLocationThread()");
        mLocationHandlerThread = new HandlerThread("Location_Thread");
        mLocationHandlerThread.start();
        mLocationHandler = new Handler(mLocationHandlerThread.getLooper());
        mLocationHandler.post(mLocationUpdateRunnable);
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
        boolean enabled = Utils.getAntiTheftStatus(mContext);
        int deviceId = Utils.getDeviceId(mContext);
        return enabled && deviceId != 0 && ANTI_THEFT_CHECK_FREQUENCY.get() != 0;
    }

    /**
     * Server API
     */

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
                mDeviceId.set(status.getDeviceId());

                // Check if location needs to be sent to server
                checkLocationStatus(status);

                // Handle Device Status
                if (status.getTriggered() == 0) {
                    if (status.getEncryptStorage() == 1) {
                        encrypt();
                    } else {
                        decript();
                    }

                    if (status.getLock() == 1) {
                        lock();
                    }

                    if (status.getWipeData() == 1) {
                        wipe();
                    }

                    if (status.getRing() == 1) {
                        ring();
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

                // Reboot device continuously until the tracker sets reboot to off
                if (status.getReboot() == 1) {
                    // Reboot;
                    Log.d(TAG, "Should Reboot Device");
                    reboot();
                }

                // Freeze screen until the track sets freeze to off
                if (status.getFreeze() == 1) {
                    freeze(true);
                } else {
                    freeze(false);
                }

                if (status.getAlert() == 1){
                    alert(true);
                } else {
                    alert(false);
                }

            }
        }).execute(Utils.getDeviceId(mContext));
    }

    private void checkLocationStatus(DeviceStatus status) {
        // Start Location Thread - executed one time only
        if (LOCATION_REFRESH_FREQUENCY.get() == 0 && status.getLocationFrequency() != 0) {
            LOCATION_REFRESH_FREQUENCY.set(status.getLocationFrequency() * 1000);
            Log.d(TAG, "Initiate Location Service " + LOCATION_REFRESH_FREQUENCY.get());
            if (LOCATION_REFRESH_FREQUENCY.get() != 0) {
                startLocationThread();
            }
        }

        if (mLocationHandler == null || mLocationHandlerThread == null) {
            if (LOCATION_REFRESH_FREQUENCY.get() != 0) {
                startLocationThread();
            }
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
        mContext.registerReceiver(mWifiReceiver, filter);
    }

    private void unregisterWifiReceiver() {
        mContext.unregisterReceiver(mWifiReceiver);
    }

    private void requestDisturbPermissions() {
        // Request Access to bypass "Do not disturb" mode
        NotificationManager n = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if(!n.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            mContext.startActivity(intent);
            return;
        }
    }

    /**
     * Request Window Overlay Permissions
     */
    private void requestOverlayPermissions() {
        if (!Settings.canDrawOverlays(mContext)) {
            Log.d(TAG, "requestOverlayPermissions");
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + "com.ndroid.ndroidclient"));
            mContext.startActivity(intent);
            return;
        }
    }

    /**
     * Request Location Permissions
     */
    private void requestLocationPermissions() {
        Intent intent = new Intent(mContext, PermissionActivity.class);
        intent.putExtra(LOCATION, true);
        mContext.startActivity(intent);
    }

    /**
     * Device Admin Policies
     */
    private void requestAdminPermissions() {
        if (!isAdminActive()) {
            Intent intent = new Intent(mContext, PermissionActivity.class);
            intent.putExtra(DEVICE_ADMIN, true);
            mContext.startActivity(intent);
        }
    }

    private boolean isAdminActive() {
        ComponentName receiver = new ComponentName(mContext, AdminReceiver.class);
        return mDeviceManager.isAdminActive(receiver);
    }

    /**
     * Remote functions
     */

    public void lock() {
        Log.d(TAG, "[ LOCK ]");
        if (isAdminActive()) {
            mDeviceManager.lockNow();
        } else {
            Log.e(TAG, "No Admin Permission");
            requestAdminPermissions();
        }
    }

    public void wipe() {
        Log.d(TAG, "[ WIPE ]");
        if (isAdminActive()) {
            mDeviceManager.wipeData(0);
        } else {
            Log.e(TAG, "No Admin Permission");
            requestAdminPermissions();
        }
    }

    public void freeze(boolean status) {
        // Initialize
        if (mDummyView == null) {
            mWindowParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
            );

            mDummyView = new RelativeLayout(mContext);
            RelativeLayout.LayoutParams dParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            mDummyView.setBackgroundColor(Color.TRANSPARENT);
            mDummyView.setLayoutParams(dParams);
            mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        }

        if (status) {
            if (!mFroze.get()) {
                Log.d(TAG, "[ FREEZE ]");
                mWindowManager.addView(mDummyView, mWindowParams);
                mFroze.set(true);
            }
        } else {
            if (mFroze.get()) {
                Log.d(TAG, "[ UNFREEZE ]");
                mWindowManager.removeView(mDummyView);
                mFroze.set(false);
            }
        }
    }

    public void alert(final boolean status) {

        if (status) {
            if (!mAlert.get()) {
                Log.d(TAG, "[ ALERT ON]");
                // Get Device Alert Info
                new GetDeviceAlertTask(new GetDeviceAlertTask.GetDeviceAlertCallback() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onFinished(DeviceAlert alert) {
                        // Initialize
                        mAlertParams = new WindowManager.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                                PixelFormat.TRANSLUCENT
                        );

                        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService
                                (Context.LAYOUT_INFLATER_SERVICE);
                        mAlertView = (RelativeLayout) inflater.inflate(R.layout.alert_dialog,null);

                        if (alert != null && mAlertView != null) {
                            TextView phone = mAlertView.findViewById(R.id.phoneText);
                            TextView email = mAlertView.findViewById(R.id.emailText);
                            TextView description = mAlertView.findViewById(R.id.descriptionText);

                            phone.setText("Phone Number - " + alert.getPhone());
                            email.setText("Email  - " + alert.getEmail());
                            description.setText(alert.getDescription());
                        }
                        mWindowManager.addView(mAlertView, mAlertParams);
                        mAlert.set(true);

                    }
                }).execute(Utils.getDeviceId(mContext));

            }
        } else {
            if (mAlert.get()) {
                Log.d(TAG, "[ ALERT OFF]");
                mWindowManager.removeView(mAlertView);
                mAlert.set(false);
            }
        }

    }



    public void reboot() {
        Log.d(TAG, "[ REBOOT ]");
        ComponentName receiver = new ComponentName(mContext, AdminReceiver.class);
        if (! mDeviceManager.isDeviceOwnerApp("com.ndroid.ndroidclient")){
            Log.e(TAG, "App Not Device Owner!!");
            return;
        }

        if (isAdminActive()) {
            mDeviceManager.reboot(receiver);
        } else {
            Log.e(TAG, "No Admin Permission");
            requestAdminPermissions();
        }
    }

    public void encrypt() {
        Log.d(TAG, "[ ENCRYPT ]");
        ComponentName receiver = new ComponentName(mContext, AdminReceiver.class);
        if (isAdminActive()) {
            mDeviceManager.setStorageEncryption(receiver, true);
        } else {
            Log.e(TAG, "No Admin Permission");
            requestAdminPermissions();
        }
    }

    public void decript() {
        Log.d(TAG, "[ DECRYPT ]");
        ComponentName receiver = new ComponentName(mContext, AdminReceiver.class);
        if (isAdminActive()) {
            mDeviceManager.setStorageEncryption(receiver, false);
        } else {
            Log.e(TAG, "No Admin Permission");
            requestAdminPermissions();
        }
    }

    public void ring() {
        Log.d(TAG, "[ RING ]");

        // Request Access to bypass "Do not disturb" mode
        NotificationManager n = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if(!n.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            mContext.startActivity(intent);
            return;
        }

        // Set volume to max on STREAM_RING
        AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),0);

        // Play ringtone
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        final Ringtone r = RingtoneManager.getRingtone(mContext, alarm);
        if (!r.isPlaying()) {
            r.play();

            // Stop after a timeout
            new Handler(). postDelayed(new Runnable() {
                @Override
                public void run() {
                    r.stop();
                    Log.d(TAG, "Stopping Ring..");
                }
            }, RING_TIMEOUT * 1000);
        }
    }


    /**
     * Location
     */

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
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

    /**
     * Remote Methods
     */

    /**
     * Device Id
     */
    @Override
    public int getDeviceId() {
        Integer id = Utils.getDeviceId(mContext);
        Log.d(TAG, "getDeviceId() " + id);
        return id;
    }
    @Override
    public void setDeviceId(int id) {
        Log.d(TAG, "setDeviceId() " + id);
        Utils.storeDeviceId(mContext, id);
    }

    /**
     * Device Name
     */
    @Override
    public String getDeviceName() {
        String name = Utils.getDeviceName(mContext);
        Log.d(TAG, "getDeviceName() " + name);
        return name;
    }

    @Override
    public void setDeviceName(String name) {
        Log.d(TAG, "setDeviceName() " + name);
        Utils.storeDeviceName(mContext, name);
    }

    /**
     * Device Pass
     */
    @Override
    public String getDevicePass() {
        String pass = Utils.getDevicePass(mContext);
        Log.d(TAG, "getDevicePass() " + pass);
        return pass;
    }

    @Override
    public void setDevicePass(String pass) {
        Log.d(TAG, "setDevicePass() " + pass);
        Utils.storeDevicePass(mContext, pass);
    }

    /**
     *  AntiTheft Status
     */

    @Override
    public boolean getAntiTheftStatus() {
        Boolean status = Utils.getAntiTheftStatus(mContext);
        Log.d(TAG, "getAntiTheftStatus() " + status);
        return status;
    }

    @Override
    public void setAntiTheftStatus(boolean status) {
        Log.d(TAG, "setAntiTheftStatus " + status);
        Utils.storeAntiTheftStatus(mContext, status);
        if (status) {
            enableAntiTheft();
        } else {
            disableAntiTheft();
        }
    }

    /**
     *  IP Address
     */
    @Override
    public String getIpAddress() {
        String ip = Utils.getIpAddress(mContext);
        Log.d(TAG, "getIpAddress() " +ip);
        return ip;
    }

    @Override
    public void setIpAddress(String ip) {
        Log.d(TAG, "setIpAddress() " +ip);
        Utils.storeIpAddress(mContext, ip);
        IP = ip;
        SERVER_URL = SERVER_URL_PREFIX + IP + SERVER_URL_SUFFIX;

        // restart service
        disableAntiTheft();
        enableAntiTheft();
    }

    /**
     *  AntiTheft Frequency
     */
    @Override
    public int getAtFrequency() {
        Integer fr = Utils.getAtFrequency(mContext);
        Log.d(TAG, "getAtFrequency() " + fr);
        return fr;
    }

    @Override
    public void setAtFrequency(int frequency) {
        Log.d(TAG, "setAtFrequency() " + frequency);
        Utils.storeAtFrequency(mContext, frequency);

        ANTI_THEFT_CHECK_FREQUENCY.set(frequency * 1000);
        // Restart service
        disableAntiTheft();
        enableAntiTheft();
    }

    @Override
    public void registerDevice(final String name, final String pass) {
        Log.d(TAG, "registerDevice() " + name + ", " + pass);
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

                Intent intent = new Intent(DEVICE_REGISTERED);
                intent.putExtra(DEVICE_REGISTERED_EXTRA_KEY, id);
                mContext.sendBroadcast(intent);
            }
        }).execute(name, pass);
    }
}
