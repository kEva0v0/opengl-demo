package com.mashiro.dataengine.chat.datalayer

import com.mashiro.dataengine.chat.crud.ChatCrudCommand
import com.mashiro.dataengine.chat.crud.ChatCrudResult
import com.mashiro.dataengine.chat.crud.ChatCrudStore
import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryChatDataLayerTest {

    @Test
    fun storesSessionMessagesAndProcessStateBySessionId() {
        val dataLayer = InMemoryChatDataLayer(
            idGenerator = IncrementalIdGenerator(),
            clock = IncrementalClock()
        )
        val sessionId = ChatSessionId("session-1")
        val session = ChatSession(sessionId, "模拟聊天")

        dataLayer.createSession(session)
        val message = dataLayer.createMessage(sessionId, "hello", ChatSender.ME)
        dataLayer.updateProcessState(sessionId, ChatProcessState(receiving = true))

        assertEquals(session, dataLayer.readSession(sessionId))
        assertEquals("1", message.id)
        assertEquals("hello", message.content)
        assertEquals(ChatSender.ME, message.sender)
        assertEquals(1L, message.timestamp)
        assertEquals(listOf(message), dataLayer.readMessages(sessionId))
        assertEquals(ChatProcessState(receiving = true), dataLayer.readProcessState(sessionId))
    }

    @Test
    fun returnsEmptyMessagesAndDefaultProcessStateWhenSessionDataIsAbsent() {
        val dataLayer = InMemoryChatDataLayer()
        val sessionId = ChatSessionId("missing")

        assertEquals(emptyList<ChatMessage>(), dataLayer.readMessages(sessionId))
        assertEquals(ChatProcessState(), dataLayer.readProcessState(sessionId))
    }

    @Test
    fun messageReadsAndWritesExecuteCrudCommandsOnRuntimePath() {
        val sessionId = ChatSessionId("session-1")
        val message = ChatMessage("message-1", "hello", ChatSender.ME, 1L)
        val crudStore = RecordingChatCrudStore(message)
        val dataLayer = InMemoryChatDataLayer(crudStore = crudStore)

        assertEquals(message, dataLayer.createMessage(sessionId, "hello", ChatSender.ME))
        assertEquals(listOf(message), dataLayer.readMessages(sessionId))
        assertEquals(
            listOf(
                ChatCrudCommand.CreateMessage(sessionId, "hello", ChatSender.ME),
                ChatCrudCommand.ReadMessages(sessionId)
            ),
            crudStore.commands
        )
    }
}

private class RecordingChatCrudStore(
    private val message: ChatMessage
) : ChatCrudStore {
    override val messages: StateFlow<List<ChatMessage>> = MutableStateFlow(emptyList())
    val commands = mutableListOf<ChatCrudCommand>()

    override fun execute(command: ChatCrudCommand): ChatCrudResult {
        commands += command
        return when (command) {
            is ChatCrudCommand.CreateMessage -> ChatCrudResult.MessageCreated(message)
            is ChatCrudCommand.ReadMessages -> ChatCrudResult.MessagesRead(listOf(message))
            is ChatCrudCommand.UpdateMessage -> ChatCrudResult.MessageUpdated(command.message)
            is ChatCrudCommand.DeleteMessage -> ChatCrudResult.MessageDeleted(true)
        }
    }
}

private class IncrementalIdGenerator : () -> String {
    private var current = 0

    override fun invoke(): String {
        current += 1
        return current.toString()
    }
}

private class IncrementalClock : () -> Long {
    private var current = 0L

    override fun invoke(): Long {
        current += 1
        return current
    }
}
