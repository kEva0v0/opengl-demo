package com.mashiro.dataengine.chat.mutator

import com.mashiro.dataengine.chat.command.SendUserMessageCommand
import com.mashiro.dataengine.chat.kernel.ChatKernel

internal class SendMessageMutator(
    private val kernel: ChatKernel
) {
    fun mutate(content: String) {
        val events = kernel.mutateIfActive { context ->
            SendUserMessageCommand(content).execute(context)
        } ?: return
        events.forEach { event ->
            kernel.eventBus.publish(event)
        }
        EchoBackendMutator(kernel).mutate(content)
    }
}
