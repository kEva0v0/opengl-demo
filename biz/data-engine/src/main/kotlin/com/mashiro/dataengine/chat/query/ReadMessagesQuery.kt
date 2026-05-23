package com.mashiro.dataengine.chat.query

import com.mashiro.dataengine.chat.kernel.ChatKernel
import com.mashiro.dataengine.chat.model.ChatMessage

internal class ReadMessagesQuery(
    private val kernel: ChatKernel
) {
    fun execute(): List<ChatMessage> {
        return kernel.dataLayer.readMessages(kernel.sessionId)
    }
}
