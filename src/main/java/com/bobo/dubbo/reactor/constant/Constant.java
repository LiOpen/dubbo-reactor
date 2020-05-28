package com.bobo.dubbo.reactor.constant;

import com.squareup.javapoet.ClassName;

/**
 * 常量类
 * @author lizhibo
 * @version 1.0.0, 2020/5/27
 * @since 1.0.0, 2020/5/27
 */
public final class Constant {

    /**
     * Spring Service注解 ClassName表示
     */
    public static final ClassName serviceAnnotationClassName = ClassName.get("org.springframework.stereotype", "Service");

    /**
     * Spring Autowired注解 ClassName表示
     */
    public static final ClassName autowiredAnnotationClassName = ClassName.get("org.springframework.beans.factory.annotation", "Autowired");

    /**
     * Reactor Mono ClassName表示
     */
    public static final ClassName monoClassName = ClassName.get("reactor.core.publisher","Mono" );
}
