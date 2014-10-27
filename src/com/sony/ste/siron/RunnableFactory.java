package com.sony.ste.siron;

import com.sony.ste.siron.debug.SetDebugRunnable;
import com.sony.ste.siron.wifi.DisableWifiRunnable;
import com.sony.ste.siron.wifi.EnableWifiRunnable;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class RunnableFactory implements IRunnableFactory {

    private final Context mContext;

    // TODO Move to some constant class
    //private static final long DEFAULT_WAIT_TIME = 10000;
    //private static final long MEDIUM_WAIT_TIME = 30000;
    //private static final long LONG_WAIT_TIME = 60000;

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
