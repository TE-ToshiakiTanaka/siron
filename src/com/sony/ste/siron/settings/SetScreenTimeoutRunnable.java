package com.sony.ste.siron.settings;

import com.sony.ste.siron.MessageManager;

import android.content.ContentResolver;
import android.content.Intent;
import android.provider.Settings;

public class SetScreenTimeoutRunnable implements Runnable {

    public static final String SET_SCREEN_TIMEOUT = "com.sony.ste.siron.SET_SCREEN_TIMEOUT";
    public static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    public static final int DEFAULT_SCREEN_TIMEOUT_TIME = 72000000;
    private final int mTime;
    private final ContentResolver mCr;
    private final Intent mOrigIntent;
    private final int mActionId;

    public SetScreenTimeoutRunnable (int time, ContentResolver cr, Intent intent, int id) {
        mOrigIntent = intent;
        mTime = time;
        mCr = cr;
        mActionId = id;
    }

    public void run() {
        boolean isSuccess = Settings.System.putInt(mCr, Settings.System.SCREEN_OFF_TIMEOUT, mTime);
        MessageManager.getInstance().sendMessage(mActionId, isSuccess, mOrigIntent);
    }
}