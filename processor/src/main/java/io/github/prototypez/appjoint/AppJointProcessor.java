package io.github.prototypez.appjoint;

import com.google.auto.service.AutoService;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import ect888.com.appjoint_core.ModuleSpec;
import ect888.com.appjoint_core.ModulesSpec;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AppJointProcessor extends AbstractProcessor {

    private static final String JOINT_CLASS_PACKAGE = "io.github.prototypez.appjoint";

    private static final String JOINT_CLASS_SIMPLE_NAME = "AppJointResult";

    private ClassName contextClass = ClassName.get("android.content", "Context");
    private ClassName applicationClass = ClassName.get("android.app", "Application");

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(
                Arrays.asList(
                        ModuleSpec.class.getCanonicalName(),
                        ModulesSpec.class.getCanonicalName()
                )
        );
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        Set<? extends Element> moduleInterfaces = roundEnvironment
                .getElementsAnnotatedWith(ModuleSpec.class);

        Set<? extends Element> modulesInterfaces = roundEnvironment
                .getElementsAnnotatedWith(ModulesSpec.class);

        if (moduleInterfaces != null) {
            for (Element element : moduleInterfaces) {
                if (element.getKind() == ElementKind.CLASS) {
                    String className = element.getSimpleName().toString();
                    String packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName()
                            .toString();
                    ModuleSpec module = element.getAnnotation(ModuleSpec.class);
                    createModuleAppInfoClass(module.value(), ClassName.get(packageName, className));
                }
            }
        }


        if (modulesInterfaces != null) {
            for (Element element : modulesInterfaces) {
                if (element.getKind() == ElementKind.CLASS) {
//                    String className = element.getSimpleName().toString();
//                    String packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName()
//                            .toString();
                    ModulesSpec modules = element.getAnnotation(ModulesSpec.class);
                    String[] moduleNames = modules.value();
                    createAppJointClass(moduleNames);
                }
            }
        }

        return false;
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
            codeBlock.add("  new $T()", ClassName.get(JOINT_CLASS_PACKAGE, JOINT_CLASS_SIMPLE_NAME + "_" + moduleNames[i]));
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
