package com.getmedcheck.lib.events;

import android.bluetooth.BluetoothGatt;

public class EventBleConnectionState {
    private BluetoothGatt gatt;
    private int status;
    private int newState;

    public EventBleConnectionState() {
    }

    public EventBleConnectionState(BluetoothGatt gatt, int status, int newState) {
        this.gatt = gatt;
        this.status = status;
        this.newState = newState;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNewState() {
        return newState;
    }

    public void setNewState(int newState) {
        this.newState = newState;
    }

    @Override
    public String toString() {
        return "EventBleConnectionState{" +
                "gatt=" + gatt +
                ", status=" + status +
                ", newState=" + newState +
                '}';
    }
}
