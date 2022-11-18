package com.mashiro.filament.render

import android.app.Activity
import android.view.SurfaceView
import com.google.android.filament.*
import com.mashiro.filament.model.Grid
import com.mashiro.filament.utils.AssetUtils

class GridRender(surfaceView: SurfaceView): BaseRenderer<Grid>(surfaceView) {

    private var gridEntity = 0

    override fun loadMaterial(activity: Activity) {
        AssetUtils.readUncompressedAsset(activity, "materials/grid.filamat").let {
            material = Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
        }
    }

    override fun addFrame(model: Grid) {
        val vertexBuffer = VertexBuffer.Builder()
            .vertexCount(model.pointList.size)
            .bufferCount(1)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, 12)
            .build(modelViewer.engine)
        vertexBuffer.setBufferAt(modelViewer.engine,0, model.vertexFloatBuffer)

        val indexBuffer = IndexBuffer.Builder()
            .indexCount(model.pointList.size)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(modelViewer.engine)
        indexBuffer.setBuffer(modelViewer.engine, model.indexData)
        gridEntity = EntityManager.get().create()
        RenderableManager.Builder(1)
            .boundingBox(Box(0.0f, 0.0f, 0.0f, 100.0f, 100.0f, 1.0f))
            .geometry(0, RenderableManager.PrimitiveType.TRIANGLE_STRIP, vertexBuffer, indexBuffer, 0, model.pointList.size)
            .material(0, material.defaultInstance)
            .build(modelViewer.engine, gridEntity)
        // 创建transform
        modelViewer.engine.transformManager.create(gridEntity)
        modelViewer.scene.addEntity(gridEntity)
    }

    override fun destroy() {
        modelViewer.engine.destroyMaterial(material)
    }
}