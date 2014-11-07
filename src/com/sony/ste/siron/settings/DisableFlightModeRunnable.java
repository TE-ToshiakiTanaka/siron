package com.sony.ste.siron.settings;

import com.sony.ste.siron.MessageManager;
import com.sony.ste.siron.settings.SettingsProviderWrapper;
import com.sony.ste.siron.settings.SettingsProviderWrapper.Setting;

import android.content.Context;
import android.content.Intent;

public class DisableFlightModeRunnable implements Runnable {

    public static final String FLIGHTMODE_OFF = "com.sony.ste.siron.FLIGHTMODE_OFF";

    private final Context mContext;
    private final Intent mOrigIntent;
    private final int mActionId;

    public DisableFlightModeRunnable(Context context, Intent intent, int id) {
        mOrigIntent = intent;
        mContext = context;
        mActionId = id;
    }

    public void run() {
        boolean isSuccess = SettingsProviderWrapper.setSettingsProvider(Setting.FLIGHT_MODE, true, mContext);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", true);
        mContext.sendBroadcast(intent);
        MessageManager.getInstance().sendMessage(mActionId, isSuccess, mOrigIntent);
    }
}