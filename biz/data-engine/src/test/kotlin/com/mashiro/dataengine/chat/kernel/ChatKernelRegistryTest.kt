package com.mashiro.dataengine.chat.kernel

import com.mashiro.dataengine.chat.backend.ChatBackend
import com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventBus
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ChatKernelRegistryTest {

    @Test
    fun registryCreatesFindsAndDestroysKernelBySessionId() {
        val dataLayer = InMemoryChatDataLayer()
        val registry = ChatKernelRegistry(dataLayer = dataLayer)
        val sessionId = ChatSessionId("session-1")

        val firstKernel = registry.createKernel(sessionId, "模拟聊天")
        val secondKernel = registry.createKernel(sessionId, "ignored")

        assertSame(firstKernel, secondKernel)
        assertSame(firstKernel, registry.findKernel(sessionId))
        assertEquals(ChatSession(sessionId, "模拟聊天"), dataLayer.readSession(sessionId))
        assertTrue(registry.destroyKernel(sessionId))
        assertNull(registry.findKernel(sessionId))
        assertEquals(false, registry.destroyKernel(sessionId))
    }

    @Test
    fun registryAcceptsBackendInterfaceImplementations() {
        val backend = RecordingChatBackend()
        val registry = ChatKernelRegistry(backendFactory = { backend })
        val kernel = registry.createKernel(ChatSessionId("session-1"), "模拟聊天")
        val replies = mutableListOf<String>()

        kernel.backend.echo("hello") { reply ->
            replies += reply
        }

        assertSame(backend, kernel.backend)
        assertEquals(listOf("hello"), replies)
    }

    @Test
    fun registryPublishesLifecycleEventsOnRegistryEventBus() {
        val registry = ChatKernelRegistry()
        val sessionId = ChatSessionId("session-1")
        val receivedEvents = mutableListOf<ChatEvent>()
        val subscription = registry.eventBus.subscribe { event ->
            receivedEvents += event
        }

        val kernel = registry.createKernel(sessionId, "模拟聊天")
        registry.destroyKernel(sessionId)
        subscription.unsubscribe()

        assertNotSame(registry.eventBus, kernel.eventBus)
        assertEquals(
            listOf(
                ChatEvent.KernelCreated(sessionId),
                ChatEvent.KernelDestroyed(sessionId)
            ),
            receivedEvents
        )
    }

    @Test
    fun registryLifecycleEventBusDoesNotReceiveKernelSessionEvents() {
        val registry = ChatKernelRegistry()
        val sessionId = ChatSessionId("session-1")
        val kernel = registry.createKernel(sessionId, "模拟聊天")
        val receivedEvents = mutableListOf<ChatEvent>()
        val subscription = registry.eventBus.subscribe { event ->
            receivedEvents += event
        }

        kernel.eventBus.publish(ChatEvent.MessagesChanged(sessionId))
        subscription.unsubscribe()

        assertEquals(emptyList<ChatEvent>(), receivedEvents)
    }

    @Test
    fun kernelPublishesEvents() {
        val registry = ChatKernelRegistry()
        val sessionId = ChatSessionId("session-1")
        val kernel = registry.createKernel(sessionId, "模拟聊天")
        val receivedEvents = mutableListOf<ChatEvent>()

        val subscription = kernel.eventBus.subscribe { event ->
            receivedEvents += event
        }
        kernel.eventBus.publish(ChatEvent.MessagesChanged(sessionId))
        subscription.unsubscribe()
        kernel.eventBus.publish(ChatEvent.ProcessStateChanged(sessionId))

        assertEquals(listOf(ChatEvent.MessagesChanged(sessionId)), receivedEvents)
    }

    @Test
    fun destroyWaitsForActiveKernelBlockBeforeMarkingInactive() {
        val kernel = ChatKernel(
            sessionId = ChatSessionId("session-1"),
            dataLayer = InMemoryChatDataLayer(),
            eventBus = ChatEventBus(),
            backend = RecordingChatBackend()
        )
        val blockStarted = CountDownLatch(1)
        val releaseBlock = CountDownLatch(1)
        val destroyFinished = CountDownLatch(1)

        val mutationThread = Thread {
            kernel.mutateIfActive {
                blockStarted.countDown()
                assertTrue(releaseBlock.await(1, TimeUnit.SECONDS))
                emptyList()
            }
        }
        mutationThread.start()
        assertTrue(blockStarted.await(1, TimeUnit.SECONDS))

        val destroyThread = Thread {
            kernel.destroy()
            destroyFinished.countDown()
        }
        destroyThread.start()

        assertEquals(1L, destroyFinished.count)
        releaseBlock.countDown()
        assertTrue(destroyFinished.await(1, TimeUnit.SECONDS))
        mutationThread.join(1_000)
        destroyThread.join(1_000)
        assertEquals(false, kernel.isActive())
    }
}

private class RecordingChatBackend : ChatBackend {
    override fun echo(content: String, onReply: (String) -> Unit) {
        onReply(content)
    }
}
