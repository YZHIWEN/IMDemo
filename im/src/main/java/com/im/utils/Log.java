package com.im.utils;

/**
 * Created by yzw on 2016/3/13 0013.
 */
public class Log {
    private static final boolean DEBUG = true;

    public static void d(String tag, String msg) {
        if (DEBUG) {
            System.out.println(tag + " " + msg);
        }
    }

}
