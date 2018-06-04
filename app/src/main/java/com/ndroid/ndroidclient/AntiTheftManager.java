package com.ndroid.ndroidclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


public class AntiTheftManager {

    public static final String TAG = "AT_Manager";
    private static AntiTheftManager sInstance;
    private static Context mContext;

    IAntiTheftService mService;
    private String action = "ANTI_THEFT_SERVICE_ACTION";
    private String packageName = "com.ndroid.ndroidclient";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = IAntiTheftService.Stub.asInterface(iBinder);
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public static AntiTheftManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AntiTheftManager(context);
        }

        return sInstance;
    }

    private AntiTheftManager(Context context) {
        mContext = context;

        Log.d(TAG, "Binding to service..");
        Intent serviceIntent = new Intent(action);
        serviceIntent.setPackage(packageName);
        context.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public Boolean isServiceAvailable() {
        return mService != null;
    }

    /**
     * Public Methods
     */

    // Device Id
    public int getDeviceId() {
        Log.d(TAG, "getDeviceId()");
        if (mService != null) {
            try {
                return mService.getDeviceId();
            } catch (RemoteException e) {
                Log.e(TAG, "getDeviceId() Exception " + e.getLocalizedMessage());
                return 0;
            }
        } else {
            Log.e(TAG, "Service is null");
            return 0;
        }
    }

    public void setDeviceId(int id) {
        Log.d(TAG, "setDeviceId() " + id);
        if (mService != null) {
            try {
                mService.setDeviceId(id);
            } catch (RemoteException e) {
                Log.e(TAG, "setDeviceId() Exception " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }

    // Device Name
    public String getDeviceName() {
        Log.d(TAG, "getDeviceName() ");
        if (mService != null) {
            try {
                return mService.getDeviceName();
            } catch (RemoteException e) {
                Log.e(TAG, "getDeviceName() Exception " + e.getLocalizedMessage());
                return null;
            }
        } else {
            Log.e(TAG, "Service is null");
            return null;
        }
    }

    public void setDeviceName(String name) {
        Log.d(TAG, "setDeviceName() " + name);
        if (mService != null) {
            try {
                mService.setDeviceName(name);
            } catch (RemoteException e) {
                Log.e(TAG, "setDeviceName() Exception " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }

    // Device Pass
    public String getDevicePass() {
        Log.d(TAG, "getDevicePass() ");
        if (mService != null) {
            try {
                return mService.getDevicePass();
            } catch (RemoteException e) {
                Log.e(TAG, "getDevicePass() Exception " + e.getLocalizedMessage());
                return null;
            }
        } else {
            Log.e(TAG, "Service is null");
            return null;
        }
    }

    public void setDevicePass(String pass) {
        Log.d(TAG, "setDevicePass() " + pass);
        if (mService != null) {
            try {
                mService.setDevicePass(pass);
            } catch (RemoteException e) {
                Log.e(TAG, "setDevicePass() Exception " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }

    // AntiTheft Status
    public boolean getAntiTheftStatus() {
        Log.d(TAG, "getAntiTheftStatus() ");
        if (mService != null) {
            try {
                return mService.getAntiTheftStatus();
            } catch (RemoteException e) {
                Log.e(TAG, "getAntiTheftStatus() Exception " + e.getLocalizedMessage());
                return false;
            }
        } else {
            Log.e(TAG, "Service is null");
            return false;
        }
    }

    public void setAntiTheftStatus(boolean status) {
        Log.d(TAG, "setAntiTheftStatus() " + status);
        if (mService != null) {
            try {
                mService.setAntiTheftStatus(status);
            } catch (RemoteException e) {
                Log.e(TAG, "setAntiTheftStatus() Exception " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }

    // IP Address
    public String getIpAddress() {
        Log.d(TAG, "getIpAddress() ");
        if (mService != null) {
            try {
                return mService.getIpAddress();
            } catch (RemoteException e) {
                Log.e(TAG, "getIpAddress() Exception " + e.getLocalizedMessage());
                return null;
            }
        } else {
            Log.e(TAG, "Service is null");
            return null;
        }
    }

    public void setIpAddress(String ip) {
        Log.d(TAG, "getIpAddress() " + ip);
        if (mService != null) {
            try {
                mService.setIpAddress(ip);
            } catch (RemoteException e) {
                Log.e(TAG, "setIpAddress() Exception " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }

    // AntiTheft Frequency
    public int getAtFrequency() {
        Log.d(TAG, "getAtFrequency() ");
        if (mService != null) {
            try {
                return mService.getAtFrequency();
            } catch (RemoteException e) {
                Log.e(TAG, "getAtFrequency() Exception " + e.getLocalizedMessage());
                return 0;
            }
        } else {
            Log.e(TAG, "Service is null");
            return 0;
        }
    }

    public void setAtFrequency(int frequency) {
        Log.d(TAG, "setAtFrequency() " + frequency);
        if (mService != null) {
            try {
                mService.setAtFrequency(frequency);
            } catch (RemoteException e) {
                Log.e(TAG, "setAtFrequency() Exception " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }

    public void registerDevice(String name, String pass) {
        Log.d(TAG, "registerDevice() " + name + ", " + pass);
        if (mService != null) {
            try {
                mService.registerDevice(name, pass);
            } catch (RemoteException e) {
                Log.e(TAG, "registerDevice() Exception " + e.getLocalizedMessage());
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }

    public void resetSettings() {
        setAntiTheftStatus(false);
        setAtFrequency(0);
        setDeviceId(0);
        setDeviceName("");
        setDevicePass("");
        setIpAddress("");
    }
}
