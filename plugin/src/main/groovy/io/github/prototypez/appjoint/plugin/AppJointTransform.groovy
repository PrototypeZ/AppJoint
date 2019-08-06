package io.github.prototypez.appjoint.plugin

import com.android.build.api.transform.*
import com.google.common.collect.Sets
import groovy.io.FileType
import io.github.prototypez.appjoint.plugin.util.Compressor
import io.github.prototypez.appjoint.plugin.util.Decompression
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.*

class AppJointTransform extends Transform {

    Project mProject

    /**
     * Classes annotated with @ModuleSpec
     */
    def moduleApplications = new ArrayList<AnnotationOrder>()
    /**
     * Classes annotated with @AppSpec
     */
    def appApplications = [:]
    /**
     * Classes annotated with @ServiceProvider
     */
    def routerAndImpl = [:]

    /**
     * The AppJoint class File
     */
    def appJointClassFile

    /*
     * The modified AppJoint class File
     */
    File appJointOutputFile

    AppJointTransform(Project project) {
        mProject = project
    }

    @Override
    String getName() {
        return "appJoint"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return Sets.immutableEnumSet(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES
        )
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        // Maybe contains the AppJoint class to write code into
        def maybeStubs = []
        // Maybe contains @ModuleSpec, @AppSpec or @ServiceProvider
        def maybeModules = []

        transformInvocation.inputs.each { input ->
            // Find annotated classes in jar
            input.jarInputs.each { jarInput ->
                if (!jarInput.file.exists()) return
                mProject.logger.info("jar input:" + jarInput.file.getAbsolutePath())
                mProject.logger.info("jar name:" + jarInput.name)

                def jarName = jarInput.name

                if (jarName == ":core") {
                    // maybe stub in dev and handle them later
                    if (maybeStubs.size() == 0) {
                        maybeStubs.add(jarInput)
                    }
                    // maybe submodule, ':core' could be user's business module
                    maybeModules.add(jarInput)
                } else if (jarName.startsWith(":")) {
                    // maybe submodule
                    maybeModules.add(jarInput)
                } else if (jarName.startsWith("io.github.prototypez:app-joint-core")) {
                    // find the stub
                    maybeStubs.clear()
                    maybeStubs.add(jarInput)
                } else {
                    // normal jars, just copy it to destination
                    def dest = transformInvocation.outputProvider.getContentLocation(jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    mProject.logger.info("jar output path:" + dest.getAbsolutePath())
                    FileUtils.copyFile(jarInput.file, dest)
                }
            }

            // Find annotated classes in dir
            input.directoryInputs.each { dirInput ->
                mProject.logger.info("dirInput.file :" + dirInput.file)

                def outDir = transformInvocation.outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                // dirInput.file is like "build/intermediates/classes/debug"
                int pathBitLen = dirInput.file.toString().length()

                def callback = { File it ->
                    if (it.exists()) {
                        def path = "${it.toString().substring(pathBitLen)}"
                        if (it.isDirectory()) {
                            new File(outDir, path).mkdirs()
                        } else {
                            def output = new File(outDir, path)
                            findAnnotatedClasses(it, output)
                            if (!output.parentFile.exists()) output.parentFile.mkdirs()
                            output.bytes = it.bytes
                        }
                    }
                }

                if (dirInput.changedFiles != null && !dirInput.changedFiles.isEmpty()) {
                    dirInput.changedFiles.keySet().each(callback)
                }
                if (dirInput.file != null && dirInput.file.exists()) {
                    dirInput.file.traverse(callback)
                }
            }
        }

        def repackageActions = [];

        // Inside submodules, find class annotated with
        // @ModuleSpec, @AppSpec or @ServiceProvider
        maybeModules.each { JarInput jarInput ->
            def repackageAction = traversalJar(
                    transformInvocation,
                    jarInput,
                    { File outputFile, File input -> return findAnnotatedClasses(input, outputFile) }
            )
            if (repackageAction) repackageActions.add(repackageAction)
        }

        // Inside local ':core' Module or remote app-joint dependency,
        // find the AppJoint class in jars
        maybeStubs.each { JarInput jarInput ->
            def repackageAction = traversalJar(
                    transformInvocation,
                    jarInput,
                    { File outputFile, File input -> return findAppJointClass(input, outputFile) }
            )
            if (repackageAction) repackageActions.add(repackageAction)
        }

        mProject.logger.info("moduleApplications: $moduleApplications")
        mProject.logger.info("appApplications: $appApplications")
        mProject.logger.info("routerAndImpl: $routerAndImpl")
        mProject.logger.info("appJointClassFile: $appJointClassFile")
        mProject.logger.info("appJointOutputFile: $appJointOutputFile")
        mProject.logger.info("repackageActions: ${repackageActions.size()}")

        if (appJointClassFile == null) {
            throw new RuntimeException("AppJoint class file not found, please check \"io.github.prototypez:app-joint-core:{latest_version}\" is in your dependency graph.")
        }

        // Insert code to AppJoint class
        def inputStream = new FileInputStream(appJointClassFile)
        ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new AppJointClassVisitor(cw)
        cr.accept(classVisitor, 0)
        appJointOutputFile.bytes = cw.toByteArray()
        inputStream.close()

        // Insert code to Application of App
        appApplications.each { File classFile, File output ->
            inputStream = new FileInputStream(classFile)
            ClassReader reader = new ClassReader(inputStream)
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
            ClassVisitor visitor = new ApplicationClassVisitor(writer)

            reader.accept(visitor, 0)
            output.bytes = writer.toByteArray()
            inputStream.close()
        }

        // After all class modifications are done, repackage all deferred jar repackage
        repackageActions.each { Closure action -> action.call() }
    }

    // Visit and change the AppJoint Class
    class AppJointClassVisitor extends ClassVisitor {

        AppJointClassVisitor(ClassVisitor cv) {
            super(Opcodes.ASM5, cv)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
            mProject.logger.info("visiting method: $access, $name, $desc, $signature, $exceptions")
            if (access == 2 && name == "<init>" && desc == "()V") {
                return new AddCodeToConstructorVisitor(methodVisitor)
            }
            return methodVisitor;
        }
    }

    class AddCodeToConstructorVisitor extends MethodVisitor {

        AddCodeToConstructorVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv)
        }

