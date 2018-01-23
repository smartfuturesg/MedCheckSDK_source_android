package com.getmedcheck.lib.listener;

import android.content.DialogInterface;

public interface OnDialogClickListener {

    int BUTTON_POSITIVE = 1;
    int BUTTON_NEUTRAL = 0;
    int BUTTON_NEGATIVE = -1;

    /**
     * @param dialog
     * @param buttonType 1 for positive, 0 for neutral and -1 for negative
     */
    void onDialogClick(DialogInterface dialog, int buttonType);
}