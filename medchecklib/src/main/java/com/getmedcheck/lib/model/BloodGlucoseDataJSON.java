package com.getmedcheck.lib.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by letsnurture on 19/1/18.
 */

public class BloodGlucoseDataJSON {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("data")
    @Expose
    private ArrayList<BloodGlucoseData> data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<BloodGlucoseData> getData() {
        return data;
    }

    public void setData(ArrayList<BloodGlucoseData> data) {
        this.data = data;
    }
}
