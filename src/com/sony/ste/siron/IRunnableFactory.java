package com.sony.ste.siron;

import android.content.Intent;
import android.os.Message;

public interface IRunnableFactory {
    Runnable build(Intent intent);
    String getIntentAction(Message msg);
    boolean getActionResult(Message msg);
    Intent getOriginalIntent(Message msg);
}
