package com.mashiro.renderengine.utils

import android.util.Log
import com.mashiro.renderengine.math.*
import com.google.android.filament.Camera
import com.google.android.filament.EntityManager
import com.google.android.filament.utils.pow
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.inverse
import dev.romainguy.kotlin.math.transpose
import kotlin.math.max
import com.google.android.filament.utils.Manipulator as CameraManipulator

typealias ClipSpacePosition = Float4

/**
 * CopyFrom @scene-view https://github.com/SceneView/sceneview-android
 * 有些计算错误，进行了修复
 */

/**
 * The camera's projection matrix
 *
 * The projection matrix used for rendering always has its far plane set to infinity.
 * This is why it may differ from the matrix set through setProjection() or setLensProjection().
 */
val Camera.projectionMatrix
    get() = DoubleArray(16).apply { getProjectionMatrix(this) }.toTransform()

fun Camera.setCustomProjection(transform: Transform, near: Float, far: Float) =
    setCustomProjection(
        transform.toFloatArray().map { it.toDouble() }.toDoubleArray(),
        near.toDouble(),
        far.toDouble()
    )

/**
 * The camera's view matrix. The view matrix is the inverse of the model matrix
 */
val Camera.viewMatrix get() = FloatArray(16).apply { getViewMatrix(this) }.toTransform()

/**
 * The camera's model matrix. The model matrix encodes the camera position and orientation, or pose
 */
var Camera.modelMatrix: Transform
    get() = FloatArray(16).apply { getModelMatrix(this) }.toTransform()
    set(value) {
        setModelMatrix(value.toFloatArray())
    }

fun Camera.getW(): Float {
    val viewSpacePosition = this.worldToViewSpace(Position(0f,0f,0f))
    val clipSpacePosition = this.viewSpaceToClipSpace(viewSpacePosition)
    return clipSpacePosition.w
}

/**
 * @see viewPortToClipSpace
 * @see viewSpaceToWorld
 */
fun Camera.clipSpaceToViewSpace(clipSpacePosition: ClipSpacePosition): Position =
    inverse(transpose(projectionMatrix)) * clipSpacePosition.xyz * clipSpacePosition.w

/**
 * @see worldToViewSpace
 * @see clipSpaceToViewPort
 */
fun Camera.viewSpaceToClipSpace(viewSpacePosition: Position): ClipSpacePosition {
    val clipSpacePosition = transpose(projectionMatrix) * ClipSpacePosition(viewSpacePosition.xyz, 1.0f)
    return ClipSpacePosition(clipSpacePosition.xyz / clipSpacePosition.w, w = clipSpacePosition.w)
}

/**
 * @see viewPortToClipSpace
 * @see clipSpaceToViewSpace
 */
fun Camera.viewSpaceToWorld(viewSpacePosition: Position): Position =
    inverse(transpose(viewMatrix)) * viewSpacePosition

/**
 * @see viewSpaceToClipSpace
 * @see clipSpaceToViewPort
 */
fun Camera.worldToViewSpace(worldPosition: Position): Position = transpose(viewMatrix).times(worldPosition)


/**
 * Sets the camera's model matrix.
 *
 * @param eye position of the camera in world space
 * @param center the point in world space the camera is looking at
 * @param up coordinate of a unit vector denoting the camera's "up" direction
 */
fun Camera.lookAt(eye: Position, center: Position, up: Direction) {
    lookAt(
        eye.x.toDouble(), eye.y.toDouble(), eye.z.toDouble(),
        center.x.toDouble(), center.y.toDouble(), center.z.toDouble(),
        up.x.toDouble(), up.y.toDouble(), up.z.toDouble()
    )
}

/**
 * The current orthonormal basis. This is usually called once per frame
 */
fun CameraManipulator.getLookAt(): LookAt =
    (List(3) { FloatArray(3) }).apply {
        getLookAt(this[0], this[1], this[2])
    }.let { (eye, target, upward) ->
        LookAt(eye.toPosition(), target.toPosition(), upward.toDirection())
    }
