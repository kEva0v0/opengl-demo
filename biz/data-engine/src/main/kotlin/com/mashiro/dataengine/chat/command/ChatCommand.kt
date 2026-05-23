package com.mashiro.dataengine.chat.command

import com.mashiro.dataengine.chat.event.ChatEvent

internal interface ChatCommand {
    fun execute(context: ChatCommandContext): List<ChatEvent>
}
