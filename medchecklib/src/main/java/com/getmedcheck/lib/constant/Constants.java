package com.getmedcheck.lib.constant;

public class Constants {

    public final static String READ_CHARACTERISTICS_UUID = "0000fff4-0000-1000-8000-00805f9b34fb";
    public final static String WRITE_CHARACTERISTICS_UUID = "0000fff5-0000-1000-8000-00805f9b34fb";
    public final static String NOTIFICATION_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public final static String BLE_COMMAND_BT09 = "BT:9";
    public final static String BLE_COMMAND_BT00 = "BT:0";
    public final static String BLE_COMMAND_BT01 = "BT:1";
    public final static String BLE_COMMAND_BT02 = "BT:2";
    public final static String BLE_COMMAND_TIME_SYNC = "TIME_SYNC";
    public final static String BLE_COMMAND_CLEAR = "CLEAR";

    public final static String BLE_HEADING_DATA = "FA5AF1F2FA5AF3F4";
    public final static String BLE_ENDING_DATA = "F5A5F5F6F5A5F7F8";
    public final static String BLE_EMPTY_DATA = "FFFFFFFFFFFFFFFF";

    public final static String TYPE_BPM = "BPM";
    public final static String TYPE_BGM = "BGM";

    public final static String BLOOD_PRESSURE_DEVICE_ID = "HL158HC";
    public final static String BLOOD_PRESSURE_DEVICE_ID_NEW = "SFBPBLE";
    public final static String BLOOD_GLUCOSE_DEVICE_ID = "HL568HC";
    public final static String BLOOD_GLUCOSE_DEVICE_ID_NEW = "SFBGBLE";
    public final static String BLOOD_GLUCOSE_SYNC_DEVICE_ID = "Sync";
    public final static String ECG_DEVICE_ID = "Checkme";



    public final static String BLOOD_PRESSURE_DEVICE_ID_FULL = "HL158HCBLE";
    public final static String BLOOD_GLUCOSE_DEVICE_ID_FULL = "HL568HCBLE";

    public final static String BLE_STATUS_NONE = "None";
    public final static String BLE_STATUS_CONNECTED = "Connected";

    public final static long TIME_OUT_TIME = 3000;
    public final static long SCAN_TIME_OUT_TIME = 5000;
    public final static String DATE_TIME_FORMAT_LOCAL = "dd-MM-yyyy hh:mm a";

    public static int REQUEST_CHECK_LOCATION_SETTINGS = 999;
}
