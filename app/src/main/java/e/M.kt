package e

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.LogUtils
import sl.wo.ip.R
import b.B
import b.B.Companion.mmkvSl
import sl.wo.ip.bean.SlAppBean
import sl.wo.ip.enevtsl.Constant
import sl.wo.ip.utils.MmkvUtils
import sl.wo.ip.utils.SpaceLockerUtils
import sl.wo.ip.utils.SpaceLockerUtils.clearApplicationData
import sl.wo.ip.utils.SpaceLockerUtils.updateLockedContent
import a.J
import a.K
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.net.JsonUtil

object M:ViewModel() {
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
        LogUtils.e("TAG", "JsonUtil.toJson(lockApps)===${JsonUtil.toJson(lockApps)}")
    }

    /**
     * 是否弹出设置密码框
     */
    fun whetherPopUpPasswordSettingBox(activity: Activity,appListAdapter: N) {
        if (B.isFrameDisplayed) {
            return
        }
        val data = mmkvSl.decodeString(Constant.LOCK_CODE_SL, "")
        if (Utils.isNullOrEmpty(data)) {
            K(activity, true).show()
        }else{
            K(activity, false)
                .setForgetButton(object : K.OnForgetClickListener {
                    override fun doForget() {
                        showClearPasswordPopUp(activity, appListAdapter,-1)
                    }
                })
                .show()
        }
    }

    /**
     * 展示清除密码弹框
     */
    fun showClearPasswordPopUp(activity: Activity, appListAdapter: N,pos: Int) {
        LogUtils.e("TAG", "展示清除密码弹框-----1")
        J(activity)
            .setMessage(activity.getString(R.string.are_you_sure_to_reset_them))
            ?.setCancelButton(object : J.OnCancelClickListener {
                override fun doCancel() {
                    unlockJump(activity, appListAdapter,pos)
                }
            })
            ?.setConfirmButton(object : J.OnConfirmClickListener {
                override fun doConfirm() {
                    clearApplicationData()
                    appListAdapter.notifyDataSetChanged()
                    if (B.isFrameDisplayed) {
                        return
                    }
                    K(activity, true).show()
                }
            })
            ?.show()
        B.forgotPassword = Constant.SKIP_TO_NORMAL_PASSWORD
    }

    /**
     * 展示设置密码弹框
     */
    fun showSettingPasswordPopUp(activity: Activity) {
        if (B.isFrameDisplayed) {
            return
        }
        K(activity, true).show()
    }

    /**
     * 无密码设置密码
     */
    fun noPasswordSetPassword(activity: Activity) {
        LogUtils.e("TAG", "展示首页密码弹框----1")
        if (B.isFrameDisplayed) {
            return
        }
        LogUtils.e("TAG", "展示首页密码弹框----2")
        if (Utils.isNullOrEmpty(mmkvSl.getString(Constant.LOCK_CODE_SL, ""))) {
            B.isFrameDisplayed = true
            LogUtils.e("TAG"," B.isFrameDisplayed==${ B.isFrameDisplayed}")
            K(activity, true).show()
        }
    }

    /**
     * 点击弹出密码弹框
     */
    fun clickToPopPasswordBox(activity: Activity, appListAdapter: N, pos: Int) {
        if (B.isFrameDisplayed) {
            return
        }
        unlockJump(activity, appListAdapter, pos)
    }


    /**
     * 解锁跳转
     */
    private fun unlockJump(activity: Activity, appListAdapter: N, pos: Int) {
        if (B.isFrameDisplayed) {
            return
        }
        K(activity, false)
            .setForgetButton(object : K.OnForgetClickListener {
                override fun doForget() {
                    showClearPasswordPopUp(activity, appListAdapter,pos)
                }
            })
            .setFinishButton(object : K.OnFinishClickListener {
                override fun doFinish() {
                    if(pos!=-1){
                        liveLock.postValue(pos)
                    }
                }
            })
            .show()
    }
}