package com.mashiro.filament.render

import android.app.Activity
import android.view.SurfaceView
import com.google.android.filament.*
import com.mashiro.filament.MyModelViewer
import com.mashiro.filament.utils.AssetUtils
import com.mashiro.filament.model.NormalPoint

class PointCloudRender(surfaceView: SurfaceView, modelViewer: MyModelViewer) : BaseRenderer<NormalPoint>(surfaceView, modelViewer), BaseAction<NormalPoint> {

    companion object {
        private const val FLOAT_SIZE = 4f
    }

    private val entityMap: MutableMap<NormalPoint, Int> = mutableMapOf()

    override fun loadMaterial(activity: Activity) {
        AssetUtils.readUncompressedAsset(activity, "materials/baked_color.filamat").let {
            material = Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
        }
    }

    override fun addFrame(model: NormalPoint) {
        if (entityMap.containsKey(model)){
            modelViewer.scene.removeEntity(entityMap[model]!!)
        }

        val vertexBuffer = VertexBuffer.Builder()
            .vertexCount(model.getPointSize())
            .bufferCount(2)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, model.getXYZDataSize())
            .attribute(VertexBuffer.VertexAttribute.COLOR,    1, VertexBuffer.AttributeType.UBYTE4,0, model.getPointSize())
            .normalized(VertexBuffer.VertexAttribute.COLOR)
            .build(modelViewer.engine)
        vertexBuffer.setBufferAt(modelViewer.engine,0, model.vertexData)
        vertexBuffer.setBufferAt(modelViewer.engine, 1, model.vertexColor)

        val indexBuffer = IndexBuffer.Builder()
            .indexCount(model.getPointSize())
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(modelViewer.engine)
        indexBuffer.setBuffer(modelViewer.engine, model.indexData)
        entityMap[model] = modelViewer.engine.entityManager.create()

        RenderableManager.Builder(1)
            .boundingBox(Box(0.0f, 0.0f, 0.0f, 100.0f, 100.0f, 100.0f))
            .geometry(0, RenderableManager.PrimitiveType.POINTS, vertexBuffer, indexBuffer)
            .material(0, material.defaultInstance)
            .build(modelViewer.engine, entityMap[model]!!)
        // 创建transform
        modelViewer.engine.transformManager.create(entityMap[model]!!)
        modelViewer.scene.addEntity(entityMap[model]!!)
    }

    override fun destroy() {
        entityMap.forEach {
            val entityManager = EntityManager.get()
            entityManager.destroy(it.value)
        }
        modelViewer.engine.destroyMaterial(material)
    }

    override fun move(model: NormalPoint){
        if (entityMap.containsKey(model)){
            val transformManager = modelViewer.engine.transformManager
            val entityInstance = transformManager.getInstance(entityMap[model]!!)
            transformManager.setTransform(entityInstance,model.vertexMatrix)
        }
    }
}