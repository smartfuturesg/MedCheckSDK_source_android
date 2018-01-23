package com.getmedcheck.lib.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import com.getmedcheck.lib.constant.Constants;

public class BleDevice implements Parcelable {

    private BluetoothDevice device;
    private String displayName = "";
    private String deviceName = "";
    private String macAddress = "";
    private String status = Constants.BLE_STATUS_NONE;

    public BleDevice() {
    }

    public BleDevice(BluetoothDevice device) {
        this.device = device;
        deviceName = device.getName();
        macAddress = device.getAddress();
    }

    public BleDevice(BluetoothDevice device, String status) {
        this.device = device;
        deviceName = device.getName();
        macAddress = device.getAddress();
        this.status = status;
    }

    protected BleDevice(Parcel in) {
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        displayName = in.readString();
        deviceName = in.readString();
        macAddress = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(device, flags);
        dest.writeString(displayName);
        dest.writeString(deviceName);
        dest.writeString(macAddress);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel in) {
            return new BleDevice(in);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
        deviceName = device.getName();
        macAddress = device.getAddress();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice bleDevice = (BleDevice) o;

        if (device != null ? !device.equals(bleDevice.device) : bleDevice.device != null)
            return false;
        if (displayName != null ? !displayName.equals(bleDevice.displayName) : bleDevice.displayName != null)
            return false;
        if (deviceName != null ? !deviceName.equals(bleDevice.deviceName) : bleDevice.deviceName != null)
            return false;
        if (macAddress != null ? !macAddress.equals(bleDevice.macAddress) : bleDevice.macAddress != null)
            return false;
        return status != null ? status.equals(bleDevice.status) : bleDevice.status == null;
    }

    @Override
    public int hashCode() {
        int result = device != null ? device.hashCode() : 0;
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (macAddress != null ? macAddress.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BleDevice{" +
                "device=" + device +
                ", displayName='" + displayName + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
