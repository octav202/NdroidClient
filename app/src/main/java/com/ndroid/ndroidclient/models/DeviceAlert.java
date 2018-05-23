package com.ndroid.ndroidclient.models;

public class DeviceAlert {

    private int deviceId;
    private String phone;
    private String email;
    private String description;

    public DeviceAlert() {
        this.deviceId = 0;
        this.phone = "";
        this.email = "";
        this.description = "";
    }

    public DeviceAlert(int deviceId, String phone, String email, String description) {
        this.deviceId = deviceId;
        this.phone = phone;
        this.email = email;
        this.description = description;
    }


    public int getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "DeviceAlert [deviceId=" + deviceId + ", phone=" + phone + ", email=" + email + ", description="
                + description + "]";
    }


}
