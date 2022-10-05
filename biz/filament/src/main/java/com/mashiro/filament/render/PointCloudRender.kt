package com.mashiro.filament.render

import android.content.Context
import android.view.SurfaceView
import com.google.android.filament.*
import com.mashiro.filament.bean.NormalPoint
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PointCloudRender(surfaceView: SurfaceView) : BaseRenderer<NormalPoint>(surfaceView) {

    companion object {
        private const val FLOAT_SIZE = 4f
    }

    private var renderable: Int = 0

    override fun getRenderer(): Int {
        return renderable
    }

    override fun setFrame(model: NormalPoint) {
        val vertexBuffer = VertexBuffer.Builder()
            .vertexCount(model.getPointSize())
            .bufferCount(1)
            .attribute(VertexBuffer.VertexAttribute.POSITION, 0, VertexBuffer.AttributeType.FLOAT3, 0, model.getStripSize())
            .attribute(VertexBuffer.VertexAttribute.COLOR,    0, VertexBuffer.AttributeType.UBYTE4, model.getPointSize() * NormalPoint.floatSize, model.getStripSize())
            .normalized(VertexBuffer.VertexAttribute.COLOR)
            .build(modelViewer.engine)
        vertexBuffer.setBufferAt(modelViewer.engine,0,model.vertexData)

        val indexBuffer = IndexBuffer.Builder()
            .indexCount(model.getPointSize())
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(modelViewer.engine)
        indexBuffer.setBuffer(modelViewer.engine, model.indexData)

        renderable = EntityManager.get().create()
        RenderableManager.Builder(1)
            .boundingBox(Box(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 0.01f))
            .geometry(0, RenderableManager.PrimitiveType.TRIANGLES, vertexBuffer, indexBuffer, 0, model.getPointSize())
            .build(modelViewer.engine, renderable)
        modelViewer.scene.addEntity(renderable)
    }
}