package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.backend.FakeChatBackend
import com.mashiro.dataengine.chat.backend.ChatScheduler
import com.mashiro.dataengine.chat.backend.ImmediateChatScheduler
import com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ChatEngineFacadeTest {

    @Test
    fun sendMessageCreatesUserMessageEchoReplyAndPublishesMessageEvent() {
        val facade = ChatEngineFacadeImpl(
            dataLayer = InMemoryChatDataLayer(
                idGenerator = IncrementalIdGenerator(),
                clock = IncrementalClock()
            ),
            backendFactory = { FakeChatBackend(ImmediateChatScheduler()) }
        )
        val sessionId = ChatSessionId("session-1")
        val observedEvents = mutableListOf<ChatEvent>()

        facade.createKernel(sessionId, "Session 1")
        val subscription = facade.eventFacade(sessionId).subscribe { event ->
            observedEvents += event
        }

        facade.sendMessage(sessionId, "hello")

        val messages = facade.dataFacade(sessionId).readMessages()
        assertEquals(2, messages.size)
        assertEquals("hello", messages[0].content)
        assertEquals(ChatSender.ME, messages[0].sender)
        assertEquals("hello", messages[1].content)
        assertEquals(ChatSender.OTHER, messages[1].sender)
        assertTrue(observedEvents.contains(ChatEvent.MessagesChanged(sessionId)))
        subscription.unsubscribe()
    }

    @Test
    fun facadeOperationsAreDrivenBySessionIdWithoutMixingMessages() {
        val facade = ChatEngineFacadeImpl(
            dataLayer = InMemoryChatDataLayer(
                idGenerator = IncrementalIdGenerator(),
                clock = IncrementalClock()
            ),
            backendFactory = { FakeChatBackend(ImmediateChatScheduler()) }
        )
        val firstSessionId = ChatSessionId("session-1")
        val secondSessionId = ChatSessionId("session-2")

        facade.createKernel(firstSessionId, "Session 1")
        facade.createKernel(secondSessionId, "Session 2")
        facade.sendMessage(firstSessionId, "hello")
        facade.receiveMessage(secondSessionId, "manual")

        val firstMessages = facade.dataFacade(firstSessionId).readMessages()
        val secondMessages = facade.dataFacade(secondSessionId).readMessages()

        assertEquals(listOf("hello", "hello"), firstMessages.map { it.content })
        assertEquals(listOf(ChatSender.ME, ChatSender.OTHER), firstMessages.map { it.sender })
        assertEquals(listOf("manual"), secondMessages.map { it.content })
        assertEquals(listOf(ChatSender.OTHER), secondMessages.map { it.sender })
    }

    @Test
    fun eventFacadeOnlyReceivesEventsForItsSessionKernel() {
        val facade = ChatEngineFacadeImpl(
            dataLayer = InMemoryChatDataLayer(
                idGenerator = IncrementalIdGenerator(),
                clock = IncrementalClock()
            ),
            backendFactory = { FakeChatBackend(ImmediateChatScheduler()) }
        )
        val firstSessionId = ChatSessionId("session-1")
        val secondSessionId = ChatSessionId("session-2")
        val observedEvents = mutableListOf<ChatEvent>()

        facade.createKernel(firstSessionId, "Session 1")
        facade.createKernel(secondSessionId, "Session 2")
        val subscription = facade.eventFacade(firstSessionId).subscribe { event ->
            observedEvents += event
        }

        facade.receiveMessage(secondSessionId, "other session")

        assertEquals(emptyList<ChatEvent>(), observedEvents)
        subscription.unsubscribe()
    }

    @Test
    fun dataFacadeResolvesKernelOnEachReadAndFailsClearlyAfterDestroy() {
        val facade = ChatEngineFacadeImpl(
            dataLayer = InMemoryChatDataLayer(
                idGenerator = IncrementalIdGenerator(),
                clock = IncrementalClock()
            ),
            backendFactory = { FakeChatBackend(ImmediateChatScheduler()) }
        )
        val sessionId = ChatSessionId("session-1")

        facade.createKernel(sessionId, "Session 1")
        val dataFacade = facade.dataFacade(sessionId)
        facade.destroyKernel(sessionId)

        val error = expectIllegalStateException {
            dataFacade.readMessages()
        }
        assertEquals("Chat kernel not found for sessionId=session-1", error.message)
    }

    @Test
    fun eventFacadeSubscribeResolvesKernelAtSubscribeTimeAndFailsClearlyAfterDestroy() {
        val facade = ChatEngineFacadeImpl(
            dataLayer = InMemoryChatDataLayer(
                idGenerator = IncrementalIdGenerator(),
                clock = IncrementalClock()
            ),
            backendFactory = { FakeChatBackend(ImmediateChatScheduler()) }
        )
        val sessionId = ChatSessionId("session-1")

        facade.createKernel(sessionId, "Session 1")
        val eventFacade = facade.eventFacade(sessionId)
        facade.destroyKernel(sessionId)

        val error = expectIllegalStateException {
            eventFacade.subscribe { }
        }
        assertEquals("Chat kernel not found for sessionId=session-1", error.message)
    }

    @Test
    fun pendingEchoReplyDoesNotMutateDestroyedKernel() {
        val scheduler = QueuedChatScheduler()
        val facade = ChatEngineFacadeImpl(
            dataLayer = InMemoryChatDataLayer(
                idGenerator = IncrementalIdGenerator(),
                clock = IncrementalClock()
            ),
            backendFactory = { FakeChatBackend(scheduler) }
        )
        val sessionId = ChatSessionId("session-1")

        facade.createKernel(sessionId, "Session 1")
        facade.sendMessage(sessionId, "hello")
        facade.destroyKernel(sessionId)

        scheduler.runQueuedActions()
        facade.createKernel(sessionId, "Session 1")

        val messages = facade.dataFacade(sessionId).readMessages()
        assertEquals(listOf("hello"), messages.map { it.content })
        assertEquals(listOf(ChatSender.ME), messages.map { it.sender })
    }

    @Test
    fun observerCanDestroyKernelFromMessagesChangedCallbackWithoutWaitingForLifecycleLock() {
        val facade = ChatEngineFacadeImpl(
            dataLayer = InMemoryChatDataLayer(
                idGenerator = IncrementalIdGenerator(),
                clock = IncrementalClock()
            ),
            backendFactory = { FakeChatBackend(ImmediateChatScheduler()) }
        )
        val sessionId = ChatSessionId("session-1")
        val destroyFinished = CountDownLatch(1)

        facade.createKernel(sessionId, "Session 1")
        facade.eventFacade(sessionId).subscribe { event ->
            if (event == ChatEvent.MessagesChanged(sessionId)) {
                Thread {
                    facade.destroyKernel(sessionId)
                    destroyFinished.countDown()
                }.start()

                assertTrue(
                    "destroyKernel should not wait for the lifecycle lock while observers run",
                    destroyFinished.await(500, TimeUnit.MILLISECONDS)
                )
            }
        }

        facade.receiveMessage(sessionId, "manual")
    }
}

private fun expectIllegalStateException(action: () -> Unit): IllegalStateException {
    try {
        action()
    } catch (error: IllegalStateException) {
        return error
    }
    fail("Expected IllegalStateException")
    error("unreachable")
}

private class IncrementalIdGenerator : () -> String {
    private var current = 0

    override fun invoke(): String {
        current += 1
        return current.toString()
    }
}

private class IncrementalClock : () -> Long {
    private var current = 0L

    override fun invoke(): Long {
        current += 1
        return current
    }
}

private class QueuedChatScheduler : ChatScheduler {
    private val queuedActions = mutableListOf<() -> Unit>()

    override fun postDelayed(delayMillis: Long, action: () -> Unit) {
        queuedActions += action
    }

    fun runQueuedActions() {
        queuedActions.toList().forEach { action ->
            action()
        }
        queuedActions.clear()
    }
}
