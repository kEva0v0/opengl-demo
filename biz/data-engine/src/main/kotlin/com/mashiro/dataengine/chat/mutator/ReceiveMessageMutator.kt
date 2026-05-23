package com.mashiro.dataengine.chat.mutator

import com.mashiro.dataengine.chat.command.ReceiveBackendMessageCommand
import com.mashiro.dataengine.chat.kernel.ChatKernel

internal class ReceiveMessageMutator(
    private val kernel: ChatKernel
) {
    fun mutate(content: String) {
        val events = kernel.mutateIfActive { context ->
            ReceiveBackendMessageCommand(content).execute(context)
        } ?: return
        events.forEach { event ->
            kernel.eventBus.publish(event)
        }
    }
}
