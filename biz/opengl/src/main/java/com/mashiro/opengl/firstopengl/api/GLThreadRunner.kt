package com.mashiro.opengl.firstopengl.api

interface GLThreadRunner {
    fun runInGlThread(block: () -> Any)
}