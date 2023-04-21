package com.four.yy.vpn.manager

import android.app.Activity
import com.four.yy.vpn.MainActivity

class ActivityManager {
    /**
     * 保存所有创建的Activity
     */
    val activityList: MutableList<Activity> = ArrayList()


    companion object {
        private var instance: ActivityManager? = null
            get() {
                if (field == null) {
                    field = ActivityManager()
                }
                return field
            }

        fun get(): ActivityManager {
            return instance!!
        }
    }

    /**
     * 添加Activity
     * @param activity
     */
    fun addActivity(activity: Activity?) {
        if (activity != null) {
            activityList.add(activity)
        }
    }

    /**
     * 移除Activity
     * @param activity
     */
    fun removeActivity(activity: Activity?) {
        if (activity != null) {
            activityList.remove(activity)
        }
    }


    /**
     * 关闭所有Activity
     */
    fun finishAllActivity() {
        for (activity in activityList) {
            activity.finish()
        }
    }
}