package com.mashiro.dataengine.chat.event

class ChatEventBus {
    private val lock = Any()
    private val subscribers = mutableListOf<SubscriptionRecord>()

    fun subscribe(subscriber: (ChatEvent) -> Unit): ChatEventSubscription {
        val record = SubscriptionRecord(subscriber)
        synchronized(lock) {
            subscribers += record
        }
        return ChatEventSubscription {
            synchronized(lock) {
                subscribers -= record
            }
        }
    }

    fun publish(event: ChatEvent) {
        val snapshot = synchronized(lock) {
            subscribers.toList()
        }
        snapshot.forEach { record ->
            record.subscriber(event)
        }
    }

    private class SubscriptionRecord(
        val subscriber: (ChatEvent) -> Unit
    )
}

class ChatEventSubscription(
    private val onUnsubscribe: () -> Unit
) {
    private val lock = Any()
    private var unsubscribed = false

    fun unsubscribe() {
        val shouldUnsubscribe = synchronized(lock) {
            if (unsubscribed) {
                false
            } else {
                unsubscribed = true
                true
            }
        }
        if (shouldUnsubscribe) {
            onUnsubscribe()
        }
    }
}
