package com.getmedcheck.lib.listener;

import android.bluetooth.BluetoothDevice;

import com.getmedcheck.lib.model.BleDevice;
import com.getmedcheck.lib.model.IDeviceData;

import java.util.ArrayList;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public abstract class MedCheckCallback {

    public void onDataReadingStateChange(int state, String message) {
    }

    public void onDataReceive(BluetoothDevice device, ArrayList<IDeviceData> deviceData, String deviceType) {
    }

    public void onClearCommand(int state) {
    }

    public void onTimeSyncCommand(int state) {
    }

    public void onScanResult(ScanResult scanResult) {
    }

    public void onConnectionStateChange(BleDevice bleDevice, int status) {
    }
}
