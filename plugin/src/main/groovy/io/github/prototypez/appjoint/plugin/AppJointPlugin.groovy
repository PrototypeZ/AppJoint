package io.github.prototypez.appjoint.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class AppJointPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.android.registerTransform(new AppJointTransform(project))
    }
}