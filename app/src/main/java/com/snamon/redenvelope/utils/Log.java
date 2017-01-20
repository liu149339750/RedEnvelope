package com.snamon.redenvelope.utils;

public class Log {

	public static boolean DEBUG = true;

	public static void setDebug(boolean isDebug) {
		DEBUG = isDebug;
	}

	public static boolean isDebug() {
		return DEBUG;
	}

	public static void d(String tag, String msg) {
		if (DEBUG)
			android.util.Log.d(tag, msg);
	}

	public static void i(String tag, String msg) {
		if (DEBUG)
			android.util.Log.i(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (DEBUG)
			android.util.Log.w(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (DEBUG)
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (DEBUG)
			android.util.Log.e(tag, msg, tr);
	}

	public static void v(String tag, String msg) {
		if (DEBUG)
			android.util.Log.v(tag, msg);
	}
}
