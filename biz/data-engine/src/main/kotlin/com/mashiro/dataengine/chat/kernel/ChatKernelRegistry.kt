package com.mashiro.dataengine.chat.kernel

import com.mashiro.dataengine.chat.backend.ChatBackend
import com.mashiro.dataengine.chat.backend.FakeChatBackend
import com.mashiro.dataengine.chat.crud.InMemoryChatCrudStore
import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventBus
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId

internal class ChatKernelRegistry(
    private val dataLayer: ChatDataLayer = InMemoryChatDataLayer(
        crudStore = InMemoryChatCrudStore()
    ),
    private val backendFactory: () -> ChatBackend = { FakeChatBackend() },
    val eventBus: ChatEventBus = ChatEventBus()
) {
    private val lock = Any()
    private val kernels = mutableMapOf<ChatSessionId, ChatKernel>()

    fun createKernel(sessionId: ChatSessionId, title: String): ChatKernel {
        val createdKernel = synchronized(lock) {
            kernels[sessionId]?.let { kernel ->
                return kernel
            }

            dataLayer.createSession(ChatSession(sessionId, title))
            val kernel = ChatKernel(
                sessionId = sessionId,
                dataLayer = dataLayer,
                eventBus = ChatEventBus(),
                backend = backendFactory()
            )
            kernels[sessionId] = kernel
            kernel
        }
        eventBus.publish(ChatEvent.KernelCreated(sessionId))
        return createdKernel
    }

    fun findKernel(sessionId: ChatSessionId): ChatKernel? {
        return synchronized(lock) {
            kernels[sessionId]
        }
    }

    fun destroyKernel(sessionId: ChatSessionId): Boolean {
        val kernel = synchronized(lock) {
            kernels.remove(sessionId)
        } ?: return false
        kernel.destroy()
        eventBus.publish(ChatEvent.KernelDestroyed(sessionId))
        return true
    }
}
