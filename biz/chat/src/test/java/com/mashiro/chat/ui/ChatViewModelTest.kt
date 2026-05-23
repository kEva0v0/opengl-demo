package com.mashiro.chat.ui

import com.mashiro.chat.ui.mvi.ChatEffect
import com.mashiro.chat.ui.mvi.ChatIntent
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventSubscription
import com.mashiro.dataengine.chat.facade.ChatDataFacade
import com.mashiro.dataengine.chat.facade.ChatEngineFacade
import com.mashiro.dataengine.chat.facade.ChatEventFacade
import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ChatViewModelTest {

    @Test
    fun inputChangeAndSendClickedSendsTrimmedMessageClearsInputAndRefreshesOnMessageEvent() {
        val sessionId = ChatSessionId("default")
        val facade = FakeChatEngineFacade()
        val initialMessages = listOf(message("1", "existing", ChatSender.OTHER))
        val refreshedMessages = initialMessages + message("2", "hello", ChatSender.ME)
        facade.messages = initialMessages
        val viewModel = ChatViewModel(facade = facade, sessionId = sessionId)

        viewModel.onIntent(ChatIntent.InputChanged(" hello "))
        viewModel.onIntent(ChatIntent.SendClicked)

        assertEquals(listOf(sessionId to "hello"), facade.sentMessages)
        assertEquals("", viewModel.uiState.value.inputText)
        assertEquals(true, viewModel.uiState.value.isSending)

        facade.messages = refreshedMessages
        facade.emit(ChatEvent.MessagesChanged(sessionId))

        assertEquals(refreshedMessages, viewModel.uiState.value.messages)
        assertEquals(false, viewModel.uiState.value.isSending)
        assertEquals(ChatEffect.ScrollToLatest, viewModel.effects.value)

        viewModel.onIntent(ChatIntent.EffectConsumed)

        assertEquals(ChatEffect.None, viewModel.effects.value)
    }

    @Test
    fun blankInputDoesNotSendMessage() {
        val facade = FakeChatEngineFacade()
        val viewModel = ChatViewModel(facade = facade, sessionId = ChatSessionId("default"))

        viewModel.onIntent(ChatIntent.InputChanged("   "))
        viewModel.onIntent(ChatIntent.SendClicked)

        assertEquals(emptyList<Pair<ChatSessionId, String>>(), facade.sentMessages)
        assertEquals("   ", viewModel.uiState.value.inputText)
        assertFalse(viewModel.uiState.value.isSending)
    }

    @Test
    fun eventForAnotherSessionIsIgnored() {
        val sessionId = ChatSessionId("default")
        val facade = FakeChatEngineFacade()
        val initialMessages = listOf(message("1", "existing", ChatSender.OTHER))
        facade.messages = initialMessages
        val viewModel = ChatViewModel(facade = facade, sessionId = sessionId)

        facade.messages = initialMessages + message("2", "other", ChatSender.ME)
        facade.emit(ChatEvent.MessagesChanged(ChatSessionId("other")))

        assertEquals(initialMessages, viewModel.uiState.value.messages)
        assertEquals(ChatEffect.None, viewModel.effects.value)
    }

    private fun message(id: String, content: String, sender: ChatSender): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            sender = sender,
            timestamp = id.toLong()
        )
    }
}

private class FakeChatEngineFacade : ChatEngineFacade {
    var messages: List<ChatMessage> = emptyList()
    val sentMessages = mutableListOf<Pair<ChatSessionId, String>>()
    private val subscribers = mutableListOf<(ChatEvent) -> Unit>()

    override fun createKernel(sessionId: ChatSessionId, title: String) = Unit

    override fun bindKernel(sessionId: ChatSessionId) = Unit

    override fun destroyKernel(sessionId: ChatSessionId): Boolean = true

    override fun sendMessage(sessionId: ChatSessionId, content: String) {
        sentMessages += sessionId to content
    }

    override fun receiveMessage(sessionId: ChatSessionId, content: String) = Unit

    override fun eventFacade(sessionId: ChatSessionId): ChatEventFacade {
        return ChatEventFacade { observer ->
            subscribers += observer
            ChatEventSubscription {
                subscribers -= observer
            }
        }
    }

    override fun dataFacade(sessionId: ChatSessionId): ChatDataFacade {
        return object : ChatDataFacade {
            override fun readSession(): ChatSession? = ChatSession(sessionId, "模拟聊天")

            override fun readMessages(): List<ChatMessage> = messages

            override fun readProcessState(): ChatProcessState = ChatProcessState()
        }
    }

    fun emit(event: ChatEvent) {
        subscribers.toList().forEach { subscriber ->
            subscriber(event)
        }
    }
}
