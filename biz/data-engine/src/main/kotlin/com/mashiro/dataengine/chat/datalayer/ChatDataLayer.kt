package com.mashiro.dataengine.chat.datalayer

import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId

internal interface ChatDataLayer {
    fun createSession(session: ChatSession): ChatSession

    fun readSession(sessionId: ChatSessionId): ChatSession?

    fun createMessage(
        sessionId: ChatSessionId,
        content: String,
        sender: ChatSender
    ): ChatMessage

    fun readMessages(sessionId: ChatSessionId): List<ChatMessage>

    fun updateProcessState(
        sessionId: ChatSessionId,
        state: ChatProcessState
    ): ChatProcessState

    fun readProcessState(sessionId: ChatSessionId): ChatProcessState
}
