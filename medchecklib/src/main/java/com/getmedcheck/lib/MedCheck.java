package com.getmedcheck.lib;

import android.content.Context;

import com.getmedcheck.lib.constant.Constants;
import com.getmedcheck.lib.events.EventBleScanResult;
import com.getmedcheck.lib.events.EventClearCommand;
import com.getmedcheck.lib.events.EventDeviceConnectionStatus;
import com.getmedcheck.lib.events.EventDeviceData;
import com.getmedcheck.lib.events.EventReadingProgress;
import com.getmedcheck.lib.events.EventTimeSyncCommand;
import com.getmedcheck.lib.listener.MedCheckCallback;
import com.getmedcheck.lib.services.MedCheckBluetoothLeService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MedCheck {

    private static MedCheck instance;
    private MedCheckCallback mCallback;

    public static synchronized MedCheck getInstance() {
        if (instance == null) {
            instance = new MedCheck();
        }
        return instance;
    }

    public void startScan(Context context) {
        MedCheckBluetoothLeService.startBluetoothLeScan(context.getApplicationContext());
    }

    public void stopScan(Context context) {
        MedCheckBluetoothLeService.stopBluetoothLeScan(context.getApplicationContext());
    }

    public void disconnectDevice(Context context) {
        MedCheckBluetoothLeService.disconnectBluetoothLeDevice(context.getApplicationContext());
    }

    public void connect(Context context, String mac) {
        MedCheckBluetoothLeService.connectDeviceUsingMacAddress(context.getApplicationContext(), mac);
    }

    public void removeOnConnectionStateChangeListener() {
        mCallback = null;
    }

    public void clearDevice(Context context, String mac) {
        MedCheckBluetoothLeService.clearDeviceUsingMacAddress(context.getApplicationContext(), mac);
    }

    public void timeSyncDevice(Context context, String mac) {
        MedCheckBluetoothLeService.timeSyncUsingMacAddress(context.getApplicationContext(), mac);
    }

    public void writeCommand(Context context, String mac) {
        MedCheckBluetoothLeService.writeCharacteristicsUsingMac(context.getApplicationContext(), mac);
    }

    public void registerCallBack(MedCheckCallback callback) {
        mCallback = callback;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void unregisterCallBack(Context context) {
        mCallback = null;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        MedCheckBluetoothLeService.stopBluetoothLeService(context.getApplicationContext());
    }

    /************************** Callback *************************/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnConnectionStatusChange(EventDeviceConnectionStatus deviceConnectionStatus) {
        if (mCallback != null) {
            mCallback.onConnectionStateChange(deviceConnectionStatus.getBleDevice(), deviceConnectionStatus.getStatus());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnDeviceScanResult(EventBleScanResult eventBleScanResult) {
        if (mCallback != null) {
            mCallback.onScanResult(eventBleScanResult.getResult());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExecuteClearCommand(EventClearCommand clearCommand) {
        if (mCallback != null) {
            mCallback.onClearCommand(clearCommand.getStatus());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExecuteTimeSyncCommand(EventTimeSyncCommand timeSyncCommand) {
        if (mCallback != null) {
            mCallback.onTimeSyncCommand(timeSyncCommand.getStatus());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataReadingStateChange(EventReadingProgress readingProgress) {
        if (mCallback != null) {
            mCallback.onDataReadingStateChange(readingProgress.getStatus(), readingProgress.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataReceive(EventDeviceData eventDeviceData) {
        if (mCallback != null) {
            mCallback.onDataReceive(eventDeviceData.getBluetoothDevice(), eventDeviceData.getModelBloodTestData(), eventDeviceData.getType());
        }
    }
}
