package com.getmedcheck.lib.utils;


import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class StringUtils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getByteInStringForm(byte[] data) {
        StringBuffer strNoByte = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            strNoByte.append(data[i] + " ");
        }
        //Log.e(TAG, "Byte: " + Arrays.toString(data));
        return strNoByte.toString() + "[" + bytesToHex(data) + "] = [" + Arrays.toString(byteArrayToBinaryByteArray(data)) + "]";
    }

    public static String getByteInString(byte[] data) {
        StringBuffer strNoByte = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            strNoByte.append(data[i] + " ");
        }
        //Log.e(TAG, "Byte: " + Arrays.toString(data));
        return strNoByte.toString() + "[" + bytesToHex(data) + "] = [" + Arrays.toString(byteArrayToBinaryByteArray(data)) + "]";
    }

    public static byte[] byteArrayToBinaryByteArray(byte[] bytes) {
        String bytesString = byteArrayToBinary(bytes);

        byte[] byteArray = new byte[bytesString.length()];
        for (int index = 0; index < byteArray.length; index++) {
            byteArray[index] = (byte) (bytesString.charAt(index) - 48);
        }
        return byteArray;
    }

    public static String getByteToBinary(byte data) {
        return String.format("%8s", Integer.toBinaryString(data & 0xFF)).replace(' ', '0');
    }

    // 8bit binary
    public static String byteArrayToBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(getByteToBinary(aByte));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String binaryToHex(String binaryString) {
        StringBuilder hexString = new StringBuilder();
        ArrayList<String> binaryGroup = convertStringToGroupArray(binaryString, 8);

        for (String binary : binaryGroup) {
            hexString.append(Integer.toHexString(Integer.parseInt(binary, 2))).append(" ");
        }
        return hexString.toString();
    }

    public static ArrayList<String> convertStringToGroupArray(String string, int groupSize) {
        if (groupSize > string.length()) {
            return null;
        }
        ArrayList<String> strings = new ArrayList<>();

        int index = 0;
        while (index < string.length() - 1) {
            strings.add(string.substring(index, index + Math.min(groupSize, Math.abs(string.length() - index))));
            index += groupSize;
        }

        return strings;
    }

    public static boolean isDigit(String text) {
        boolean isDigit = true;
        if (!TextUtils.isEmpty(text)) {
            for (int i = 0; i < text.length(); i++) {
                if (!Character.isDigit(text.charAt(i))) {
                    isDigit = false;
                    break;
                }
            }
        } else {
            isDigit = false;
        }
        return isDigit;
    }

    public static boolean isNumber(String text) {
        if (!TextUtils.isEmpty(text)) {
            Pattern pattern = Pattern.compile("^(([0-9]*)|(([0-9]*)\\.([0-9]*)))$");
            return pattern.matcher(text).matches();
        }
        return true;
    }
}
