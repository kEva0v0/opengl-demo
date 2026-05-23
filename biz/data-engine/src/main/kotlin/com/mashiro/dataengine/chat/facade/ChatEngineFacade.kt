package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.backend.ChatBackend
import com.mashiro.dataengine.chat.backend.FakeChatBackend
import com.mashiro.dataengine.chat.crud.InMemoryChatCrudStore
import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayer
import com.mashiro.dataengine.chat.session.ChatSessionId

interface ChatEngineFacade {
    fun createKernel(sessionId: ChatSessionId, title: String)

    fun bindKernel(sessionId: ChatSessionId)

    fun destroyKernel(sessionId: ChatSessionId): Boolean

    fun sendMessage(sessionId: ChatSessionId, content: String)

    fun receiveMessage(sessionId: ChatSessionId, content: String)

    fun eventFacade(sessionId: ChatSessionId): ChatEventFacade

    fun dataFacade(sessionId: ChatSessionId): ChatDataFacade

    companion object {
        fun create(): ChatEngineFacade {
            return ChatEngineFacadeImpl(
                dataLayer = InMemoryChatDataLayer(
                    crudStore = InMemoryChatCrudStore()
                ),
                backendFactory = { FakeChatBackend() }
            )
        }
    }
}
