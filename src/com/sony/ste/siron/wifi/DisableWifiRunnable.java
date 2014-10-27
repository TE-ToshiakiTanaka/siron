package com.sony.ste.siron.wifi;

import com.sony.ste.siron.MessageManager;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class DisableWifiRunnable implements Runnable {
    public static final String WIFI_OFF = "com.sony.ste.siron.WIFI_OFF";
    private final Intent mOrigIntent;
    private final int mActionId;
    private final Context mContext;

    public DisableWifiRunnable(Context context, Intent intent, int id) {
        mOrigIntent = intent;
        mActionId = id;
        mContext = context;
    }

    @Override
    public void run() {
        WifiManager mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        boolean isSuccess = false;
        if(!mWifiManager.isWifiEnabled()) isSuccess = true;
        else isSuccess = mWifiManager.setWifiEnabled(false);
        MessageManager.getInstance().sendMessage(mActionId, isSuccess, mOrigIntent);
    }
}
