package com.sony.ste.siron.debug;

import com.sony.ste.siron.MessageManager;

import android.content.Intent;

public class SetDebugRunnable implements Runnable {

    public static final String DEBUG_ON = "com.sony.ste.siron.DEBUG_ON";
    private final Intent mOrigIntent;
    private final int mActionId;

    public SetDebugRunnable(Intent intent, int id) {
        mOrigIntent = intent;
        mActionId = id;
    }

    @Override
    public void run() {
        Debug.setUserDebug(true);
        MessageManager.getInstance().sendMessage(mActionId, true, mOrigIntent);
    }
}
