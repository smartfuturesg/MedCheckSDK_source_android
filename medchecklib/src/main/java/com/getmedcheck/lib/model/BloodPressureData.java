package com.getmedcheck.lib.model;

import android.os.Parcel;
import android.text.TextUtils;

import com.getmedcheck.lib.constant.Constants;
import com.getmedcheck.lib.utils.DateTimeUtils;
import com.getmedcheck.lib.utils.StringUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;

public class BloodPressureData implements IDeviceData<BloodPressureData> {

    public static final Creator<BloodPressureData> CREATOR = new Creator<BloodPressureData>() {
        @Override
        public BloodPressureData createFromParcel(Parcel in) {
            return new BloodPressureData(in);
        }

        @Override
        public BloodPressureData[] newArray(int size) {
            return new BloodPressureData[size];
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
    @SerializedName("systolicFlag")
    @Expose
    private String systolicFlag = "";
    @SerializedName("diastolicFlag")
    @Expose
    private String diastolicFlag = "";
    @SerializedName("IHB")
    @Expose
    private String IHB = "";
    @SerializedName("systolic")
    @Expose
    private String systolic = "0";
    @SerializedName("diastolic")
    @Expose
    private String diastolic = "0";
    @SerializedName("heartRate")
    @Expose
    private String heartRate = "0";
    @SerializedName("binaryString")
    @Expose
    private String binaryString = "";
    @SerializedName("hexString")
    @Expose
    private String hexString = "";
    @SerializedName("dateTime")
    @Expose
    private long dateTime;

    public BloodPressureData() {
    }

    public BloodPressureData(byte[] rawData, String yearInitial) {
        hexString = StringUtils.bytesToHex(rawData);
        binaryString = StringUtils.byteArrayToBinary(rawData);
        processData(yearInitial);
    }

    protected BloodPressureData(Parcel in) {
        macAddress = in.readString();
        year = in.readString();
        month = in.readString();
        day = in.readString();
        hours = in.readString();
        minute = in.readString();
        amPm = in.readString();
        systolicFlag = in.readString();
        diastolicFlag = in.readString();
        IHB = in.readString();
        systolic = in.readString();
        diastolic = in.readString();
        heartRate = in.readString();
        binaryString = in.readString();
        hexString = in.readString();
        dateTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(macAddress);
        dest.writeString(year);
        dest.writeString(month);
        dest.writeString(day);
        dest.writeString(hours);
        dest.writeString(minute);
        dest.writeString(amPm);
        dest.writeString(systolicFlag);
        dest.writeString(diastolicFlag);
        dest.writeString(IHB);
        dest.writeString(systolic);
        dest.writeString(diastolic);
        dest.writeString(heartRate);
        dest.writeString(binaryString);
        dest.writeString(hexString);
        dest.writeLong(dateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    private void processData(String yearInitial) {

        int yearData = getYearData(yearInitial);

        DecimalFormat df = new DecimalFormat("00");

        year = "" + (2000 + yearData + Integer.parseInt(binaryString.substring(0, 4), 2));
        month = df.format(Integer.parseInt(binaryString.substring(4, 8), 2));
        day = df.format(Integer.parseInt(binaryString.substring(8, 16), 2));
        amPm = "" + Integer.parseInt(binaryString.substring(16, 17), 2);
        int ihb = Integer.parseInt(binaryString.substring(19, 20), 2);
        if (ihb==0) {
            IHB = "00";
        } else {
            IHB = "10";
        }
        hours = df.format(Integer.parseInt(binaryString.substring(20, 24), 2));
        minute = df.format(Integer.parseInt(binaryString.substring(24, 32), 2));

        systolicFlag = Integer.toHexString(Integer.parseInt(binaryString.substring(32, 36), 2));
        diastolicFlag = Integer.toHexString(Integer.parseInt(binaryString.substring(36, 40), 2));

        int systolicAddValue = 100 * getNumber(systolicFlag);
        int diaSystolicAddValue = 100 * getNumber(diastolicFlag);

        systolic = Integer.toHexString(Integer.parseInt(binaryString.substring(40, 48), 2));
        if (StringUtils.isNumber(systolic)) {
            systolic = String.valueOf(Integer.parseInt(systolic) + systolicAddValue);
        }
        diastolic = Integer.toHexString(Integer.parseInt(binaryString.substring(48, 56), 2));
        if (StringUtils.isNumber(diastolic)) {
            diastolic = String.valueOf(Integer.parseInt(diastolic) + diaSystolicAddValue);
        }
        heartRate = "" + Integer.parseInt(binaryString.substring(56, 64), 2);

        String builderDate = day + "-" +
                month + "-" +
                year + " " +
                hours + ":" +
                minute + " " +
                (amPm.equals("0") ? "AM" : "PM");

        dateTime = DateTimeUtils.getTimeFromStringDate(Constants.DATE_TIME_FORMAT_LOCAL, builderDate);
    }

    private int getYearData(String yearInitial) {
        if (yearInitial != null && yearInitial.length() > 4) {
            try {
                return Integer.parseInt(yearInitial.substring(2, 4), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private int getNumber(String numberStr) {
        if (!TextUtils.isEmpty(numberStr)) {
            try {
                return Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public String getPrintData() {
        return "[Systolic=" + systolic +
                ", Diastolic=" + diastolic +
                ", HeartRate=" + heartRate +
                ", Time=" + DateTimeUtils.getFormattedDate(Constants.DATE_TIME_FORMAT_LOCAL, dateTime) + "]";
    }

    @Override
    public String toString() {
        return "[" +
                "hex=" + hexString +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hours=" + hours +
                ", minute=" + minute +
                ", amPm='" + amPm + '\'' +
                ", systolicFlag=" + systolicFlag +
                ", diastolicFlag=" + diastolicFlag +
                ", systolic=" + systolic +
                ", diastolic=" + diastolic +
                ", heartRate=" + heartRate +
                ']';
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIHB() {
        return IHB;
    }

    public void setIHB(String IHB) {
        this.IHB = IHB;
    }

    public String getSystolic() {
        return systolic;
    }

    public void setSystolic(String systolic) {
        this.systolic = systolic;
    }

    public String getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(String diastolic) {
        this.diastolic = diastolic;
    }

    public String getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(String heartRate) {
        this.heartRate = heartRate;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getAmPm() {
        return amPm;
    }

    public void setAmPm(String amPm) {
        this.amPm = amPm;
    }

    public String getSystolicFlag() {
        return systolicFlag;
    }

    public void setSystolicFlag(String systolicFlag) {
        this.systolicFlag = systolicFlag;
    }

    public String getDiastolicFlag() {
        return diastolicFlag;
    }

    public void setDiastolicFlag(String diastolicFlag) {
        this.diastolicFlag = diastolicFlag;
    }

    @Override
    public String getType() {
        return Constants.TYPE_BPM;
    }

    @Override
    public BloodPressureData getObject() {
        return this;
    }
}
