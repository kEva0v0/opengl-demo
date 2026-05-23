package com.mashiro.dataengine.chat.kernel

import com.mashiro.dataengine.chat.backend.ChatBackend
import com.mashiro.dataengine.chat.command.ChatCommandContext
import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventBus
import com.mashiro.dataengine.chat.session.ChatSessionId

internal class ChatKernel(
    val sessionId: ChatSessionId,
    val dataLayer: ChatDataLayer,
    val eventBus: ChatEventBus,
    val backend: ChatBackend
) {
    private val lifecycleLock = Any()
    private var active = true

    fun isActive(): Boolean {
        return synchronized(lifecycleLock) {
            active
        }
    }

    fun mutateIfActive(block: (ChatCommandContext) -> List<ChatEvent>): List<ChatEvent>? {
        return synchronized(lifecycleLock) {
            if (!active) {
                null
            } else {
                block(ChatCommandContext.from(this))
            }
        }
    }

    fun destroy() {
        synchronized(lifecycleLock) {
            active = false
        }
    }
}
