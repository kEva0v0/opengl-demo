package com.mashiro.uitest.pagehelper

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mashiro.uitest.pagehelper.api.IPageDisappearNotification
import com.mashiro.uitest.pagehelper.bean.PageDisappearReason
import com.mashiro.uitest.pagehelper.bean.PageTreeItem
import com.mashiro.uitest.pagehelper.bean.PageTreeType
import com.mashiro.uitest.pagehelper.store.PageTreeNode
import com.mashiro.uitest.pagehelper.store.findNode
import java.lang.ref.WeakReference

interface IPageTreeLifecycle{
    // 通知不可见视图
    fun onActivityStart(activity: AppCompatActivity)

    // 刷新ViewTree视图
    fun onActivityPause(activity: AppCompatActivity)

    // 处理退后台的情况
    fun onActivityStop(activity: AppCompatActivity)

    // 通知不可见视图
    fun onFragmentResume(fragment: Fragment)
    // 刷新ViewTree上面的视图
    fun onFragmentPause(fragment: Fragment)

    // 强制删除动作
    fun onActivityDestroy(activity: AppCompatActivity)

    fun onFragmentDestroy(fragment: Fragment)
}

object PageTreeHelper: IPageTreeLifecycle {

    private val listenerMap = mutableMapOf<String, WeakReference<IPageDisappearNotification>>()

    private var viewTree = PageTreeNode(
        pageTreeItem = PageTreeItem(
            PageTreeType.ROOT,
            "viewTreeRoot"
        )
    )

    override fun onActivityStart(activity: AppCompatActivity) {
        appendActivityNode(activity)
        notifyAndRemove(PageDisappearReason.JUMP_TO_ACTIVITY, activity::class.java.simpleName)
    }

    override fun onActivityPause(activity: AppCompatActivity) {
        val targetItem = PageTreeItem.createActivity(activity)
        viewTree.findNode{ it.pageTreeItem == targetItem }?.let {
            it.disappear = true
        }
    }

    override fun onActivityStop(activity: AppCompatActivity) {
        // 这里可能有进后台的
        notifyAndRemove(PageDisappearReason.JUMP_TO_BACKGROUND, activity::class.java.simpleName)
    }

    override fun onFragmentPause(fragment: Fragment) {
        viewTree.findNode { it.pageTreeItem == PageTreeItem.createFragment(fragment) }?.let {
            it.disappear = true
        }
    }

    override fun onFragmentResume(fragment: Fragment) {
        appendFragmentNode(fragment)
        // 这里会用第一个resume的fragment通知出去
        notifyAndRemove(PageDisappearReason.JUMP_TO_FRAGMENT, fragment::class.java.simpleName)
    }

    override fun onActivityDestroy(activity: AppCompatActivity) {
        notifyAndRemove(PageDisappearReason.QUIT, activity::class.java.simpleName)
        removeNode(activity)
    }

    override fun onFragmentDestroy(fragment: Fragment) {
        notifyAndRemove(PageDisappearReason.QUIT, fragment::class.java.simpleName)
        removeNode(fragment)
    }

    private fun notifyAndRemove(pageDisappearReason: PageDisappearReason, pageName: String) {
        viewTree.notifySubTree{
            listenerMap[it]?.get()?.notifyDisappearReason(pageDisappearReason, pageName)
        }
        viewTree.removeDisappearNode()
    }

    private fun appendActivityNode(activity: AppCompatActivity): PageTreeNode {
        val pageTreeItem = PageTreeItem.createActivity(activity)
        viewTree.findNode { it.pageTreeItem == pageTreeItem }?.let {
            return it
        }
        val pageNode = PageTreeNode(pageTreeItem)
        pageNode.parentNode = viewTree
        viewTree.addChild(PageTreeNode(pageTreeItem))
        if (activity is IPageDisappearNotification) {
            listenerMap[pageTreeItem.simpleName] = WeakReference(activity as IPageDisappearNotification)
        }
        return pageNode
    }

    private fun appendFragmentNode(fragment: Fragment): PageTreeNode {
        val curItem = PageTreeItem.createFragment(fragment)
        viewTree.findNode { it.pageTreeItem == curItem }?.let {
            return it
        }
        val parentNode: PageTreeNode = if (fragment.parentFragment == null) {
            appendActivityNode(fragment.activity as AppCompatActivity)
        } else {
            appendFragmentNode(fragment.parentFragment as Fragment)
        }
        val pageTreeNode = PageTreeNode(curItem)
        pageTreeNode.parentNode = parentNode
        parentNode.addChild(pageTreeNode)
        if (fragment is IPageDisappearNotification) {
            listenerMap[curItem.simpleName] = WeakReference(fragment as IPageDisappearNotification)
        }
        return pageTreeNode
    }

    private fun removeNode(activity: AppCompatActivity) {
        val curItem = PageTreeItem.createActivity(activity)
        viewTree.findNode { it.pageTreeItem == curItem }?.let {
            it.parentNode?.removeChild(it)
            it.parentNode = null
        }
        listenerMap.remove(curItem.simpleName)
    }

    private fun removeNode(fragment: Fragment) {
        val curItem = PageTreeItem.createFragment(fragment)
        viewTree.findNode { it.pageTreeItem == curItem }?.let {
            it.parentNode?.removeChild(it)
            it.parentNode = null
        }
        listenerMap.remove(curItem.simpleName)
    }
}