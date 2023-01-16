package com.vkas.spacelocker.uisl.main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import com.vkas.spacelocker.R
import com.vkas.spacelocker.appsl.App
import com.vkas.spacelocker.appsl.App.Companion.mmkvSl
import com.vkas.spacelocker.basesl.BaseViewModel
import com.vkas.spacelocker.bean.CommLockInfo
import com.vkas.spacelocker.bean.SlAppBean
import com.vkas.spacelocker.enevtsl.Constant
import com.vkas.spacelocker.lock.CommLockInfoManager
import com.vkas.spacelocker.utils.KLog
import com.vkas.spacelocker.utils.LockWindow
import com.vkas.spacelocker.utils.MmkvUtils
import com.vkas.spacelocker.utils.SpaceLockerUtils
import com.vkas.spacelocker.utils.SpaceLockerUtils.clearApplicationData
import com.vkas.spacelocker.utils.SpaceLockerUtils.updateLockedContent
import com.vkas.spacelocker.widget.LockerDialog
import com.vkas.spacelocker.widget.PasswordDialog
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.net.JsonUtil

class MainViewModel(application: Application) : BaseViewModel(application) {
    //初始化服务器数据
    val liveLock: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    /**
     * 存储已加锁的应用
     */
    fun storeLockedApplications(appList: MutableList<SlAppBean>) {
        val lockApps: MutableList<String> = ArrayList()
        appList.forEach {
            if (it.isLocked) {
                it.packageNameSl?.let { it1 -> lockApps.add(it1) }
            }
        }
        MmkvUtils.set(Constant.STORE_LOCKED_APPLICATIONS, JsonUtil.toJson(lockApps))
        SpaceLockerUtils.appList = updateLockedContent(appList)
        KLog.e("TAG", "JsonUtil.toJson(lockApps)===${JsonUtil.toJson(lockApps)}")
    }

    /**
     * 是否弹出设置密码框
     */
    fun whetherPopUpPasswordSettingBox(activity: Activity) {
        if (App.isFrameDisplayed) {
            return
        }
        val data = mmkvSl.decodeString(Constant.LOCK_CODE_SL, "")
        if (Utils.isNullOrEmpty(data)) {
            PasswordDialog(activity, true).show()
        }
    }

    /**
     * 展示清除密码弹框
     */
    fun showClearPasswordPopUp(activity: Activity, appListAdapter: AppListAdapter,pos: Int) {
        KLog.e("TAG", "展示清除密码弹框-----1")
        LockerDialog(activity)
            .setMessage(activity.getString(R.string.are_you_sure_to_reset_them))
            ?.setCancelButton(object : LockerDialog.OnCancelClickListener {
                override fun doCancel() {
                    unlockJump(activity, appListAdapter,pos)
                }
            })
            ?.setConfirmButton(object : LockerDialog.OnConfirmClickListener {
                override fun doConfirm() {
                    clearApplicationData()
                    appListAdapter.notifyDataSetChanged()
                    if (App.isFrameDisplayed) {
                        return
                    }
                    PasswordDialog(activity, true).show()
                }
            })
            ?.show()
        App.forgotPassword = Constant.SKIP_TO_NORMAL_PASSWORD
    }

    /**
     * 展示设置密码弹框
     */
    fun showSettingPasswordPopUp(activity: Activity) {
        if (App.isFrameDisplayed) {
            return
        }
        clearApplicationData()
        PasswordDialog(activity, true).show()
    }

    /**
     * 无密码设置密码
     */
    fun noPasswordSetPassword(activity: Activity) {
        KLog.e("TAG", "展示首页密码弹框----1")

        if (App.isFrameDisplayed) {
            return
        }
        KLog.e("TAG", "展示首页密码弹框----2")

        if (Utils.isNullOrEmpty(mmkvSl.getString(Constant.LOCK_CODE_SL, ""))) {
            PasswordDialog(activity, true).show()
        }
    }

    /**
     * 点击弹出密码弹框
     */
    fun clickToPopPasswordBox(activity: Activity, appListAdapter: AppListAdapter, pos: Int) {
        if (App.isFrameDisplayed) {
            return
        }
        unlockJump(activity, appListAdapter, pos)
    }


    /**
     * 解锁跳转
     */
    private fun unlockJump(activity: Activity, appListAdapter: AppListAdapter, pos: Int) {
        if (App.isFrameDisplayed) {
            return
        }
        PasswordDialog(activity, false)
            .setForgetButton(object : PasswordDialog.OnForgetClickListener {
                override fun doForget() {
                    showClearPasswordPopUp(activity, appListAdapter,pos)
                }
            })
            .setFinishButton(object : PasswordDialog.OnFinishClickListener {
                override fun doFinish() {
                    if(pos!=-1){
                        liveLock.postValue(pos)
                    }
                }
            })
            .show()
    }
}