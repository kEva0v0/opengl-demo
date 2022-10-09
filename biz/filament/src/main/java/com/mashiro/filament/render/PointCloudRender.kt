package com.mashiro.filament.render

import android.view.SurfaceView
import com.google.android.filament.*
import com.mashiro.filament.bean.NormalPoint

class PointCloudRender(surfaceView: SurfaceView) : BaseRenderer<NormalPoint>(surfaceView), BaseAction<NormalPoint> {

    companion object {
        private const val FLOAT_SIZE = 4f
    }

    private val entityMap: MutableMap<NormalPoint, Int> = mutableMapOf()

    override fun addFrame(model: NormalPoint) {
        if (entityMap.containsKey(model)){
            modelViewer.scene.removeEntity(entityMap[model]!!)
        }

        val vertexBuffer = VertexBuffer.Builder()
            .vertexCount(model.getPointSize())
            .bufferCount(1)
            .attribute(VertexBuffer.VertexAttribute.COLOR,    0, VertexBuffer.AttributeType.FLOAT4, model.getOffsetStripSize(), model.getStripSize())
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, model.getStripSize())
//            .normalized(VertexBuffer.VertexAttribute.COLOR)
//            .attribute(VertexBuffer.VertexAttribute.TANGENTS, 0, VertexBuffer.AttributeType.FLOAT4, model.getOffsetStripSize(), model.getStripSize())
            .build(modelViewer.engine)
        model.vertexData.position(0)
        vertexBuffer.setBufferAt(modelViewer.engine,0, model.vertexData)

        val indexBuffer = IndexBuffer.Builder()
            .indexCount(model.getPointSize())
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(modelViewer.engine)
        indexBuffer.setBuffer(modelViewer.engine, model.indexData)
        entityMap[model] = EntityManager.get().create()
        RenderableManager.Builder(1)
            .boundingBox(Box(0.0f, 0.0f, 0.0f, 100.0f, 100.0f, 100.0f))
            .geometry(0, RenderableManager.PrimitiveType.POINTS, vertexBuffer, indexBuffer)
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
    }

    override fun move(model: NormalPoint){
        if (entityMap.containsKey(model)){
            val transformManager = modelViewer.engine.transformManager
            val entityInstance = transformManager.getInstance(entityMap[model]!!)
            transformManager.setTransform(entityInstance,model.vertexMatrix)
        }
    }
}