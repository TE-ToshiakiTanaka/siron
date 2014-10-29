package com.sony.ste.siron;

import com.sony.ste.siron.debug.SetDebugRunnable;
import com.sony.ste.siron.wifi.DisableWifiRunnable;
import com.sony.ste.siron.wifi.EnableWifiRunnable;
import com.sony.ste.siron.settings.DisableAutoRotateRunnable;
import com.sony.ste.siron.settings.EnableAutoRotateRunnable;
import com.sony.ste.siron.settings.SetScreenTimeoutRunnable;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
                case SET_SCREEN_TIMOUT_ACTION:
                    String val = intent.getStringExtra(SetScreenTimeoutRunnable.KEY_SCREEN_TIMEOUT);
                    int timeout = SetScreenTimeoutRunnable.DEFAULT_SCREEN_TIMEOUT_TIME;
                    if(this.isInt(val)) timeout = Integer.parseInt(val);
                    Log.d("Siron", "Timeout : " + timeout);
                    SetScreenTimeoutRunnable setScreenTimeout = new SetScreenTimeoutRunnable(
                            timeout, mContext.getContentResolver(), intent, actionId);
                    return new LogRunnable(setScreenTimeout, command);
                case SET_DEBUG_ON_ACTION:
                    SetDebugRunnable setDebug = new SetDebugRunnable(intent, actionId);
                    return new LogRunnable(setDebug, command);
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
