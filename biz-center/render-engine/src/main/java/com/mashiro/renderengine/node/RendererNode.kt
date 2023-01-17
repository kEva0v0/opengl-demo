package com.mashiro.renderengine.node

import com.mashiro.renderengine.geometries.BaseGeometry
import com.mashiro.renderengine.math.Entity
import com.mashiro.renderengine.renderer.BaseRenderer
import com.google.android.filament.Engine
import com.google.android.filament.Scene

class RendererNode private constructor(
    engine: Engine, scene: Scene, nodeObserver: NodeObserver?
) : Node(engine, scene, nodeObserver) {

    class Builder{
        private var engine: Engine? = null
        private var scene: Scene? = null
        private var observer: NodeObserver? = null
        private var geometry: BaseGeometry? = null
        private var renderer: BaseRenderer? = null
        private var parentNode: Entity? = null

        fun bindEngine(engine: Engine): Builder {
            this.engine = engine
            return this
        }

        fun bindScene(scene: Scene): Builder {
            this.scene = scene
            return this
        }

        fun bindObserver(observer: NodeObserver?): Builder {
            this.observer = observer
            return this
        }

        fun bindGeometry(geometry: BaseGeometry): Builder{
            this.geometry = geometry
            return this
        }

        fun bindRenderer(baseRenderer: BaseRenderer): Builder {
            this.renderer = baseRenderer
            return this
        }

        fun bindParent(node: Entity): Builder {
            this.parentNode = node
            return this
        }

        fun build(): RendererNode? {
            var rendererNode: RendererNode? = null
            if (engine != null && scene != null && geometry != null) {
                rendererNode = RendererNode(engine!!, scene!!, observer).apply {
                    this.renderer = this@Builder.renderer
                    this.geometry = this@Builder.geometry
                }
                if (parentNode != null) {
                    rendererNode.parentNode = parentNode
                }
            }
            return rendererNode
        }
    }
}