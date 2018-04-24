package com.ndroid.ndroidclient.server;


import android.os.AsyncTask;

import com.ndroid.ndroidclient.models.DeviceStatus;

public class SendDeviceStatusTask extends AsyncTask<DeviceStatus, Void, Boolean> {

    private SendDeviceStatusCallback mCallback;

    public SendDeviceStatusTask(SendDeviceStatusCallback callback) {
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(DeviceStatus... status) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Authenticate
        return ServerApi.sendDeviceStatus(status[0]);
    }

    @Override
    protected void onPreExecute() {
        mCallback.onStarted();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mCallback.onFinished(result);
    }

    public interface SendDeviceStatusCallback {
        void onStarted();

        void onFinished(Boolean result);
    }


}

