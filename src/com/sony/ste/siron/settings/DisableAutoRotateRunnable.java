package com.sony.ste.siron.settings;

import com.sony.ste.siron.MessageManager;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class DisableAutoRotateRunnable implements Runnable {

    public static final String AUTO_ROTATE_OFF = "com.sony.ste.siron.AUTO_ROTATE_OFF";

    private final Context mContext;
    private final Intent mOrigIntent;
    private final int mActionId;

    public DisableAutoRotateRunnable(Context context, Intent intent, int id) {
        mOrigIntent = intent;
        mContext = context;
        mActionId = id;
    }

    public void run() {
        boolean isSuccess = Settings.System.putInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        MessageManager.getInstance().sendMessage(mActionId, isSuccess, mOrigIntent);
    }
}