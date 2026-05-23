# Chat Facade Kernel MVI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor chat so Android UI uses MVI and only talks to `:biz:data-engine` through a single facade API keyed by `sessionId`.

**Architecture:** `:biz:data-engine` owns Facade, Kernel registry, Mutator/Query orchestration, Command atoms, EventFacade, DataFacade, and in-memory DataLayer. `:biz:chat` owns Compose UI and ViewModel only; it does not construct or import Kernel, backend, CRUD store, dispatcher, mutators, queries, or commands. The Android layer sends intents to ViewModel, ViewModel calls `ChatEngineFacade` with `sessionId`, subscribes to facade events, queries messages through facade data access, and reduces MVI state.

**Tech Stack:** Kotlin/JVM module `:biz:data-engine`, Android Compose module `:biz:chat`, Kotlin Flow/SharedFlow/StateFlow, JUnit4, Gradle 7.4, Kotlin 1.7.10, Compose compiler 1.3.0.

---

## File Structure

Create or modify these files:

- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatSessionId.kt`: session id value object.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatSession.kt`: session metadata model.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatProcessState.kt`: process state model.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/event/ChatEvent.kt`: all events exposed to upper layers.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/event/ChatEventBus.kt`: internal SharedFlow event publisher.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/kernel/ChatKernel.kt`: internal runtime object for one session.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/kernel/ChatKernelRegistry.kt`: session id to Kernel map, internal to facade implementation.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacade.kt`: only public business entry for Android/UI.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEventFacade.kt`: event subscription facade.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatDataFacade.kt`: data query facade.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacadeImpl.kt`: facade implementation that creates/finds/destroys Kernel by session id.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/datalayer/ChatDataLayer.kt`: aggregate in-memory storage boundary.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/datalayer/InMemoryChatDataLayer.kt`: in-memory implementation for messages, sessions, and process state.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/SendMessageMutator.kt`: business mutator for sending.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/ReceiveMessageMutator.kt`: business mutator for receiving.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/EchoBackendMutator.kt`: fake backend echo orchestration.
- Create `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/query/ReadMessagesQuery.kt`: query object for message reads.
- Modify `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/*.kt`: move existing command atoms under the new Kernel/Mutator context.
- Delete or retire `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/ChatCommandDispatcher.kt` and `EchoBackendCommand.kt`: mutators orchestrate command atoms directly inside data-engine.
- Modify `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/crud/*.kt`: keep CRUD command-driven and move access behind `ChatDataLayer`.
- Modify `biz/chat/src/main/java/com/mashiro/chat/ui/ChatViewModel.kt`: replace direct backend/store/dispatcher references with MVI reducer and facade calls.
- Modify `biz/chat/src/main/java/com/mashiro/chat/ui/ChatScreen.kt`: accept `ChatUiState`, `ChatIntent`, and `ChatEffect` style inputs.
- Create `biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatIntent.kt`: MVI user and event intents.
- Create `biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatUiState.kt`: MVI state.
- Create `biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatEffect.kt`: one-shot UI effects.
- Modify `biz/data-engine/src/test/kotlin/com/mashiro/dataengine/chat/command/ChatCommandDispatcherTest.kt`: replace direct dispatcher expectations with facade/kernel behavior tests.
- Create `biz/data-engine/src/test/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacadeTest.kt`: facade-only upper-layer API tests.
- Create `biz/data-engine/src/test/kotlin/com/mashiro/dataengine/chat/kernel/ChatKernelRegistryTest.kt`: registry behavior tests.
- Modify `biz/chat/src/test/java/com/mashiro/chat/ui/ChatViewModelTest.kt`: MVI ViewModel tests against a fake facade.

## Public API Target

The upper layer depends only on facade-layer operations plus value/event models. It must not hold `ChatKernel`, command, CRUD, mutator, query, backend, or any per-session runtime handle. The upper layer always passes `sessionId` into facade calls; the facade resolves the matching Kernel internally.

The upper layer may import only these data-engine packages:

```kotlin
com.mashiro.dataengine.chat.facade.ChatEngineFacade
com.mashiro.dataengine.chat.facade.ChatEventFacade
com.mashiro.dataengine.chat.facade.ChatDataFacade
com.mashiro.dataengine.chat.event.ChatEvent
com.mashiro.dataengine.chat.model.ChatMessage
com.mashiro.dataengine.chat.model.ChatSender
com.mashiro.dataengine.chat.session.ChatSessionId
```

The upper layer must not import these packages:

