package com.mashiro.chat.ui.mvi

import com.mashiro.dataengine.chat.event.ChatEvent

sealed class ChatIntent {
    data class InputChanged(val text: String) : ChatIntent()
    object SendClicked : ChatIntent()
    object EffectConsumed : ChatIntent()
    data class EventArrived(val event: ChatEvent) : ChatIntent()
}
