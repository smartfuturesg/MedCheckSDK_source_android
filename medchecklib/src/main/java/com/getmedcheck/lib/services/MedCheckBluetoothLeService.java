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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.getmedcheck.lib.R;
import com.getmedcheck.lib.constant.Constants;
import com.getmedcheck.lib.data.AppData;
import com.getmedcheck.lib.events.EventBleConnectionState;
import com.getmedcheck.lib.events.EventBleScanResult;
import com.getmedcheck.lib.events.EventClearCommand;
import com.getmedcheck.lib.events.EventDeviceData;
import com.getmedcheck.lib.events.EventReadingProgress;
import com.getmedcheck.lib.events.EventTimeSyncCommand;
import com.getmedcheck.lib.model.BleDevice;
import com.getmedcheck.lib.model.BloodGlucoseData;
import com.getmedcheck.lib.model.BloodPressureData;
import com.getmedcheck.lib.model.IDeviceData;
import com.getmedcheck.lib.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class MedCheckBluetoothLeService extends Service {

    private static final String TAG = MedCheckBluetoothLeService.class.getSimpleName();
    private static final String ACTION_START_SCAN = "com.getmedcheck.lib.services.ACTION_START_SCAN";
    private static final String ACTION_STOP_SCAN = "com.getmedcheck.lib.services.ACTION_STOP_SCAN";
    private static final String ACTION_STOP_SERVICE = "com.getmedcheck.lib.services.ACTION_STOP_SERVICE";
    private static final String ACTION_DISCONNECT = "com.getmedcheck.lib.services.ACTION_DISCONNECT";
    private static final String ACTION_CONNECT_DEVICE_USING_MAC = "com.getmedcheck.lib.services.ACTION_CONNECT_DEVICE_USING_MAC";
    private static final String ACTION_WRITE_CHARACTERISTICS_USING_MAC = "com.getmedcheck.lib.services.ACTION_WRITE_CHARACTERISTICS_USING_MAC";
    private static final String ACTION_CLEAR_DEVICE_USING_MAC = "com.getmedcheck.lib.services.ACTION_CLEAR_DEVICE_USING_MAC";
    private static final String ACTION_TIME_SYNC_USING_MAC = "com.getmedcheck.lib.services.ACTION_TIME_SYNC_USING_MAC";

    private static final String DEVICE_MAC_ADDRESS = "DEVICE_MAC_ADDRESS";

    private BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
    private ArrayList<IDeviceData> bloodTestDataArrayList = new ArrayList<>();
    private StringBuilder mStringBuilderBgm = new StringBuilder();
    private String currentCharacteristics = "";
    private boolean bloodTestDataSend;
    // time sync
    private Handler mHandlerSyncTimer;
    private Runnable mRunnableSyncTimer;
    // clear command
    private CountDownTimer mCountDownTimerClear;
    private String mDeviceMacAddress = "";
    private String mReConnectDeviceMacAddress = "";
    private String bt9Data = "";
    private String mAction = "";

    private BroadcastReceiver mBluetoothOnOffReceiver;

    //----------------------------------- Callback -------------------------------------------
    private ScanCallback mScanCallback;
    private BluetoothGatt mBluetoothGatt;

    public static void startBluetoothLeScan(Context context) {
        Intent intent = new Intent(context, MedCheckBluetoothLeService.class);
        intent.setAction(ACTION_START_SCAN);
        context.startService(intent);
    }

    public static void stopBluetoothLeScan(Context context) {
        Intent intent = new Intent(context, MedCheckBluetoothLeService.class);
        intent.setAction(ACTION_STOP_SCAN);
        context.startService(intent);
    }

    public static void stopBluetoothLeService(Context context) {
        Intent intent = new Intent(context, MedCheckBluetoothLeService.class);
        intent.setAction(ACTION_STOP_SERVICE);
        context.startService(intent);
    }

    public static void disconnectBluetoothLeDevice(Context context) {
        Intent intent = new Intent(context, MedCheckBluetoothLeService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    public static void connectDeviceUsingMacAddress(Context context, String macAddress) {
        Intent intent = new Intent(context, MedCheckBluetoothLeService.class);
        intent.setAction(ACTION_CONNECT_DEVICE_USING_MAC);
        intent.putExtra(DEVICE_MAC_ADDRESS, macAddress);
        context.startService(intent);
    }

    public static void clearDeviceUsingMacAddress(Context context, String macAddress) {
        Intent intent = new Intent(context, MedCheckBluetoothLeService.class);
        intent.setAction(ACTION_CLEAR_DEVICE_USING_MAC);
        intent.putExtra(DEVICE_MAC_ADDRESS, macAddress);
        context.startService(intent);
    }

    public static void timeSyncUsingMacAddress(Context context, String macAddress) {
        Intent intent = new Intent(context, MedCheckBluetoothLeService.class);
        intent.setAction(ACTION_TIME_SYNC_USING_MAC);
        intent.putExtra(DEVICE_MAC_ADDRESS, macAddress);
        context.startService(intent);
    }

    public static void writeCharacteristicsUsingMac(Context context, String macAddress) {
        Intent intent = new Intent(context, MedCheckBluetoothLeService.class);
        intent.setAction(ACTION_WRITE_CHARACTERISTICS_USING_MAC);
        intent.putExtra(DEVICE_MAC_ADDRESS, macAddress);
        context.startService(intent);
    }

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

    private void registerBluetoothOnOffReceiver() {
        mBluetoothOnOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                    if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_DISCONNECTING
                                && intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1) == BluetoothAdapter.STATE_ON) {
                            disconnectDevice();
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            mDeviceMacAddress = "";
            switch (intent.getAction()) {
                case ACTION_START_SCAN:
                    mAction = ACTION_START_SCAN;
                    startScan();
                    break;
                case ACTION_STOP_SCAN:
                    mAction = ACTION_STOP_SCAN;
                    stopScan();
                    break;
                case ACTION_STOP_SERVICE:
                    mAction = ACTION_STOP_SERVICE;
                    stopScan();
                    disconnectDevice();
                    stopSelf();
                    break;
                case ACTION_DISCONNECT:
                    mAction = ACTION_DISCONNECT;
                    disconnectDevice();
                    break;
                case ACTION_CONNECT_DEVICE_USING_MAC:
                    mAction = ACTION_CONNECT_DEVICE_USING_MAC;
                    mDeviceMacAddress = intent.getStringExtra(DEVICE_MAC_ADDRESS);
                    if (!TextUtils.isEmpty(mDeviceMacAddress)) {
                        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mDeviceMacAddress);
                        if (device != null) {
                            connectBluetoothLeDevice(device);
                        }
                    }
                    break;
                case ACTION_WRITE_CHARACTERISTICS_USING_MAC:
                    mAction = ACTION_WRITE_CHARACTERISTICS_USING_MAC;
                    mDeviceMacAddress = intent.getStringExtra(DEVICE_MAC_ADDRESS);
                    if (mBluetoothGatt != null) {
                        writeCharacteristicCommand(mBluetoothGatt, Constants.WRITE_CHARACTERISTICS_UUID, Constants.BLE_COMMAND_BT09);
                    }
                    break;
                case ACTION_CLEAR_DEVICE_USING_MAC:
                    mAction = ACTION_CLEAR_DEVICE_USING_MAC;
                    mDeviceMacAddress = intent.getStringExtra(DEVICE_MAC_ADDRESS);
                    if (mBluetoothGatt != null) {
                        // currently clear device is supported only in blood pressure device
                        if (mBluetoothGatt.getDevice().getName().startsWith(Constants.BLOOD_PRESSURE_DEVICE_ID)
                                || mBluetoothGatt.getDevice().getName().contains(Constants.BLOOD_PRESSURE_DEVICE_ID_NEW)) {
                            writeClearCommand(mBluetoothGatt, getBpmUser());
                        }
                    }
                case ACTION_TIME_SYNC_USING_MAC:
                    mAction = ACTION_TIME_SYNC_USING_MAC;
                    mDeviceMacAddress = intent.getStringExtra(DEVICE_MAC_ADDRESS);
                    if (mBluetoothGatt != null) {
                        // currently time sync is supported only in blood pressure device
                        if (mBluetoothGatt.getDevice().getName().startsWith(Constants.BLOOD_PRESSURE_DEVICE_ID)
                                || mBluetoothGatt.getDevice().getName().contains(Constants.BLOOD_PRESSURE_DEVICE_ID_NEW)) {
                            writeTimeSyncCommand(mBluetoothGatt);
                        }
                    }
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    //------------------------------------ Scan BLE ----------------------------------------

    private void disconnectDevice() {

        if (AppData.getInstance().getConnectedDeviceGhatt().size() > 0) {
            ArrayList<BluetoothGatt> bluetoothGatts = new ArrayList<>(AppData.getInstance().getConnectedDeviceGhatt());
            for (BluetoothGatt bluetoothGatt : bluetoothGatts) {
                bluetoothGatt.disconnect();
            }

            AppData.getInstance().clearConnectedGhattList();
        } else if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }


    private void disconnectDeviceBeforeConnection(final BluetoothDevice bluetoothDevice) {

        if (AppData.getInstance().getConnectedDeviceGhatt().size() > 0) {
            ArrayList<BluetoothGatt> bluetoothGatts = new ArrayList<>(AppData.getInstance().getConnectedDeviceGhatt());
            for (BluetoothGatt bluetoothGatt : bluetoothGatts) {
                bluetoothGatt.disconnect();
            }

            AppData.getInstance().clearConnectedGhattList();
        } else if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }


        try {
            Thread.sleep(5000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = bluetoothDevice.connectGatt(MedCheckBluetoothLeService.this, false, new MyBluetoothGattCallback(), BluetoothDevice.TRANSPORT_LE);
            } else {
                mBluetoothGatt = bluetoothDevice.connectGatt(MedCheckBluetoothLeService.this, false, new MyBluetoothGattCallback());
            }

            AppData.getInstance().addConnectedGhatt(mBluetoothGatt);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.CONNECTING, "Connecting"));
    }

    private void connectBluetoothLeDevice(final BluetoothDevice bluetoothDevice) {
        // MUST! disconnect and close previously connected GATT
        // because when you second time connect without disconnecting gatt callback methods call increment every time
        // ex: first time onDataReadingStateChange is called 1 time, second time it is called two times and so on.


        if (AppData.getInstance().getConnectedDeviceGhatt().size() > 0) {
            disconnectDeviceBeforeConnection(bluetoothDevice);
            return;
        }

        try {
            Thread.sleep(1000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = bluetoothDevice.connectGatt(MedCheckBluetoothLeService.this, false, new MyBluetoothGattCallback(), BluetoothDevice.TRANSPORT_LE);
            } else {
                mBluetoothGatt = bluetoothDevice.connectGatt(MedCheckBluetoothLeService.this, false, new MyBluetoothGattCallback());
            }

            AppData.getInstance().addConnectedGhatt(mBluetoothGatt);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.CONNECTING, "Connecting"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBluetoothOnOffReceiver();
        stopScan();
    }

    private void startScan() {
        if (mScanCallback == null) {
            initScanCallback();
            scanner.startScan(mScanCallback);
        }
    }

    private void startScanWithDelay() {
        if (mScanCallback == null) {
            initScanCallback();

            try {
                Thread.sleep(5000);
                scanner.startScan(mScanCallback);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void initScanCallback() {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            return;
        }
        mScanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                // if ble device name is not empty then
                if (result.getDevice().getName() != null) {
                    // if device name start with med check device name then
                    if (result.getDevice().getName().startsWith("HL") ||
                            result.getDevice().getName().startsWith("SFBPBLE")
                            || result.getDevice().getName().startsWith("SFBGBLE")) {
                        EventBus.getDefault().post(new EventBleScanResult(callbackType, result));
                    }
                }
                if (!TextUtils.isEmpty(mDeviceMacAddress)) {
                    if (!TextUtils.isEmpty(result.getDevice().getAddress()) && result.getDevice().getAddress().equals(mDeviceMacAddress)) {
                        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mDeviceMacAddress);
                        if (device != null && !TextUtils.isEmpty(mReConnectDeviceMacAddress) && mDeviceMacAddress.equalsIgnoreCase(mReConnectDeviceMacAddress)) {
                            mReConnectDeviceMacAddress = "";
                            connectBluetoothLeDevice(device);
                        }
                    }
                }
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


    private void stopScan() {
        if (mScanCallback != null) {
            scanner.stopScan(mScanCallback);
            mScanCallback = null;
            cancelTimeSyncTimer();
        }
    }

    private void cancelTimeSyncTimer() {
        if (mHandlerSyncTimer != null && mRunnableSyncTimer != null) {
            mHandlerSyncTimer.removeCallbacks(mRunnableSyncTimer);
        }
    }

    //----------------------------- MyBluetoothGattCallback ----------------------------------

    private void writeCharacteristicCommand(BluetoothGatt gatt, String uuid, String value) {
        for (BluetoothGattService gattService : gatt.getServices()) {
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {

                if (gattCharacteristic.getUuid().toString().equals(uuid)) {
                    gattCharacteristic.setValue(value);
                    gatt.writeCharacteristic(gattCharacteristic);
                    currentCharacteristics = value;
                    Log.d(TAG, "#writeCharacteristicCommand with: uuid = [" + uuid + "], value = [" + value + "] :: " + gattService.getUuid().toString());
                }
            }
        }
    }

    private void writeBleCommand(BluetoothGatt gatt, String uuid, byte[] value, String characteristicValue) {
        for (BluetoothGattService gattService : gatt.getServices()) {
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                if (gattCharacteristic.getUuid().toString().equals(uuid)) {
                    gattCharacteristic.setValue(value);
                    gatt.writeCharacteristic(gattCharacteristic);
                    currentCharacteristics = characteristicValue;
                    Log.d(TAG, "writeCharacteristicCommand with: uuid = [" + uuid + "], value = [" + currentCharacteristics + "]");
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

    /**
     * get Blood pressure device user index
     *
     * @return index of user
     */
    public int getBpmUser() {
        if (!TextUtils.isEmpty(bt9Data)) {
            int user = Integer.parseInt(bt9Data.substring(0, 2));
            switch (user) {
                case 0:
                    return 0x01;
                case 10:
                    return 0x02;
                case 20:
                    return 0x03;
            }
        }
        // all user
        return 0x00;
    }

    /**
     * get command user wise
     *
     * @return command
     */
    public String getBpmUserCommand() {
        int user;
        String command = Constants.BLE_COMMAND_BT00;
        if (!TextUtils.isEmpty(bt9Data)) {
            user = Integer.parseInt(bt9Data.substring(0, 2));
            switch (user) {
                case 0:
                    command = Constants.BLE_COMMAND_BT00;
                    break;
                case 10:
                    command = Constants.BLE_COMMAND_BT01;
                    break;
                case 20:
                    command = Constants.BLE_COMMAND_BT02;
                    break;
            }
            Log.e(TAG, "onCharacteristicChanged: User = " + user + " command: " + command);
        }
        return command;
    }

    /**
     * get stating index of data reading
     *
     * @return starting index
     */
    public int getBpmUserStartIndex() {
        int user;
        int startIndex = 0;
        if (!TextUtils.isEmpty(bt9Data)) {
            String data = bt9Data.substring(0, 2);
            if (!StringUtils.isDigit(data)) {
                return 0;
            }
            user = Integer.parseInt(data);
            // 0 for first, 10 for second user and 20 for Third User
            switch (user) {
                case 0:
                    // if user 0 then 14-16, 2 bytes is for start index of user 1
                    startIndex = Integer.parseInt(bt9Data.substring(14, 16), 16);
                    break;
                case 10:
                    // if user 0 then 8-10, 2 bytes is for start index of user 2
                    startIndex = Integer.parseInt(bt9Data.substring(8, 10), 16);
                    break;
                case 20:
                    // if user 0 then 10-12, 2 bytes is for start index of user 3
                    startIndex = Integer.parseInt(bt9Data.substring(10, 12), 16);
                    break;
            }
            Log.e(TAG, "onCharacteristicChanged: User = " + user + " Start: " + startIndex);
        }
        return startIndex;
    }

    /**
     * Write clear command
     * Note: currently clear command supported only in blood pressure devices
     *
     * @param gatt bluetooth gatt
     * @param user user index of device
     */
    private void writeClearCommand(BluetoothGatt gatt, int user) {
        if (mCountDownTimerClear != null) {
            mCountDownTimerClear.cancel();
        }

        // clear time command timer, wait for timeout sec, if successful then timer canceled
        mCountDownTimerClear = new CountDownTimer(Constants.TIME_OUT_TIME, Constants.TIME_OUT_TIME) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                EventBus.getDefault().post(new EventClearCommand(EventClearCommand.CLEAR_FAIL));
            }
        };

        EventBus.getDefault().post(new EventClearCommand(EventClearCommand.CLEAR_START));

        // return 0x59 if success or no response
        byte[] byteCmd = new byte[2];
        byteCmd[0] = (byte) 0xA9;
        byteCmd[1] = (byte) user;

        writeBleCommand(gatt, Constants.WRITE_CHARACTERISTICS_UUID, byteCmd, Constants.BLE_COMMAND_CLEAR);
        mCountDownTimerClear.start();
    }

    /**
     * Write time sync command
     *
     * @param gatt
     */
    private void writeTimeSyncCommand(BluetoothGatt gatt) {
        byte[] byteCmd = new byte[7];
        Calendar aCalendar = Calendar.getInstance();
        byteCmd[0] = -95;
        byteCmd[1] = (byte) (aCalendar.get(Calendar.YEAR) - 2000);
        byteCmd[2] = (byte) (aCalendar.get(Calendar.MONTH) + 1);
        byteCmd[3] = (byte) aCalendar.get(Calendar.DAY_OF_MONTH);
        byteCmd[4] = (byte) aCalendar.get(Calendar.HOUR_OF_DAY);
        byteCmd[5] = (byte) aCalendar.get(Calendar.MINUTE);
        byteCmd[6] = (byte) aCalendar.get(Calendar.SECOND);

        writeBleCommand(gatt, Constants.WRITE_CHARACTERISTICS_UUID, byteCmd, Constants.BLE_COMMAND_TIME_SYNC);
        EventBus.getDefault().post(new EventTimeSyncCommand(EventTimeSyncCommand.TIME_SYNC_START));
        EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.TIME_SYNC, "Time Sync"));
    }

    /**
     * start countdown timer for sync command
     *
     * @param time time
     * @param gatt bluetooth gatt
     */
    private void startCountDownTimerForWait(long time, final BluetoothGatt gatt) {
        cancelTimeSyncTimer();
        mHandlerSyncTimer = new Handler(Looper.getMainLooper());
        mRunnableSyncTimer = new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new EventTimeSyncCommand(EventTimeSyncCommand.TIME_SYNC_FAIL));
                writeCharacteristicCommand(gatt, Constants.WRITE_CHARACTERISTICS_UUID, Constants.BLE_COMMAND_BT09);
            }
        };
        mHandlerSyncTimer.postDelayed(mRunnableSyncTimer, time);
    }

    private class MyBluetoothGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            EventBus.getDefault().post(new EventBleConnectionState(gatt, status, newState));

            AppData appData = AppData.getInstance();

            BleDevice bleDevice = new BleDevice(gatt.getDevice(), Constants.BLE_STATUS_CONNECTED);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // store connected device
                appData.addConnectedDevice(bleDevice);

                EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.CONNECTED, "Connected"));

                stopScan();


                try {
                    Thread.sleep(1000);
                    gatt.discoverServices();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                // remove connected device
                appData.removeConnectedDevice(bleDevice);

                if (!AppData.getInstance().isLiveReading()) {

                    if (status == 133 && newState == 0
                            && !TextUtils.isEmpty(mDeviceMacAddress) && !TextUtils.isEmpty(mAction)
                            && mAction.equalsIgnoreCase(ACTION_CONNECT_DEVICE_USING_MAC)) {

                        if (!TextUtils.isEmpty(mDeviceMacAddress)) {
                            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mDeviceMacAddress);
                            if (device != null) {
                                connectBluetoothLeDevice(device);
                            }
                        }
                    } else {
                        EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.DISCONNECTED, "Disconnected"));
                    }

                } else {
                    mReConnectDeviceMacAddress = mDeviceMacAddress;
                }

                if (gatt != null) {
                    gatt.close();
                }

                if (gatt == mBluetoothGatt) {
                    mBluetoothGatt = null;
                }

            }
            Log.e(TAG, "GATT STATUS: " + status + " = " + newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                try {
                    Thread.sleep(500);
                    listServices(gatt);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            Log.e(TAG, "onDescriptorWrite , descriptor's owner characteristic=" + descriptor.getCharacteristic().getUuid() + ", descriptor uuid = " + descriptor.getUuid() + ",status=" + status);

            //Log.e(TAG, "onDescriptorWrite: " + descriptor.getUuid().toString() + " - " + status + " Val: " + StringUtils.getByteInString(descriptor.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {

                // if BPM then call time sync
                if (gatt.getDevice() != null && gatt.getDevice().getName().startsWith(Constants.BLOOD_PRESSURE_DEVICE_ID)
                        || gatt.getDevice().getName().contains(Constants.BLOOD_PRESSURE_DEVICE_ID_NEW)) {
                    // write time sync command
                    writeTimeSyncCommand(gatt);
                    // start timer for 3 sec to wait for time sync command, if time sync command not executed within 3 sec
                    // automatic bt9 command executed
                    startCountDownTimerForWait(Constants.TIME_OUT_TIME, gatt);
                } else {
                    writeCharacteristicCommand(gatt, Constants.WRITE_CHARACTERISTICS_UUID, Constants.BLE_COMMAND_BT09);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            String hexVal = StringUtils.bytesToHex(characteristic.getValue());
            Log.e(TAG, "#onCharacteristicChanged: hexVal = " + hexVal + " ------ " + currentCharacteristics);

            // hex val 52 means start new reading
            if (hexVal.equals("52")) {
                // hex = 52 = string R
                AppData.getInstance().setLiveReading(true);

                String message = "";
                // Measuring Blood Pressure/Blood Glucose
                if (!TextUtils.isEmpty(gatt.getDevice().getName())
                        && (gatt.getDevice().getName().startsWith(Constants.BLOOD_PRESSURE_DEVICE_ID)
                        || gatt.getDevice().getName().startsWith(Constants.BLOOD_PRESSURE_DEVICE_ID_NEW))) {
                    message = getString(R.string.measuring_blood_pressure);
                } else if (!TextUtils.isEmpty(gatt.getDevice().getName())
                        && (gatt.getDevice().getName().startsWith(Constants.BLOOD_GLUCOSE_DEVICE_ID)
                        || gatt.getDevice().getName().startsWith(Constants.BLOOD_GLUCOSE_DEVICE_ID_NEW))) {
                    message = getString(R.string.measuring_blood_glucose);
                }

                EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.NEW_READING, message));


                disconnectDevice();
                // restart scan so device connect
                startScanWithDelay();

                mReConnectDeviceMacAddress = mDeviceMacAddress;

            }

            // clear command success
            if (hexVal.equals("59")) {
                if (mCountDownTimerClear != null) {
                    mCountDownTimerClear.cancel();
                }
                EventBus.getDefault().post(new EventClearCommand(EventClearCommand.CLEAR_COMPLETE));
                currentCharacteristics = "";
            }

            // time sync
            else if (currentCharacteristics.equals(Constants.BLE_COMMAND_TIME_SYNC)) {
                EventBus.getDefault().post(new EventTimeSyncCommand(EventTimeSyncCommand.TIME_SYNC_COMPLETE));
                cancelTimeSyncTimer();
                writeCharacteristicCommand(gatt, Constants.WRITE_CHARACTERISTICS_UUID, Constants.BLE_COMMAND_BT09);
            }
            // command BT:9
            else if (currentCharacteristics.equals(Constants.BLE_COMMAND_BT09)) {

                // heading byte
                if (hexVal.equals(Constants.BLE_HEADING_DATA)) {
                    EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.PROCESSING, "Processing"));
                }

                // store bt:9 command result, it is used for date calculation
                if (!hexVal.equals(Constants.BLE_HEADING_DATA)
                        && !hexVal.equals(Constants.BLE_ENDING_DATA)
                        && !hexVal.equals(Constants.BLE_EMPTY_DATA)) {
                    bt9Data = hexVal;
                }

                // if previous command is bt:9 and value result is its ending value then execute new command
                if (hexVal.equals(Constants.BLE_ENDING_DATA)) {
                    if (!TextUtils.isEmpty(mDeviceMacAddress)) {
                        if (gatt.getDevice().getAddress().equals(mDeviceMacAddress)) {

                            // BPM
                            if (gatt.getDevice().getName().startsWith(Constants.BLOOD_PRESSURE_DEVICE_ID)
                                    || gatt.getDevice().getName().contains(Constants.BLOOD_PRESSURE_DEVICE_ID_NEW)) {

                                int startIndex = getBpmUserStartIndex();
                                int preStartIndex = AppData.getInstance().getReadingStartIndex(mDeviceMacAddress);
                                if (preStartIndex != -1 && startIndex == preStartIndex) {

                                }
                                AppData.getInstance().setReadingStartIndex(mDeviceMacAddress, startIndex);

                                Log.e(TAG, "Start Command: " + startIndex);
                                if (startIndex == 1) {
                                    if (gatt.getDevice() != null && !TextUtils.isEmpty(gatt.getDevice().getAddress())) {
                                        AppData.getInstance().setBleReadingValue(gatt.getDevice().getAddress(), 0);
                                    }
                                }

                                String command = getBpmUserCommand();
                                writeCharacteristicCommand(gatt, Constants.WRITE_CHARACTERISTICS_UUID, command);
                                Log.e(TAG, "Write HL158HC " + command + " on " + mDeviceMacAddress);

                            }
                            // BGM
                            else if (gatt.getDevice().getName().startsWith(Constants.BLOOD_GLUCOSE_DEVICE_ID)
                                    || gatt.getDevice().getName().startsWith(Constants.BLOOD_GLUCOSE_DEVICE_ID_NEW)) {
                                writeCharacteristicCommand(gatt, Constants.WRITE_CHARACTERISTICS_UUID, Constants.BLE_COMMAND_BT00);
                                Log.e(TAG, "Write HL568HC " + Constants.BLE_COMMAND_BT00 + " on " + mDeviceMacAddress);
                            }
                        }
                    }
                }

            }
            // command BT:0, BT:1 and BT:2
            else if (currentCharacteristics.equals(Constants.BLE_COMMAND_BT00)
                    || currentCharacteristics.equals(Constants.BLE_COMMAND_BT01)
                    || currentCharacteristics.equals(Constants.BLE_COMMAND_BT02)) {

                // for other command BT:0, BT:1, BT:2

                String type = "";
                if (!TextUtils.isEmpty(gatt.getDevice().getName())
                        && (gatt.getDevice().getName().startsWith(Constants.BLOOD_PRESSURE_DEVICE_ID)
                        || gatt.getDevice().getName().startsWith(Constants.BLOOD_PRESSURE_DEVICE_ID_NEW))) {
                    type = Constants.TYPE_BPM;
                } else if (!TextUtils.isEmpty(gatt.getDevice().getName())
                        && (gatt.getDevice().getName().startsWith(Constants.BLOOD_GLUCOSE_DEVICE_ID)
                        || gatt.getDevice().getName().startsWith(Constants.BLOOD_GLUCOSE_DEVICE_ID_NEW))) {
                    type = Constants.TYPE_BGM;
                }

                // start command
                if (hexVal.equals(Constants.BLE_HEADING_DATA)) {
                    bloodTestDataSend = false;
                    bloodTestDataArrayList.clear();
                    mStringBuilderBgm = new StringBuilder();
                }

                // data value
                if (!hexVal.equals(Constants.BLE_HEADING_DATA) && !hexVal.equals(Constants.BLE_ENDING_DATA) && !hexVal.equals(Constants.BLE_EMPTY_DATA)) {
                    if (type.equals(Constants.TYPE_BPM)) {
                        BloodPressureData bloodPressureData = new BloodPressureData(characteristic.getValue(), bt9Data);
                        if (!bloodTestDataArrayList.contains(bloodPressureData)
                                && Integer.parseInt(bloodPressureData.getSystolic()) != 0
                                && Integer.parseInt(bloodPressureData.getDiastolic()) != 0
                                && Integer.parseInt(bloodPressureData.getHeartRate()) != 0) {

                            bloodTestDataArrayList.add(bloodPressureData);
                            //Log.e(TAG, "onCharacteristicChanged() BPM hex = [" + StringUtils.getByteInStringForm(characteristic.getValue()) + "]");
                            Log.e(TAG, "Print Data: " + bloodPressureData.getPrintData());
                        }
                    } else if (type.equals(Constants.TYPE_BGM)) {
                        // append data if bgm because bgm data length is 48 and bpm is 64
                        if (mStringBuilderBgm != null) {
                            mStringBuilderBgm.append(StringUtils.byteArrayToBinary(characteristic.getValue()));
                        }
                    }
                }

                // end command
                if (hexVal.equals(Constants.BLE_ENDING_DATA)) {
                    if (type.equals(Constants.TYPE_BGM)) {
                        if (mStringBuilderBgm != null) {
                            ArrayList<String> arrayList = StringUtils.convertStringToGroupArray(mStringBuilderBgm.toString(), 48);
                            if (arrayList != null) {
                                for (String hexString : arrayList) {
                                    Log.e(TAG, "onCharacteristicChanged() BGM hex = [" + hexString + "]");
                                    bloodTestDataArrayList.add(new BloodGlucoseData(hexString, bt9Data));
                                }

                                int start = Integer.parseInt(bt9Data.substring(4, 6), 16);
                                int end = Integer.parseInt(bt9Data.substring(6, 8), 16);

                                if (start < bloodTestDataArrayList.size() && end < bloodTestDataArrayList.size()) {
                                    ArrayList<IDeviceData> bgmTestArrayList = new ArrayList<>();
                                    bgmTestArrayList.addAll(bloodTestDataArrayList.subList(start, end));
                                    bloodTestDataArrayList.clear();
                                    bloodTestDataArrayList.addAll(bgmTestArrayList);
                                }
                                Log.e(TAG, "onCharacteristicChanged: Start -> " + start + " ,End -> " + end);
                            }
                        }
                    } else if (type.equals(Constants.TYPE_BPM)) {
                        int startIndex = getBpmUserStartIndex();
                        Log.e(TAG, "End Command: " + startIndex);

                        ArrayList<IDeviceData> bgmTestArrayList = new ArrayList<>();
                        bgmTestArrayList.addAll(bloodTestDataArrayList.subList(0, startIndex));
                        bloodTestDataArrayList.clear();
                        bloodTestDataArrayList.addAll(bgmTestArrayList);
                    }

                    if (!bloodTestDataSend) {
                        if (bloodTestDataArrayList.size() > 0) {
                            EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.COMPLETED, "Completed"));
                        } else {
                            EventBus.getDefault().post(new EventReadingProgress(EventReadingProgress.NO_DATA_FOUND, "No data found in device."));
                        }
                        EventBus.getDefault().post(new EventDeviceData(gatt.getDevice(), bloodTestDataArrayList, type));
                        bloodTestDataSend = true;
                        AppData.getInstance().setLiveReading(false);
                        //stopScan();
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e(TAG, "onCharacteristicWrite: value = " + new String(characteristic.getValue()) + " ------ " + currentCharacteristics + " Status: " + status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
    }

}
