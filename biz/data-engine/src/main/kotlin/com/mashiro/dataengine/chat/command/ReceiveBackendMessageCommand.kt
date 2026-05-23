package com.mashiro.dataengine.chat.command

import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.model.ChatSender

internal class ReceiveBackendMessageCommand(
    private val content: String
) : ChatCommand {
    override fun execute(context: ChatCommandContext): List<ChatEvent> {
        context.dataLayer.createMessage(
            sessionId = context.sessionId,
            content = content,
            sender = ChatSender.OTHER
        )
        return listOf(ChatEvent.MessagesChanged(context.sessionId))
    }
}
