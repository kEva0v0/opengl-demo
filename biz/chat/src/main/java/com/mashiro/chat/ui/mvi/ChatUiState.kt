package com.mashiro.chat.ui.mvi

import com.mashiro.dataengine.chat.model.ChatMessage

data class ChatUiState(
    val chatName: String = "模拟聊天",
    val inputText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false
)
