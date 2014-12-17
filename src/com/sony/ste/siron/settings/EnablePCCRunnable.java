package com.sony.ste.siron.settings;

import com.sony.ste.siron.MessageManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class EnablePCCRunnable implements Runnable {
    public static final String PC_COMPANION_ENABLE = "com.sony.ste.siron.PC_COMPANION_ENABLE";
    private static final String ACTION_SET_PCC_MODE = "com.sonyericsson.usbux.action.SET_PCC_MODE";
    private static final String KEY_UI_EXTRA = "ui_extra";
    private static final String USBUX_PKG = "com.sonyericsson.usbux";
    private static final String USBUX_SERVICE = "com.sonyericsson.usbux.service.UsbService";
    
    private final Context mContext;
    private final Intent mOrigIntent;
    private final int mActionId;

    public EnablePCCRunnable(Context context, Intent intent, int id) {
        mOrigIntent = intent;
        mContext = context;
        mActionId = id;
    }

    public void run() {
        Intent intent = new Intent(ACTION_SET_PCC_MODE);
        intent.putExtra(KEY_UI_EXTRA, true);
        intent.setClassName(USBUX_PKG, USBUX_SERVICE);
        mContext.startService(intent);

        PackageManager pm = mContext.getPackageManager();
        int prevState = pm.getApplicationEnabledSetting(USBUX_PKG);
        pm.setApplicationEnabledSetting(USBUX_PKG, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        pm.setApplicationEnabledSetting(USBUX_PKG, prevState, 0);
        MessageManager.getInstance().sendMessage(mActionId, true, mOrigIntent);
    }
}
