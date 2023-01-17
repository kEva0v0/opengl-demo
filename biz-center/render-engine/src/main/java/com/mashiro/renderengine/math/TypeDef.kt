package com.mashiro.renderengine.math

import dev.romainguy.kotlin.math.*

typealias Position = Float3
typealias RenderColor = Float4
typealias UvCoordinate = Float2

typealias Translation = Float3
typealias Rotation = Float3
typealias Scale = Float3
typealias Direction = Float3
typealias Size = Float3
typealias Axis = VectorComponent
typealias Transform = Mat4


typealias Entity = Int

enum class RenderType{
    RES_FILE,
    BITMAP,
    VIEW
}