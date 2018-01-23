package com.getmedcheck.lib.events;

public class EventClearCommand {

    public static final int CLEAR_FAIL = -1;
    public static final int CLEAR_START = 1;
    public static final int CLEAR_COMPLETE = 2;
    private int status;

    public EventClearCommand(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
