package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventSubscription

fun interface ChatEventFacade {
    fun subscribe(observer: (ChatEvent) -> Unit): ChatEventSubscription
}
