package com.mashiro.dataengine.chat.datalayer

import com.mashiro.dataengine.chat.crud.ChatCrudCommand
import com.mashiro.dataengine.chat.crud.ChatCrudResult
import com.mashiro.dataengine.chat.crud.ChatCrudStore
import com.mashiro.dataengine.chat.crud.InMemoryChatCrudStore
import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId

internal class InMemoryChatDataLayer(
    idGenerator: () -> String = { System.nanoTime().toString() },
    clock: () -> Long = { System.currentTimeMillis() },
    private val crudStore: ChatCrudStore = InMemoryChatCrudStore(
        idGenerator = idGenerator,
        clock = clock
    )
) : ChatDataLayer {
    private val lock = Any()
    private val sessions = mutableMapOf<ChatSessionId, ChatSession>()
    private val processStates = mutableMapOf<ChatSessionId, ChatProcessState>()

    override fun createSession(session: ChatSession): ChatSession {
        return synchronized(lock) {
            sessions[session.id] = session
            session
        }
    }

    override fun readSession(sessionId: ChatSessionId): ChatSession? {
        return synchronized(lock) {
            sessions[sessionId]
        }
    }

    override fun createMessage(
        sessionId: ChatSessionId,
        content: String,
        sender: ChatSender
    ): ChatMessage {
        return synchronized(lock) {
            val result = crudStore.execute(
                ChatCrudCommand.CreateMessage(sessionId, content, sender)
            ) as ChatCrudResult.MessageCreated
            result.message
        }
    }

    override fun readMessages(sessionId: ChatSessionId): List<ChatMessage> {
        return synchronized(lock) {
            val result = crudStore.execute(
                ChatCrudCommand.ReadMessages(sessionId)
            ) as ChatCrudResult.MessagesRead
            result.messages
        }
    }

    override fun updateProcessState(
        sessionId: ChatSessionId,
        state: ChatProcessState
    ): ChatProcessState {
        return synchronized(lock) {
            processStates[sessionId] = state
            state
        }
    }

    override fun readProcessState(sessionId: ChatSessionId): ChatProcessState {
        return synchronized(lock) {
            processStates[sessionId] ?: ChatProcessState()
        }
    }
}
