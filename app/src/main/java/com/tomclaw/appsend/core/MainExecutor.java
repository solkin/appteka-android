package com.tomclaw.appsend.core;

import android.os.Handler;
import android.os.Looper;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 09.11.13
 * Time: 13:33
 */
public class MainExecutor {

    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public static boolean isMainThread() {
        return mainHandler.getLooper().getThread() == Thread.currentThread();
    }

    /**
     * Performs a task on the main thread. If the current thread is main, execution immediately.
     *
     * @param runnable to execute
     */
    public static void execute(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    /**
     * Executes runnable on the main thread after specified delay.
     *
     * @param runnable to execute
     * @param delay    delay in milliseconds until the code will be executed
     */
    public static void executeLater(Runnable runnable, long delay) {
        mainHandler.postDelayed(runnable, delay);
    }
}
