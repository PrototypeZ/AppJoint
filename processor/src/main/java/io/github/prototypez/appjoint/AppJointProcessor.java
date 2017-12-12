package io.github.prototypez.appjoint;

import com.google.auto.service.AutoService;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import io.github.prototypez.appjoint.core.ModuleSpec;
import io.github.prototypez.appjoint.core.ModulesSpec;
import io.github.prototypez.appjoint.core.RouterProvider;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AppJointProcessor extends AbstractProcessor {

    private Messager messager;

    private static final String JOINT_CLASS_PACKAGE = "io.github.prototypez.appjoint";

    private static final String JOINT_CLASS_SIMPLE_NAME = "AppJointResult";
    private static final String ROUTER_JOINT_CLASS_SIMPLE_NAME = "RouterJointResult";

    private ClassName contextClass = ClassName.get("android.content", "Context");
    private ClassName applicationClass = ClassName.get("android.app", "Application");

    private static final String APP_MODULE_NAME = "app";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(
                Arrays.asList(
                        ModuleSpec.class.getCanonicalName(),
                        ModulesSpec.class.getCanonicalName(),
                        RouterProvider.class.getCanonicalName()
                )
        );
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        messager = processingEnv.getMessager();

        try {
            String moduleName = processModuleSpec(set, roundEnvironment);
            processRouterProvider(set, roundEnvironment, moduleName);
            processModulesSpec(set, roundEnvironment);
        } catch (Throwable throwable) {
            error(throwable.getMessage());
            for (StackTraceElement s : throwable.getStackTrace()) {
                error(s.toString() + "\n");
            }
        }

        return false;
    }

    private String processModuleSpec(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> moduleInterfaces = roundEnvironment
                .getElementsAnnotatedWith(ModuleSpec.class);

        if (moduleInterfaces != null) {
            for (Element element : moduleInterfaces) {
                if (element.getKind() == ElementKind.CLASS) {
                    String className = element.getSimpleName().toString();
                    String packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName()
                            .toString();
                    ModuleSpec module = element.getAnnotation(ModuleSpec.class);
                    createModuleAppInfoClass(module.value(), ClassName.get(packageName, className));
                    return module.value();
                }
            }
        }

        Set<? extends Element> modulesInterfaces = roundEnvironment
                .getElementsAnnotatedWith(ModulesSpec.class);
        if (modulesInterfaces != null) {
            for (Element element : modulesInterfaces) {
                if (element.getKind() == ElementKind.CLASS) {
                    return APP_MODULE_NAME;
                }
            }
        }

        return "";
    }

    private void processModulesSpec(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> modulesInterfaces = roundEnvironment
                .getElementsAnnotatedWith(ModulesSpec.class);

        if (modulesInterfaces != null) {
            for (Element element : modulesInterfaces) {
                if (element.getKind() == ElementKind.CLASS) {
                    ModulesSpec modules = element.getAnnotation(ModulesSpec.class);
                    String[] moduleNames = modules.value();
                    createAppJointClass(moduleNames);
                }
            }
        }
    }

    private void processRouterProvider(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment, String moduleName) {

        if (moduleName.equals("")) return;

        Set<? extends Element> routerProviderInterfaces = roundEnvironment
                .getElementsAnnotatedWith(RouterProvider.class);

        Map<TypeMirror, ClassName> routerImplementMap = new HashMap<>();

        if (routerProviderInterfaces != null) {
            for (Element element : routerProviderInterfaces) {
                if (element.getKind() == ElementKind.CLASS) {
                    String className = element.getSimpleName().toString();
                    String packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName()
                            .toString();
                    RouterProvider module = element.getAnnotation(RouterProvider.class);
                    ClassName annotatedClass = ClassName.get(packageName, className);
                    List<? extends TypeMirror> interfaces = ((TypeElement)element).getInterfaces();
                    interfaces.forEach(o -> routerImplementMap.put(o, annotatedClass));
//                    createModuleAppInfoClass(module.value(), ClassName.get(packageName, className));
                }
            }


            TypeSpec.Builder jointClass = TypeSpec.classBuilder(
                    ClassName.get(JOINT_CLASS_PACKAGE, ROUTER_JOINT_CLASS_SIMPLE_NAME + "_" + moduleName)
            )
                    .addModifiers(Modifier.PUBLIC);

            // Field moduleApplication
            CodeBlock.Builder codeBlock = CodeBlock.builder();
            codeBlock.add("new $T(){{\n", HashMap.class);
            for (Map.Entry<TypeMirror, ClassName> entry : routerImplementMap.entrySet()) {
                codeBlock.add("  put($T.class, $T.class);\n", entry.getKey(), entry.getValue());
            }
            codeBlock.add("}}");
            FieldSpec routerProvidersField = FieldSpec
                    .builder(Map.class, "ROUTER_PROVIDER_MAP")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(codeBlock.build())
                    .build();
            jointClass.addField(routerProvidersField);

            // Create
            TypeSpec jointType = jointClass.build();
            JavaFile javaFile = JavaFile.builder(
                    JOINT_CLASS_PACKAGE,
                    jointType
            )
                    .build();

            // Finally, write the source to file
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    private void createModuleAppInfoClass(String moduleName, ClassName appClassName) {
        TypeSpec.Builder jointClass = TypeSpec.classBuilder(
                ClassName.get(JOINT_CLASS_PACKAGE, JOINT_CLASS_SIMPLE_NAME + "_" + moduleName)
        )
                .addModifiers(Modifier.PUBLIC);

        // Field moduleApplication
        FieldSpec moduleApplicationsField = FieldSpec
                .builder(applicationClass, "INSTANCE")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", appClassName)
                .build();
        jointClass.addField(moduleApplicationsField);

        // Create
        TypeSpec jointType = jointClass.build();
        JavaFile javaFile = JavaFile.builder(
                JOINT_CLASS_PACKAGE,
                jointType
        )
                .build();

        // Finally, write the source to file
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAppJointClass(String[] moduleNames) {
        TypeSpec.Builder jointClass = TypeSpec.classBuilder(
                ClassName.get(JOINT_CLASS_PACKAGE, JOINT_CLASS_SIMPLE_NAME)
        )
                .addModifiers(Modifier.PUBLIC);


        // Field moduleApplication
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.add("$T.asList(\n", Arrays.class);
        for (int i = 0; moduleNames != null && i < moduleNames.length; i++) {
            codeBlock.add("  $T.INSTANCE", ClassName.get(JOINT_CLASS_PACKAGE, JOINT_CLASS_SIMPLE_NAME + "_" + moduleNames[i]));
            if (i == moduleNames.length - 1) {
                codeBlock.add("\n");
            } else {
                codeBlock.add(",\n");
            }
        }
        codeBlock.add(")");
        FieldSpec moduleApplicationsField = FieldSpec
                .builder(List.class, "INSTANCES")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(codeBlock.build())
                .build();
        jointClass.addField(moduleApplicationsField);

        // Field moduleApplication
        CodeBlock.Builder routerProviderCallback = CodeBlock.builder();
        routerProviderCallback.add("new $T(){{\n", HashMap.class);
        routerProviderCallback.add("  putAll($T.ROUTER_PROVIDER_MAP);\n", ClassName.get(JOINT_CLASS_PACKAGE, ROUTER_JOINT_CLASS_SIMPLE_NAME + "_" + APP_MODULE_NAME));
        for (int i = 0; moduleNames != null && i < moduleNames.length; i++) {
            routerProviderCallback.add("  putAll($T.ROUTER_PROVIDER_MAP);\n", ClassName.get(JOINT_CLASS_PACKAGE, ROUTER_JOINT_CLASS_SIMPLE_NAME + "_" + moduleNames[i]));
        }
        routerProviderCallback.add("}}");

        FieldSpec moduleRoutersField = FieldSpec
                .builder(Map.class, "ROUTERS_PROVIDER_MAP")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(routerProviderCallback.build())
                .build();
        jointClass.addField(moduleRoutersField);

        // Create
        TypeSpec jointType = jointClass.build();
        JavaFile javaFile = JavaFile.builder(
                JOINT_CLASS_PACKAGE,
                jointType
        )
                .build();

        // Finally, write the source to file
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void error(String error) {
        messager.printMessage(Diagnostic.Kind.ERROR, error);
    }

    private void debug(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
