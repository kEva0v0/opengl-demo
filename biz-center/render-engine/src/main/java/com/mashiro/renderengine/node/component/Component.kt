package com.mashiro.renderengine.node.component

import android.content.Context
import com.mashiro.renderengine.math.Entity
import com.google.android.filament.Engine
import com.google.android.filament.Scene

interface Component {
    val entity: Entity

    val engine: Engine

    val scene: Scene
}