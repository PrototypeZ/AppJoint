package io.github.prototypez.appjoint.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.google.common.collect.Sets
import groovy.io.FileType
import io.github.prototypez.appjoint.plugin.util.Compressor
import io.github.prototypez.appjoint.plugin.util.Decompression
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class AppJointTransform extends Transform {

    Project mProject

    /**
     * Classes annotated with @ModuleSpec
     */
    def moduleApplications = []
    /**
     * Classes annotated with @ModulesSpec
     */
    def appApplications = [:]
    /**
     * Classes annotated with @RouterProvider
     */
    def routerAndImpl = [:]

    /**
     * The AppJoint class File
     */
    def appJointClassFile

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
            // Maybe contains @ModuleSpec, @ModulesSpec or @RouterProvider
            def possibleModules = []

            input.jarInputs.each { jarInput ->
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
            // @ModuleSpec, @ModulesSpec or @RouterProvider
            possibleModules.each {jarInput ->
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
                    findAnnotatedClasses(it)
                }

                // re-package the folder to jar
                def dest = transformInvocation.outputProvider.getContentLocation(
                        jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                Compressor zc = new Compressor(dest.getAbsolutePath())
                zc.compress(repackageFolder.getAbsolutePath())
            }

            // Inside local ':core' Module or remote app-joint dependency,
            // find the AppJoint class in jars
            possibleStubs.each {jarInput ->
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

                // re-package the folder to jar
                def dest = transformInvocation.outputProvider.getContentLocation(
                        jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                Compressor zc = new Compressor(dest.getAbsolutePath())
                zc.compress(repackageFolder.getAbsolutePath())
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
                            findAnnotatedClasses(it)
                            findAppJointClass(it)
                            new File(outDir, path).bytes = it.bytes
                        }
                    }
                }

                if (dirInput.changedFiles == null || dirInput.changedFiles.isEmpty()) {
                    dirInput.file.traverse(callback)
                } else {
                    dirInput.changedFiles.keySet().each(callback)
                }
            }

            mProject.logger.info("moduleApplications: $moduleApplications")
            mProject.logger.info("appApplications: $appApplications")
            mProject.logger.info("routerAndImpl: $routerAndImpl")
            mProject.logger.info("appJointClassFile: $appJointClassFile")
        }
    }

    // Find the AppJoint class
    void findAppJointClass(File file) {
        if (!file.exists() || !file.name.endsWith(".class")) {
            return
        }
        ClassReader cr = new ClassReader(new FileInputStream(file))
        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces)
                if (name == "io/github/prototypez/appjoint/AppJoint") {
                    appJointClassFile = file
                }
            }
        }, 0)
    }

    // Check @ModuleSpec, @ModulesSpec, @RouterProvider existence
    void findAnnotatedClasses(File file) {
        if (!file.exists() || !file.name.endsWith(".class")) {
            return
        }
        ClassReader cr = new ClassReader(new FileInputStream(file))
        cr.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                mProject.logger.info("visiting $desc")
                switch (desc) {
                    case "Lio/github/prototypez/appjoint/core/ModuleSpec;":
                        moduleApplications.add(cr.className)
                        break
                    case "Lio/github/prototypez/appjoint/core/ModulesSpec;":
                        appApplications[cr.className] = file
                        break
                    case "Lio/github/prototypez/appjoint/core/RouterProvider;":
                        cr.interfaces.each { routerAndImpl[it] = cr.className }
                        break
                }
                return super.visitAnnotation(desc, visible)
            }
        }, 0)
    }
}