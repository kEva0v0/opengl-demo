package com.mashiro.uitest.pagehelper

import androidx.appcompat.app.AppCompatActivity
import com.mashiro.uitest.pagehelper.api.IPageDisappearNotification
import com.mashiro.uitest.pagehelper.bean.PageDisappearReason

abstract class BaseActivity: AppCompatActivity(), IPageDisappearNotification{

    private var notified = false

    override fun onStart() {
        super.onStart()
        notified = false
    }

    override fun notifyDisappearReason(pageDisappearReason: PageDisappearReason, pageName: String) {
        notified = true
    }
}