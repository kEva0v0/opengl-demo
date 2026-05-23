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
