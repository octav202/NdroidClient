package com.ndroid.ndroidclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.ndroid.ndroidclient.server.AddDeviceTask;

public class MainActivity extends AppCompatActivity {

    private Switch mEnabledSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEnabledSwitch = (Switch) findViewById(R.id.enabled);
        mEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    int id = Utils.getDeviceId(getApplicationContext());
                    if (id == 0) {
                        // Register Device
                        showRegisterDialog();
                    } else {
                        Toast.makeText(getApplicationContext(), "Id :" + id, Toast.LENGTH_SHORT).show();
                        startService(new Intent(getApplicationContext(), AntiTheftService.class));
                    }
                } else {
                    stopService(new Intent(getApplicationContext(), AntiTheftService.class));
                }
            }
        });
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

    private void registerDevice(String name, String pass) {
        new AddDeviceTask(new AddDeviceTask.AddDeviceCallback() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onFinished(int id) {
                Toast.makeText(getApplicationContext(), "Registered Id :" + id, Toast.LENGTH_SHORT).show();
                Utils.storeDeviceId(getApplicationContext(), id);
                startService(new Intent(getApplicationContext(), AntiTheftService.class));
            }
        }).execute(name, pass);
    }
}

