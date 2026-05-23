package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.backend.ChatBackend
import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.kernel.ChatKernel
import com.mashiro.dataengine.chat.kernel.ChatKernelRegistry
import com.mashiro.dataengine.chat.mutator.ReceiveMessageMutator
import com.mashiro.dataengine.chat.mutator.SendMessageMutator
import com.mashiro.dataengine.chat.query.ReadMessagesQuery
import com.mashiro.dataengine.chat.session.ChatSessionId

internal class ChatEngineFacadeImpl(
    dataLayer: ChatDataLayer,
    backendFactory: () -> ChatBackend
) : ChatEngineFacade {
    private val registry = ChatKernelRegistry(
        dataLayer = dataLayer,
        backendFactory = backendFactory
    )

    override fun createKernel(sessionId: ChatSessionId, title: String) {
        registry.createKernel(sessionId, title)
    }

    override fun bindKernel(sessionId: ChatSessionId) {
        requireKernel(sessionId)
    }

    override fun destroyKernel(sessionId: ChatSessionId): Boolean {
        return registry.destroyKernel(sessionId)
    }

    override fun sendMessage(sessionId: ChatSessionId, content: String) {
        SendMessageMutator(requireKernel(sessionId)).mutate(content)
    }

    override fun receiveMessage(sessionId: ChatSessionId, content: String) {
        ReceiveMessageMutator(requireKernel(sessionId)).mutate(content)
    }

    override fun eventFacade(sessionId: ChatSessionId): ChatEventFacade {
        return ChatEventFacade { observer ->
            requireKernel(sessionId).eventBus.subscribe(observer)
        }
    }

    override fun dataFacade(sessionId: ChatSessionId): ChatDataFacade {
        return DefaultChatDataFacade(
            readSession = {
                val kernel = requireKernel(sessionId)
                kernel.dataLayer.readSession(kernel.sessionId)
            },
            readMessages = { ReadMessagesQuery(requireKernel(sessionId)).execute() },
            readProcessState = {
                val kernel = requireKernel(sessionId)
                kernel.dataLayer.readProcessState(kernel.sessionId)
            }
        )
    }

    private fun requireKernel(sessionId: ChatSessionId): ChatKernel {
        return registry.findKernel(sessionId)
            ?: error("Chat kernel not found for sessionId=${sessionId.value}")
    }
}
