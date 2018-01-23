package com.getmedcheck.lib.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    private static final String TAG = PreferenceHelper.class.getSimpleName();
    private static PreferenceHelper sInstance;
    protected Context mContext;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private PreferenceHelper(Context context) {
        mContext = context;
        int stringId = context.getApplicationInfo().labelRes;
        pref = context.getSharedPreferences(context.getString(stringId) + "_SharedPreferences", 0);
        editor = pref.edit();
    }

    public static synchronized PreferenceHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new PreferenceHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setValue(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void setValue(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void setValue(String key, double value) {
        setValue(key, Double.toString(value));
    }

    public void setValue(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void setValue(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public void setValue(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public String getStringValue(String key, String defaultValue) {
        return pref.getString(key, defaultValue);
    }

    public int getIntValue(String key, int defaultValue) {
        return pref.getInt(key, defaultValue);
    }

    public long getLongValue(String key, long defaultValue) {
        return pref.getLong(key, defaultValue);
    }

    public float getFloatValue(String key, float defaultValue) {
        return pref.getFloat(key, defaultValue);
    }

    public boolean getBoolanValue(String keyFlag, boolean defaultValue) {
        return pref.getBoolean(keyFlag, defaultValue);
    }

    public void removeKey(String key) {

        if (editor != null) {
            editor.remove(key);
            editor.commit();
        }
    }

    public void clear() {
        editor.clear().commit();
    }

    public boolean isContain(String key) {
        return pref.contains(key);
    }

}


