package com.mashiro.dataengine.chat.backend

internal interface ChatBackend {
    fun echo(content: String, onReply: (String) -> Unit)
}
