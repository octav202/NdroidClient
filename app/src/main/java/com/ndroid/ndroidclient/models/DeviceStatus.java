package com.ndroid.ndroidclient.models;

public class DeviceStatus {

    private int deviceId;
    private Integer lock;
    private Integer wipeData;
    private Integer encryptStorage;
    private Integer reboot;
    private Integer locationFrequency;
    private Integer ring;
    private Integer freeze;
    private Integer alert;
    private Integer triggered;

    public DeviceStatus() {
        this.deviceId = 0;
        this.lock = 0;
        this.wipeData = 0;
        this.encryptStorage = 0;
        this.reboot = 0;
        this.triggered = 0;
        this.ring = 0;
        this.freeze = 0;
        this.alert = 0;
        this.locationFrequency = 0;
    }

    public DeviceStatus(int deviceId, Integer lock, Integer wipeData, Integer encryptStorage,
                        Integer reboot, Integer frequency, Integer ring, Integer freeze, Integer alert, Integer triggered) {
        this.deviceId = deviceId;
        this.lock = lock;
        this.wipeData = wipeData;
        this.encryptStorage = encryptStorage;
        this.reboot = reboot;
        this.locationFrequency = frequency;
        this.ring = ring;
        this.freeze = freeze;
        this.alert = alert;
        this.triggered = triggered;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getLock() {
        return lock;
    }

    public void setLock(Integer lock) {
        this.lock = lock;
    }

    public Integer getWipeData() {
        return wipeData;
    }

    public void setWipeData(Integer wipeData) {
        this.wipeData = wipeData;
    }

    public Integer getEncryptStorage() {
        return encryptStorage;
    }

    public void setEncryptStorage(Integer encryptStorage) {
        this.encryptStorage = encryptStorage;
    }

    public Integer getReboot() {
        return reboot;
    }

    public void setReboot(Integer reboot) {
        this.reboot = reboot;
    }

    public Integer getLocationFrequency() {
        return locationFrequency;
    }

    public void setLocationFrequency(Integer freq) {
        this.locationFrequency = freq;
    }

    public Integer getTriggered() {
        return triggered;
    }

    public void setTriggered(Integer triggered) {
        this.triggered = triggered;
    }

    public Integer getRing() {
        return ring;
    }

    public void setRing(Integer ring) {
        this.ring = ring;
    }

    public Integer getFreeze() {
        return freeze;
    }

    public void setFreeze(Integer freeze) {
        this.freeze = freeze;
    }

    public Integer getAlert() {
        return alert;
    }

    public void setAlert(Integer alert) {
        this.alert = alert;
    }

    @Override
    public String toString() {
        return "[ deviceId - " + deviceId + " ]\n" +
                "[ lock - " + lock + " ]\n" +
                "[ wipeData - " + wipeData + " ]\n" +
                "[ encryptStorage - " + encryptStorage + " ]\n" +
                "[ reboot - " + reboot + " ]\n" +
                "[ locationFrequency - " + locationFrequency + "\n" +
                "[ ring - " + ring + " ]\n" +
                "[ freeze - " + freeze + " ]\n" +
                "[ alert - " + alert + " ]\n" +
                "[ triggered - " + triggered + " ]";
    }
}

