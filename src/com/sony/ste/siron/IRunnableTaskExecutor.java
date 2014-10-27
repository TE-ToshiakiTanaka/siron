package com.sony.ste.siron;

public interface IRunnableTaskExecutor {
    void submitTask(Runnable runnable);
    void submitTaskFirst(Runnable runnable);
    void execute() throws IllegalThreadStateException;
    void shutdown();
    void clearTasks();
    boolean hasTasks();
}