```kotlin
com.mashiro.dataengine.chat.kernel.*
com.mashiro.dataengine.chat.command.*
com.mashiro.dataengine.chat.crud.*
com.mashiro.dataengine.chat.datalayer.*
com.mashiro.dataengine.chat.mutator.*
com.mashiro.dataengine.chat.query.*
com.mashiro.dataengine.chat.backend.*
```

## Task 1: Introduce Session Models and DataLayer

**Files:**
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatSessionId.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatSession.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatProcessState.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/datalayer/ChatDataLayer.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/datalayer/InMemoryChatDataLayer.kt`
- Test: `biz/data-engine/src/test/kotlin/com/mashiro/dataengine/chat/datalayer/InMemoryChatDataLayerTest.kt`

- [ ] **Step 1: Write the failing DataLayer test**

```kotlin
package com.mashiro.dataengine.chat.datalayer

import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryChatDataLayerTest {
    @Test
    fun storesSessionMessagesAndProcessStateBySessionId() {
        val sessionId = ChatSessionId("session-1")
        val dataLayer = InMemoryChatDataLayer(
            idGenerator = IncrementalIdGenerator(),
            clock = IncrementalClock()
        )

        dataLayer.createSession(ChatSession(sessionId, "模拟聊天"))
        dataLayer.createMessage(sessionId, "hello", ChatSender.ME)
        dataLayer.updateProcessState(sessionId, ChatProcessState(receiving = true))

        assertEquals(ChatSession(sessionId, "模拟聊天"), dataLayer.readSession(sessionId))
        assertEquals("hello", dataLayer.readMessages(sessionId).single().content)
        assertEquals(ChatSender.ME, dataLayer.readMessages(sessionId).single().sender)
        assertEquals(ChatProcessState(receiving = true), dataLayer.readProcessState(sessionId))
    }
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
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :biz:data-engine:test --tests com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayerTest
```

Expected: FAIL with unresolved references for `ChatSessionId`, `ChatSession`, `ChatProcessState`, `InMemoryChatDataLayer`.

- [ ] **Step 3: Implement session models and DataLayer**

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatSessionId.kt
package com.mashiro.dataengine.chat.session

data class ChatSessionId(val value: String)
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatSession.kt
package com.mashiro.dataengine.chat.session

data class ChatSession(
    val id: ChatSessionId,
    val title: String
)
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/session/ChatProcessState.kt
package com.mashiro.dataengine.chat.session

data class ChatProcessState(
    val receiving: Boolean = false
)
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/datalayer/ChatDataLayer.kt
package com.mashiro.dataengine.chat.datalayer

import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId

interface ChatDataLayer {
    fun createSession(session: ChatSession)
    fun readSession(sessionId: ChatSessionId): ChatSession?
    fun createMessage(sessionId: ChatSessionId, content: String, sender: ChatSender): ChatMessage
    fun readMessages(sessionId: ChatSessionId): List<ChatMessage>
    fun updateProcessState(sessionId: ChatSessionId, state: ChatProcessState)
    fun readProcessState(sessionId: ChatSessionId): ChatProcessState
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/datalayer/InMemoryChatDataLayer.kt
package com.mashiro.dataengine.chat.datalayer

import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId
import java.util.UUID

class InMemoryChatDataLayer(
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ChatDataLayer {
    private val sessions = linkedMapOf<ChatSessionId, ChatSession>()
    private val messages = linkedMapOf<ChatSessionId, List<ChatMessage>>()
    private val processStates = linkedMapOf<ChatSessionId, ChatProcessState>()

    override fun createSession(session: ChatSession) {
        sessions[session.id] = session
        messages.putIfAbsent(session.id, emptyList())
        processStates.putIfAbsent(session.id, ChatProcessState())
    }

    override fun readSession(sessionId: ChatSessionId): ChatSession? = sessions[sessionId]

    override fun createMessage(sessionId: ChatSessionId, content: String, sender: ChatSender): ChatMessage {
        val message = ChatMessage(idGenerator(), content, sender, clock())
        messages[sessionId] = readMessages(sessionId) + message
        return message
    }

    override fun readMessages(sessionId: ChatSessionId): List<ChatMessage> = messages[sessionId].orEmpty()

    override fun updateProcessState(sessionId: ChatSessionId, state: ChatProcessState) {
        processStates[sessionId] = state
    }

    override fun readProcessState(sessionId: ChatSessionId): ChatProcessState {
        return processStates[sessionId] ?: ChatProcessState()
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
./gradlew :biz:data-engine:test --tests com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayerTest
```

Expected: PASS.

## Task 2: Add Event Bus and Kernel Registry Hidden Behind Facade

**Files:**
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/event/ChatEvent.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/event/ChatEventBus.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/kernel/ChatKernel.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/kernel/ChatKernelRegistry.kt`
- Test: `biz/data-engine/src/test/kotlin/com/mashiro/dataengine/chat/kernel/ChatKernelRegistryTest.kt`

- [ ] **Step 1: Write failing registry/event test**

```kotlin
package com.mashiro.dataengine.chat.kernel

import com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatKernelRegistryTest {
    @Test
    fun registryCreatesFindsAndDestroysKernelBySessionId() {
        val registry = ChatKernelRegistry(InMemoryChatDataLayer())
        val sessionId = ChatSessionId("session-1")

        val created = registry.createKernel(sessionId, "模拟聊天")
        val found = registry.findKernel(sessionId)

        assertSame(created, found)
        assertTrue(registry.destroyKernel(sessionId))
        assertEquals(null, registry.findKernel(sessionId))
    }

    @Test
    fun kernelPublishesEventsWithoutLeavingKernelTypePublicToUpperLayer() {
        val registry = ChatKernelRegistry(InMemoryChatDataLayer())
        val sessionId = ChatSessionId("session-1")
        val kernel = registry.createKernel(sessionId, "模拟聊天")
        val received = mutableListOf<ChatEvent>()

        val subscription = kernel.eventBus.subscribe { event ->
            received.add(event)
        }
        kernel.eventBus.publish(ChatEvent.MessagesChanged(sessionId))

        assertEquals(listOf(ChatEvent.MessagesChanged(sessionId)), received)
        subscription.unsubscribe()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :biz:data-engine:test --tests com.mashiro.dataengine.chat.kernel.ChatKernelRegistryTest
```

Expected: FAIL with unresolved references for event/kernel classes.

- [ ] **Step 3: Implement events, subscription, kernel, and registry**

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/event/ChatEvent.kt
package com.mashiro.dataengine.chat.event

import com.mashiro.dataengine.chat.session.ChatSessionId

sealed class ChatEvent {
    data class KernelCreated(val sessionId: ChatSessionId) : ChatEvent()
    data class KernelDestroyed(val sessionId: ChatSessionId) : ChatEvent()
    data class MessagesChanged(val sessionId: ChatSessionId) : ChatEvent()
    data class ProcessStateChanged(val sessionId: ChatSessionId) : ChatEvent()
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/event/ChatEventBus.kt
package com.mashiro.dataengine.chat.event

class ChatEventBus {
    private val observers = linkedSetOf<(ChatEvent) -> Unit>()

    fun publish(event: ChatEvent) {
        observers.toList().forEach { observer -> observer(event) }
    }

    fun subscribe(observer: (ChatEvent) -> Unit): ChatEventSubscription {
        observers.add(observer)
        return ChatEventSubscription { observers.remove(observer) }
    }
}

class ChatEventSubscription(private val unsubscribeAction: () -> Unit) {
    fun unsubscribe() {
        unsubscribeAction()
    }
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/kernel/ChatKernel.kt
package com.mashiro.dataengine.chat.kernel

import com.mashiro.dataengine.chat.backend.FakeChatBackend
import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEventBus
import com.mashiro.dataengine.chat.session.ChatSessionId

internal class ChatKernel(
    val sessionId: ChatSessionId,
    val dataLayer: ChatDataLayer,
    val eventBus: ChatEventBus,
    val backend: FakeChatBackend
)
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/kernel/ChatKernelRegistry.kt
package com.mashiro.dataengine.chat.kernel

import com.mashiro.dataengine.chat.backend.FakeChatBackend
import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventBus
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId

internal class ChatKernelRegistry(
    private val dataLayer: ChatDataLayer,
    private val backendFactory: () -> FakeChatBackend = { FakeChatBackend() }
) {
    private val kernels = linkedMapOf<ChatSessionId, ChatKernel>()

    fun createKernel(sessionId: ChatSessionId, title: String): ChatKernel {
        return kernels.getOrPut(sessionId) {
            dataLayer.createSession(ChatSession(sessionId, title))
            ChatKernel(sessionId, dataLayer, ChatEventBus(), backendFactory()).also { kernel ->
                kernel.eventBus.publish(ChatEvent.KernelCreated(sessionId))
            }
        }
    }

    fun findKernel(sessionId: ChatSessionId): ChatKernel? = kernels[sessionId]

    fun destroyKernel(sessionId: ChatSessionId): Boolean {
        val kernel = kernels.remove(sessionId) ?: return false
        kernel.eventBus.publish(ChatEvent.KernelDestroyed(sessionId))
        return true
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
./gradlew :biz:data-engine:test --tests com.mashiro.dataengine.chat.kernel.ChatKernelRegistryTest
```

Expected: PASS.

## Task 3: Build Facade-Only Data-Engine API

**Files:**
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacade.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacadeImpl.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEventFacade.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatDataFacade.kt`
- Test: `biz/data-engine/src/test/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacadeTest.kt`

- [ ] **Step 1: Write failing facade-only test**

```kotlin
package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.backend.FakeChatBackend
import com.mashiro.dataengine.chat.backend.ImmediateChatScheduler
import com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatEngineFacadeTest {
    @Test
    fun upperLayerSendsBySessionIdAndConsumesEventsThenQueriesMessages() {
        val facade = ChatEngineFacade.create(
            dataLayer = InMemoryChatDataLayer(
                idGenerator = IncrementalIdGenerator(),
                clock = IncrementalClock()
            ),
            backendFactory = { FakeChatBackend(ImmediateChatScheduler()) }
        )
        val sessionId = ChatSessionId("session-1")
        val events = mutableListOf<ChatEvent>()

        facade.createKernel(sessionId, "模拟聊天")
        val subscription = facade.eventFacade(sessionId).subscribe { event ->
            events.add(event)
        }

        facade.sendMessage(sessionId, "hello")

        val messages = facade.dataFacade(sessionId).readMessages()
        assertEquals(2, messages.size)
        assertEquals(ChatSender.ME, messages[0].sender)
        assertEquals("hello", messages[0].content)
        assertEquals(ChatSender.OTHER, messages[1].sender)
        assertEquals("hello", messages[1].content)
        assertTrue(events.contains(ChatEvent.MessagesChanged(sessionId)))

        subscription.unsubscribe()
    }
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
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :biz:data-engine:test --tests com.mashiro.dataengine.chat.facade.ChatEngineFacadeTest
```

Expected: FAIL with unresolved references for facade classes.

- [ ] **Step 3: Implement facade interfaces and implementation**

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacade.kt
package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.backend.FakeChatBackend
import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.datalayer.InMemoryChatDataLayer
import com.mashiro.dataengine.chat.session.ChatSessionId

interface ChatEngineFacade {
    fun createKernel(sessionId: ChatSessionId, title: String)
    fun bindKernel(sessionId: ChatSessionId)
    fun destroyKernel(sessionId: ChatSessionId): Boolean
    fun sendMessage(sessionId: ChatSessionId, content: String)
    fun receiveMessage(sessionId: ChatSessionId, content: String)
    fun eventFacade(sessionId: ChatSessionId): ChatEventFacade
    fun dataFacade(sessionId: ChatSessionId): ChatDataFacade

    companion object {
        fun create(
            dataLayer: ChatDataLayer = InMemoryChatDataLayer(),
            backendFactory: () -> FakeChatBackend = { FakeChatBackend() }
        ): ChatEngineFacade {
            return ChatEngineFacadeImpl(dataLayer, backendFactory)
        }
    }
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEventFacade.kt
package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventSubscription

interface ChatEventFacade {
    fun subscribe(observer: (ChatEvent) -> Unit): ChatEventSubscription
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatDataFacade.kt
package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession

interface ChatDataFacade {
    fun readSession(): ChatSession?
    fun readMessages(): List<ChatMessage>
    fun readProcessState(): ChatProcessState
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacadeImpl.kt
package com.mashiro.dataengine.chat.facade

import com.mashiro.dataengine.chat.backend.FakeChatBackend
import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventSubscription
import com.mashiro.dataengine.chat.kernel.ChatKernel
import com.mashiro.dataengine.chat.kernel.ChatKernelRegistry
import com.mashiro.dataengine.chat.mutator.SendMessageMutator
import com.mashiro.dataengine.chat.mutator.ReceiveMessageMutator
import com.mashiro.dataengine.chat.session.ChatSessionId

internal class ChatEngineFacadeImpl(
    private val dataLayer: ChatDataLayer,
    backendFactory: () -> FakeChatBackend
) : ChatEngineFacade {
    private val registry = ChatKernelRegistry(dataLayer, backendFactory)

    override fun createKernel(sessionId: ChatSessionId, title: String) {
        registry.createKernel(sessionId, title)
    }

    override fun bindKernel(sessionId: ChatSessionId) {
        requireKernel(sessionId)
    }

    override fun destroyKernel(sessionId: ChatSessionId): Boolean = registry.destroyKernel(sessionId)

    override fun sendMessage(sessionId: ChatSessionId, content: String) {
        SendMessageMutator(requireKernel(sessionId)).mutate(content)
    }

    override fun receiveMessage(sessionId: ChatSessionId, content: String) {
        ReceiveMessageMutator(requireKernel(sessionId)).mutate(content)
    }

    override fun eventFacade(sessionId: ChatSessionId): ChatEventFacade {
        val kernel = requireKernel(sessionId)
        return object : ChatEventFacade {
            override fun subscribe(observer: (ChatEvent) -> Unit): ChatEventSubscription =
                kernel.eventBus.subscribe(observer)
        }
    }

    override fun dataFacade(sessionId: ChatSessionId): ChatDataFacade {
        return object : ChatDataFacade {
            override fun readSession() = dataLayer.readSession(sessionId)
            override fun readMessages() = dataLayer.readMessages(sessionId)
            override fun readProcessState() = dataLayer.readProcessState(sessionId)
        }
    }

    private fun requireKernel(sessionId: ChatSessionId): ChatKernel {
        return registry.findKernel(sessionId)
            ?: error("Chat kernel not found for sessionId=${sessionId.value}")
    }
}
```

- [ ] **Step 4: Run test to verify it now fails only for missing mutators**

Run:

```bash
./gradlew :biz:data-engine:test --tests com.mashiro.dataengine.chat.facade.ChatEngineFacadeTest
```

Expected: FAIL with unresolved references for `SendMessageMutator` and `ReceiveMessageMutator`.

## Task 4: Move Business Flow Into Mutator/Query Over Command Atoms

**Files:**
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/SendMessageMutator.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/ReceiveMessageMutator.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/EchoBackendMutator.kt`
- Create: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/query/ReadMessagesQuery.kt`
- Modify: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/ChatCommandContext.kt`
- Modify: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/SendUserMessageCommand.kt`
- Modify: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/ReceiveBackendMessageCommand.kt`
- Delete if unused: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/ChatCommandDispatcher.kt`
- Delete if unused: `biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/EchoBackendCommand.kt`
- Test: `biz/data-engine/src/test/kotlin/com/mashiro/dataengine/chat/facade/ChatEngineFacadeTest.kt`

- [ ] **Step 1: Keep the failing facade test from Task 3 as the red test**

Run:

```bash
./gradlew :biz:data-engine:test --tests com.mashiro.dataengine.chat.facade.ChatEngineFacadeTest
```

Expected: FAIL with missing mutators or command context mismatch.

- [ ] **Step 2: Implement Kernel-scoped command context and mutators**

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/ChatCommandContext.kt
package com.mashiro.dataengine.chat.command

import com.mashiro.dataengine.chat.datalayer.ChatDataLayer
import com.mashiro.dataengine.chat.event.ChatEventBus
import com.mashiro.dataengine.chat.session.ChatSessionId

data class ChatCommandContext(
    val sessionId: ChatSessionId,
    val dataLayer: ChatDataLayer,
    val eventBus: ChatEventBus
)
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/SendUserMessageCommand.kt
package com.mashiro.dataengine.chat.command

import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.model.ChatSender

class SendUserMessageCommand(
    private val content: String
) : ChatCommand {
    override fun execute(context: ChatCommandContext) {
        context.dataLayer.createMessage(context.sessionId, content, ChatSender.ME)
        context.eventBus.publish(ChatEvent.MessagesChanged(context.sessionId))
    }
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/command/ReceiveBackendMessageCommand.kt
package com.mashiro.dataengine.chat.command

import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.model.ChatSender

class ReceiveBackendMessageCommand(
    private val content: String
) : ChatCommand {
    override fun execute(context: ChatCommandContext) {
        context.dataLayer.createMessage(context.sessionId, content, ChatSender.OTHER)
        context.eventBus.publish(ChatEvent.MessagesChanged(context.sessionId))
    }
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/SendMessageMutator.kt
package com.mashiro.dataengine.chat.mutator

import com.mashiro.dataengine.chat.command.ChatCommandContext
import com.mashiro.dataengine.chat.command.SendUserMessageCommand
import com.mashiro.dataengine.chat.kernel.ChatKernel

internal class SendMessageMutator(
    private val kernel: ChatKernel
) {
    fun mutate(content: String) {
        SendUserMessageCommand(content).execute(
            ChatCommandContext(kernel.sessionId, kernel.dataLayer, kernel.eventBus)
        )
        EchoBackendMutator(kernel).mutate(content)
    }
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/ReceiveMessageMutator.kt
package com.mashiro.dataengine.chat.mutator

import com.mashiro.dataengine.chat.command.ChatCommandContext
import com.mashiro.dataengine.chat.command.ReceiveBackendMessageCommand
import com.mashiro.dataengine.chat.kernel.ChatKernel

internal class ReceiveMessageMutator(
    private val kernel: ChatKernel
) {
    fun mutate(content: String) {
        ReceiveBackendMessageCommand(content).execute(
            ChatCommandContext(kernel.sessionId, kernel.dataLayer, kernel.eventBus)
        )
    }
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/mutator/EchoBackendMutator.kt
package com.mashiro.dataengine.chat.mutator

import com.mashiro.dataengine.chat.kernel.ChatKernel

internal class EchoBackendMutator(
    private val kernel: ChatKernel
) {
    fun mutate(content: String) {
        kernel.backend.echo(content) { reply ->
            ReceiveMessageMutator(kernel).mutate(reply)
        }
    }
}
```

```kotlin
// biz/data-engine/src/main/kotlin/com/mashiro/dataengine/chat/query/ReadMessagesQuery.kt
package com.mashiro.dataengine.chat.query

import com.mashiro.dataengine.chat.kernel.ChatKernel

internal class ReadMessagesQuery(
    private val kernel: ChatKernel
) {
    fun query() = kernel.dataLayer.readMessages(kernel.sessionId)
}
```

- [ ] **Step 3: Run facade test to verify it passes**

Run:

```bash
./gradlew :biz:data-engine:test --tests com.mashiro.dataengine.chat.facade.ChatEngineFacadeTest
```

Expected: PASS.

- [ ] **Step 4: Run all data-engine tests**

Run:

```bash
./gradlew :biz:data-engine:test
```

Expected: PASS.

## Task 5: Replace Android ViewModel With MVI and Facade-Only Dependency

**Files:**
- Create: `biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatIntent.kt`
- Create: `biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatUiState.kt`
- Create: `biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatEffect.kt`
- Modify: `biz/chat/src/main/java/com/mashiro/chat/ui/ChatViewModel.kt`
- Modify: `biz/chat/src/test/java/com/mashiro/chat/ui/ChatViewModelTest.kt`

- [ ] **Step 1: Write failing MVI ViewModel test**

```kotlin
package com.mashiro.chat.ui

import com.mashiro.chat.ui.mvi.ChatEffect
import com.mashiro.chat.ui.mvi.ChatIntent
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.facade.ChatDataFacade
import com.mashiro.dataengine.chat.facade.ChatEngineFacade
import com.mashiro.dataengine.chat.facade.ChatEventFacade
import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender
import com.mashiro.dataengine.chat.session.ChatProcessState
import com.mashiro.dataengine.chat.session.ChatSession
import com.mashiro.dataengine.chat.session.ChatSessionId
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatViewModelTest {
    @Test
    fun mviSendClickedCallsFacadeAndRefreshesStateFromDataFacadeOnMessageChangedEvent() {
        val facade = FakeChatEngineFacade()
        val viewModel = ChatViewModel(facade = facade)

        viewModel.onIntent(ChatIntent.InputChanged(" hello "))
        viewModel.onIntent(ChatIntent.SendClicked)
        facade.emit(ChatEvent.MessagesChanged(ChatSessionId("default")))

        assertEquals("hello", facade.sentMessages.single())
        assertEquals("", viewModel.uiState.value.inputText)
        assertEquals(2, viewModel.uiState.value.messages.size)
        assertEquals(ChatSender.ME, viewModel.uiState.value.messages[0].sender)
        assertEquals(ChatSender.OTHER, viewModel.uiState.value.messages[1].sender)
        assertEquals(ChatEffect.ScrollToLatest, viewModel.effects.value)
    }
}

private class FakeChatEngineFacade : ChatEngineFacade {
    val sentMessages = mutableListOf<String>()
    private var eventObserver: ((ChatEvent) -> Unit)? = null
    private val sessionId = ChatSessionId("default")
    private val messages = listOf(
        ChatMessage("1", "hello", ChatSender.ME, 1L),
        ChatMessage("2", "hello", ChatSender.OTHER, 2L)
    )

    override fun createKernel(sessionId: ChatSessionId, title: String) {}
    override fun bindKernel(sessionId: ChatSessionId) {}
    override fun destroyKernel(sessionId: ChatSessionId) = true
    override fun sendMessage(sessionId: ChatSessionId, content: String) {
        sentMessages.add(content)
    }
    override fun receiveMessage(sessionId: ChatSessionId, content: String) {}
    override fun eventFacade(sessionId: ChatSessionId): ChatEventFacade {
        return object : ChatEventFacade {
            override fun subscribe(observer: (ChatEvent) -> Unit) =
                com.mashiro.dataengine.chat.event.ChatEventSubscription {
                    eventObserver = null
                }.also {
                    eventObserver = observer
                }
        }
    }
    override fun dataFacade(sessionId: ChatSessionId): ChatDataFacade {
        return object : ChatDataFacade {
            override fun readSession() = ChatSession(this@FakeChatEngineFacade.sessionId, "模拟聊天")
            override fun readMessages() = messages
            override fun readProcessState() = ChatProcessState()
        }
    }
    fun emit(event: ChatEvent) {
        eventObserver?.invoke(event)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :biz:chat:testDebugUnitTest --tests com.mashiro.chat.ui.ChatViewModelTest
```

Expected: FAIL with missing `ChatIntent`, `ChatUiState`, `ChatEffect`, `uiState`, `effects`, and `onIntent`.

- [ ] **Step 3: Implement MVI contracts**

```kotlin
// biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatIntent.kt
package com.mashiro.chat.ui.mvi

import com.mashiro.dataengine.chat.event.ChatEvent

sealed class ChatIntent {
    data class InputChanged(val text: String) : ChatIntent()
    object SendClicked : ChatIntent()
    data class EventArrived(val event: ChatEvent) : ChatIntent()
}
```

```kotlin
// biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatUiState.kt
package com.mashiro.chat.ui.mvi

import com.mashiro.dataengine.chat.model.ChatMessage

data class ChatUiState(
    val chatName: String = "模拟聊天",
    val inputText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false
)
```

```kotlin
// biz/chat/src/main/java/com/mashiro/chat/ui/mvi/ChatEffect.kt
package com.mashiro.chat.ui.mvi

sealed class ChatEffect {
    object None : ChatEffect()
    object ScrollToLatest : ChatEffect()
}
```

- [ ] **Step 4: Implement facade-only ViewModel**

```kotlin
package com.mashiro.chat.ui

import androidx.lifecycle.ViewModel
import com.mashiro.chat.ui.mvi.ChatEffect
import com.mashiro.chat.ui.mvi.ChatIntent
import com.mashiro.chat.ui.mvi.ChatUiState
import com.mashiro.dataengine.chat.event.ChatEvent
import com.mashiro.dataengine.chat.event.ChatEventSubscription
import com.mashiro.dataengine.chat.facade.ChatEngineFacade
import com.mashiro.dataengine.chat.session.ChatSessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel(
    private val facade: ChatEngineFacade = ChatEngineFacade.create(),
    private val sessionId: ChatSessionId = ChatSessionId("default")
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    private val _effects = MutableStateFlow<ChatEffect>(ChatEffect.None)
    private var subscription: ChatEventSubscription? = null

    val uiState: StateFlow<ChatUiState> = _uiState
    val effects: StateFlow<ChatEffect> = _effects

    init {
        facade.createKernel(sessionId, "模拟聊天")
        facade.bindKernel(sessionId)
        subscription = facade.eventFacade(sessionId).subscribe { event ->
            onIntent(ChatIntent.EventArrived(event))
        }
        refreshMessages()
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.InputChanged -> _uiState.value = _uiState.value.copy(inputText = intent.text)
            ChatIntent.SendClicked -> sendCurrentInput()
            is ChatIntent.EventArrived -> handleEvent(intent.event)
        }
    }

    private fun sendCurrentInput() {
        val content = _uiState.value.inputText.trim()
        if (content.isEmpty()) {
            return
        }
        _uiState.value = _uiState.value.copy(inputText = "", isSending = true)
        facade.sendMessage(sessionId, content)
    }

    private fun handleEvent(event: ChatEvent) {
        if (event is ChatEvent.MessagesChanged && event.sessionId == sessionId) {
            refreshMessages()
            _uiState.value = _uiState.value.copy(isSending = false)
            _effects.value = ChatEffect.ScrollToLatest
        }
    }

    private fun refreshMessages() {
        val dataFacade = facade.dataFacade(sessionId)
        _uiState.value = _uiState.value.copy(
            chatName = dataFacade.readSession()?.title ?: "模拟聊天",
            messages = dataFacade.readMessages()
        )
    }

    override fun onCleared() {
        subscription?.unsubscribe()
        facade.destroyKernel(sessionId)
        super.onCleared()
    }
}
```

- [ ] **Step 5: Run ViewModel test**

Run:

```bash
./gradlew :biz:chat:testDebugUnitTest --tests com.mashiro.chat.ui.ChatViewModelTest
```

Expected: PASS.

## Task 6: Update Compose UI To Consume MVI State and Intent

**Files:**
- Modify: `biz/chat/src/main/java/com/mashiro/chat/ui/ChatScreen.kt`

- [ ] **Step 1: Replace `ChatRoute` with MVI state consumption**

```kotlin
@Composable
fun ChatRoute(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val effect by viewModel.effects.collectAsState()

    ChatScreen(
        state = uiState,
        effect = effect,
        onIntent = viewModel::onIntent
    )
}
```

- [ ] **Step 2: Replace `ChatScreen` signature**

```kotlin
@Composable
fun ChatScreen(
    state: ChatUiState,
    effect: ChatEffect,
    onIntent: (ChatIntent) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(effect, state.messages.size) {
        if (effect == ChatEffect.ScrollToLatest && state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F4F4))
        ) {
            ChatTitle(chatName = state.chatName)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                state = listState
            ) {
                items(state.messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }
            }
            ChatInputBar(
                inputText = state.inputText,
                onInputChange = { text -> onIntent(ChatIntent.InputChanged(text)) },
                onSendMessage = { onIntent(ChatIntent.SendClicked) }
            )
        }
    }
}
```

- [ ] **Step 3: Add imports**

```kotlin
import com.mashiro.chat.ui.mvi.ChatEffect
import com.mashiro.chat.ui.mvi.ChatIntent
import com.mashiro.chat.ui.mvi.ChatUiState
```

- [ ] **Step 4: Run chat module tests and compile**

Run:

```bash
./gradlew :biz:chat:testDebugUnitTest
./gradlew :app:assembleDebug
```

Expected: both PASS.

## Task 7: Enforce No Upper-Layer Kernel/Command/CRUD Imports

**Files:**
- Test: shell verification command
- Modify if needed: `biz/chat/src/main/java/com/mashiro/chat/ui/ChatViewModel.kt`, `biz/chat/src/main/java/com/mashiro/chat/ui/ChatScreen.kt`, `biz/chat/src/test/java/com/mashiro/chat/ui/ChatViewModelTest.kt`

- [ ] **Step 1: Run import boundary check**

Run:

```bash
rg -n "com\\.mashiro\\.dataengine\\.chat\\.(kernel|command|crud|datalayer|mutator|query|backend)" biz/chat/src
```

Expected: exit code 1, no matches.

- [ ] **Step 2: Run allowed data-engine import check**

Run:

```bash
rg -n "com\\.mashiro\\.dataengine\\.chat\\." biz/chat/src
```

Expected output includes only imports from:

```text
com.mashiro.dataengine.chat.event
com.mashiro.dataengine.chat.facade
com.mashiro.dataengine.chat.model
com.mashiro.dataengine.chat.session
```

- [ ] **Step 3: If boundary check finds forbidden imports, replace them with facade calls**

Use this pattern:

```kotlin
// Forbidden
import com.mashiro.dataengine.chat.command.SendUserMessageCommand
import com.mashiro.dataengine.chat.crud.InMemoryChatCrudStore

// Allowed
import com.mashiro.dataengine.chat.facade.ChatEngineFacade
import com.mashiro.dataengine.chat.session.ChatSessionId
```

- [ ] **Step 4: Run all verification**

Run:

```bash
./gradlew :biz:data-engine:test
./gradlew :biz:chat:testDebugUnitTest
./gradlew :app:assembleDebug
git diff --check
```

Expected: all commands pass.

## Self-Review

- Spec coverage: Covers facade-only upper-layer API, internal Kernel registry, sessionId-based lookup, EventFacade subscription, DataFacade querying, Mutator/Query over Command over DataLayer, MVI state/intent/effect UI, and independent JVM tests for `:biz:data-engine`.
- Content scan: no unresolved markers or vague implementation notes remain.
- Type consistency: `ChatSessionId`, `ChatEvent`, `ChatEngineFacade`, `ChatEventFacade`, `ChatDataFacade`, `ChatUiState`, `ChatIntent`, and `ChatEffect` names are consistent across tasks.
- Scope check: This is one coherent refactor: data-engine public API boundary plus Android MVI consumer. It remains small enough for one implementation plan because persistence stays in memory and backend remains fake echo.
