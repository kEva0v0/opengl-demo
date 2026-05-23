package com.mashiro.dataengine.chat.model

data class ChatMessage(
    val id: String,
    val content: String,
    val sender: ChatSender,
    val timestamp: Long
)

enum class ChatSender {
    ME,
    OTHER
}
