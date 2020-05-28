package com.bobo.dubbo.reactor;

import com.bobo.dubbo.reactor.exception.NotFoundOriginDubboMethodException;
import com.bobo.dubbo.reactor.utils.AnnotationProcessorUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * 注解处理器入口类
 * @author lizhibo
 * @version 1.0.0, 2020/5/23
 * @since 1.0.0, 2020/5/23
 */
public class DubboReactorProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private AnnotationProcessorUtils annotationProcessorUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        annotationProcessorUtils = AnnotationProcessorUtils.newInstance(processingEnv.getElementUtils());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(DubboReactor.class)) {
            if (annotatedElement.getKind() != ElementKind.INTERFACE) {
                error(annotatedElement, "@DubboReactor注解只能标注在接口上");
                return true;
            }
            try {
                new DubboServiceInfo((TypeElement) annotatedElement, annotationProcessorUtils).generateCode(filer);
            } catch (NotFoundOriginDubboMethodException e) {
                error(annotatedElement, "找不到对应的原始dubbo方法");
                return true;
            } catch (IOException e) {
                error(annotatedElement, e.getMessage());
                return true;
            }
        }
        return true;
    }

    private void error(Element e, String msg) {
        messager.printMessage(Kind.ERROR, msg, e);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(DubboReactor.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
