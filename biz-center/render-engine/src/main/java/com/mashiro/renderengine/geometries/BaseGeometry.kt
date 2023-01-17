package com.mashiro.renderengine.geometries

import android.util.Log
import com.mashiro.renderengine.Constants
import com.mashiro.renderengine.math.*
import com.mashiro.renderengine.model.*
import com.google.android.filament.Box
import com.google.android.filament.Engine
import com.google.android.filament.IndexBuffer
import com.google.android.filament.VertexBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * 每个model对应的geometry，负责model数据与Engine的交互
 * 如果需要新增model，请务必实现对应的geometry
 *
 * @param vertexBuffer 图形Buffer
 * @param indexBuffer 序号Buffer
 *
 * @see PointCloudGeometry
 */
abstract class BaseGeometry {

    companion object {
        private const val POSITION_SIZE = 3 // x, y, z
        private const val DIRECTION_SIZE = 3 // dx, dy, dz
        private const val TANGENT_SIZE = 4 // Quaternion: x, y, z, w
        private const val UV_SIZE = 2 // x, y
        private const val COLOR_SIZE = 4 // r, g, b, a
    }

    lateinit var vertexBuffer: VertexBuffer
    lateinit var indexBuffer: IndexBuffer
    lateinit var boundingBox: Box

    private var positionList = listOf<Float>()
    private var directionList = listOf<Float>()
    private var uvCoordinateList = listOf<Float>()
    private var colorList = listOf<Float>()
    private var meshList = listOf<Submesh>()

    protected var needBuild = false
    protected var engine: Engine? = null
    protected var hasInit = false


    /**
     * 初始化，将vertexBuffer和IndexBuffer创建出来
     */
    fun init(engine: Engine){
        val positionList = initPositionList()
        val directionList = initDirectionList()
        val uvCoordinateList = initUvCoordinateList()
        val colorList = initColorList()
        val meshList = initMeshList()

        val positionLegal = positionList.isNotEmpty() && positionList.size % POSITION_SIZE == 0
        val directionLegal = directionList.isNotEmpty() && directionList.size % DIRECTION_SIZE == 0
        val uvCoordinateLegal = uvCoordinateList.isNotEmpty() && uvCoordinateList.size % UV_SIZE == 0
        val colorLegal = colorList.isNotEmpty() && colorList.size % COLOR_SIZE == 0
        
        // Position should never be null or empty
        if (!positionLegal) {
            return
        }
        this.engine = engine
        vertexBuffer = VertexBuffer.Builder().apply {
            bufferCount(
                1 + // Position never empty
                (if (directionLegal) 1 else 0) +
                (if (uvCoordinateLegal) 1 else 0) +
                (if (colorLegal) 1 else 0)
            )
            vertexCount(positionList.size)

            // Position Attribute
            var bufferIndex = 0
            attribute(
                VertexBuffer.VertexAttribute.POSITION,
                bufferIndex,
                VertexBuffer.AttributeType.FLOAT3,
                0,
                POSITION_SIZE * Float.SIZE_BYTES
            )
            // Tangents Attribute
            if (directionLegal) {
                bufferIndex++
                attribute(
                    VertexBuffer.VertexAttribute.TANGENTS,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    TANGENT_SIZE * Float.SIZE_BYTES
                )
                normalized(VertexBuffer.VertexAttribute.TANGENTS)
            }
            // Uv Attribute
            if (uvCoordinateLegal) {
                bufferIndex++
                attribute(
                    VertexBuffer.VertexAttribute.UV0,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT2,
                    0,
                    UV_SIZE * Float.SIZE_BYTES
                )
            }
            // Color Attribute
            if (colorLegal) {
                bufferIndex++
                attribute(
                    VertexBuffer.VertexAttribute.COLOR,
                    bufferIndex,
                    VertexBuffer.AttributeType.FLOAT4,
                    0,
                    COLOR_SIZE * Float.SIZE_BYTES
                )
                normalized(VertexBuffer.VertexAttribute.COLOR)
            }
        }.build(engine)
        indexBuffer = IndexBuffer.Builder()
            // Determine how many indices there are
            .indexCount(positionList.size / POSITION_SIZE)
            .bufferType(IndexBuffer.Builder.IndexType.UINT)
            .build(engine)
        hasInit = true
        setVertexData(positionList, directionList, uvCoordinateList, colorList, engine)
        setIndexData(positionList, meshList, engine)
    }

    /**
     * 获取Position List
     */

    abstract fun initPositionList(): List<Float>

    /**
     * 获取几何中心
     */
    fun getCentroid(): Position {
        var centroid = Position()
        positionList.let {
            var index = 0
            while (index < it.size) {
                centroid.x += it[index]
                centroid.y += it[index + 1]
                centroid.z += it[index + 2]
                index += POSITION_SIZE
            }
            centroid /= (positionList.size / POSITION_SIZE).toFloat()
        }
        return centroid
    }

