package com.bobo.dubbo.reactor;

import com.alibaba.dubbo.rpc.RpcContext;
import com.bobo.dubbo.reactor.constant.Constant;
import com.bobo.dubbo.reactor.exception.NotFoundOriginDubboMethodException;
import com.bobo.dubbo.reactor.utils.*;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.List;

/**
 * 主逻辑类，解析生成代码需要的信息，并生成代码。
 * @author lizhibo
 * @version 1.0.0, 2020/5/23
 * @since 1.0.0, 2020/5/23
 */
public class DubboServiceInfo {

    private final ServiceClassInfo serviceClassInfo;

    private final OriginDubboServiceInfo originDubboServiceInfo;

    private final AnnotationProcessorUtils annotationProcessorUtils;

    public DubboServiceInfo(TypeElement typeElement, AnnotationProcessorUtils annotationProcessorUtils) {
        this.annotationProcessorUtils = annotationProcessorUtils;
        this.serviceClassInfo = new ServiceClassInfo(typeElement);
        this.originDubboServiceInfo = new OriginDubboServiceInfo(typeElement);
    }

    /**
     * 生成代码
     */
    public void generateCode(Filer filer) throws IOException {
        JavaFile javaFile = JavaFile.builder(serviceClassInfo.packagePath, buildClass())
                .addStaticImport(FutureUtils.class, "*")
                .addStaticImport(RpcContext.class, "getContext")
                .addStaticImport(TypeUtils.class, "*")
                .build();
        javaFile.writeTo(filer);
    }

    private TypeSpec buildClass() {
        return TypeSpec.classBuilder(serviceClassInfo.classSimpleName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(JavaPoetUtils.buildAnnotationSpec(Constant.serviceAnnotationClassName))
                .addSuperinterface(serviceClassInfo.serviceType)
                .addField(
                        FieldSpec.builder(originDubboServiceInfo.serviceTypeName, originDubboServiceInfo.injectFieldName, Modifier.PRIVATE)
                                .addAnnotation(JavaPoetUtils.buildAnnotationSpec(Constant.autowiredAnnotationClassName))
                                .build()
                )
                .addMethods(buildMethods())
                .build();
    }

    private Iterable<MethodSpec> buildMethods() {
        return JavaPoetUtils.buildMethods(serviceClassInfo.typeElement, this::buildMethod);
    }

    private MethodSpec buildMethod(Symbol.MethodSymbol methodSymbol) {
        MethodInfo methodInfo = new MethodInfo(methodSymbol);
        return MethodSpec.methodBuilder(methodInfo.methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(methodInfo.returnTypeName)
                .addParameters(methodInfo.parameterSpecList)
                .addStatement(buildCode(methodInfo, methodInfo.param), Constant.monoClassName)
                .build();
    }

    private String buildCode(MethodInfo methodInfo, String param) {
        if (JavaPoetUtils.isMono(methodInfo.returnType)) {
            return "return cast($T.fromFuture(convert(getContext().asyncCall(() -> "
                    + originDubboServiceInfo.injectFieldName + "." + methodInfo.methodName + "(" + param + ")))))";
        } else {
            String returnTypeName = originDubboServiceInfo.findReturnTypeName(methodInfo);
            return "return $T.fromFuture(convert(getContext().asyncCall(() -> "
                    + originDubboServiceInfo.injectFieldName + "." + methodInfo.methodName + "(" + param + "))))" +
                    ".flatMapIterable(obj -> castTo" + returnTypeName + "(obj))";
        }
    }

    /**
     * 方法信息
     */
    private static class MethodInfo {

        /**
         * 方法返回的typeName
         */
        private final TypeName returnTypeName;

        /**
         * 方法参数列表
         */
        private final List<ParameterSpec> parameterSpecList;

        /**
         * 参数逗号拼接字符串
         */
        private final String param;

        /**
         * 方法名
         */
        private final String methodName;

        /**
         * 方法返回类型SimpleName
         */
        private final Type returnType;

        public MethodInfo(Symbol.MethodSymbol methodSymbol) {
            this.returnTypeName = JavaPoetUtils.getTypeName(methodSymbol.getReturnType());
            this.returnType = methodSymbol.getReturnType();
            this.parameterSpecList = JavaPoetUtils.getParameterSpecList(methodSymbol);
            this.param = JavaPoetUtils.joinParamToString(parameterSpecList);
            this.methodName = methodSymbol.getSimpleName().toString();
        }

        /**
         * 判断参数个数和类型是否相等
         *
         * @param methodInfo 方法信息
         * @return 参数个数和类型是否相等
         */
        public boolean paramTypeEquals(MethodInfo methodInfo) {
            if (parameterSpecList.size() != methodInfo.parameterSpecList.size()) {
                return false;
            }
            for (int i = 0; i < parameterSpecList.size(); i++) {
                ParameterSpec param1 = parameterSpecList.get(i);
                ParameterSpec param2 = methodInfo.parameterSpecList.get(i);
                if (!param1.type.equals(param2.type)) {
                    return false;
                }
            }
            return true;
        }
    }


    /**
     * 原始dubbo接口信息
     */
    private class OriginDubboServiceInfo {

        /**
         * 注入原始dubbo接口用到的类型名字
         */
        private final TypeName serviceTypeName;

        /**
         * 注入原始dubbo接口用到的属性名字
         */
        private final String injectFieldName;

        /**
         * 原始Dubbo接口类型元素
         */
        private final TypeElement serviceTypeElement;

        public OriginDubboServiceInfo(TypeElement serviceTypeElement) {
            this.serviceTypeElement = annotationProcessorUtils.getDubboReactorServiceElement(serviceTypeElement);
            this.serviceTypeName = JavaPoetUtils.getTypeName(serviceTypeElement);
            injectFieldName = StringUtils.lowerFirstChar(annotationProcessorUtils.getSimpleName(serviceTypeElement));
        }

        /**
         * 查找原始dubbo对应方法的返回类型（不带泛型）
         *
         * @param methodInfo 方法信息
         * @return 原始dubbo对应方法的返回类型（不带泛型）
         */
        public String findReturnTypeName(MethodInfo methodInfo) {
            return serviceTypeElement.getEnclosedElements()
                    .stream()
                    .filter(element -> element.getKind() == ElementKind.METHOD)
                    .filter(element -> element.getSimpleName().toString().equals(methodInfo.methodName))
                    .map(element -> new MethodInfo((Symbol.MethodSymbol) element))
                    .filter(method -> method.paramTypeEquals(methodInfo))
                    .findFirst()
                    .map(method -> annotationProcessorUtils.getSimpleName(method.returnType))
                    .orElseThrow(NotFoundOriginDubboMethodException::new);
        }

    }

    /**
     * 生成类的信息
     */
    private class ServiceClassInfo {

        /**
         * 生成代码包的路径
         */
        private final String packagePath;

        /**
         * 生成代码类的名字
         */
        private final String classSimpleName;

        /**
         * 我们自定义reactor接口的类型表示
         */
        private final TypeMirror serviceType;

        private final TypeElement typeElement;

        public ServiceClassInfo(TypeElement typeElement) {
            this.typeElement = typeElement;
            this.serviceType = typeElement.asType();
            this.packagePath = annotationProcessorUtils.getPackagePath(typeElement);
            this.classSimpleName = annotationProcessorUtils.getSimpleName(typeElement) + "Impl";
        }
    }
}
