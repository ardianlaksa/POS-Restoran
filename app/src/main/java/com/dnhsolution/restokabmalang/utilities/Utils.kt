package com.dnhsolution.restokabmalang.utilities

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import com.dnhsolution.restokabmalang.R

object Utils {
    private var sTheme = 0
    const val THEME_FIRST = 0
    const val THEME_SECOND = 1
    const val THEME_THIRD = 2
    const val THEME_FOURTH = 3
    const val THEME_FIFTH = 4
    const val THEME_SIXTH = 5
    var mLastClickTime=0L

    @JvmStatic
    fun changeToTheme(activity: Activity, theme: Int) {
        sTheme = theme
        activity.finish()
        activity.startActivity(Intent(activity, activity.javaClass))
        activity.overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    }

    @JvmStatic
    fun onActivityCreateSetTheme(activity: Activity) {
        when (sTheme) {
            THEME_FIRST -> activity.setTheme(R.style.Theme_First)
            THEME_SECOND -> activity.setTheme(R.style.Theme_Second)
            THEME_THIRD -> activity.setTheme(R.style.Theme_Third)
            THEME_FOURTH -> activity.setTheme(R.style.Theme_Fourth)
            THEME_FIFTH -> activity.setTheme(R.style.Theme_Fifth)
            THEME_SIXTH -> activity.setTheme(R.style.Theme_Sixth)
            else -> activity.setTheme(R.style.Theme_First)
        }
    }

    @JvmStatic
    fun isOpenRecently():Boolean{
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return true
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return false
    }
}