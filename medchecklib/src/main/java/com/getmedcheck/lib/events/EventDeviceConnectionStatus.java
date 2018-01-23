package com.getmedcheck.lib.events;

import com.getmedcheck.lib.model.BleDevice;

public class EventDeviceConnectionStatus {
    public static final int NONE = 0;
    public static final int CONNECT = 1;
    public static final int DISCONNECT = 2;
    private BleDevice bleDevice;
    private int status = NONE;

    public EventDeviceConnectionStatus() {
    }

    public EventDeviceConnectionStatus(BleDevice bleDevice, int status) {
        this.bleDevice = bleDevice;
        this.status = status;
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
