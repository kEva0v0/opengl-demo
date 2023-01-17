package com.mashiro.renderengine.engine.api

import com.mashiro.renderengine.engine.impl.EngineConfig

interface Pipeline {
    val engineConfig: EngineConfig
}