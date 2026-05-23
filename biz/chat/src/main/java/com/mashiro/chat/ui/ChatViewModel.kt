package com.mashiro.chat.ui

import androidx.lifecycle.ViewModel
import com.mashiro.chat.ui.mvi.ChatEffect
import com.mashiro.chat.ui.mvi.ChatIntent
import com.mashiro.chat.ui.mvi.ChatUiState
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventSubscription
import com.mashiro.dataengine.chat.facade.ChatDataFacade
import com.mashiro.dataengine.chat.facade.ChatEngineFacade
import com.mashiro.dataengine.chat.session.ChatSessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatViewModel(
    private val facade: ChatEngineFacade = ChatEngineFacade.create(),
    private val sessionId: ChatSessionId = ChatSessionId("default")
) : ViewModel() {
    private val dataFacade: ChatDataFacade
    private val subscription: ChatEventSubscription
    private val _uiState = MutableStateFlow(ChatUiState(chatName = DEFAULT_CHAT_NAME))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _effects = MutableStateFlow<ChatEffect>(ChatEffect.None)
    val effects: StateFlow<ChatEffect> = _effects.asStateFlow()

    init {
        facade.createKernel(sessionId, DEFAULT_CHAT_NAME)
        facade.bindKernel(sessionId)
        subscription = facade.eventFacade(sessionId).subscribe { event ->
            onIntent(ChatIntent.EventArrived(event))
        }
        dataFacade = facade.dataFacade(sessionId)
        _uiState.value = _uiState.value.copy(messages = dataFacade.readMessages())
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.InputChanged -> {
                _uiState.update { state ->
                    state.copy(inputText = intent.text)
                }
            }
            ChatIntent.SendClicked -> sendCurrentInput()
            ChatIntent.EffectConsumed -> {
                _effects.value = ChatEffect.None
            }
            is ChatIntent.EventArrived -> handleEvent(intent.event)
        }
    }

    override fun onCleared() {
        subscription.unsubscribe()
        facade.destroyKernel(sessionId)
        super.onCleared()
    }

    private fun sendCurrentInput() {
        val trimmedInput = _uiState.value.inputText.trim()
        if (trimmedInput.isEmpty()) {
            return
        }
        _uiState.update { state ->
            state.copy(
                inputText = "",
                isSending = true
            )
        }
        facade.sendMessage(sessionId, trimmedInput)
    }

    private fun handleEvent(event: ChatEvent) {
        if (event.sessionId != sessionId) {
            return
        }
        if (event is ChatEvent.MessagesChanged) {
            _uiState.update { state ->
                state.copy(
                    messages = dataFacade.readMessages(),
                    isSending = false
                )
            }
            _effects.value = ChatEffect.ScrollToLatest
        }
    }

    private companion object {
        const val DEFAULT_CHAT_NAME = "模拟聊天"
    }
}
