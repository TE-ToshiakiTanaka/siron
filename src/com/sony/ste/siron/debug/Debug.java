package com.sony.ste.siron.debug;

public class Debug {
    private Debug() {}
    private volatile static boolean isUserDebugEnabled = false;
    private static boolean isDevelopmentDebugEnabled = false;

    public static boolean isUserEnabled() {
        return isUserDebugEnabled || isDevelopmentDebugEnabled;
    }

    public static boolean isDevelopmentEnabled() {
        return isDevelopmentDebugEnabled;
    }

    public synchronized static void setUserDebug(boolean isEnabled) {
        isUserDebugEnabled = isEnabled;
    }
}
