package com.sony.ste.siron;

import static com.sony.ste.siron.SironService.APP_TAG;
import android.util.Log;

public class LogRunnable implements Runnable {
    Runnable mActionRunnable;
    String mCommand;

    public LogRunnable(Runnable actionRunnable, String command) {
        this.mActionRunnable = actionRunnable;
        this.mCommand = command;
    }

    @Override
    public void run() {
        Log.d(APP_TAG, "Executing command " + mCommand);
        mActionRunnable.run();
        Log.d(APP_TAG, "Done executing command " + mCommand);
    }
}