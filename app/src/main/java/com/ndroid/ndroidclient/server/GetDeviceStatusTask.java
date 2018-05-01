package com.ndroid.ndroidclient.server;


import android.os.AsyncTask;

import com.ndroid.ndroidclient.models.DeviceStatus;

public class GetDeviceStatusTask extends AsyncTask<Integer, Void, DeviceStatus> {

    private GetDeviceStatusCallback mCallback;

    public GetDeviceStatusTask(GetDeviceStatusCallback callback) {
        mCallback = callback;
    }

    @Override
    protected DeviceStatus doInBackground(Integer... deviceId) {
        return ServerApi.getDeviceStatus(deviceId[0]);
    }

    @Override
    protected void onPreExecute() {
        mCallback.onStarted();
    }

    @Override
    protected void onPostExecute(DeviceStatus status) {
        mCallback.onFinished(status);
    }

    public interface GetDeviceStatusCallback {
        void onStarted();

        void onFinished(DeviceStatus status);
    }
}

