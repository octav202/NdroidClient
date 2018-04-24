package com.ndroid.ndroidclient.models;

public class Device {

	private int deviceId;
	private String deviceName;
	private String devicePass;

	public Device() {
		this.deviceId = 0;
		this.deviceName = "";
		this.devicePass = "";
	}

	public Device(int deviceId, String deviceName, String devicePass) {
		this.deviceId = deviceId;
		this.deviceName = deviceName;
		this.devicePass = devicePass;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDevicePass() {
		return devicePass;
	}

	public void setDevicePass(String devicePass) {
		this.devicePass = devicePass;
	}

	@Override
	public String toString() {
		return "Device [deviceId=" + deviceId + ", deviceName=" + deviceName + ", devicePass=" + devicePass + "]";
	}

	
}
