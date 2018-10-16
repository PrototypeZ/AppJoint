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
    def moduleApplications = []
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

    boolean appJointClassInJar = false

    File appJointJarRepackageFolder

    File appJointJarDest

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

        transformInvocation.inputs.each { input ->

            // Maybe contains the AppJoint class to write code into
            def possibleStubs = []
            // Maybe contains @ModuleSpec, @AppSpec or @ServiceProvider
            def possibleModules = []

            input.jarInputs.each { jarInput ->
                if (!jarInput.file.exists()) return
                mProject.logger.info("jar input:" + jarInput.file.getAbsolutePath())
                def jarName = jarInput.name
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                mProject.logger.info("jar name:" + jarName)

                if (jarName == ":core") {
                    // maybe stub in dev and handle them later
                    possibleStubs.add(jarInput)
                    // maybe submodule, ':core' could be user's business module
                    possibleModules.add(jarInput)
                } else if (jarName.startsWith(":")) {
                    // maybe submodule
                    possibleModules.add(jarInput)
                } else if (jarName.startsWith("io.github.prototypez:app-joint-core")) {
                    // find the stub
                    possibleStubs.add(jarInput)
                } else {
                    def dest = transformInvocation.outputProvider.getContentLocation(jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    mProject.logger.info("jar output path:" + dest.getAbsolutePath())
                    FileUtils.copyFile(jarInput.file, dest)
                }
            }

            // Inside submodules, find class annotated with
            // @ModuleSpec, @AppSpec or @ServiceProvider
            possibleModules.each { jarInput ->
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

                unzipDir.eachFileRecurse(FileType.FILES) { File it ->
                    findAnnotatedClasses(it, repackageFolder)
                }

                // re-package the folder to jar
                def dest = transformInvocation.outputProvider.getContentLocation(
                        jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                Compressor zc = new Compressor(dest.getAbsolutePath())
                zc.compress(repackageFolder.getAbsolutePath())
            }

            // Inside local ':core' Module or remote app-joint dependency,
            // find the AppJoint class in jars
            possibleStubs.each { jarInput ->
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

                unzipDir.eachFileRecurse(FileType.FILES) { File it ->
                    findAppJointClass(it)
                }

                if (appJointClassFile != null) {
                    // find the AppJoint class in jar
                    appJointClassInJar = true
                }

                // re-package the folder to jar
                def dest = transformInvocation.outputProvider.getContentLocation(
                        jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                if (appJointClassFile == null) {
                    Compressor zc = new Compressor(dest.getAbsolutePath())
                    zc.compress(repackageFolder.getAbsolutePath())
                } else {
                    appJointJarDest = dest
                    appJointJarRepackageFolder = repackageFolder
                }
            }

            // Find annotated classes and AppJoint class in dir
            input.directoryInputs.each { dirInput ->
                def outDir = transformInvocation.outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                // dirInput.file is like "build/intermediates/classes/debug"
                int pathBitLen = dirInput.file.toString().length()
                mProject.logger.info("dirInput.file :" + dirInput.file)
                def callback = { File it ->
                    if (it.exists()) {
                        def path = "${it.toString().substring(pathBitLen)}"
                        if (it.isDirectory()) {
                            new File(outDir, path).mkdirs()
                        } else {
                            def output = new File(outDir, path)
                            findAnnotatedClasses(it, output)
                            findAppJointClass(it)
                            if (!output.parentFile.exists()) output.parentFile.mkdirs()
                            output.bytes = it.bytes
                        }
                    }
                }

                if (dirInput.changedFiles == null || dirInput.changedFiles.isEmpty()) {
                    dirInput.file.traverse(callback)
                } else {
                    dirInput.changedFiles.keySet().each(callback)
                }
            }

        }
        mProject.logger.info("moduleApplications: $moduleApplications")
        mProject.logger.info("appApplications: $appApplications")
        mProject.logger.info("routerAndImpl: $routerAndImpl")
        mProject.logger.info("appJointClassFile: $appJointClassFile")

        // Insert code to AppJoint class
      def inputStream = new FileInputStream(appJointClassFile)
      ClassReader cr = new ClassReader(inputStream)
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new AppJointClassVisitor(cw)

        cr.accept(classVisitor, 0)

        def outputFile = new File(
                appJointJarRepackageFolder,
                "io/github/prototypez/appjoint/AppJoint.class"
        )
        outputFile.bytes = cw.toByteArray()
      inputStream.close()

        Compressor zc = new Compressor(appJointJarDest.getAbsolutePath())
        zc.compress(appJointJarRepackageFolder.getAbsolutePath())

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

      moduleApplications = []
      appApplications = [:]
      routerAndImpl = [:]
      appJointClassFile = null
      appJointClassInJar = false
      appJointJarRepackageFolder = null
      appJointJarDest = null

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
                    moduleApplications.each { insertApplicationAdd(it) }
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

    // Find the AppJoint class
    void findAppJointClass(File file) {
        if (!file.exists() || !file.name.endsWith(".class")) {
            return
        }
      def inputStream = new FileInputStream(file)
      ClassReader cr = new ClassReader(inputStream)
        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces)
                if (name == "io/github/prototypez/appjoint/AppJoint") {
                    appJointClassFile = file
                }
            }
        }, 0)
      inputStream.close()
    }

    // Check @ModuleSpec, @AppSpec, @ServiceProvider existence
    void findAnnotatedClasses(File file, File output) {
        if (!file.exists() || !file.name.endsWith(".class")) {
            return
        }
      def inputStream = new FileInputStream(file)
      ClassReader cr = new ClassReader(inputStream)
        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                mProject.logger.info("visiting $desc")
                switch (desc) {
                    case "Lio/github/prototypez/appjoint/core/ModuleSpec;":
                        moduleApplications.add(cr.className)
                        break
                    case "Lio/github/prototypez/appjoint/core/AppSpec;":
                        appApplications[file] = output
                        break
                    case "Lio/github/prototypez/appjoint/core/ServiceProvider;":
                        cr.interfaces.each { routerAndImpl[it] = cr.className }
                        break
                }
                return super.visitAnnotation(desc, visible)
            }
        }, 0)
      inputStream.close()
    }
}