package com.getmedcheck.lib.data;

import android.bluetooth.BluetoothGatt;

import com.getmedcheck.lib.events.EventDeviceConnectionStatus;
import com.getmedcheck.lib.model.BleDevice;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

public class AppData {

    private static final AppData ourInstance = new AppData();
    private HashMap<String, Integer> bleReadingValue = new HashMap<>();
    private boolean isLiveReading;
    private HashMap<String, Boolean> mapNewReadingShown = new HashMap<>();
    private HashMap<String, Integer> readingStartIndexMap = new HashMap<>();
    private ArrayList<BleDevice> connectedDeviceList = new ArrayList<>();
    private ArrayList<BluetoothGatt> connectedDeviceGhatt = new ArrayList<>();

    private AppData() {
    }

    public static AppData getInstance() {
        return ourInstance;
    }

    public void setBleReadingValue(String deviceType, int value) {
        bleReadingValue.put(deviceType, value);
    }

    public int getBleReadingValue(String deviceType) {
        if (bleReadingValue.containsKey(deviceType)) {
            return bleReadingValue.get(deviceType);
        }
        return 0;
    }

    public boolean isLiveReading() {
        return isLiveReading;
    }

    public void setLiveReading(boolean liveReading) {
        isLiveReading = liveReading;
    }

    public void setNewReadingShown(String deviceName) {
        mapNewReadingShown.put(deviceName, true);
    }

    public boolean getNewReadingShown(String deviceName) {
        if (mapNewReadingShown.containsKey(deviceName)) {
            return mapNewReadingShown.get(deviceName);
        }
        return false;
    }

    public int getReadingStartIndex(String macAddress) {
        return readingStartIndexMap.containsKey(macAddress) ? readingStartIndexMap.get(macAddress) : -1;
    }

    public void setReadingStartIndex(String macAddress, int index) {
        this.readingStartIndexMap.put(macAddress, index);
    }

    public void addConnectedDevice(BleDevice bleDevice) {
        if (!connectedDeviceList.contains(bleDevice)) {
            connectedDeviceList.add(bleDevice);
            EventBus.getDefault().post(new EventDeviceConnectionStatus(bleDevice, EventDeviceConnectionStatus.CONNECT));
        }
    }

    public void addConnectedGhatt(BluetoothGatt bleBluetoothGatt) {
        if (!connectedDeviceGhatt.contains(bleBluetoothGatt)) {
            connectedDeviceGhatt.add(bleBluetoothGatt);
        }
    }

    public void removeConnectedGhatt(BluetoothGatt bleBluetoothGatt) {
        if (connectedDeviceGhatt.contains(bleBluetoothGatt)) {
            connectedDeviceGhatt.remove(bleBluetoothGatt);
        }
    }

    public void clearConnectedGhattList() {
        if (connectedDeviceGhatt != null && connectedDeviceGhatt.size()>0) {
            connectedDeviceGhatt.clear();
        }
    }

    public ArrayList<BluetoothGatt> getConnectedDeviceGhatt() {
        return connectedDeviceGhatt;
    }

    public void setConnectedDeviceGhatt(ArrayList<BluetoothGatt> connectedDeviceGhatt) {
        this.connectedDeviceGhatt = connectedDeviceGhatt;
    }

    public ArrayList<BleDevice> getConnectedDevices() {
        return connectedDeviceList;
    }

    public void clearConnectedDevice() {
        connectedDeviceList.clear();
    }

    public boolean hasConnectedDevice() {
        return connectedDeviceList.size() > 0;
    }

    public void removeConnectedDevice(BleDevice bleDevice) {
        if (connectedDeviceList.remove(bleDevice)) {
            EventBus.getDefault().post(new EventDeviceConnectionStatus(bleDevice, EventDeviceConnectionStatus.DISCONNECT));
        }
    }

    public boolean containInConnectedDevice(BleDevice bleDevice) {
        return connectedDeviceList.contains(bleDevice);
    }
}
