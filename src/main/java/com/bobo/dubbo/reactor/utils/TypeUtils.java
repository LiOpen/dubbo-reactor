package com.bobo.dubbo.reactor.utils;

import java.util.List;
import java.util.Set;

/**
 * 类型转换工具类
 * @author lizhibo
 * @version 1.0.0, 2020/5/23
 * @since 1.0.0, 2020/5/23
 */
public class TypeUtils {

    public static <T> T cast(Object mono) {
        return (T) mono;
    }

    public static Set castToSet(Object obj) {
        return ((Set) obj);
    }

    public static List castToList(Object obj) {
        return ((List) obj);
    }
}
