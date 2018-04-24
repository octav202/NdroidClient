package com.ndroid.ndroidclient.server;


import android.os.AsyncTask;

import com.ndroid.ndroidclient.models.DeviceLocation;

public class SendLocationTask extends AsyncTask<DeviceLocation, Void, Boolean> {

    private SendLocationCallback mCallback;

    public SendLocationTask(SendLocationCallback callback) {
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(DeviceLocation... deviceLocation) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Authenticate
        return ServerApi.sendLocation(deviceLocation[0]);
    }

    @Override
    protected void onPreExecute() {
        mCallback.onStarted();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mCallback.onFinished(result);
    }

    public interface SendLocationCallback {
        void onStarted();

        void onFinished(Boolean result);
    }


}

