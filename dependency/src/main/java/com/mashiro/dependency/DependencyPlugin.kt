package com.mashiro.dependency

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class DependencyPlugin: Plugin<Project> {
    override fun apply(p0: Project) {
        println("hello! my plugin")
    }
}

object DDoop {
    const val bb = "aa"
}