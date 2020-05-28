package com.bobo.dubbo.reactor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在接口声明这个注解，并将Service属性指向原有的dubbo接口，可以在编译期生成该接口的实现类，并返回reactor中的Mono或Flux类型,例如:
 * <pre>
 * @DubboReactor(service = SecretService.class)
 * public interface SecretReactorService {
 *
 *      Mono<Object> process(String el, String password);
 * }
 * </pre>
 * @author lizhibo
 * @version 1.0.0, 2020/5/23
 * @since 1.0.0, 2020/5/23
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface DubboReactor {

    Class service();
}
