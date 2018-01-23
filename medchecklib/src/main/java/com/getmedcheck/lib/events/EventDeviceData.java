package com.getmedcheck.lib.events;

import android.bluetooth.BluetoothDevice;

import com.getmedcheck.lib.model.IDeviceData;

import java.util.ArrayList;

public class EventDeviceData {

    private BluetoothDevice bluetoothDevice;
    private ArrayList<IDeviceData> modelBloodTestData = new ArrayList<>();
    private String type = "";

    public EventDeviceData() {
    }

    public EventDeviceData(BluetoothDevice bluetoothDevice, ArrayList<IDeviceData> modelBloodTestData, String type) {
        this.bluetoothDevice = bluetoothDevice;
        this.modelBloodTestData = modelBloodTestData;
        this.type = type;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public ArrayList<IDeviceData> getModelBloodTestData() {
        return modelBloodTestData;
    }

    public void setModelBloodTestData(ArrayList<IDeviceData> modelBloodTestData) {
        this.modelBloodTestData = modelBloodTestData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
