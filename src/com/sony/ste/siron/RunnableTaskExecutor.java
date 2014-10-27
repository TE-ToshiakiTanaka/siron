package com.sony.ste.siron;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.ArrayList;

public final class RunnableTaskExecutor implements IRunnableTaskExecutor {

    private final ArrayList<Runnable> mTaskList = new ArrayList<Runnable>();
    private final HandlerThread mWorkerThread;
    private final Handler mWorkerHandler;

    private RunnableTaskExecutor() {
        mWorkerThread = new HandlerThread("ServiceWorkerThread");
        mWorkerThread.start();
        mWorkerHandler = new Handler(mWorkerThread.getLooper());
    }

    public final static IRunnableTaskExecutor createNewExecutor() {
        return new RunnableTaskExecutor();
    }

    public final synchronized void shutdown() {
        final Looper looper = mWorkerThread.getLooper();
        looper.quit();
        try {
            mWorkerThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public final synchronized void submitTask(Runnable runnable) {
        mTaskList.add(runnable);
    }

    public final synchronized void execute() {
        if (!mWorkerThread.isAlive()) {
            throw new IllegalThreadStateException(
                    "Can not execute because underlaying thread is dead");
        }
        if (!mTaskList.isEmpty()) {
            for (Runnable runnable : mTaskList) {
                mWorkerHandler.post(runnable);
            }
            mTaskList.clear();
        }
    }

    public final synchronized void clearTasks() {
        mTaskList.clear();
    }

    public final synchronized boolean hasTasks() {
        return !mTaskList.isEmpty();
    }

    public synchronized void submitTaskFirst(Runnable runnable) {
        mTaskList.add(0, runnable);
    }
}
