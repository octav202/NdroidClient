package com.ndroid.ndroidclient.server;

import android.os.AsyncTask;


public class AddDeviceTask extends AsyncTask<String, Void, Integer> {

    private AddDeviceCallback mCallback;

    public AddDeviceTask(AddDeviceCallback callback) {
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        String name = strings[0];
        String pass = strings[1];
        // Authenticate
        return ServerApi.addDevice(name, pass);
    }

    @Override
    protected void onPreExecute() {
        mCallback.onStarted();
    }

    @Override
    protected void onPostExecute(Integer result) {
        mCallback.onFinished(result);
    }

    public interface AddDeviceCallback {
        void onStarted();

        void onFinished(int id);
    }


}

