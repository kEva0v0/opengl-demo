package com.mashiro.uitest.pagehelper.bean

enum class PageDisappearReason {
    // 去别的Activity
    JUMP_TO_ACTIVITY,
    // 去别的Fragment
    JUMP_TO_FRAGMENT,
    // 去后台
    JUMP_TO_BACKGROUND,
    // 被退出
    QUIT,
    // 被自己通知，比如通知弹窗
    CUR_FRAGMENT
}