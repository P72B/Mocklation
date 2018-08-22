package de.p72b.mocklation.util;

import android.util.Log;

public class Logger {
    private static boolean mEnabled = true;

    private Logger() {}

    public static void enableLogging(boolean enable) {
        mEnabled = enable;
    }

    public static void i(String tag, String string) {
        if (mEnabled) {
            Log.i(tag, string);
        }
    }

    public static void e(String tag, String string) {
        if (mEnabled) {
            Log.e(tag, string);
        }
    }

    public static void e(String tag, String string, Throwable throwable) {
        if (mEnabled) {
            Log.e(tag, string, throwable);
        }
    }

    public static void d(String tag, String string) {
        if (mEnabled) {
            Log.d(tag, string);
        }
    }

    public static void v(String tag, String string) {
        if (mEnabled) {
            Log.v(tag, string);
        }
    }

    public static void w(String tag, String string) {
        if (mEnabled) {
            Log.w(tag, string);
        }
    }
}
