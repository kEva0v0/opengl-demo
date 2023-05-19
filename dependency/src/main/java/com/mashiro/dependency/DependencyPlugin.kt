package com.mashiro.dependency

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

class DependencyPlugin: Plugin<Settings> {
    override fun apply(p0: Settings) {
        println("hello! my plugin")
    }
}