        @Override
        void visitInsn(int opcode) {
            switch (opcode) {
                case Opcodes.IRETURN:
                case Opcodes.FRETURN:
                case Opcodes.ARETURN:
                case Opcodes.LRETURN:
                case Opcodes.DRETURN:
                case Opcodes.RETURN:
                    moduleApplications.sort { a, b -> a.order <=> b.order }
                    for (int i = 0; i < moduleApplications.size(); i++) {
                        mProject.logger.info("insertApplicationAdd order:${moduleApplications[i].order} className:${moduleApplications[i].className}")
                        insertApplicationAdd(moduleApplications[i].className)
                    }
                    routerAndImpl.each { router, impl -> insertRoutersPut(router, impl) }
                    break
            }
            super.visitInsn(opcode)
        }

        /**
         * add "moduleApplications.add(new Application())" statement
         * @param applicationName internal name like "android/app/Application"
         */
        void insertApplicationAdd(String applicationName) {
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitFieldInsn(Opcodes.GETFIELD, "io/github/prototypez/appjoint/AppJoint", "moduleApplications", "Ljava/util/List;")
            mv.visitTypeInsn(Opcodes.NEW, applicationName)
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, applicationName, "<init>", "()V", false)
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
            mv.visitInsn(Opcodes.POP)
        }

        void insertRoutersPut(String router, String impl) {
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitFieldInsn(Opcodes.GETFIELD, "io/github/prototypez/appjoint/AppJoint", "routersMap", "Ljava/util/Map;")
            mv.visitLdcInsn(Type.getObjectType(router))
            mv.visitLdcInsn(Type.getObjectType(impl))
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true)
            mv.visitInsn(Opcodes.POP)
        }
    }

    // Visit and change the classes annotated with @Modules annotation
    class ApplicationClassVisitor extends ClassVisitor {

        boolean onCreateDefined
        boolean attachBaseContextDefined
        boolean onConfigurationChangedDefined
        boolean onLowMemoryDefined
        boolean onTerminateDefined
        boolean onTrimMemoryDefined


        ApplicationClassVisitor(ClassVisitor cv) {
            super(Opcodes.ASM5, cv)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
            mProject.logger.info("visiting method: $access, $name, $desc, $signature, $exceptions")
            switch (name + desc) {
                case "onCreate()V":
                    onCreateDefined = true
                    return new AddCallAppJointMethodVisitor(methodVisitor, "onCreate", "()V", false, false)
                case "attachBaseContext(Landroid/content/Context;)V":
                    attachBaseContextDefined = true
                    return new AddCallAppJointMethodVisitor(methodVisitor, "attachBaseContext", "(Landroid/content/Context;)V", true, false)
                case "onConfigurationChanged(Landroid/content/res/Configuration;)V":
                    onConfigurationChangedDefined = true
                    return new AddCallAppJointMethodVisitor(methodVisitor, "onConfigurationChanged", "(Landroid/content/res/Configuration;)V", true, false)
                case "onLowMemory()V":
                    onLowMemoryDefined = true
                    return new AddCallAppJointMethodVisitor(methodVisitor, "onLowMemory", "()V", false, false)
                case "onTerminate()V":
                    onTerminateDefined = true
                    return new AddCallAppJointMethodVisitor(methodVisitor, "onTerminate", "()V", false, false)
                case "onTrimMemory(I)V":
                    onTrimMemoryDefined = true
                    return new AddCallAppJointMethodVisitor(methodVisitor, "onTrimMemory", "(I)V", false, true)

            }
            return methodVisitor
        }

        @Override
        void visitEnd() {
            if (!attachBaseContextDefined) {
                defineMethod(4, "attachBaseContext", "(Landroid/content/Context;)V", true, false)
            }
            if (!onCreateDefined) {
                defineMethod(1, "onCreate", "()V", false, false)
            }
            if (!onConfigurationChangedDefined) {
                defineMethod(1, "onConfigurationChanged", "(Landroid/content/res/Configuration;)V", true, false)
            }
            if (!onLowMemoryDefined) {
                defineMethod(1, "onLowMemory", "()V", false, false)
            }
            if (!onTerminateDefined) {
                defineMethod(1, "onTerminate", "()V", false, false)
            }
            if (!onTrimMemoryDefined) {
                defineMethod(1, "onTrimMemory", "(I)V", false, true)
            }
            super.visitEnd()
        }

        void defineMethod(int access, String name, String desc, boolean aLoad1, boolean iLoad1) {
            MethodVisitor methodVisitor = this.visitMethod(access, name, desc, null, null)
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
            if (aLoad1) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
            }
            if (iLoad1) {
                methodVisitor.visitVarInsn(Opcodes.ILOAD, 1)
            }
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/app/Application", name, desc, false)
            methodVisitor.visitInsn(Opcodes.RETURN)
            methodVisitor.visitEnd()
        }
    }

    class AddCallAppJointMethodVisitor extends MethodVisitor {

        String name
        String desc
        boolean aLoad1
        boolean iLoad1

        AddCallAppJointMethodVisitor(MethodVisitor mv, String name, String desc, boolean aLoad1, boolean iLoad1) {
            super(Opcodes.ASM5, mv)
            this.name = name
            this.desc = desc
            this.aLoad1 = aLoad1
            this.iLoad1 = iLoad1
        }

        void visitInsn(int opcode) {
            switch (opcode) {
                case Opcodes.IRETURN:
                case Opcodes.FRETURN:
                case Opcodes.ARETURN:
                case Opcodes.LRETURN:
                case Opcodes.DRETURN:
                case Opcodes.RETURN:
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "io/github/prototypez/appjoint/AppJoint", "get", "()Lio/github/prototypez/appjoint/AppJoint;", false)
                    if (aLoad1) {
                        mv.visitVarInsn(Opcodes.ALOAD, 1)
                    }
                    if (iLoad1) {
                        mv.visitVarInsn(Opcodes.ILOAD, 1)
                    }
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "io/github/prototypez/appjoint/AppJoint", name, desc, false)
                    break
            }
            super.visitInsn(opcode)
        }
    }

    /**
     * Find the AppJoint class, this method doesn't change the class file
     * @param file: the class file to be checked
     * @param outputFile: where the modified class should be output to
     * @return whether the file is AppJoint class file
     */
    boolean findAppJointClass(File file, File outputFile) {
        if (!file.exists() || !file.name.endsWith(".class")) {
            return
        }
        boolean found = false;
        def inputStream = new FileInputStream(file)
        ClassReader cr = new ClassReader(inputStream)
        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces)
                if (name == "io/github/prototypez/appjoint/AppJoint") {
                    appJointClassFile = file
                    appJointOutputFile = outputFile
                    found = true
                }
            }
        }, 0)
        inputStream.close()
        return found
    }

    /**
     * Check @ModuleSpec, @AppSpec, @ServiceProvider existence
     * doesn't change any class file
     *
     * @param file : the file to be checked
     * @param output : the class modification output path, if it needs
     * @return whether this class needs to be modify
     */
    boolean findAnnotatedClasses(File file, File output) {
        if (!file.exists() || !file.name.endsWith(".class")) {
            return
        }
        def needsModification = false
        def inputStream = new FileInputStream(file)
        ClassReader cr = new ClassReader(inputStream)
        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            private isModuleSpec = false
            static class AnnotationMethodsVisitor extends AnnotationVisitor {

                AnnotationMethodsVisitor() {
                    super(Opcodes.ASM5)
                }

                @Override
                AnnotationVisitor visitAnnotation(String name, String desc) {
                    mProject.logger.info("Annotation: name=$name desc=$desc")
                    return super.visitAnnotation(name, desc);
                }

                @Override
                void visit(String name, Object value) {
                    mProject.logger.info("Annotation value: name=$name value=$value")
                    super.visit(name, value)
                }
            }

            @Override
            AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                mProject.logger.info("visiting $desc")
                switch (desc) {
                    case "Lio/github/prototypez/appjoint/core/ModuleSpec;":
                        isModuleSpec = true
                        moduleApplications.add(new AnnotationOrder(cr.className))
                        break
                    case "Lio/github/prototypez/appjoint/core/AppSpec;":
                        appApplications[file] = output
                        needsModification = true
                        break
                    case "Lio/github/prototypez/appjoint/core/ServiceProvider;":
                        cr.interfaces.each { routerAndImpl[it] = cr.className }
                        break
                }
                if (isModuleSpec) {
                    return new AnnotationMethodsVisitor() {
                        @Override
                        void visit(String name, Object value) {
                            def moduleApplication = moduleApplications.find({
                                it.className == cr.className
                            })
                            if (moduleApplication) {
                                moduleApplication.order = Integer.valueOf(value)
                            }
                            super.visit(name, value)
                        }
                    }
                }
                return super.visitAnnotation(desc, visible)
            }
        }, 0)
        inputStream.close()
        return needsModification
    }

    class AnnotationOrder {
        private int order = 1000
        private String className

        AnnotationOrder(String className) {
            this.className = className
        }
    }
    /**
     * Unzip jarInput, traversal all files, do something, and repackage it back to jar(Optional)
     * @param transformInvocation From Transform Api
     * @param jarInput From Transform Api
     * @param closure something you wish to do while traversal, return true if you want to repackage later
     * @return repackage action if you return true in closure, null if you return false in every traversal
     */
    static Closure traversalJar(TransformInvocation transformInvocation, JarInput jarInput, Closure closure) {
        def jarName = jarInput.name

        File unzipDir = new File(
                jarInput.file.getParent(),
                jarName.replace(":", "") + "_unzip")
        if (unzipDir.exists()) {
            unzipDir.delete()
        }
        unzipDir.mkdirs()
        Decompression.uncompress(jarInput.file, unzipDir)

        File repackageFolder = new File(
                jarInput.file.getParent(),
                jarName.replace(":", "") + "_repackage"
        )

        FileUtils.copyDirectory(unzipDir, repackageFolder)

        boolean repackageLater = false
        unzipDir.eachFileRecurse(FileType.FILES, { File it ->
            File outputFile = new File(repackageFolder, it.absolutePath.split("_unzip")[1])
            boolean result = closure.call(outputFile, it)
            if (result) repackageLater = true
        })

        def repackageAction = {
            def dest = transformInvocation.outputProvider.getContentLocation(
                    jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
            Compressor zc = new Compressor(dest.getAbsolutePath())
            zc.compress(repackageFolder.getAbsolutePath())
        }

        if (!repackageLater) {
            repackageAction.call()
        } else {
            return repackageAction
        }
    }
}