package com.fcbox.rxbus;

import android.util.Log;

/**
 * 日志包装类 .
 */

public class BusLog {

    public static final String TAG = "RxBus";

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable throwable) {
        Log.e(TAG, msg + " - " + throwable.getMessage());
    }
}
