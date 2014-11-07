package com.sony.ste.siron.settings;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import java.lang.reflect.Method;

/** This class will translate pre JB MR1 (API 17) to its correct provider. */
public class SettingsProviderWrapper {

    private static final String TAG = SettingsProviderWrapper.class.getSimpleName();
    @SuppressLint("InlinedApi")
	public static final int BATTERY_PLUGGED_ANY = 
    		BatteryManager.BATTERY_PLUGGED_AC 
    		| BatteryManager.BATTERY_PLUGGED_USB 
    		| BatteryManager.BATTERY_PLUGGED_WIRELESS;
    private SettingsProviderWrapper() {
        // No instance
    }

    public enum Setting {
        STAY_AWAKE, FLIGHT_MODE
    }

    public static final boolean setSettingsProvider(Setting setting, boolean enable,
            Context context) {
        boolean ret = false;
        try {
            switch (setting) {
                case STAY_AWAKE:
                    ret = setStayAwake(context, enable);
                    break;
                case FLIGHT_MODE:
                    ret = setFlightMode(context, enable);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e + e.getMessage());
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean setStayAwake(Context context, boolean enable) throws Exception {
        boolean ret = false;
        // 0 is never but it is not defined
        int value = enable ? BATTERY_PLUGGED_ANY : 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ret = Settings.Global.putInt(context.getContentResolver(),
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN, value);
        } else {
            Class<?> clazz = Class.forName("android.provider.Settings$System");
            Method m = clazz.getMethod("putInt", ContentResolver.class, String.class, int.class);
            Object obj = m.invoke(null, context.getContentResolver(), new String(
                    "stay_on_while_plugged_in"), value);
            if (obj instanceof Boolean) {
                ret = (Boolean)obj;
            }
        }
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static boolean setFlightMode(Context context, boolean enable) throws Exception {
        boolean ret = false;
        // No values defined
        int value = enable ? 1 : 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ret = Settings.Global.putInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, value);
        } else {
            Class<?> clazz = Class.forName("android.provider.Settings$System");
            Method m = clazz.getMethod("putInt", ContentResolver.class, String.class, int.class);
            Object obj = m.invoke(null, context.getContentResolver(), new String(
                    "airplane_mode_on"), value);
            if (obj instanceof Boolean) {
                ret = (Boolean)obj;
            }
        }
        return ret;
    }
}