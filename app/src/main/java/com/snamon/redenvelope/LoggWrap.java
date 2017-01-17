package com.snamon.redenvelope;

import android.util.Log;

/**
 * .
 */

public class LoggWrap {

    private static final String TAG = "snamon";

    public static void i(String msg) {
        Log.v(TAG, msg);
    }

    public static void e(String msg) {
        Log.v(TAG, msg);
    }

    public static void e(String msg, Throwable t) {
        Log.v(TAG, msg + " - " + t.getMessage());
    }

}
