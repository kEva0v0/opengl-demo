package com.mashiro.opengl.firstopengl

import androidx.lifecycle.ViewModel

abstract class BaseOpenGLViewModel: ViewModel() {

    open fun addPoint(){}

    open fun removeLastPoint(){}

    open fun clearAll(){}
}