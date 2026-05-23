package com.mashiro.dataengine.chat.crud

import com.mashiro.dataengine.chat.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

internal interface ChatCrudStore {
    val messages: StateFlow<List<ChatMessage>>

    fun execute(command: ChatCrudCommand): ChatCrudResult
}
