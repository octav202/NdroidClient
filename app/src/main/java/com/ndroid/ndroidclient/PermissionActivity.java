package com.ndroid.ndroidclient;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import static com.ndroid.ndroidclient.Constants.DEVICE_ADMIN;
import static com.ndroid.ndroidclient.Constants.LOCATION;
import static com.ndroid.ndroidclient.Constants.TAG;

public class PermissionActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_layout);
        Log.d("AT_", "Activity Created");

        Intent i = getIntent();
        if (i != null) {
            // Request Admin Permissions
            if (i.getBooleanExtra(DEVICE_ADMIN, false)) {
                Log.d(TAG, "requestAdminPermissions");
                ComponentName receiver = new ComponentName(getApplicationContext(), AdminReceiver.class);
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, receiver);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "EXTRA ADMIN REQUEST EXPLANATION");
                startActivity(intent);
            }

            // Request Location Permission
            if (i.getBooleanExtra(LOCATION, false)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            }
        }

        finish();
    }
}
