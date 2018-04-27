package com.ndroid.ndroidclient;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.ndroid.ndroidclient.Constants.TAG;

public class PermissionActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_layout);
        Log.d("AT_", "Activity Created");

        Log.d(TAG, "requestAdminPermissions");
        ComponentName receiver = new ComponentName(getApplicationContext(), AntiTheftService.AdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,receiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "EXTRA ADMIN REQUEST EXPLANATION");
        startActivity(intent);

        finish();
    }
}
