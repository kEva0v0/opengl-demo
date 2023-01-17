package com.mashiro.renderengine.node

enum class NodeStatus {
    // 正在创建
    CREATING,
    // 未做操作
    PENDING,
    // 即将渲染
    DIRTY,
    // 渲染中
    RENDERING,
    // 销毁
    DESTROY
}