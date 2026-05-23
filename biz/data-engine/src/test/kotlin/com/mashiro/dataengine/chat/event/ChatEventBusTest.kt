package com.mashiro.dataengine.chat.event

import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatEventBusTest {

    @Test
    fun subscribeTreatsEachCallAsIndependentRegistration() {
        val eventBus = ChatEventBus()
        val sessionId = ChatSessionId("session-1")
        var receivedCount = 0
        val observer: (ChatEvent) -> Unit = {
            receivedCount += 1
        }

        val firstSubscription = eventBus.subscribe(observer)
        val secondSubscription = eventBus.subscribe(observer)

        eventBus.publish(ChatEvent.MessagesChanged(sessionId))
        firstSubscription.unsubscribe()
        eventBus.publish(ChatEvent.MessagesChanged(sessionId))
        secondSubscription.unsubscribe()
        eventBus.publish(ChatEvent.MessagesChanged(sessionId))

        assertEquals(3, receivedCount)
    }

    @Test
    fun subscriptionCanBeCreatedByTestsAndDownstreamModules() {
        var unsubscribed = false
        val subscription = ChatEventSubscription {
            unsubscribed = true
        }

        subscription.unsubscribe()

        assertEquals(true, unsubscribed)
    }
}
