package com.vkas.spacelocker.basesl

import android.app.Activity
import java.util.*

object ActivityStackManager {
    private val activityStack: Stack<Activity> = Stack()

    /**
     * 添加 Activity 到任务栈
     */
    fun addActivity(activity: Activity) {
        activityStack.add(activity)
    }

    /**
     * 从任务栈移除指定的 Activity
     */
    fun removeActivity(activity: Activity) {
        activityStack.remove(activity)
    }

    /**
     * 结束指定的 Activity
     */
    fun finishActivity(activity: Activity) {
        activity.finish()
        removeActivity(activity)
    }

    /**
     * 结束所有的 Activity
     */
    fun finishAllActivities() {
        for (activity in activityStack) {
            activity.finish()
        }
        activityStack.clear()
    }
}