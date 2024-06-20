package com.mashiro.uitest.pagehelper.store


fun PageTreeNode.findNode(block: (PageTreeNode) -> Boolean): PageTreeNode? {
    if (block(this)) {
        return this
    }
    for (child in children) {
        val foundNode = child.findNode(block)
        if (foundNode != null) {
            return foundNode
        }
    }
    return null
}
