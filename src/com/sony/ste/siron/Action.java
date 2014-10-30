package com.sony.ste.siron;

import static com.sony.ste.siron.debug.SetDebugRunnable.DEBUG_ON;
import static com.sony.ste.siron.wifi.EnableWifiRunnable.WIFI_ON;
import static com.sony.ste.siron.wifi.DisableWifiRunnable.WIFI_OFF;
import static com.sony.ste.siron.wifi.WifiConnectRunnable.WIFI_CONNECT_OPEN;
import static com.sony.ste.siron.wifi.WifiConnectRunnable.WIFI_CONNECT_WEP;
import static com.sony.ste.siron.wifi.WifiConnectRunnable.WIFI_CONNECT_PERSONAL;
import static com.sony.ste.siron.wifi.WifiConnectRunnable.WIFI_CONNECT_ENTERPRISE_EAP_TLS;
import static com.sony.ste.siron.wifi.WifiConnectRunnable.WIFI_CONNECT_ENTERPRISE_EAP_PEAP;
import static com.sony.ste.siron.wifi.WifiConnectRunnable.WIFI_CONNECT_ENTERPRISE_EAP_TTLS;
import static com.sony.ste.siron.wifi.WifiDisConnectRunnable.WIFI_DISCONNECT;
import static com.sony.ste.siron.settings.EnableAutoRotateRunnable.AUTO_ROTATE_ON;
import static com.sony.ste.siron.settings.DisableAutoRotateRunnable.AUTO_ROTATE_OFF;
import static com.sony.ste.siron.settings.SetScreenTimeoutRunnable.SET_SCREEN_TIMEOUT;
import static com.sony.ste.siron.generic.ExecuteTasksFinishedNotificationRunnable.MSG_EXECUTE_TASKS_DONE;

public enum Action {
    DO_TASKS_DONE(MSG_EXECUTE_TASKS_DONE),

    SET_WIFI_ON_ACTION(WIFI_ON),
    SET_WIFI_OFF_ACTION(WIFI_OFF),
    SET_AUTO_ROTATE_ON_ACTION(AUTO_ROTATE_ON),
    SET_AUTO_ROTATE_OFF_ACTION(AUTO_ROTATE_OFF),
    SET_SCREEN_TIMOUT_ACTION(SET_SCREEN_TIMEOUT),
    SET_DEBUG_ON_ACTION(DEBUG_ON),
    DO_WIFI_CONNECT_OPEN(WIFI_CONNECT_OPEN),
    DO_WIFI_CONNECT_WEP(WIFI_CONNECT_WEP),
    DO_WIFI_CONNECT_PERSONAL(WIFI_CONNECT_PERSONAL),
    DO_WIFI_CONNECT_ENTERPRISE_EAP_TLS(WIFI_CONNECT_ENTERPRISE_EAP_TLS),
    DO_WIFI_CONNECT_ENTERPRISE_EAP_PEAP(WIFI_CONNECT_ENTERPRISE_EAP_PEAP),
    DO_WIFI_CONNECT_ENTERPRISE_EAP_TTLS(WIFI_CONNECT_ENTERPRISE_EAP_TTLS),
    DO_WIFI_DISCONNECT(WIFI_DISCONNECT),

    UNKNOWN("unknown");

    private final String mIntentActionValue;

    private Action(String intentAction) {
        mIntentActionValue = intentAction;
    }

    public static int getActionId(Action action) {
        return action.ordinal();
    }

    public static int getActionIdFromActionValue(String actionValue) {
        for (Action action : Action.values()) {
            if (actionValue.equalsIgnoreCase(action.getActionValue())) {
                return action.ordinal();
            }
        }
        // Could not find any match in our Action list.
        throw new RuntimeException("Could not find action with value [ " + actionValue
                + " ] in list");
    }

    public static Action getActionFromValue(String actionValue) {
        for (Action action : Action.values()) {
            if (actionValue.equalsIgnoreCase(action.getActionValue())) {
                return action;
            }
        }
        // Could not find any match in our Action list.
        throw new RuntimeException("Could not find action value [ " + actionValue + " ] in list");
    }

    public static Action getActionFromId(int id) {
        if (id >= 0 && id <= Action.values().length) {
            return Action.values()[id];
        }
        throw new IndexOutOfBoundsException("Id + [ " + id + " ] is outside of range, max is "
                + Action.values().length);
    }

    public String getActionValue() {
        return mIntentActionValue;
    }
}
