package com.bobo.dubbo.reactor.utils;

/**
 * @author lizhibo
 * @version 1.0.0, 2020/5/27
 * @since 1.0.0, 2020/5/27
 */
public abstract class StringUtils {

    public static String lowerFirstChar(String str) {
        return str.substring(0, 1) + str.substring(1);
    }
}
