package com.mashiro.renderengine.engine.api

import com.mashiro.renderengine.math.Position
import com.mashiro.renderengine.utils.*
import com.google.android.filament.Camera
import com.google.android.filament.View
import com.google.android.filament.utils.Manipulator
import dev.romainguy.kotlin.math.Float2

interface CameraPipeline: Pipeline {
    var cameraManipulator: Manipulator

    val camera: Camera

    val view: View

    fun resetCameraManipulator(
        cameraPosition: Position,
        targetPosition: Position,
        upVector: Position
    ) {
        engineConfig.cameraPosition = cameraPosition
        engineConfig.targetPosition = targetPosition
        engineConfig.upVector = upVector
        cameraManipulator = Manipulator.Builder()
            .targetPosition(
                engineConfig.targetPosition.x,
                engineConfig.targetPosition.y,
                engineConfig.targetPosition.z
            )
            .upVector(
                engineConfig.upVector.x,
                engineConfig.upVector.y,
                engineConfig.upVector.z
            )
            .orbitHomePosition(
                engineConfig.cameraPosition.x,
                engineConfig.cameraPosition.y,
                engineConfig.cameraPosition.z
            )
            .viewport(maxOf(view.viewport.width, 1), maxOf(view.viewport.height, 1))
            .orbitSpeed(
                engineConfig.orbitSpeed.x,
                engineConfig.orbitSpeed.y
            )
            .zoomSpeed(engineConfig.zoomSpeed)
            .build(Manipulator.Mode.ORBIT)
    }

    fun getViewportCentroid(): Position {
        return Position(view.viewport.width/2.0f, view.viewport.height/2.0f)
    }

    // 获取放大系数
    fun getZoom(): Double

    fun setCameraProjectType(projection: Camera.Projection) {
        engineConfig.cameraProjection = projection
        updateCameraProjection()
    }

    fun updateCameraProjection() {
        when(engineConfig.cameraProjection){
            Camera.Projection.PERSPECTIVE -> {
                val width = view.viewport.width
                val height = view.viewport.height
                val aspect = width.toDouble() / height.toDouble()
                camera.setLensProjection(engineConfig.cameraFocalLength.toDouble(), aspect, engineConfig.nearPlane, engineConfig.farPlane)
            }
            Camera.Projection.ORTHO -> {
                val width = view.viewport.width
                val height = view.viewport.height
                val aspect = width.toDouble() / height.toDouble()
                camera.setProjection(Camera.Projection.ORTHO,-aspect * getZoom(), aspect * getZoom(), -getZoom(), getZoom(), engineConfig.nearPlane, engineConfig.farPlane)
            }
        }
    }

    fun updateCameraLookAt(){
        cameraManipulator.getLookAt().let { (eye, target, upward) ->
            camera.lookAt(eye, target, upward)
        }
    }

    fun worldToViewport(worldPosition: Position): Position {
        val viewSpacePosition = camera.worldToViewSpace(worldPosition)
        val clipSpacePosition = camera.viewSpaceToClipSpace(viewSpacePosition)
        val viewPortSize = Float2(x = view.viewport.width.toFloat(), y = view.viewport.height.toFloat())
        return ((clipSpacePosition + 1.0f) * 0.5f * viewPortSize).xyz.apply {
            y = view.viewport.height - y
        }
    }

    /**
     * 获取视口坐标到世界坐标的映射
     * 默认坐标为视口中心
     */
    fun viewportToWorld(viewportPosition: Position = getViewportCentroid(), needNormalizer: Boolean = true): Position {
        val viewPortSize =
            Float2(x = view.viewport.width.toFloat(), y = view.viewport.height.toFloat())
        // Normalize between -1 and 1.
        val clipSpacePosition = ClipSpacePosition(
            viewportPosition / viewPortSize * 2.0f - if (needNormalizer) 1.0f else 0.0f,
            w = camera.getW()
        )
        val viewSpacePosition = camera.clipSpaceToViewSpace(clipSpacePosition)
        return camera.viewSpaceToWorld(viewSpacePosition)
    }

    fun distanceFromViewportToWorld(distance: Position): Position {
        return viewportToWorld(distance.xyz, false) - viewportToWorld(Position(), false)
    }
}