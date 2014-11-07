package com.sony.ste.siron.wifi;

import com.sony.ste.siron.MessageManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;

public class WifiWPSConnectRunnable implements Runnable {

	private final Context mContext;
    private final int mActionId;
    private final Intent mOrigIntent;
    private WifiWPSConnectMonitor mKeyGuardMonitor = null;
    
    private static final String WPS = "wps";
    
    public static final int MAX_WPS_MONITOR_TIMEOUT = 10000;
    private static final String TAG = WifiWPSConnectRunnable.class.getSimpleName();

	public static final String WIFI_CONNECT_WPS = "com.sony.ste.siron.WIFI_CONNECT_WPS";

    public WifiWPSConnectRunnable(Context context, Intent intent, int id) {
        mContext = context;
        mActionId = id;
        mOrigIntent = intent;
    }

    @Override
    public void run() {
    	validateNotAppThread();
        Handler handler = new Handler(mContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                mKeyGuardMonitor = new WifiWPSConnectMonitor(mContext);
            }
        });
        int monitorCreateTimeMax = 501;
        int time = 0;
        // We need to wait for the monitor object to be created.
        while (mKeyGuardMonitor == null && time < monitorCreateTimeMax) {
            SystemClock.sleep(100);
            time = +100;
        }
        if (mKeyGuardMonitor != null) {
            ComponentName cn = new ComponentName(mContext, WifiWPSConnectActivity.class);
            Intent unlockIntent = new Intent();
            unlockIntent.setComponent(cn);
            unlockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String wps = mOrigIntent.getStringExtra(WPS);
            unlockIntent.putExtra("wps", wps);
            mContext.startActivity(unlockIntent);
            boolean isSuccess = mKeyGuardMonitor.waitKeyGuardUnlocked(MAX_WPS_MONITOR_TIMEOUT);
            mKeyGuardMonitor.onFinished();
            MessageManager.getInstance().sendMessage(mActionId, isSuccess, mOrigIntent);
        } else {
            throw new RuntimeException(TAG + " Failed to create monitor object within "
                    + monitorCreateTimeMax + " ms");
        }
    }

    private void validateNotAppThread() {
        if (Thread.currentThread().getName().equals("main")) {
            throw new RuntimeException(TAG + " Must not be executed on main thread");
        }
    }

}