    fun getExtent(): Translation {
        val minPos = Translation()
        val maxPos = Translation()
        positionList.let {
            var index = 0
            while (index < it.size) {
                minPos.x = minOf(it[index], minPos.x)
                minPos.y = minOf(it[index + 1], minPos.y)
                minPos.z = minOf(it[index + 2], minPos.z)
                maxPos.x = maxOf(it[index], maxPos.x)
                maxPos.y = maxOf(it[index + 1], maxPos.y)
                maxPos.z = maxOf(it[index + 2], maxPos.z)
                index += POSITION_SIZE
            }
        }
        return maxPos - minPos
    }

    /**
     * 获取法向量 List
     */
    open fun initDirectionList(): List<Float> = mutableListOf()

    /**
     * 获取纹理坐标 List
     */
    open fun initUvCoordinateList(): List<Float> = mutableListOf()

    /**
     * 获取颜色 List
     * 排序：a, r, g, b
     */
    open fun initColorList(): List<Float> = mutableListOf()

    /**
     * 自定义mesh
     */
    open fun initMeshList(): List<Submesh> = mutableListOf()

    /**
     * 手动设置vertex
     */
    fun setVertexData(
        positionList : List<Float>,
        directionList : List<Float> = emptyList(),
        uvCoordinateList : List<Float> = emptyList(),
        colorList : List<Float> = emptyList(),
        engine: Engine
    ){
        if (!hasInit) {
            Log.e("${Constants.TAG}-${this.javaClass.simpleName}", "$this has not initialize")
            return
        }
        this.engine = engine
        this.positionList = positionList
        this.directionList = directionList
        this.uvCoordinateList = uvCoordinateList
        this.colorList = colorList

        val positionLegal = positionList.isNotEmpty() && positionList.size % POSITION_SIZE == 0
        val directionLegal = directionList.isNotEmpty() && directionList.size % DIRECTION_SIZE == 0
        val uvCoordinateLegal = uvCoordinateList.isNotEmpty() && uvCoordinateList.size % UV_SIZE == 0
        val colorLegal = colorList.isNotEmpty() && colorList.size % COLOR_SIZE == 0
        if (!positionLegal) {
            Log.e("${Constants.TAG}-${this.javaClass.simpleName}", "position is not legal")
            return
        }
        var bufferIndex = 0

        vertexBuffer.setBufferAt(
            engine, bufferIndex,
            FloatBuffer.allocate(positionList.size).apply {
                put(positionList.toFloatArray())
                flip()
            }, 0,
            positionList.size
        )

        if (directionLegal) {
            bufferIndex++
            vertexBuffer.setBufferAt(
                engine, bufferIndex,
                // 转化成四元数
                FloatBuffer.allocate(directionList.size / DIRECTION_SIZE * TANGENT_SIZE).apply {
                    var index = 0
                    while (index < directionList.size) {
                        put(normalToTangent(
                            Direction(
                                directionList[index], // nx
                                directionList[index + 1], // ny
                                directionList[index + 2] // nz
                            )
                        ).toFloatArray())
                        index += DIRECTION_SIZE
                    }
                    flip()
                }, 0,
                directionList.size / DIRECTION_SIZE * TANGENT_SIZE
            )
        }

        if (uvCoordinateLegal) {
            bufferIndex++
            vertexBuffer.setBufferAt(
                engine, bufferIndex,
                FloatBuffer.allocate(uvCoordinateList.size).apply {
                    put(uvCoordinateList.toFloatArray())
                    rewind()
                }, 0,
                uvCoordinateList.size
            )
        }

        if (colorLegal) {
            bufferIndex++
            vertexBuffer.setBufferAt(
                engine, bufferIndex,
                FloatBuffer.allocate(colorList.size).apply {
                    put(colorList.toFloatArray())
                    rewind()
                }, 0,
                colorList.size
            )
        }

        boundingBox = Box(getCentroid().toFloatArray(), (getExtent()/2f).toFloatArray())
        markNeedsBuild()
    }

    /**
     * 手动设置index
     * 这里的submeshs可以为空
     */
    fun setIndexData(positionList: List<Float>, submeshes: List<Submesh>, engine: Engine){
        if (!hasInit) {
            Log.e("${Constants.TAG}-${this.javaClass.simpleName}", "$this has not initialize")
            return
        }
        this.engine = engine
        if (submeshes.isNotEmpty()) {
            meshList = submeshes
            indexBuffer.setBuffer(engine,
                IntBuffer.allocate(submeshes.sumOf { it.triangleIndices.size }).apply {
                    submeshes.flatMap { it.triangleIndices }.forEach { put(it) }
                    flip()
                })
        } else {
            indexBuffer.setBuffer(engine,
                IntBuffer.allocate(positionList.size / POSITION_SIZE).apply {
                    (0 until positionList.size / POSITION_SIZE).forEach { put(it) }
                    flip()
                })
        }
        markNeedsBuild()
    }

    /**
     * 释放
     */
    open fun destroyGeometry() {
        if (hasInit){
            engine?.destroyVertexBuffer(vertexBuffer)
            engine?.destroyIndexBuffer(indexBuffer)
            hasInit = false
        }
    }

    /**
     * Build状态
     * 判断是否改动到了纹理和细节
     */

    fun isNeedBuild(): Boolean { return needBuild }

    protected fun markNeedsBuild(){ needBuild = true }

    fun finishBuild() { needBuild = false }
}