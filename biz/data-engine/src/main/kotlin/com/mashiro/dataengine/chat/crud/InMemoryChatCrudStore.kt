package com.mashiro.dataengine.chat.crud

import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.session.ChatSessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

internal class InMemoryChatCrudStore(
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ChatCrudStore {
    private val messagesBySession = mutableMapOf<ChatSessionId, List<ChatMessage>>()
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    override val messages: StateFlow<List<ChatMessage>> = _messages

    override fun execute(command: ChatCrudCommand): ChatCrudResult {
        return when (command) {
            is ChatCrudCommand.CreateMessage -> createMessage(command)
            is ChatCrudCommand.ReadMessages -> ChatCrudResult.MessagesRead(
                messagesBySession[command.sessionId].orEmpty()
            )
            is ChatCrudCommand.UpdateMessage -> updateMessage(command.sessionId, command.message)
            is ChatCrudCommand.DeleteMessage -> deleteMessage(command.sessionId, command.id)
        }
    }

    private fun createMessage(command: ChatCrudCommand.CreateMessage): ChatCrudResult.MessageCreated {
        val message = ChatMessage(
            id = idGenerator(),
            content = command.content,
            sender = command.sender,
            timestamp = clock()
        )
        messagesBySession[command.sessionId] = messagesBySession[command.sessionId].orEmpty() + message
        publishSnapshot()
        return ChatCrudResult.MessageCreated(message)
    }

    private fun updateMessage(
        sessionId: ChatSessionId,
        message: ChatMessage
    ): ChatCrudResult.MessageUpdated {
        val sessionMessages = messagesBySession[sessionId].orEmpty()
        val index = sessionMessages.indexOfFirst { it.id == message.id }
        if (index == -1) {
            return ChatCrudResult.MessageUpdated(null)
        }
        messagesBySession[sessionId] = sessionMessages.toMutableList().also { messages ->
            messages[index] = message
        }
        publishSnapshot()
        return ChatCrudResult.MessageUpdated(message)
    }

    private fun deleteMessage(
        sessionId: ChatSessionId,
        id: String
    ): ChatCrudResult.MessageDeleted {
        val oldMessages = messagesBySession[sessionId].orEmpty()
        val newMessages = oldMessages.filterNot { it.id == id }
        val deleted = oldMessages.size != newMessages.size
        if (deleted) {
            messagesBySession[sessionId] = newMessages
            publishSnapshot()
        }
        return ChatCrudResult.MessageDeleted(deleted)
    }

    private fun publishSnapshot() {
        _messages.value = messagesBySession.values.flatten()
    }
}
