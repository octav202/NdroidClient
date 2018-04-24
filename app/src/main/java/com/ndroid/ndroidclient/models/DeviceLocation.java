package com.ndroid.ndroidclient.models;

public class DeviceLocation {

    private int deviceId;
    private double lat;
    private double lon;
    private String timeStamp;

    public DeviceLocation() {
        this.deviceId = 0;
        this.lat = 0;
        this.lon = 0;
        this.timeStamp = "";
    }

    public DeviceLocation(int deviceId, double lat, double lon, String timeStamp) {
        this.deviceId = deviceId;
        this.lat = lat;
        this.lon = lon;
        this.timeStamp = timeStamp;
    }
    public int getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    @Override
    public String toString() {
        return "DeviceLocation [deviceId=" + deviceId + ", lat=" + lat + ", lon=" + lon + ", timeStamp=" + timeStamp + "]";
    }

}
