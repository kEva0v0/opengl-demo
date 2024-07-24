package com.mashiro.uitest.pagehelper.bean

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

enum class PageTreeType{
    ROOT,
    ACTIVITY,
    FRAGMENT
}

data class PageTreeItem(
    val pageTreeType: PageTreeType,
    // 需要保证这不是混淆的！！
    val simpleName: String
) {
    companion object {
        fun createActivity(activity: AppCompatActivity): PageTreeItem {
            return PageTreeItem(pageTreeType = PageTreeType.ACTIVITY, simpleName = activity.javaClass.simpleName + "-" + activity.hashCode())
        }

        fun createFragment(fragment: Fragment): PageTreeItem {
            return PageTreeItem(pageTreeType = PageTreeType.FRAGMENT, simpleName = fragment.javaClass.simpleName + "-" + fragment.hashCode())
        }
    }
}
