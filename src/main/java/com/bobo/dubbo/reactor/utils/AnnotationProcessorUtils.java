package com.bobo.dubbo.reactor.utils;

import com.bobo.dubbo.reactor.DubboReactor;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;

/**
 * 注解处理器工具类
 *
 * @author lizhibo
 * @version 1.0.0, 2020/5/27
 * @since 1.0.0, 2020/5/27
 */
public class AnnotationProcessorUtils {

    private final Elements elementUtils;

    private static AnnotationProcessorUtils INSTANCE;

    private AnnotationProcessorUtils(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    /**
     * 创建单例
     */
    public static synchronized AnnotationProcessorUtils newInstance(Elements elementUtils) {
        if (INSTANCE == null) {
            INSTANCE = new AnnotationProcessorUtils(elementUtils);
        }
        return INSTANCE;
    }

    /**
     * 获取包的path
     *
     * @param typeElement 类或者接口元素表示
     * @return 包的路径
     */
    public String getPackagePath(Element typeElement) {
        return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
    }

    /**
     * 获取元素的名字
     */
    public String getSimpleName(Element typeElement) {
        return typeElement.getSimpleName().toString();
    }

    /**
     * 获取元素的名字
     */
    public String getSimpleName(Type type) {
        return type.asElement().getSimpleName().toString();
    }

    /**
     * 获取@DubboReactor注解service属性执行接口的TypeElement元素
     *
     * @param typeElement 类或者接口元素表示
     * @return 原始dubbo接口的TypeElement
     */
    public TypeElement getDubboReactorServiceElement(TypeElement typeElement) {
        try {
            DubboReactor dubboReactor = typeElement.getAnnotation(DubboReactor.class);
            dubboReactor.service();//触发异常
            return null;
        } catch (MirroredTypeException mte) {
            DeclaredType declaredType = (DeclaredType) mte.getTypeMirror();
            return (TypeElement) declaredType.asElement();
        }
    }


}
