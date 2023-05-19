package com.mashiro.renderengine.node.component

import com.mashiro.renderengine.math.*
import dev.romainguy.kotlin.math.*

/**
 * 坐标系统
 * 表征一个物体相对其父节点or世界原点的偏移
 */
interface TransformComponent: RelationComponent, GeometryComponent {

    /**
     * 相对旋转矩阵&绝对旋转矩阵
     */
    var currentTransform: Transform
        get() = transpose(FloatArray(16).apply {
            transformManager.getTransform(transformInstance, this)
        }.toTransform())
        set(value) {
            transformManager.setTransform(transformInstance, transpose(value).toFloatArray())
        }

    val worldTransform: Transform
        get() = transpose(Transform.of(*FloatArray(16).apply {
            transformManager.getWorldTransform(transformInstance, this)
        }))

    /**
     * 相对坐标
     */
    var transformPosition: Translation
        get() = currentTransform.position
        set(value) {
            this.currentTransform = transformWithQuaternion(value, transformQuaternion, transformScale)
        }

    var worldTransformPosition: Translation
        get() = worldTransform.position
        set(value) {
            transformPosition = getWorldToParent() * value
        }

    /**
     * 相对四元数
     */
    var transformQuaternion: Quaternion
        get() = currentTransform.quaternion
        set(value) {
            currentTransform = transformWithQuaternion(transformPosition, value, transformScale)
        }

    var worldTransformQuaternion: Quaternion
        get() = worldTransform.toQuaternion()
        set(value) {
            transformQuaternion = getWorldToParent().toQuaternion() * value
        }

    /**
     * 相对缩放
     */
    var transformScale: Scale
        get() = currentTransform.scale
        set(value) {
            currentTransform = transformWithQuaternion(transformPosition, transformQuaternion, value)
        }

    var worldTransformScale: Scale
        get() = worldTransform.scale
        set(value) {
            transformScale = (getWorldToParent() * scale(value)).scale
        }

    /**
     * 相对旋转
     */
    var transformRotation: Rotation
        get() = transformQuaternion.toEulerAngles()
        set(value) {
            transformQuaternion = Quaternion.fromEuler(value)
        }

    var worldTransformRotation: Rotation
        get() = worldTransform.quaternion.toEulerAngles()
        set(value) {
            transformQuaternion = getWorldToParent().toQuaternion() * Quaternion.fromEuler(value)
        }


    private fun getWorldToParent(): Transform {
        return getParentInstance()?.let { parentInstance ->
            inverse(Transform.of(*FloatArray(16).apply {
                transformManager.getWorldTransform(parentInstance, this)
            }))
        } ?: Transform()
    }

    fun transform(
        transform: Transform
    ) {
        this.currentTransform = transform
    }

    fun transform(
        translation: Translation = this.transformPosition,
        rotation: Rotation = this.transformRotation,
        scale: Scale = this.transformScale
    ) {
        val currentTransform = transformWithRotation(translation, rotation, scale)
        transform(currentTransform)
    }

    fun addTransform(
        transform: Transform
    ) {
        this.currentTransform = transform.times(this.currentTransform)
    }

    fun addTransform(
        translation: Translation = Translation(),
        rotation: Rotation = Rotation(),
        scale: Scale = Scale(1f,1f,1f)
    ) {
        val currentTransform = transformWithRotation(translation, rotation, scale)
        addTransform(currentTransform)
    }

    /**
     * 将物体以centroid为中心进行旋转
     * 默认centroid为几何中心
     */
    fun rotateWithCentroid(
        quaternion: Quaternion,
        centroid: Position? = null
    ) {
        val worldCentroid = centroid ?: getWorldCentroid()
        if (worldCentroid != null){
            val inverseT = transformWithQuaternion(
                translation = worldCentroid * -1f
            )
            val rotateT = transformWithQuaternion(quaternion = quaternion)
            val originT = transformWithQuaternion(
                translation = worldCentroid
            )
            addTransform(originT*rotateT*inverseT)
        }
    }
}