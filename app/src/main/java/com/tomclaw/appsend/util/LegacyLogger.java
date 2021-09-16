package com.tomclaw.appsend.util;

import android.util.Log;

import com.tomclaw.appsend.core.Config;

/**
 * Created by Solkin on 07.02.2015.
 */
public class LegacyLogger {

    public static void log(String message) {
        Log.d(Config.LOG_TAG, message);
    }

    public static void logWithPrefix(String prefix, String message) {
        Log.d(Config.LOG_TAG, "[" + prefix + "] " + message);
    }

    public static void log(String message, Throwable ex) {
        Log.d(Config.LOG_TAG, message, ex);
    }
}
