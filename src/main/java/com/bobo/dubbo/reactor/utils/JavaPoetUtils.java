package com.bobo.dubbo.reactor.utils;

import com.bobo.dubbo.reactor.constant.Constant;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * JavaPoet工具类
 *
 * @author lizhibo
 * @version 1.0.0, 2020/5/27
 * @since 1.0.0, 2020/5/27
 */
public abstract class JavaPoetUtils {

    public static List<ParameterSpec> getParameterSpecList(Symbol.MethodSymbol methodSymbol) {
        return methodSymbol.getParameters().stream().map(JavaPoetUtils::getParameter).collect(toList());
    }

    public static ParameterSpec getParameter(Symbol.VarSymbol varSymbol) {
        return ParameterSpec.builder(TypeName.get(varSymbol.type), varSymbol.getSimpleName().toString()).build();
    }

    public static String joinParamToString(List<ParameterSpec> parameterSpecList) {
        return parameterSpecList.stream().map(parameterSpec -> parameterSpec.name).collect(joining(","));
    }

    public static AnnotationSpec buildAnnotationSpec(ClassName className) {
        return AnnotationSpec.builder(className).build();
    }

    public static Iterable<MethodSpec> buildMethods(TypeElement typeElement, Function<Symbol.MethodSymbol, MethodSpec> buildMethodSpec) {
        return typeElement.getEnclosedElements().stream()
                .filter(element -> element.getKind().equals(ElementKind.METHOD))
                .map(element -> buildMethodSpec.apply((Symbol.MethodSymbol) element))
                .collect(toList());
    }

    /**
     * 获取typeElement的TypeName
     *
     * @param typeElement 类型元素
     * @return typeElement的TypeName
     */
    public static TypeName getTypeName(TypeElement typeElement) {
        return TypeName.get(typeElement.asType());
    }

    /**
     * 判断Type是否是Mono
     */
    public static boolean isMono(Type type) {
        return getClassName(type).equals(Constant.monoClassName);
    }

    /**
     * 获取ClassName
     */
    private static ClassName getClassName(Type type) {
        return ((ParameterizedTypeName) getTypeName(type)).rawType;
    }

    /**
     * 获取Type的TypeName
     */
    public static TypeName getTypeName(Type type) {
        return TypeName.get(type);
    }
}
