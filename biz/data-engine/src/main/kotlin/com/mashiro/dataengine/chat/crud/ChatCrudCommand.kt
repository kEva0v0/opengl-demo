package com.mashiro.dataengine.chat.crud

import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatSessionId

internal sealed class ChatCrudCommand {
    data class CreateMessage(
        val sessionId: ChatSessionId,
        val content: String,
        val sender: ChatSender
    ) : ChatCrudCommand()

    data class ReadMessages(
        val sessionId: ChatSessionId
    ) : ChatCrudCommand()

    data class UpdateMessage(
        val sessionId: ChatSessionId,
        val message: ChatMessage
    ) : ChatCrudCommand()

    data class DeleteMessage(
        val sessionId: ChatSessionId,
        val id: String
    ) : ChatCrudCommand()
}
