package com.woaiqw.appcompiler.utils;


/**
 * Created by haoran on 2018/8/16.
 */

class StringUtils {

    private StringUtils() { /* cannot be instantiated */ }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
