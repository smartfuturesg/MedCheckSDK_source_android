package com.getmedcheck.lib.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.getmedcheck.lib.constant.Constants;
import com.getmedcheck.lib.events.EventBleConnectionState;
import com.getmedcheck.lib.events.EventReadingProgress;
import com.getmedcheck.lib.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public abstract class MedCheckBluetoothService<T> extends Service {

    private static final String TAG = MedCheckBluetoothService.class.getSimpleName();
    protected static final String ACTION_START_SCAN = "com.getmedcheck.lib.services.ACTION_START_SCAN";
    protected static final String ACTION_STOP_SCAN = "com.getmedcheck.lib.services.ACTION_STOP_SCAN";
    protected static final String ACTION_DISCONNECT = "com.getmedcheck.lib.services.ACTION_DISCONNECT";
    protected static final String ACTION_CONNECT_DEVICE_USING_MAC = "com.getmedcheck.lib.services.ACTION_CONNECT_DEVICE_USING_MAC";
    protected static final String ACTION_WRITE_CHARACTERISTICS_USING_MAC = "com.getmedcheck.lib.services.ACTION_WRITE_CHARACTERISTICS_USING_MAC";
    protected static final String ACTION_CLEAR_DEVICE_USING_MAC = "com.getmedcheck.lib.services.ACTION_CLEAR_DEVICE_USING_MAC";
    protected static final String ACTION_TIME_SYNC_USING_MAC = "com.getmedcheck.lib.services.ACTION_TIME_SYNC_USING_MAC";
    protected static final String DEVICE_MAC_ADDRESS = "DEVICE_MAC_ADDRESS";

    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int PROCESSING = 3;
    public static final int COMPLETED = 4;
    public static final int DISCONNECTED = 5;
    public static final int ERROR = 6;
    public static final int NEW_READING = 7;
    public static final int NO_DATA_FOUND = 8;
    public static final int TIME_SYNC = 9;

    private static final int ERROR_CODE_GATT_NULL = 1;
    private static final int ERROR_CODE_SCAN_CALLBACK_NULL = 2;

    private BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
    private BroadcastReceiver mBluetoothOnOffReceiver;
    private ScanCallback mScanCallback;
    private BluetoothGatt mBluetoothGatt;
    private String currentCharacteristics = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerBluetoothOnOffReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBluetoothOnOffReceiver();
        stopScan();
    }

    private void registerBluetoothOnOffReceiver() {
        mBluetoothOnOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                    if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_DISCONNECTING
                                && intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1) == BluetoothAdapter.STATE_ON) {
                            onBluetoothOff();
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothOnOffReceiver, intentFilter);
    }

    private void unregisterBluetoothOnOffReceiver() {
        if (mBluetoothOnOffReceiver != null) {
            unregisterReceiver(mBluetoothOnOffReceiver);
            mBluetoothOnOffReceiver = null;
        }
    }

    protected void writeCharacteristics() {
        if (mBluetoothGatt != null) {
            writeCharacteristicCommand(mBluetoothGatt, Constants.WRITE_CHARACTERISTICS_UUID, Constants.BLE_COMMAND_BT09);
        } else {
            onError(ERROR_CODE_GATT_NULL, new Exception());
        }
    }

    protected void connectDeviceUsingMac(String deviceMacAddress) {
        if (!TextUtils.isEmpty(deviceMacAddress)) {
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceMacAddress);
            if (device != null) {
                connectBluetoothLeDevice(device);
            }
        }
    }

    protected void finish() {
        disconnectDevice();
        stopScan();
        stopSelf();
    }

    protected void startScan() {
        stopScan();
        if (mScanCallback == null) {
            initScanCallback();
            scanner.startScan(mScanCallback);
        } else {
            onError(ERROR_CODE_SCAN_CALLBACK_NULL, new Exception());
        }
    }

    protected void stopScan() {
        if (mScanCallback != null) {
            scanner.stopScan(mScanCallback);
            mScanCallback = null;
            //cancelTimeSyncTimer();
        } else {
            onError(ERROR_CODE_SCAN_CALLBACK_NULL, new Exception());
        }
    }

    protected void disconnectDevice() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt.disconnect();
        } else {
            onError(ERROR_CODE_GATT_NULL, new Exception());
        }
    }

    protected void connectBluetoothLeDevice(BluetoothDevice bluetoothDevice) {
        // MUST! disconnect and close previously connected GATT
        // because when you second time connect without disconnecting gatt callback methods call increment every time
        // ex: first time onDataReadingStateChange is called 1 time, second time it is called two times and so on.
        disconnectDevice();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = bluetoothDevice.connectGatt(this, false, new MyBluetoothGattCallback(), BluetoothDevice.TRANSPORT_LE);
        } else {
            mBluetoothGatt = bluetoothDevice.connectGatt(this, false, new MyBluetoothGattCallback());
        }

        onStartConnecting();
    }

    private void initScanCallback() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return;
        }
        mScanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                onBleScanResult(callbackType, result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    //----------------------------- MyBluetoothGattCallback ----------------------------------

    protected void writeCharacteristicCommand(BluetoothGatt gatt, String uuid, String value) {
        for (BluetoothGattService gattService : gatt.getServices()) {
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {

                if (gattCharacteristic.getUuid().toString().equals(uuid)) {
                    gattCharacteristic.setValue(value);
                    gatt.writeCharacteristic(gattCharacteristic);
                    //currentCharacteristics = value;
                    //Log.d(TAG, "#writeCharacteristicCommand with: uuid = [" + uuid + "], value = [" + value + "] :: " + gattService.getUuid().toString());
                }
            }
        }
    }

    protected void writeBleCommand(BluetoothGatt gatt, String uuid, byte[] value, String characteristicValue) {
        for (BluetoothGattService gattService : gatt.getServices()) {
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                if (gattCharacteristic.getUuid().toString().equals(uuid)) {
                    gattCharacteristic.setValue(value);
                    gatt.writeCharacteristic(gattCharacteristic);
                    //currentCharacteristics = characteristicValue;
                    //Log.d(TAG, "writeCharacteristicCommand with: uuid = [" + uuid + "], value = [" + currentCharacteristics + "]");
                }
            }
        }
    }

    private void listServices(final BluetoothGatt gatt) {

        // get list of services
        for (BluetoothGattService gattService : gatt.getServices()) {

            // get characteristics list for particular service
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {

                // if characteristics match with read characteristics
                if (gattCharacteristic.getUuid().toString().equals(Constants.READ_CHARACTERISTICS_UUID)) {

                    // enable notification
                    gatt.setCharacteristicNotification(gattCharacteristic, true);

                    UUID uuid = UUID.fromString(Constants.NOTIFICATION_DESCRIPTOR_UUID);
                    BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(uuid);
                    if (descriptor != null) {
                        // set notification enable value
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        boolean isWrite = gatt.writeDescriptor(descriptor);
                        Log.e(TAG, "listServices: " + descriptor.getUuid() + " = is write:  " + isWrite);
                        break;
                    }
                }
            }
        }
    }

    private class MyBluetoothGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            EventBus.getDefault().post(new EventBleConnectionState(gatt, status, newState));

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onConnectDevice(gatt);
                gatt.discoverServices();
                stopScan();
            } else {
                onDisconnectDevice(gatt);
            }
            Log.e(TAG, "GATT STATUS: " + status + " = " + newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                listServices(gatt);
            }
        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                executeCommand(gatt, descriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            onCommandResultReceive(gatt, characteristic);

            String hexVal = StringUtils.bytesToHex(characteristic.getValue());
            String currentCharacteristics = "";

            // hex val 52 means start new reading
            if (hexVal.equals("52")) {
                onStartNewReading(gatt, characteristic);
            }
            // command BT:9
            else if (currentCharacteristics.equals(Constants.BLE_COMMAND_BT09)) {

                // heading byte
                if (hexVal.equals(Constants.BLE_HEADING_DATA)) {
                    EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.PROCESSING, "Processing"));
                }
            }
        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //Log.e(TAG, "onCharacteristicWrite: value = " + new String(characteristic.getValue()) + " ------ " + currentCharacteristics + " Status: " + status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
    }

    protected abstract void onStartNewReading(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    protected abstract void onCommandResultReceive(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    protected abstract void executeCommand(BluetoothGatt gatt, BluetoothGattDescriptor descriptor);

    protected abstract void onStartConnecting();

    protected abstract void onConnectDevice(BluetoothGatt gatt);

    protected abstract void onDisconnectDevice(BluetoothGatt gatt);

    protected abstract void onBluetoothOff();

    protected abstract void onBleScanResult(int callbackType, ScanResult result);

    protected abstract void onError(int errorCode, Exception e);
}
