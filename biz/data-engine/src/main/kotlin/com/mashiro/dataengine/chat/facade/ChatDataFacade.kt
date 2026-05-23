package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession

interface ChatDataFacade {
    fun readSession(): ChatSession?

    fun readMessages(): List<ChatMessage>

    fun readProcessState(): ChatProcessState
}

internal class DefaultChatDataFacade(
    private val readSession: () -> ChatSession?,
    private val readMessages: () -> List<ChatMessage>,
    private val readProcessState: () -> ChatProcessState
) : ChatDataFacade {
    override fun readSession(): ChatSession? {
        return readSession.invoke()
    }

    override fun readMessages(): List<ChatMessage> {
        return readMessages.invoke()
    }

    override fun readProcessState(): ChatProcessState {
        return readProcessState.invoke()
    }
}
