package com.mashiro.dataengine.chat.crud

import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryChatCrudStoreTest {

    @Test
    fun executeCreateAndReadMessagesAsCrudCommands() {
        val store = InMemoryChatCrudStore(
            idGenerator = IncrementalIdGenerator(),
            clock = IncrementalClock()
        )
        val sessionId = ChatSessionId("session-1")

        val createResult = store.execute(
            ChatCrudCommand.CreateMessage(sessionId, "hello", ChatSender.ME)
        ) as ChatCrudResult.MessageCreated
        val readResult = store.execute(
            ChatCrudCommand.ReadMessages(sessionId)
        ) as ChatCrudResult.MessagesRead

        assertEquals("1", createResult.message.id)
        assertEquals("hello", createResult.message.content)
        assertEquals(ChatSender.ME, createResult.message.sender)
        assertEquals(listOf(createResult.message), readResult.messages)
    }

    @Test
    fun executeUpdateAndDeleteMessagesAsCrudCommands() {
        val store = InMemoryChatCrudStore(
            idGenerator = IncrementalIdGenerator(),
            clock = IncrementalClock()
        )
        val sessionId = ChatSessionId("session-1")
        val created = store.execute(
            ChatCrudCommand.CreateMessage(sessionId, "draft", ChatSender.ME)
        ) as ChatCrudResult.MessageCreated

        val updatedMessage = created.message.copy(content = "sent")
        val updateResult = store.execute(
            ChatCrudCommand.UpdateMessage(sessionId, updatedMessage)
        ) as ChatCrudResult.MessageUpdated
        val deleteResult = store.execute(
            ChatCrudCommand.DeleteMessage(sessionId, updatedMessage.id)
        ) as ChatCrudResult.MessageDeleted
        val readResult = store.execute(
            ChatCrudCommand.ReadMessages(sessionId)
        ) as ChatCrudResult.MessagesRead

        assertEquals(updatedMessage, updateResult.message)
        assertTrue(deleteResult.deleted)
        assertTrue(readResult.messages.isEmpty())
    }

    @Test
    fun executeUpdateAndDeleteReturnMissWhenIdDoesNotExist() {
        val store = InMemoryChatCrudStore()
        val sessionId = ChatSessionId("session-1")

        val updateResult = store.execute(
            ChatCrudCommand.UpdateMessage(
                sessionId,
                ChatMessage("missing", "hello", ChatSender.ME, 1L)
            )
        ) as ChatCrudResult.MessageUpdated
        val deleteResult = store.execute(
            ChatCrudCommand.DeleteMessage(sessionId, "missing")
        ) as ChatCrudResult.MessageDeleted

        assertEquals(null, updateResult.message)
        assertFalse(deleteResult.deleted)
    }

    @Test
    fun executeCommandsDoNotMixMessagesAcrossSessions() {
        val store = InMemoryChatCrudStore(
            idGenerator = IncrementalIdGenerator(),
            clock = IncrementalClock()
        )
        val firstSessionId = ChatSessionId("session-1")
        val secondSessionId = ChatSessionId("session-2")

        store.execute(ChatCrudCommand.CreateMessage(firstSessionId, "first", ChatSender.ME))
        store.execute(ChatCrudCommand.CreateMessage(secondSessionId, "second", ChatSender.OTHER))

        val firstRead = store.execute(
            ChatCrudCommand.ReadMessages(firstSessionId)
        ) as ChatCrudResult.MessagesRead
        val secondRead = store.execute(
            ChatCrudCommand.ReadMessages(secondSessionId)
        ) as ChatCrudResult.MessagesRead

        assertEquals(listOf("first"), firstRead.messages.map { it.content })
        assertEquals(listOf("second"), secondRead.messages.map { it.content })
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
