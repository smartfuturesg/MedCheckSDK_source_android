package com.getmedcheck.lib.events;

public class EventReadingProgress {

    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int PROCESSING = 3;
    public static final int COMPLETED = 4;
    public static final int DISCONNECTED = 5;
    public static final int ERROR = 6;
    public static final int NEW_READING = 7;
    public static final int NO_DATA_FOUND = 8;
    public static final int TIME_SYNC = 9;
    public static final int DATA_CLEARED = 10;

    private int status;
    private String message = "";

    public EventReadingProgress() {
    }

    public EventReadingProgress(int status) {
        this.status = status;
    }

    public EventReadingProgress(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
