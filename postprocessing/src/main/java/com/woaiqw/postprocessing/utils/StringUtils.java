package com.woaiqw.postprocessing.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by haoran on 2018/10/10.
 */
public class StringUtils {

    public static Map<String, String> mapStringToMap(String str) {
        str = str.substring(1, str.length() - 1);
        String[] arr = str.split(",");
        Map<String, String> map = new HashMap<>();
        for (String string : arr) {
            String key = string.split("=")[0];
            String value = string.split("=")[1];
            map.put(key, value);
        }
        return map;
    }

}
