package com.ndroid.ndroidclient;

interface IAntiTheftService {
	// Device Id
	int getDeviceId();
	void setDeviceId(int id);

    // Device Name
    String getDeviceName();
    void setDeviceName(String name);

    // Device Pass
    String getDevicePass();
    void setDevicePass(String pass);

    // AntiTheft Status
    boolean getAntiTheftStatus();
    void setAntiTheftStatus(boolean status);

    // IP Address
    String getIpAddress();
    void setIpAddress(String ip);

    // AntiTheft Frequency
    int getAtFrequency();
    void setAtFrequency(int frequency);

    // Register Device
    void registerDevice(String name, String pass);
}
