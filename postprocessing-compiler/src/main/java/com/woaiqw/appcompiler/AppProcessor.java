package com.woaiqw.appcompiler;

import com.woaiqw.appcompiler.utils.Constants;
import com.woaiqw.appcompiler.utils.Logger;
import com.woaiqw.postprocessingannotation.App;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public class AppProcessor extends AbstractProcessor {

    private Logger logger;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        logger = new Logger(processingEnv.getMessager());
        filer = processingEnv.getFiler();

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(App.class);

        logger.info("start --- processor");
        for (Element element : elements) {
            generateJavaAppProxyFile(element);
        }
        logger.info("generate --- end !!!");
        return true;
    }

    private void generateJavaAppProxyFile(Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            TypeElement classElement = (TypeElement) element;
            PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
            String className = classElement.getSimpleName().toString();
            String path = packageElement.getQualifiedName().toString() + "." + className;
            logger.info("path:" + path);
            App annotation = element.getAnnotation(App.class);

            String name = annotation.name();
            int priority = annotation.priority();
            boolean async = annotation.async();
            long delay = annotation.delay();
            String generateClassName = name + "$$Proxy";

            StringBuilder builder = new StringBuilder();
            builder.append("package ");
            builder.append(Constants.PACKAGE_NAME);
            builder.append(";");
            builder.append("\n");
            builder.append("\n");

            builder.append("/**");
            builder.append("\n");
            builder.append(Constants.EXPLAIN);
            builder.append("\n");
            builder.append(" */");

            builder.append("\n");
            builder.append("\n");
            builder.append("public final class ");
            builder.append(generateClassName);
            builder.append("{");
            builder.append("\n");
            builder.append("\n");

            builder.append("public static final String path = \"");
            builder.append(path);
            builder.append("\";");

            builder.append("public static final String name = \"");
            builder.append(name);
            builder.append("\";");

            builder.append("public static final int priority = ");
            builder.append(priority);
            builder.append(";");

            builder.append("public static final boolean async = ");
            builder.append(async);
            builder.append(";");

            builder.append("public static final long delay = ");
            builder.append(delay);
            builder.append(";");

            builder.append("\n");
            builder.append("\n");
            builder.append("}");


            try {
                JavaFileObject source = filer.createSourceFile(Constants.PACKAGE_NAME + generateClassName);
                Writer writer = source.openWriter();
                writer.write(builder.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            logger.info(">>>" + generateClassName + "<<<");
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Constants.ANNOTATION_ACTION_PATH);
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

}
