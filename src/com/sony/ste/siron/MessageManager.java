package com.sony.ste.siron;

import com.sony.ste.siron.debug.Debug;
import static com.sony.ste.siron.SironService.APP_TAG;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MessageManager {

    private static final String TAG = MessageManager.class.getSimpleName();
    private final Handler mTargetHandler;
    private static MessageManager sInstance;
    private static final int NOT_USED = -1;
    private static final int SUCCESS = 1;
    private static final int FAILURE = 0;

    private MessageManager(Handler targetHandler) {
        mTargetHandler = targetHandler;
    }

    /**
     * Creates and instance of the Message manager. All message will be sent to
     * the same handler.
     *
     * @param targetHandler to send all message to.
     * @return MessageManager instance
     */
    public synchronized static MessageManager createNewMessageManager(Handler targetHandler) {
        if (targetHandler == null) {
            throw new IllegalArgumentException("Handler can not be null");
        }
        sInstance = new MessageManager(targetHandler);
        return sInstance;
    }

    /**
     * Get the instance. See this more like shortcut to an instance that we
     * would otherwise have to include as an argument to each runnable task.
     *
     * @return MessageManager instance, can be null.
     */
    public static MessageManager getInstance() {
        return sInstance;
    }

    /**
     * Get the result from this message.
     *
     * @param msg to retrieve result from
     * @return boolean true is successful, false otherwise.
     */
    public boolean getResultFromMessage(Message msg) {
        switch (msg.arg1) {
            case SUCCESS:
                return true;
            case FAILURE:
                return false;
        }
        throw new IndexOutOfBoundsException(
                "Message does not have correct possible values, either 1 or 0. Use message manager to create messages");
    }

    /**
     * Get the intent from this message.
     *
     * @param msg to retrieve result from
     * @return Intent
     */
    public Intent getIntentFromMessage(Message msg) {
        return ((Intent)msg.obj);
    }

    /**
     * Creates and message from arguments and sends it to the handler associated
     * with this manager. For id < 0 no message will be sent and false returned.
     *
     * @param id to include in this message
     * @param isSuccess true is successful, false otherwise
     * @param intent to include in message.
     * @return true if a message was sent. False otherwise.
     */
    public synchronized boolean sendMessage(int id, boolean isSuccess, Intent intent) {
        if (id < 0 || !mTargetHandler.getLooper().getThread().isAlive()) {
            if (Debug.isUserEnabled()) {
                Log.d(APP_TAG, TAG + " Not sending message because id [" + id
                        + "] is less than zero or underlaying message queue has died ["
                        + !mTargetHandler.getLooper().getThread().isAlive() + "]");
            }
            return false;
        }
        int isSuccessInt = isSuccess ? SUCCESS : FAILURE;
        Message msg = mTargetHandler.obtainMessage(id, isSuccessInt, NOT_USED, intent);
        msg.sendToTarget();
        return true;
    }

    /**
     * Send a message to message handler.
     *
     * @param id Message id.
     * @return true if a message was sent. False otherwise.
     */
    public synchronized boolean sendMessage(int message) {
        if (!mTargetHandler.getLooper().getThread().isAlive()) {
            if (Debug.isUserEnabled()) {
                Log.d(APP_TAG, TAG + " Not sending message because underlaying message queue has died ["
                        + !mTargetHandler.getLooper().getThread().isAlive() + "]");
            }
            return false;
        }
        mTargetHandler.obtainMessage(message).sendToTarget();
        return true;
    }

    public synchronized boolean sendMessage(int message, int arg){
        if (!mTargetHandler.getLooper().getThread().isAlive()) {
            if (Debug.isUserEnabled()) {
                Log.d(APP_TAG, TAG + " Not sending message because underlaying message queue has died ["
                        + !mTargetHandler.getLooper().getThread().isAlive() + "]");
            }
            return false;
        }

        Message msg = mTargetHandler.obtainMessage(message,SUCCESS, arg);
        msg.sendToTarget();

        return true;
    }

    /**
     * Get the id from this message.
     *
     * @return int id
     */
    public int getIdFromMessage(Message msg) {
        return msg.what;
    }
}