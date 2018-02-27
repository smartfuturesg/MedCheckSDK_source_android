package com.getmedcheck.lib.model;

import android.os.Parcel;

import com.getmedcheck.lib.constant.Constants;
import com.getmedcheck.lib.utils.DateTimeUtils;
import com.getmedcheck.lib.utils.StringUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;

public class BloodGlucoseData implements IDeviceData<BloodGlucoseData> {

    public static final Creator<BloodGlucoseData> CREATOR = new Creator<BloodGlucoseData>() {
        @Override
        public BloodGlucoseData createFromParcel(Parcel in) {
            return new BloodGlucoseData(in);
        }

        @Override
        public BloodGlucoseData[] newArray(int size) {
            return new BloodGlucoseData[size];
        }
    };


    @SerializedName("macAddress")
    @Expose
    private String macAddress = "";
    @SerializedName("year")
    @Expose
    private String year = "0";
    @SerializedName("month")
    @Expose
    private String month = "0";
    @SerializedName("day")
    @Expose
    private String day = "0";
    @SerializedName("hours")
    @Expose
    private String hours = "0";
    @SerializedName("minute")
    @Expose
    private String minute = "0";
    @SerializedName("amPm")
    @Expose
    private String amPm = "";
    @SerializedName("low")
    @Expose
    private String low = "0";
    @SerializedName("high")
    @Expose
    private String high = "0";
    @SerializedName("acPc")
    @Expose
    private String acPc = "";
    @SerializedName("binaryString")
    @Expose
    private String binaryString = "";
    @SerializedName("hexString")
    @Expose
    private String hexString = "";
    @SerializedName("dateTime")
    @Expose
    private Long dateTime;

    public BloodGlucoseData() {
    }

    public BloodGlucoseData(String binaryString, String yearInitial) {
        this.binaryString = binaryString;
        hexString = StringUtils.binaryToHex(binaryString);
        processData(yearInitial);
    }

    protected BloodGlucoseData(Parcel in) {
        macAddress = in.readString();
        binaryString = in.readString();
        hexString = in.readString();
        year = in.readString();
        month = in.readString();
        day = in.readString();
        hours = in.readString();
        minute = in.readString();
        amPm = in.readString();
        low = in.readString();
        high = in.readString();
        acPc = in.readString();
        dateTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(macAddress);
        dest.writeString(binaryString);
        dest.writeString(hexString);
        dest.writeString(year);
        dest.writeString(month);
        dest.writeString(day);
        dest.writeString(hours);
        dest.writeString(minute);
        dest.writeString(amPm);
        dest.writeString(low);
        dest.writeString(high);
        dest.writeString(acPc);
        dest.writeLong(dateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getAcPcStringValue() {
        switch (acPc) {
            case "01":
                return "Before Meal";
            case "10":
                return "After Meal";
            default:
                return "None";
        }
    }

    private void processData(String yearInitial) {

        DecimalFormat df = new DecimalFormat("00");
        amPm = "" + Integer.parseInt(binaryString.substring(0, 1), 2);
        // year base is 2000 as per documentation of device
        year = "" + (2000 + Integer.parseInt(binaryString.substring(1, 8), 2));
        month = df.format(Integer.parseInt(binaryString.substring(8, 12), 2));
        hours = df.format(Integer.parseInt(binaryString.substring(12, 16), 2));
        day = df.format(Integer.parseInt(binaryString.substring(16, 24), 2));
        acPc = binaryString.substring(24, 26);
        minute = df.format(Integer.parseInt(binaryString.substring(26, 32), 2));
        int intLow = Integer.parseInt(binaryString.substring(32, 40), 2);
        int intHigh = Integer.parseInt(binaryString.substring(40, 48), 2);

        if (intLow !=0) {
            intHigh = Integer.parseInt(binaryString.substring(32, 48), 2);
        }
        low = "" + intLow;
        high = "" + intHigh;

        String builderDate = day + "-" +
                month + "-" +
                year + " " +
                hours + ":" +
                minute + " " +
                (amPm.equals("0") ? "AM" : "PM");
        dateTime = DateTimeUtils.getTimeFromStringDate(Constants.DATE_TIME_FORMAT_LOCAL, builderDate);
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getAcPc() {
        return acPc;
    }

    public void setAcPc(String acPc) {
        this.acPc = acPc;
    }

    public String getBinaryString() {
        return binaryString;
    }

    public void setBinaryString(String binaryString) {
        this.binaryString = binaryString;
    }

    public String getHexString() {
        return hexString;
    }

    public void setHexString(String hexString) {
        this.hexString = hexString;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String getType() {
        return Constants.TYPE_BGM;
    }

    @Override
    public BloodGlucoseData getObject() {
        return this;
    }
}
