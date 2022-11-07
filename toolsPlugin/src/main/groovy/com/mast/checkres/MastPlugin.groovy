package com.mast.checkres


import org.gradle.api.Plugin
import org.gradle.api.Project

class MastPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.extensions.add('tiny',Tiny)
        project.afterEvaluate {
            new CheckRes().createResTask(project)
        }
    }
}