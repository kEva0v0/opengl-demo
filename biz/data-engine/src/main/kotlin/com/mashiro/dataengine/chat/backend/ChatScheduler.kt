package com.mashiro.dataengine.chat.backend

internal interface ChatScheduler {
    fun postDelayed(delayMillis: Long, action: () -> Unit)
}

internal class ImmediateChatScheduler : ChatScheduler {
    override fun postDelayed(delayMillis: Long, action: () -> Unit) {
        action()
    }
}

internal class ThreadChatScheduler : ChatScheduler {
    override fun postDelayed(delayMillis: Long, action: () -> Unit) {
        Thread {
            Thread.sleep(delayMillis)
            action()
        }.start()
    }
}
