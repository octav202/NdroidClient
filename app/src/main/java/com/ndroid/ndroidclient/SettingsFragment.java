package com.ndroid.ndroidclient;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import static com.ndroid.ndroidclient.Constants.DEVICE_REGISTERED;
import static com.ndroid.ndroidclient.Constants.DEVICE_REGISTERED_EXTRA_KEY;


public class SettingsFragment extends PreferenceFragment{
    private static final String TAG = "AT_SettingsFragment";

    public static final String KEY_STATUS = "status";
    public static final String KEY_IP = "ip";
    public static final String KEY_CHECK_FREQUENCY = "frequency";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_DEVICE_PASS = "device_pass";
    public static final String KEY_RESET = "reset";

    SwitchPreference mStatusPreference;
    EditTextPreference mIpAddressPreference;
    EditTextPreference mFrequencyPreference;
    EditTextPreference mDeviceIdPreference;
    EditTextPreference mDeviceNamePreference;
    EditTextPreference mDevicePassPreference;
    Preference mResetPreference;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DEVICE_REGISTERED)) {
                Integer id = intent.getIntExtra(DEVICE_REGISTERED_EXTRA_KEY, 0);
                Log.d(TAG, "onReceive() " + DEVICE_REGISTERED + ", id : " + id);
                if (id == 0) {
                    mStatusPreference.setChecked(false);
                    mStatusPreference.setSummary(R.string.disabled);
                    Toast.makeText(context, "Registration failed.", Toast.LENGTH_SHORT).show();
                } else {
                    mStatusPreference.setSummary(R.string.enabled);
                    mDeviceIdPreference.setSummary(id.toString());
                    String name = AntiTheftManager.getInstance(getActivity().getApplicationContext()).getDeviceName();
                    mDeviceNamePreference.setSummary(name);
                    String pass = AntiTheftManager.getInstance(getActivity().getApplicationContext()).getDevicePass();
                    mDevicePassPreference.setSummary(pass);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.antitheft_settings);

        mStatusPreference = (SwitchPreference) findPreference(KEY_STATUS);
        mIpAddressPreference = (EditTextPreference) findPreference(KEY_IP);
        mFrequencyPreference = (EditTextPreference) findPreference(KEY_CHECK_FREQUENCY);
        mDeviceIdPreference = (EditTextPreference) findPreference(KEY_DEVICE_ID);
        mDeviceNamePreference = (EditTextPreference) findPreference(KEY_DEVICE_NAME);
        mDevicePassPreference = (EditTextPreference) findPreference(KEY_DEVICE_PASS);
        mResetPreference = (Preference) findPreference(KEY_RESET);
        setPreferenceValues();
        setListeners();
        registerReceiver();

    }

    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DEVICE_REGISTERED);
        getActivity().getApplicationContext().registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver() {
        try {
            getActivity().getApplicationContext().unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException exc) {
            Log.e(TAG, "Unregister Receiver Failed");
        }
    }

    private void setPreferenceValues() {
        boolean status = AntiTheftManager.getInstance(getActivity().getApplicationContext()).getAntiTheftStatus();
        mStatusPreference.setChecked(status);
        if (status) {
            mStatusPreference.setSummary(R.string.enabled);
        } else {
            mStatusPreference.setSummary(R.string.disabled);
        }

        String ip = AntiTheftManager.getInstance(getActivity().getApplicationContext()).getIpAddress();
        mIpAddressPreference.setSummary(ip);

        Integer freq = AntiTheftManager.getInstance(getActivity().getApplicationContext()).getAtFrequency();
        mFrequencyPreference.setSummary(freq.toString());

        Integer id = AntiTheftManager.getInstance(getActivity().getApplicationContext()).getDeviceId();
        mDeviceIdPreference.setSummary(id.toString());

        String name = AntiTheftManager.getInstance(getActivity().getApplicationContext()).getDeviceName();
        mDeviceNamePreference.setSummary(name);

        String pass = AntiTheftManager.getInstance(getActivity().getApplicationContext()).getDevicePass();
        mDevicePassPreference.setSummary(pass);
    }

    private void setListeners() {
        mStatusPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof SwitchPreference) {
                    boolean checked = (Boolean) newValue;

                    AntiTheftManager manager = AntiTheftManager.getInstance(getActivity().getApplicationContext());

                    if (checked) {
                        if (manager.getIpAddress() == null || manager.getIpAddress().isEmpty()) {
                            Toast.makeText(getActivity(), "Please set an IP Address.", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        if (manager.getAtFrequency() == 0) {
                            Toast.makeText(getActivity(), "Please set a frequency.", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        int id = manager.getDeviceId();
                        if (id == 0) {
                            // Register Device
                            showRegisterDialog();
                        } else {
                            manager.setAntiTheftStatus(true);
                            mStatusPreference.setSummary(R.string.enabled);
                        }
                    } else {
                        showDisableDialog();
                    }
                }
                return true;
            }
        });

        mIpAddressPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof EditTextPreference) {
                    String text = (String) newValue;
                    preference.setSummary(text);
                    AntiTheftManager.getInstance(getActivity().getApplicationContext()).setIpAddress(text);
                }
                return true;
            }
        });

        mFrequencyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof EditTextPreference) {
                    String text = (String) newValue;
                    preference.setSummary(text);
                    Integer frequency = Integer.parseInt(text);
                    AntiTheftManager.getInstance(getActivity().getApplicationContext()).setAtFrequency(frequency);
                }
                return true;
            }
        });

        mResetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getApplicationContext().getResources().
                        getString(R.string.at_key_reset_title));
                builder.setMessage(getActivity().getApplicationContext().getResources().
                        getString(R.string.at_key_reset_warning));

                LinearLayout layout= new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText passInput = new EditText(getActivity());
                passInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passInput.setHint(R.string.pass);
                layout.addView(passInput);
                builder.setView(layout);

                // Ok Button
                builder.setPositiveButton(getActivity().getApplicationContext().getResources().
                        getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AntiTheftManager manager = AntiTheftManager.getInstance(getActivity().getApplicationContext());
                        if (passInput.getText().toString().equals(manager.getDevicePass())) {
                            manager.resetSettings();
                            setPreferenceValues();
                        } else {
                            Toast.makeText(getActivity(), "Invalid Password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Cancel Button
                builder.setNegativeButton(getActivity().getApplicationContext().getResources().
                        getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();

                return true;
            }
        });


        mDeviceIdPreference.setEnabled(false);
        mDeviceNamePreference.setEnabled(false);
        mDevicePassPreference.setEnabled(false);
    }


    /**
     * Request user to register device.
     */
    private void showRegisterDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        LinearLayout layout= new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText nameInput = new EditText(getActivity());
        nameInput.setHint(R.string.device_name);
        final EditText passInput = new EditText(getActivity());
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
                AntiTheftManager.getInstance(getActivity()).registerDevice(name, pass);
            }
        });
        alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mStatusPreference.setChecked(false);
                        mStatusPreference.setSummary(R.string.disabled);
                        dialog.cancel();
                    }
                });
        alert.create().show();
    }

    /**
     * Authenticate when disabling AntiTheft.
     */
    private void showDisableDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        LinearLayout layout= new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText passInput = new EditText(getActivity());
        passInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passInput.setHint(R.string.pass);
        layout.addView(passInput);
        alert.setView(layout);

        alert.setTitle(R.string.register);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                AntiTheftManager manager = AntiTheftManager.getInstance(getActivity());

                String pass = passInput.getText().toString().trim();
                if (pass.equals(manager.getDevicePass())) {
                    manager.setAntiTheftStatus(false);
                    mStatusPreference.setChecked(false);
                    mStatusPreference.setSummary(R.string.disabled);
                } else {
                    mStatusPreference.setChecked(true);
                    mStatusPreference.setSummary(R.string.enabled);
                    Toast.makeText(getActivity(), "Invalid Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alert.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                        mStatusPreference.setChecked(true);
                        mStatusPreference.setSummary(R.string.enabled);
                    }
                });
        alert.create().show();
    }

}
