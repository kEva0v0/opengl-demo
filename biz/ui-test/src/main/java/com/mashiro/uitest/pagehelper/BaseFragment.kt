package com.mashiro.uitest.pagehelper

import androidx.fragment.app.Fragment
import com.mashiro.uitest.pagehelper.api.IPageDisappearNotification
import com.mashiro.uitest.pagehelper.bean.PageDisappearReason

abstract class BaseFragment: Fragment(), IPageDisappearNotification {

    private var notified = false

    override fun onResume() {
        super.onResume()
        notified = false
    }

    override fun notifyDisappearReason(pageDisappearReason: PageDisappearReason, pageName: String) {
        notified = true
    }
}