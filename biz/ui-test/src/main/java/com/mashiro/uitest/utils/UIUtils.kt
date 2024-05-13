package com.mashiro.uitest.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Point
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

object UIUtils {
    /**
     * dp转px
     *
     * @param context
     * @param dp
     * @return
     */
    @JvmStatic
    fun dp2px(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    /**
     * dp转px,float
     *
     * @param context
     * @param dp
     * @return
     */
    @JvmStatic
    fun dp2pxFloat(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    /**
     * 获取 app 的屏幕高度，包含状态栏高度、不包含虚拟按键高度
     * 切调用的时候，需要保证 hasNavigationBar(context) 的时机在 Activity#onResume() 之后，否则无法正确判断是否存在 NavigationBar
     */
    @JvmStatic
    fun getAppScreenHeight(context: Context): Int {
        return getRealScreenHeight(context) - (if (hasNavigationBar(context)) getNavigationBarHeight(context) else 0)
    }

    @JvmStatic
    fun getAppScreenWidth(context: Context): Int {
        val resources = context.resources
        val dm = resources.displayMetrics
        return dm.widthPixels
    }

    /**
     * 获取真实手机屏幕的高度
     * ！！！！附才坑记录: resources.displayMetrics.heightPixels这个方法是拿不到某些手机上的准确高度的
     */
    @JvmStatic
    fun getRealScreenHeight(context: Context): Int {
        val point = Point()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(point)
        return point.y
    }

    /**
     *  该方法需要在View完全被绘制出来之后调用，否则判断不了
     *  在比如 onWindowFocusChanged（）方法中可以得到正确的结果
     */
    @JvmStatic
    fun hasNavigationBar(context: Context): Boolean {
        val activity: Activity = getActivityByContext(context) ?: return false
        val vp = activity.window.decorView as? ViewGroup
        if (vp != null) {
            for (i in 0 until vp.childCount) {
                if (vp.getChildAt(i).id != View.NO_ID && "navigationBarBackground" == activity.resources.getResourceEntryName(
                        vp.getChildAt(i).id
                    )
                    && vp.getChildAt(i).visibility == View.VISIBLE
                ) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun getActivityByContext(c: Context): Activity? {
        var context = c
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    @JvmStatic
    fun getNavigationBarHeight(context: Context): Int {
        var navigationBarHeight = 0
        val resources = context.applicationContext.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return navigationBarHeight
    }

}