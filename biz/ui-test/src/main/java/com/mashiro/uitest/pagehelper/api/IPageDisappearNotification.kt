package com.mashiro.uitest.pagehelper.api

import com.mashiro.uitest.pagehelper.bean.PageDisappearReason

interface IPageDisappearNotification {

    fun notifyDisappearReason(pageDisappearReason: PageDisappearReason, pageName: String)

}