package com.mashiro.dataengine.chat.command

import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.kernel.ChatKernel
import com.mashiro.dataengine.chat.session.ChatSessionId

internal data class ChatCommandContext(
    val sessionId: ChatSessionId,
    val dataLayer: ChatDataLayer
) {
    companion object {
        fun from(kernel: ChatKernel): ChatCommandContext {
            return ChatCommandContext(
                sessionId = kernel.sessionId,
                dataLayer = kernel.dataLayer
            )
        }
    }
}
