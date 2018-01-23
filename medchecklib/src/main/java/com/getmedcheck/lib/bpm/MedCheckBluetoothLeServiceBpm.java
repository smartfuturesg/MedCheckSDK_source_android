package com.getmedcheck.lib.bpm;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.getmedcheck.lib.model.BloodPressureData;
import com.getmedcheck.lib.services.MedCheckBluetoothService;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class MedCheckBluetoothLeServiceBpm extends MedCheckBluetoothService<BloodPressureData> {

    @Override
    protected void onStartNewReading(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    protected void onCommandResultReceive(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    protected void executeCommand(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {

    }

    @Override
    protected void onStartConnecting() {

    }

    @Override
    protected void onConnectDevice(BluetoothGatt gatt) {

    }

    @Override
    protected void onDisconnectDevice(BluetoothGatt gatt) {

    }

    @Override
    protected void onBluetoothOff() {

    }

    @Override
    protected void onBleScanResult(int callbackType, ScanResult result) {

    }

    @Override
    protected void onError(int errorCode, Exception e) {

    }
}
