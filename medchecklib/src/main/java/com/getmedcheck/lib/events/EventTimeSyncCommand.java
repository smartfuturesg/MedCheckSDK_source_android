package com.getmedcheck.lib.events;

public class EventTimeSyncCommand {

    public static final int TIME_SYNC_FAIL = -1;
    public static final int TIME_SYNC_START = 1;
    public static final int TIME_SYNC_COMPLETE = 2;
    private int status;

    public EventTimeSyncCommand(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
