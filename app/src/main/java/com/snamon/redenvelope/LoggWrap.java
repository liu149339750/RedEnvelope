package com.snamon.redenvelope;

import android.util.Log;

/**
 * @author snamon 2017-01-19
 * 日志包装类 .
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
