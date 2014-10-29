package com.sony.ste.siron.settings;

import com.sony.ste.siron.MessageManager;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class EnableAutoRotateRunnable implements Runnable {

    public static final String AUTO_ROTATE_ON = "com.sony.ste.siron.AUTO_ROTATE_ON";

    private final Context mContext;
    private final Intent mOrigIntent;
    private final int mActionId;

    public EnableAutoRotateRunnable(Context context, Intent intent, int id) {
        mOrigIntent = intent;
        mContext = context;
        mActionId = id;
    }

    public void run() {
        boolean isSuccess = Settings.System.putInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
        MessageManager.getInstance().sendMessage(mActionId, isSuccess, mOrigIntent);
    }
}