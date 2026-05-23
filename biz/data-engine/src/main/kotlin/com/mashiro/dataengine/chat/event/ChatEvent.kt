package com.mashiro.dataengine.chat.event

import com.mashiro.dataengine.chat.session.ChatSessionId

sealed class ChatEvent(open val sessionId: ChatSessionId) {
    data class KernelCreated(override val sessionId: ChatSessionId) : ChatEvent(sessionId)
    data class KernelDestroyed(override val sessionId: ChatSessionId) : ChatEvent(sessionId)
    data class MessagesChanged(override val sessionId: ChatSessionId) : ChatEvent(sessionId)
    data class ProcessStateChanged(override val sessionId: ChatSessionId) : ChatEvent(sessionId)
}
