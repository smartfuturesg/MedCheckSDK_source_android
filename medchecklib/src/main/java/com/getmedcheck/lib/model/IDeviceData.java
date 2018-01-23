package com.getmedcheck.lib.model;

import android.os.Parcelable;


public interface IDeviceData<T> extends Parcelable {

    String getType();

    T getObject();

}
