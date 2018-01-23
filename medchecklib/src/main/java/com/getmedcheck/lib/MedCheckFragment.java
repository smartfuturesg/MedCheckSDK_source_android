package com.getmedcheck.lib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.getmedcheck.lib.constant.Constants;
import com.getmedcheck.lib.listener.MedCheckCallback;
import com.getmedcheck.lib.listener.OnDialogClickListener;
import com.getmedcheck.lib.model.BleDevice;
import com.getmedcheck.lib.model.BloodGlucoseData;
import com.getmedcheck.lib.model.BloodGlucoseDataJSON;
import com.getmedcheck.lib.model.BloodPressureData;
import com.getmedcheck.lib.model.BloodPressureDataJSON;
import com.getmedcheck.lib.model.IDeviceData;
import com.getmedcheck.lib.utils.PermissionHelper;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.util.ArrayList;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

import static com.getmedcheck.lib.constant.Constants.REQUEST_CHECK_LOCATION_SETTINGS;

public abstract class MedCheckFragment extends Fragment {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 155;
    private static final int REQUEST_CODE_OPEN_SETTING = 156;

    private BluetoothAdapter mBluetoothAdapter;

    public MedCheckFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerCallback();
        requestLocationPermission();
    }

    private void registerCallback() {
        MedCheck.getInstance().registerCallBack(new MedCheckCallback() {

            @Override
            public void onTimeSyncCommand(int state) {
                onDeviceTimeSyncCommand(state);
            }

            @Override
            public void onConnectionStateChange(BleDevice bleDevice, int status) {
                onDeviceConnectionStateChange(bleDevice, status);
            }

            @Override
            public void onScanResult(ScanResult scanResult) {
                onDeviceScanResult(scanResult);
            }

            @Override
            public void onDataReadingStateChange(int state, String message) {
                onDeviceDataReadingStateChange(state, message);
            }

            @Override
            public void onDataReceive(BluetoothDevice device, ArrayList<IDeviceData> deviceData, String deviceType) {
                String jsonString = "";
                Gson gson = new Gson();

                if (deviceType.equals(Constants.TYPE_BPM)) {

                    BloodPressureDataJSON bloodPressureDataJSON = new BloodPressureDataJSON();

                    bloodPressureDataJSON.setType(Constants.TYPE_BPM);

                    if (deviceData.size() == 0 || deviceData == null) {
                        bloodPressureDataJSON.setMessage("No Data Found");
                        bloodPressureDataJSON.setData(new ArrayList<BloodPressureData>());
                    } else {
                        bloodPressureDataJSON.setMessage("Data Found");

                        ArrayList<BloodPressureData> dataArrayList = new ArrayList<>();

                        for (IDeviceData deviceDatum : deviceData) {

                            BloodPressureData bloodPressureData = (BloodPressureData) deviceDatum;
                            dataArrayList.add(bloodPressureData);

                        }

                        bloodPressureDataJSON.setData(dataArrayList);

                    }

                    jsonString = gson.toJson(bloodPressureDataJSON);

                } else if (deviceType.equals(Constants.TYPE_BGM)) {

                    BloodGlucoseDataJSON bloodGlucoseDataJSON = new BloodGlucoseDataJSON();

                    bloodGlucoseDataJSON.setType(Constants.TYPE_BGM);

                    if (deviceData.size() == 0 || deviceData == null) {
                        bloodGlucoseDataJSON.setMessage("No Data Found");
                        bloodGlucoseDataJSON.setData(new ArrayList<BloodGlucoseData>());
                    } else {
                        bloodGlucoseDataJSON.setMessage("Data Found");

                        ArrayList<BloodGlucoseData> dataArrayList = new ArrayList<>();

                        for (IDeviceData deviceDatum : deviceData) {

                            BloodGlucoseData bloodGlucoseData = (BloodGlucoseData) deviceDatum;
                            dataArrayList.add(bloodGlucoseData);

                        }

                        bloodGlucoseDataJSON.setData(dataArrayList);
                    }

                    jsonString = gson.toJson(bloodGlucoseDataJSON);

                }

                onDeviceDataReceive(device, deviceData, jsonString, deviceType);
            }

            @Override
            public void onClearCommand(int state) {
                onDeviceClearCommand(state);
            }
        });
    }

    private void requestLocationPermission() {
        if (PermissionHelper.hasPermissions(getContext(), PermissionHelper.Permission.LOCATIONS_PERMISSION)) {
            onPermissionGranted();
        } else {
            PermissionHelper.requestPermissions(this,
                    PermissionHelper.Permission.LOCATIONS_PERMISSION,
                    REQUEST_CODE_LOCATION_PERMISSION);
        }
    }

    protected void checkAllConditions() {

        if (PermissionHelper.hasPermissions(getContext(), PermissionHelper.Permission.LOCATIONS_PERMISSION)) {


            LocationSettingsRequest.Builder locationSettingBuilder = new LocationSettingsRequest.Builder();
            locationSettingBuilder.addLocationRequest(new LocationRequest());
            LocationSettingsRequest locationSetting = locationSettingBuilder.build();

            final Task<LocationSettingsResponse> task;

            //Need to check whether location settings are satisfied
            SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
            task = settingsClient.checkLocationSettings(locationSetting);

            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    if (isBluetoothOn()) {
                        startScan();
                    } else {
                        enableBluetooth();
                    }
                }
            });

            task.addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    enableGps();
                }
            });


        } else {
            PermissionHelper.requestPermissions(this,
                    PermissionHelper.Permission.LOCATIONS_PERMISSION,
                    REQUEST_CODE_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionHelper.hasPermissions(getContext(), permissions)) {
            enableGps();
        } else {
            if (PermissionHelper.shouldShowPermissionRationale(this, permissions)) {
                // if permission is denied only
                showPermissionRationale();
            } else {
                // if permission is denied with not ask again
                onPermissionDenied();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_SETTING) {
            if (PermissionHelper.hasPermissions(getContext(), PermissionHelper.Permission.LOCATIONS_PERMISSION)) {
                enableGps();
            }
        } else if (requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
            if (PermissionHelper.hasPermissions(getContext(), PermissionHelper.Permission.LOCATIONS_PERMISSION)) {
                if (resultCode == 0) {
                    Toast.makeText(getContext(), "Need location access for better results", Toast.LENGTH_LONG).show();
                } else {
                    enableGps();
                }
            }
        }
    }

    private void onPermissionDenied() {
        PermissionHelper.showDialog(getContext(), getString(R.string.permission_label),
                getString(R.string.permission_setting_message),
                getString(R.string.dialog_label_setting), new OnDialogClickListener() {
                    @Override
                    public void onDialogClick(DialogInterface dialog, int buttonType) {
                        PermissionHelper.openSettingScreen(MedCheckFragment.this, REQUEST_CODE_OPEN_SETTING);
                    }
                });
    }

    private void showPermissionRationale() {
        PermissionHelper.showDialog(getContext(), getString(R.string.permission_label),
                getString(R.string.permission_message),
                getString(R.string.dialog_label_allow), new OnDialogClickListener() {
                    @Override
                    public void onDialogClick(DialogInterface dialog, int buttonType) {
                        PermissionHelper.requestPermissions(MedCheckFragment.this,
                                PermissionHelper.Permission.LOCATIONS_PERMISSION, REQUEST_CODE_LOCATION_PERMISSION);
                    }
                });
    }

    private void onPermissionGranted() {
        if (!isBluetoothOn()) {
            enableBluetooth();
            return;
        }
        onPermissionGrantedAndBluetoothOn();
    }

    private boolean isBluetoothOn() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    private void enableBluetooth() {
        PermissionHelper.showDialog(getContext(), getString(R.string.bluetooth_enable_dialog_label),
                getString(R.string.bluetooth_enable_dialog_message),
                getString(R.string.dialog_label_enable), new OnDialogClickListener() {
                    @Override
                    public void onDialogClick(DialogInterface dialog, int buttonType) {
                        if (mBluetoothAdapter != null) {
                            mBluetoothAdapter.enable();
                            // wait for some time because bluetooth enable take some time
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (isBluetoothOn()) {
                                        onPermissionGrantedAndBluetoothOn();
                                    }
                                }
                            }, 100);
                        }
                    }
                });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MedCheck.getInstance().unregisterCallBack();
    }

    public void enableGps() {


        if (!(getActivity() == null ||
                getActivity().isFinishing() || getActivity().isDestroyed())) {
            //Create LocationSettingRequest object using locationRequest
            LocationSettingsRequest.Builder locationSettingBuilder = new LocationSettingsRequest.Builder();
            locationSettingBuilder.addLocationRequest(new LocationRequest());
            LocationSettingsRequest locationSetting = locationSettingBuilder.build();

            final Task<LocationSettingsResponse> task;

            //Need to check whether location settings are satisfied
            SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
            task = settingsClient.checkLocationSettings(locationSetting);

            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    onPermissionGranted();
                }
            });

            task.addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case CommonStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(getActivity(),
                                        REQUEST_CHECK_LOCATION_SETTINGS);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    protected void onPermissionGrantedAndBluetoothOn() {
    }

    protected void startScan() {
    }

    protected void onDeviceClearCommand(int state) {
    }

    private void onDeviceTimeSyncCommand(int state) {
    }

    protected void onDeviceScanResult(ScanResult scanResult) {
    }

    protected void onDeviceDataReadingStateChange(int state, String message) {
    }

    protected void onDeviceDataReceive(BluetoothDevice device, ArrayList<IDeviceData> deviceData, String json, String deviceType) {
    }

    protected void onDeviceConnectionStateChange(BleDevice bleDevice, int status) {
    }

}
