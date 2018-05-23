package com.ndroid.ndroidclient.server;


import android.os.AsyncTask;

import com.ndroid.ndroidclient.models.DeviceAlert;

public class GetDeviceAlertTask extends AsyncTask<Integer, Void, DeviceAlert> {

    private GetDeviceAlertCallback mCallback;

    public GetDeviceAlertTask(GetDeviceAlertCallback callback) {
        mCallback = callback;
    }

    @Override
    protected DeviceAlert doInBackground(Integer... deviceId) {
        return ServerApi.getDeviceAlert(deviceId[0]);
    }

    @Override
    protected void onPreExecute() {
        mCallback.onStarted();
    }

    @Override
    protected void onPostExecute(DeviceAlert status) {
        mCallback.onFinished(status);
    }

    public interface GetDeviceAlertCallback {
        void onStarted();

        void onFinished(DeviceAlert alert);
    }
}

