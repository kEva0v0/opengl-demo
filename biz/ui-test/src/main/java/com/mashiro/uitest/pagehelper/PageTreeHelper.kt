package com.mashiro.uitest.pagehelper

import androidx.fragment.app.Fragment
import com.mashiro.uitest.pagehelper.api.IPageDisappearNotification
import com.mashiro.uitest.pagehelper.bean.PageDisappearReason
import com.mashiro.uitest.pagehelper.bean.PageTreeItem
import com.mashiro.uitest.pagehelper.bean.PageTreeType
import com.mashiro.uitest.pagehelper.store.PageTreeNode
import com.mashiro.uitest.pagehelper.store.findNode
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

interface IPageTreeLifecycle{
    // 通知不可见视图
    fun onActivityStart(activity: BaseActivity) {

    }

    // 刷新ViewTree视图
    fun onActivityPause(activity: BaseActivity) {

    }

    // 处理退后台的情况
    fun onActivityStop(activity: BaseActivity) {

    }

    // 通知不可见视图
    fun onFragmentResume(fragment: BaseFragment) {

    }

    // 刷新ViewTree上面的视图
    fun onFragmentPause(fragment: BaseFragment) {

    }

    // 强制删除动作
    fun onActivityDestroy(activity: BaseActivity) {

    }

    fun onFragmentDestroy(fragment: BaseFragment)
}

interface IPageTreeObserver {
    fun addListener(name: String, notification: IPageDisappearNotification)

    fun removeListener(name: String, notification: IPageDisappearNotification)
}

object PageTreeHelper: IPageTreeLifecycle {

    private val listenerMap = ConcurrentHashMap<String, WeakReference<IPageDisappearNotification>>()

    private var viewTree = PageTreeNode(
        pageTreeItem = PageTreeItem(
            PageTreeType.ROOT,
            "viewTreeRoot"
        )
    )

    override fun onActivityStart(activity: BaseActivity) {
        appendActivityNode(activity)
        notifyAndRemove(PageDisappearReason.JUMP_TO_ACTIVITY)
    }

    override fun onActivityPause(activity: BaseActivity) {
        val targetItem = PageTreeItem(pageTreeType = PageTreeType.ACTIVITY, simpleName = activity.javaClass.simpleName)
        viewTree.findNode{ it.pageTreeItem == targetItem }?.let {
            it.disappear = true
        }
    }

    override fun onActivityStop(activity: BaseActivity) {
        // 这里可能有进后台的
        notifyAndRemove(PageDisappearReason.JUMP_TO_BACKGROUND)
    }

    override fun onFragmentPause(fragment: BaseFragment) {
        val targetItem = PageTreeItem(pageTreeType = PageTreeType.FRAGMENT, simpleName = fragment.javaClass.simpleName)
        viewTree.findNode { it.pageTreeItem == targetItem }?.let {
            it.disappear = true
        }
    }

    override fun onFragmentResume(fragment: BaseFragment) {
        appendFragmentNode(fragment)
        notifyAndRemove(PageDisappearReason.JUMP_TO_FRAGMENT)
    }

    override fun onActivityDestroy(activity: BaseActivity) {
        removeNode(activity)
    }

    override fun onFragmentDestroy(fragment: BaseFragment) {
        removeNode(fragment)
    }

    private fun notifyAndRemove(pageDisappearReason: PageDisappearReason) {
        viewTree.notifySubTree{
            listenerMap[it]?.get()?.notifyDisappearReason(pageDisappearReason)
        }
        viewTree.removeDisappearNode()
    }

    private fun appendActivityNode(activity: BaseActivity): PageTreeNode {
        val pageTreeItem = PageTreeItem(
            pageTreeType = PageTreeType.ACTIVITY,
            simpleName = activity.javaClass.simpleName
        )
        viewTree.findNode { it.pageTreeItem == pageTreeItem }?.let {
            return it
        }
        val pageNode = PageTreeNode(pageTreeItem)
        pageNode.parentNode = viewTree
        viewTree.addChild(PageTreeNode(pageTreeItem))
        listenerMap[activity.javaClass.simpleName] = WeakReference(activity as IPageDisappearNotification)
        return pageNode
    }

    private fun appendFragmentNode(fragment: BaseFragment): PageTreeNode {
        val curItem = PageTreeItem(pageTreeType = PageTreeType.FRAGMENT, simpleName = fragment.javaClass.simpleName)
        viewTree.findNode { it.pageTreeItem == curItem }?.let {
            return it
        }
        val parentNode: PageTreeNode = if (fragment.parentFragment == null) {
            appendActivityNode(fragment.activity as BaseActivity)
        } else {
            appendFragmentNode(fragment.parentFragment as BaseFragment)
        }
        val pageTreeNode = PageTreeNode(PageTreeItem(pageTreeType = PageTreeType.FRAGMENT, simpleName = fragment.javaClass.simpleName))
        pageTreeNode.parentNode = parentNode
        parentNode.addChild(pageTreeNode)
        listenerMap[fragment.javaClass.simpleName] = WeakReference(fragment as IPageDisappearNotification)
        return pageTreeNode
    }

    private fun removeNode(activity: BaseActivity) {
        viewTree.findNode { it.pageTreeItem == PageTreeItem(pageTreeType = PageTreeType.ACTIVITY, simpleName = activity.javaClass.simpleName) }?.let {
            it.parentNode?.removeChild(it)
            it.parentNode = null
        }
        listenerMap.remove(activity.javaClass.simpleName)
    }

    private fun removeNode(fragment: BaseFragment) {
        viewTree.findNode { it.pageTreeItem == PageTreeItem(pageTreeType = PageTreeType.FRAGMENT, simpleName = fragment.javaClass.simpleName) }?.let {
            it.parentNode?.removeChild(it)
            it.parentNode = null
        }
        listenerMap.remove(fragment.javaClass.simpleName)
    }
}