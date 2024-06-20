package com.mashiro.uitest.pagehelper.store

import com.mashiro.uitest.pagehelper.BaseActivity
import com.mashiro.uitest.pagehelper.BaseFragment
import com.mashiro.uitest.pagehelper.api.IPageDisappearNotification
import com.mashiro.uitest.pagehelper.bean.PageDisappearReason
import com.mashiro.uitest.pagehelper.bean.PageTreeItem

data class PageTreeNode(
    val pageTreeItem: PageTreeItem
) {

    var hasNotify: Boolean = false

    var disappear: Boolean = false

    var parentNode: PageTreeNode? = null

    val children = mutableSetOf<PageTreeNode>()

    fun notifySubTree(notify: (String) -> Unit) {
        if (disappear && !hasNotify) {
            hasNotify = true
            notify.invoke(pageTreeItem.simpleName)
        }
        children.forEach { child -> child.notifySubTree(notify) }
    }

    fun removeDisappearNode() {
        children.removeAll { it.disappear }
        children.forEach { child ->
            child.removeDisappearNode()
        }
    }

    fun addChild(pageTreeNode: PageTreeNode) {
        children.add(pageTreeNode)
    }

    fun removeChild(pageTreeNode: PageTreeNode) {
        children.remove(pageTreeNode)
    }
}

