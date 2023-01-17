package com.mashiro.renderengine.engine.impl

import com.mashiro.renderengine.math.Position
import com.mashiro.renderengine.math.RenderColor
import com.google.android.filament.Camera.Projection
import dev.romainguy.kotlin.math.Float2

/**
 * 右手坐标系
 *
 * ------- +y ----- -z
 *
 * ---------|----/----
 *
 * ---------|--/------
 *
 * -x - - - 0 - - - +x
 *
 * ------/--|---------
 *
 * ----/----|---------
 *
 * +z ---- -y --------
 */
data class EngineConfig(
    var backGroundColor: RenderColor = RenderColor(0.035f, 0.035f, 0.035f, 1f),
    // 相机位置设置
    var cameraPosition: Position = Position(0f,10f,0f),
    var targetPosition: Position = Position(0f,0f,0f),
    var upVector: Position = Position(0f,0f,-1f),
    var orbitSpeed: Float2 = Float2(0.01f, 0.01f),
    var zoomSpeed: Float = 0.1f,
    // 相机偏移角，相对于(0,1,0)
    var correspondVector: Position = Position(0f, 1f, 0f),
    var minCameraAngle: Float = 0f,
    var maxCameraAngle: Float = 180f,
    // 投影类型
    var cameraProjection: Projection = Projection.ORTHO,
    var defaultScale: Float = 10.0f,
    var minScale: Float = 1.0f,
    var maxScale: Float = 20.0f,
    // 正交投影设置
    var cameraFocalLength: Float = 10f,
    var nearPlane: Double = 1.0,
    var farPlane: Double = 1000.0,
    // 透视投影设置

)