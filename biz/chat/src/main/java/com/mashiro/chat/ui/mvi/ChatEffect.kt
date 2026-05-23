package com.mashiro.chat.ui.mvi

sealed class ChatEffect {
    object None : ChatEffect()
    object ScrollToLatest : ChatEffect()
}
