package com.ndroid.ndroidclient;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.ndroid.ndroidclient.server.AddDeviceTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = Constants.TAG + MainActivity.class.getSimpleName();
    private Switch mEnabledSwitch;
    private EditText mIpText;
    private EditText mFrequencyText;
    private EditText mDeviceName;
    private EditText mDevicePass;
    private EditText mDeviceId;

    private AntiTheftService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AntiTheftService.LocalBinder binder = (AntiTheftService.LocalBinder) service;
            mService = binder.getService();
            if (mService != null) {
                mIpText.setText(mService.getIpAddress());
                mFrequencyText.setText(String.valueOf(mService.getAtFrequency()));

                // Update Device info
                int id = mService.getDeviceId();
                if (id != 0) {
                    mDeviceId.setText(String.valueOf(id));
                    mDeviceName.setText(mService.getDeviceName());
                    mDevicePass.setText(mService.getDevicePass());
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEnabledSwitch = (Switch) findViewById(R.id.enabled);
        mIpText = (EditText) findViewById(R.id.ip_text);
        mIpText.setSingleLine();
        mFrequencyText = (EditText) findViewById(R.id.at_frequency);
        mFrequencyText.setSingleLine();
        mFrequencyText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mDeviceName = (EditText) findViewById(R.id.device_name);
        mDevicePass = (EditText) findViewById(R.id.pass);
        mDeviceId = (EditText) findViewById(R.id.device_id);

        mEnabledSwitch.setChecked(Utils.getAntiTheftStatus(getApplicationContext()));
        mEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mService == null) return;

                if (isChecked) {
                    int id = mService.getDeviceId();
                    if (id == 0) {
                        // Register Device
                        showRegisterDialog();
                    } else {
                        mService.setAntiTheftStatus(true);
                    }
                } else {
                    mService.setAntiTheftStatus(false);
                }
            }
        });

        mIpText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mService != null) {
                        mService.setIpAddress(v.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });

        mFrequencyText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mService != null) {
                        mService.setAtFrequency(Integer.parseInt(v.getText().toString()));
                    }
                    return true;
                }
                return false;
            }
        });

        // Bind to service
        Intent intent = new Intent(getApplicationContext(), AntiTheftService.class);
        if (!bindService(intent, mConnection, BIND_AUTO_CREATE)){
            Log.e(TAG, "Bind to service failed");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    /**
     * Request user to register device.
     */
    private void showRegisterDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LinearLayout layout= new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText nameInput = new EditText(this);
        nameInput.setHint(R.string.device_name);
        final EditText passInput = new EditText(this);
        passInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passInput.setHint(R.string.pass);
        layout.addView(nameInput);
        layout.addView(passInput);
        alert.setView(layout);

        alert.setTitle(R.string.register);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = nameInput.getText().toString().trim();
                String pass = passInput.getText().toString().trim();
                registerDevice(name, pass);
            }
        });
        alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        alert.create().show();
    }

    /**
     * Register Device on server
     * @param name
     * @param pass
     */
    private void registerDevice(final String name, final String pass) {

        if (mService != null) {
            mService.registerDevice(name, pass, new AddDeviceTask.AddDeviceCallback() {
                @Override
                public void onStarted() {
                }

                @Override
                public void onFinished(int id) {
                    if (id == 0) {
                        Log.e(TAG, "Registration failed!");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mEnabledSwitch.setChecked(false);
                            }
                        });
                        return;
                    }

                    mDeviceId.setText(String.valueOf(id));
                    mDeviceName.setText(String.valueOf(name));
                    mDevicePass.setText(String.valueOf(pass));
                }
            });
        }

    }
}

