package young.eventdispatcher.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import young.eventdispatcher.DispatcherHandle;
import young.eventdispatcher.ThreadMode;
import young.eventdispatcher.WeakReferenceQueue;
import young.eventdispatcher.annotation.Subscribe;

public class EventDispatcherProcessor extends AbstractProcessor {

    private final static String DISPATCH = "dispatch";
    private final static String CLASS_NAME = "GeneratedDispatcherHandleImpl";
    private final static String PACKAGE_NAME = "young.eventdispatcher";
    private final static String VOID = "void";
    private TypeSpec.Builder mTypeSpec;
    private MethodSpec.Builder mConstructor;
    private MethodSpec.Builder mDispatch;
    private int mSubscribeCount;

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        set.add(Subscribe.class.getName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mDispatch = MethodSpec.methodBuilder(DISPATCH)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(int.class, "methodId")
                .addAnnotation(Override.class)
                .addParameter(Object.class, "subscriber")
                .addParameter(Object.class, "event")
                .beginControlFlow("switch (methodId)")
                .returns(Object.class);

        mConstructor = MethodSpec.constructorBuilder()
                .addParameter(WeakReferenceQueue.class, "unsubscriber")
                .addStatement("super(unsubscriber)")
                .addModifiers(Modifier.PROTECTED);
        mTypeSpec = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.FINAL)
                .superclass(DispatcherHandle.class)
                .addJavadoc("Generated code, do not modify.")
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "{$S, $S}", "deprecation", "unchecked").build());

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return processImpl(annotations, roundEnv);
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            error(writer.toString());
            return true;
        }
    }

    private boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment
            roundEnv) throws IOException {
        processSubscribe(annotations, roundEnv);
        if (annotations.size() > 0) {
            mTypeSpec.addMethod(mConstructor.build());
            mDispatch.endControlFlow();
            mDispatch.addStatement("return null");
            mTypeSpec.addMethod(mDispatch.build());
            JavaFile.builder(PACKAGE_NAME, mTypeSpec.build())
                    .addStaticImport(ThreadMode.POSTING)
                    .addStaticImport(ThreadMode.MAIN)
                    .addStaticImport(ThreadMode.BACKGROUND)
                    .addStaticImport(ThreadMode.ASYNC)
                    .build()
                    .writeTo(processingEnv.getFiler());
        }
        return true;
    }

    private void processSubscribe(Set<? extends TypeElement> annotations, RoundEnvironment
            roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Subscribe.class);
        for (Element element : elements) {
            try {
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    if (!executableElement.getModifiers().contains(Modifier.PUBLIC))
                        throw new Exception();
                    Subscribe subscribe = executableElement.getAnnotation(Subscribe.class);
                    TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
                    List<? extends VariableElement> parameters = executableElement.getParameters();
                    if (parameters.size() <= 0) throw new Exception();

                    ArrayList<Object> registerSubscribeObj = new ArrayList<>();
                    StringBuilder registerSubscribeStr = new StringBuilder();

                    ArrayList<Object> dispatchObj = new ArrayList<>();
                    StringBuilder dispatchStr = new StringBuilder();

                    try {
                        ClassName className = ClassName.bestGuess(typeElement.asType().toString().replaceAll("<.+>", ""));
                        registerSubscribeObj.add(className);
                        dispatchObj.add(className);
                    } catch (Exception e) {
                        TypeName typeName = ClassName.get(typeElement.asType());
                        registerSubscribeObj.add(typeName);
                        dispatchObj.add(typeName);
                    }

                    String name = getSubscribeName();
                    mTypeSpec.addField(
                            FieldSpec.builder(int.class, name, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                                    .initializer("$L", mSubscribeCount++).build()
                    );

                    registerSubscribeObj.add(name);

                    registerSubscribeObj.add(subscribe.threadMode());
                    registerSubscribeObj.add(getClassFromAnnotation(subscribe));
                    registerSubscribeObj.add(subscribe.cache());
                    registerSubscribeObj.add(subscribe.priority());

                    registerSubscribeStr.append("registerSubscribe($T.class, $N, $L, $T.class, $L, $L");

                    dispatchObj.add(0, name);
                    dispatchObj.add(executableElement.getSimpleName().toString());


                    String r = executableElement.getReturnType().toString().toLowerCase();
                    if (!VOID.equals(r)) {
                        dispatchStr.append("case $N: return getSubscribe(subscriber, $T.class).$L(");
                    } else {
                        dispatchStr.append("case $N: getSubscribe(subscriber, $T.class).$L(");
                    }


                    for (int i = 0; i < parameters.size(); i++) {
                        VariableElement parameter = parameters.get(i);
                        if (i == 0) {
                            dispatchStr.append("getEvent(event, $T.class)");
                        } else {
                            dispatchStr.append(", getEvent(event, $T.class)");
                        }
                        dispatchObj.add(parameter);

                        registerSubscribeStr.append(", $T.class");
                        registerSubscribeObj.add(parameter);
                    }


                    registerSubscribeStr.append(")");
                    mConstructor.addStatement(registerSubscribeStr.toString(), registerSubscribeObj.toArray(new Object[]{}));

                    if (!VOID.equals(r)) {
                        dispatchStr.append(")");
                    } else {
                        dispatchStr.append("); break");
                    }
                    mDispatch.addStatement(dispatchStr.toString(), dispatchObj.toArray(new Object[]{}));

                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    private String getSubscribeName() {
        return "SUBSCRIBE_ID_" + mSubscribeCount;
    }

    private TypeMirror getClassFromAnnotation(Subscribe subscribe) {
        try {
            subscribe.flag();
        } catch (MirroredTypeException e) {
            TypeMirror typeMirror = e.getTypeMirror();
            return typeMirror;
        }
        return null;
    }

    private void log(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void error(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
