package com.mashiro.dataengine.chat.backend

internal class FakeChatBackend(
    private val scheduler: ChatScheduler = ThreadChatScheduler(),
    private val replyDelayMillis: Long = 500L
) : ChatBackend {
    override fun echo(content: String, onReply: (String) -> Unit) {
        scheduler.postDelayed(replyDelayMillis) {
            onReply(content)
        }
    }
}
