package com.sony.ste.siron.wifi;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.sony.ste.siron.debug.Debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class WifiWPSConnectMonitor {

    private final Context mContext;
    private final static String TAG = WifiWPSConnectMonitor.class.getSimpleName();
	private static final String APP_TAG = null;
    private final Semaphore mWPSConnectSemaphore = new Semaphore(0);
    private WifiWPSConnectBroadcastReciever mWPSBroadcastReciever;

    public WifiWPSConnectMonitor(Context context) {
        mWPSConnectSemaphore.drainPermits();
        mContext = context;
        registerReceiver();
    }

    private void registerReceiver() {
        if (Debug.isDevelopmentEnabled()) {
            Log.d(APP_TAG, TAG + " Register WPSConnectBroadcastReciever ");
        }
        if (mWPSBroadcastReciever == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiWPSConnectActivity.WPSCONNECT);
            mWPSBroadcastReciever = new WifiWPSConnectBroadcastReciever();
            mContext.registerReceiver(mWPSBroadcastReciever, filter);
        }
    }

    void onFinished() {
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (Debug.isDevelopmentEnabled()) {
            Log.d(APP_TAG, TAG + " Unregister KeyGuardBroadcastReciever ");
        }
        if (mWPSBroadcastReciever != null) {
            mContext.unregisterReceiver(mWPSBroadcastReciever);
            mWPSBroadcastReciever = null;
        }
    }

    public boolean waitKeyGuardUnlocked(long max_ms) {
        boolean isSuccess = false;
        try {
            isSuccess = mWPSConnectSemaphore.tryAcquire(max_ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return isSuccess;
    }

    private class WifiWPSConnectBroadcastReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mWPSConnectSemaphore.release();
        }
    }
}
