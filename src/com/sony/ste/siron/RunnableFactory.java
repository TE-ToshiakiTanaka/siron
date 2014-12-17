package com.sony.ste.siron;

import com.sony.ste.siron.debug.SetDebugRunnable;
import com.sony.ste.siron.wifi.DisableWifiRunnable;
import com.sony.ste.siron.wifi.EnableWifiRunnable;
import com.sony.ste.siron.wifi.WifiConnectRunnable;
import com.sony.ste.siron.wifi.WifiDisConnectRunnable;
import com.sony.ste.siron.wifi.WifiWPSConnectRunnable;
import com.sony.ste.siron.settings.DisableAutoRotateRunnable;
import com.sony.ste.siron.settings.DisableFlightModeRunnable;
import com.sony.ste.siron.settings.DisablePCCRunnable;
import com.sony.ste.siron.settings.EnableAutoRotateRunnable;
import com.sony.ste.siron.settings.EnableFlightModeRunnable;
import com.sony.ste.siron.settings.EnablePCCRunnable;
import com.sony.ste.siron.settings.SetScreenTimeoutRunnable;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunnableFactory implements IRunnableFactory {

    private final Context mContext;
    private static IRunnableFactory sInstance = null;

    public static IRunnableFactory createNewFactory(Context ctx, Handler handler) {
        if (sInstance != null) {
            return sInstance;
        } else {
            return new RunnableFactory(ctx, handler);
        }
    }

    private RunnableFactory(Context ctx, Handler handler) {
        if (ctx == null || handler == null) {
            throw new IllegalArgumentException("No argument can be null");
        }
        mContext = ctx;
        MessageManager.createNewMessageManager(handler);
    }

    // TODO All runnable should have the same constructor args and manage
    // their intent data within their own class.
    public Runnable build(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String intentAction = intent.getAction();
            String command = intentAction.substring(SironService.PACKAGE_NAME_LEN);
            Action action = Action.getActionFromValue(intentAction);
            int actionId = Action.getActionId(action);
            switch (action) {
                case SET_WIFI_ON_ACTION:
                    EnableWifiRunnable wifiEnable = new EnableWifiRunnable(mContext, intent, actionId);
                    return new LogRunnable(wifiEnable, command);
                case SET_WIFI_OFF_ACTION:
                    DisableWifiRunnable wifiDisable = new DisableWifiRunnable(mContext, intent, actionId);
                    return new LogRunnable(wifiDisable, command);
                case SET_AUTO_ROTATE_ON_ACTION:
                    EnableAutoRotateRunnable rotateEnable = new EnableAutoRotateRunnable(mContext, intent, actionId);
                    return new LogRunnable(rotateEnable, command);
                case SET_AUTO_ROTATE_OFF_ACTION:
                    DisableAutoRotateRunnable rotateDisable = new DisableAutoRotateRunnable(mContext, intent, actionId);
                    return new LogRunnable(rotateDisable, command);
                case SET_FLIGHTMODE_ON_ACTION:
                    EnableFlightModeRunnable flightmodeEnable = new EnableFlightModeRunnable(mContext, intent, actionId);
                    return new LogRunnable(flightmodeEnable, command);
                case SET_FLIGHTMODE_OFF_ACTION:
                    DisableFlightModeRunnable flightmodeDisable = new DisableFlightModeRunnable(mContext, intent, actionId);
                    return new LogRunnable(flightmodeDisable, command);
                case SET_SCREEN_TIMOUT_ACTION:
                    String val = intent.getStringExtra(SetScreenTimeoutRunnable.KEY_SCREEN_TIMEOUT);
                    int timeout = SetScreenTimeoutRunnable.DEFAULT_SCREEN_TIMEOUT_TIME;
                    if(this.isInt(val)) timeout = Integer.parseInt(val);
                    SetScreenTimeoutRunnable setScreenTimeout = new SetScreenTimeoutRunnable(
                            timeout, mContext.getContentResolver(), intent, actionId);
                    return new LogRunnable(setScreenTimeout, command);
                case SET_PC_COMPANION_ON_ACTION:
                	EnablePCCRunnable pccEnable = new EnablePCCRunnable(mContext, intent, actionId);
                	return new LogRunnable(pccEnable, command);
                case SET_PC_COMPANION_OFF_ACTION:
                	DisablePCCRunnable pccDisable = new DisablePCCRunnable(mContext, intent, actionId);
                	return new LogRunnable(pccDisable, command);
                case SET_DEBUG_ON_ACTION:
                    SetDebugRunnable setDebug = new SetDebugRunnable(intent, actionId);
                    return new LogRunnable(setDebug, command);
                case DO_WIFI_CONNECT_OPEN:
                case DO_WIFI_CONNECT_WEP:
                case DO_WIFI_CONNECT_PERSONAL:
                case DO_WIFI_CONNECT_ENTERPRISE_EAP_TLS:
                case DO_WIFI_CONNECT_ENTERPRISE_EAP_PEAP:
                case DO_WIFI_CONNECT_ENTERPRISE_EAP_TTLS:
                	WifiConnectRunnable wifiConnect = new WifiConnectRunnable(mContext, intent, actionId);
                	return new LogRunnable(wifiConnect, command);
                case DO_WIFI_CONNECT_WPS:
                	WifiWPSConnectRunnable wifiwps = new WifiWPSConnectRunnable(mContext, intent, actionId);
                	return new LogRunnable(wifiwps, command);
                case DO_WIFI_DISCONNECT:
                	WifiDisConnectRunnable wifiDisConnect = new WifiDisConnectRunnable(mContext, intent, actionId);
                	return new LogRunnable(wifiDisConnect, command);
                default:
            }
        }
        // Unknown intent do nothing.
        // TODO this should not return null but rather an Unknown runnable instead
        return null;
    }

    private boolean isInt(String val){
        String reg = "\\A[-]?[0-9]+\\z";
        Matcher m1 = Pattern.compile(reg).matcher(val);
        return m1.find();
    }

    public String getIntentAction(Message msg) {
        int actionId = MessageManager.getInstance().getIdFromMessage(msg);
        Action action = Action.getActionFromId(actionId);
        return action.getActionValue();
    }

    public boolean getActionResult(Message msg) {
        return MessageManager.getInstance().getResultFromMessage(msg);
    }

    public Intent getOriginalIntent(Message msg) {
        return MessageManager.getInstance().getIntentFromMessage(msg);
    }
}
