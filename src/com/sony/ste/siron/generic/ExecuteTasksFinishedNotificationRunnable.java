package com.sony.ste.siron.generic;

import com.sony.ste.siron.Action;
import com.sony.ste.siron.MessageManager;

public class ExecuteTasksFinishedNotificationRunnable implements Runnable {
    public static final String MSG_EXECUTE_TASKS_DONE = "com.sony.ste.siron.MSG_EXECUTE_TASKS_DONE";
    private int startId;
    private int actionId;

    public ExecuteTasksFinishedNotificationRunnable(int startId) {
       this.startId = startId;
       Action action = Action.getActionFromValue(MSG_EXECUTE_TASKS_DONE);
       actionId = Action.getActionId(action);
    }

    @Override
    public void run() {
        MessageManager.getInstance().sendMessage(actionId, startId);
    }

}
