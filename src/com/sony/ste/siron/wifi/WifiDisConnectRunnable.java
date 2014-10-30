package com.sony.ste.siron.wifi;

import static com.sony.ste.siron.SironService.APP_TAG;

import com.sony.ste.siron.MessageManager;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiDisConnectRunnable implements Runnable {
	public static final String WIFI_DISCONNECT = "com.sony.ste.siron.WIFI_DISCONNECT";
    private final Intent mOrigIntent;
    private final int mActionId;
    private final Context mContext;
    private WifiManager mWifiManager = null;

    public WifiDisConnectRunnable(Context context, Intent intent, int id) {
        mOrigIntent = intent;
        mActionId = id;
        mContext = context;
    }

    @Override
    public void run() {
    	mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		if (mWifiManager != null) {
			Log.i(APP_TAG, "Wi-Fi Manager is successfully initialized.");
			if (!mWifiManager.isWifiEnabled()) {
				mWifiManager.setWifiEnabled(true);
			}
			Log.i(APP_TAG, "Start to disconnect from current network");
			if (this.mWifiManager != null) {
				int networkId = this.mWifiManager.getConnectionInfo().getNetworkId();
				if (networkId != -1) {
					// Disconnect Current Connection
					this.mWifiManager.disconnect();
					this.mWifiManager.removeNetwork(networkId);
				}
				this.mWifiManager.startScan();
			} else {
				Log.e(APP_TAG, "WifiManager was failed to be initialized");
			}
			Log.i(APP_TAG, "Disconnection Finished.");
		}
        MessageManager.getInstance().sendMessage(mActionId, true, mOrigIntent);
    }
}
