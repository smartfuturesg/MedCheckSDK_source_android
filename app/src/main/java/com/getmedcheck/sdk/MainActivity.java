package com.getmedcheck.sdk;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.getmedcheck.lib.MedCheck;
import com.getmedcheck.lib.MedCheckActivity;
import com.getmedcheck.lib.model.BleDevice;

import java.util.ArrayList;
import java.util.HashMap;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class MainActivity extends MedCheckActivity implements OnItemClickListener<BleDevice>, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private HashMap<String, BleDevice> mDeviceHashMap = new HashMap<>();
    private RecyclerView mRvScanResult;
    private Button mBtnStartScan, mBtnStopScan;
    private LinearLayout mLlProgressLayout;
    private ScanResultAdapter mScanResultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        requestLocationPermission();
    }

    private void initViews() {

        mLlProgressLayout = findViewById(R.id.llProgressLayout);
        mBtnStopScan = findViewById(R.id.btnStopScan);
        mBtnStopScan.setOnClickListener(this);
        mBtnStartScan = findViewById(R.id.btnStartScan);
        mBtnStartScan.setOnClickListener(this);

        mScanResultAdapter = new ScanResultAdapter(this);
        mScanResultAdapter.setOnItemClickListener(this);
        mRvScanResult = findViewById(R.id.rvScanResult);
        mRvScanResult.setLayoutManager(new LinearLayoutManager(this));
        mRvScanResult.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRvScanResult.setAdapter(mScanResultAdapter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerCallback();
    }

    @Override
    protected void onDeviceScanResult(ScanResult scanResult) {
        super.onDeviceScanResult(scanResult);
        BleDevice bleDevice = new BleDevice(scanResult.getDevice());
        mDeviceHashMap.put(bleDevice.getDevice().getAddress(), bleDevice);

        if (mScanResultAdapter != null) {
            mScanResultAdapter.setItems(new ArrayList<>(mDeviceHashMap.values()));
        }
    }

    @Override
    public void onItemClick(View view, BleDevice object, int position) {
        if (mBtnStopScan.isClickable()) {
            mBtnStopScan.performClick();
        }

        DeviceConnectionActivity.start(this, object);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStopScan:
                MedCheck.getInstance().stopScan(this);
                mBtnStopScan.setClickable(false);
                mBtnStartScan.setClickable(true);
                mLlProgressLayout.setVisibility(View.GONE);
                break;
            case R.id.btnStartScan:
                checkAllConditions();
                break;
            default:
                break;
        }
    }

    @Override
    protected void startScan() {
        super.startScan();
        mDeviceHashMap.clear();
        if (mScanResultAdapter != null) {
            mScanResultAdapter.clear();
        }
        MedCheck.getInstance().startScan(this);
        mBtnStopScan.setClickable(true);
        mBtnStartScan.setClickable(false);
        mLlProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPermissionGrantedAndBluetoothOn() {
        super.onPermissionGrantedAndBluetoothOn();
    }
}
