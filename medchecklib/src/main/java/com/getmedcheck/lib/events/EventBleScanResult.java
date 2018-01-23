package com.getmedcheck.lib.events;


import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class EventBleScanResult {

    private int callbackType;
    private ScanResult result;

    public EventBleScanResult(int callbackType, ScanResult result) {
        this.callbackType = callbackType;
        this.result = result;
    }

    public int getCallbackType() {
        return callbackType;
    }

    public void setCallbackType(int callbackType) {
        this.callbackType = callbackType;
    }

    public ScanResult getResult() {
        return result;
    }

    public void setResult(ScanResult result) {
        this.result = result;
    }
}
