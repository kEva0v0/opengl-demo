package com.mashiro.dataengine.chat.crud

import com.mashiro.dataengine.chat.model.ChatMessage

internal sealed class ChatCrudResult {
    data class MessageCreated(
        val message: ChatMessage
    ) : ChatCrudResult()

    data class MessagesRead(
        val messages: List<ChatMessage>
    ) : ChatCrudResult()

    data class MessageUpdated(
        val message: ChatMessage?
    ) : ChatCrudResult()

    data class MessageDeleted(
        val deleted: Boolean
    ) : ChatCrudResult()
}
