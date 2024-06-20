package com.mashiro.uitest.pagehelper.bean

enum class PageTreeType{
    ROOT,
    ACTIVITY,
    FRAGMENT
}

data class PageTreeItem(
    val pageTreeType: PageTreeType,
    // 需要保证这不是混淆的！！
    val simpleName: String
)
