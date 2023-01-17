package com.vkas.spacelocker.utils

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import com.vkas.spacelocker.bean.SlAppBean
import com.vkas.spacelocker.enevtsl.Constant
import kotlinx.coroutines.DelicateCoroutinesApi
import com.google.gson.reflect.TypeToken
import com.vkas.spacelocker.appsl.App.Companion.mmkvSl
import com.vkas.spacelocker.bean.SlAdBean
import com.vkas.spacelocker.bean.SlDetailBean
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.net.JsonUtil
import com.xuexiang.xutil.resource.ResourceUtils


object SpaceLockerUtils {
    //应用列表
    var appList:MutableList<SlAppBean> = ArrayList()
    //是否在右边（在App列表界面）
    var isOnRight:Int = 0
    /**
     * 获取应用列表
     */
    @DelicateCoroutinesApi
    fun getAppList(context: Context){
        GlobalScope.launch(Dispatchers.IO) {
            getAllLauncherIconPackages(context)
        }
    }
    // 需要获取所有apk 添加permission <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>
    private fun getAllLauncherIconPackages(context: Context) {
        val launcherIconPackageList: MutableList<SlAppBean> = ArrayList()
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        //set MATCH_ALL to prevent any filtering of the results
        val resolveInfos =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        for (info in resolveInfos) {
          val data =  context.packageManager.getApplicationInfo(info.activityInfo.packageName,0)
            if(info.activityInfo.packageName != context.packageName){
                val slAppBean = SlAppBean()
                slAppBean.appNameSl = context.packageManager.getApplicationLabel(data).toString()
                slAppBean.packageNameSl = info.activityInfo.packageName
                slAppBean.appIconSl = context.packageManager.getApplicationIcon(data)
                slAppBean.installTime = context.packageManager.getPackageInfo(info.activityInfo.packageName, 0).firstInstallTime
                launcherIconPackageList.add(slAppBean)
            }
        }
        appList = updateLockedContent(launcherIconPackageList)
            .asSequence()
            .distinctBy { it.packageNameSl }
            .sortedByDescending { it.installTime }
            .sortedBy { it.isLocked }
            .toMutableList()
        KLog.e("TAG","appList---1--${appList.size}")
    }

    /**
     * 更新已加锁内容
     */
    fun updateLockedContent(launcherIconPackageList:MutableList<SlAppBean>):MutableList<SlAppBean>{
        var lockApps: MutableList<String> = ArrayList()
        val data = mmkvSl.decodeString(Constant.STORE_LOCKED_APPLICATIONS, "")
        if (!Utils.isNullOrEmpty(data)) {
            lockApps = JsonUtil.fromJson(
                data,
                object : TypeToken<MutableList<String>?>() {}.type
            )
        }
        lockApps.forEach { lock ->
            launcherIconPackageList.forEach { all ->
                if (all.packageNameSl == lock) {
                    all.isLocked = true
                }
            }
        }
        return launcherIconPackageList
    }
    /**
     * 清除应用数据
     */
    fun clearApplicationData(){
        //清除密码
        MmkvUtils.set(Constant.LOCK_CODE_SL, "")
        MmkvUtils.set(Constant.STORE_LOCKED_APPLICATIONS, "")
        appList.forEach {
            it.isLocked = false
        }
    }

    /**
     * 广告排序
     */
    private fun adSortingSl(slAdBean: SlAdBean): SlAdBean {
        val adBean: SlAdBean = SlAdBean()
        val slOpen = slAdBean.sl_open.sortedWith(compareByDescending { it.sl_weight })
        val slBack = slAdBean.sl_back.sortedWith(compareByDescending { it.sl_weight })

        val slAppList = slAdBean.sl_app_list.sortedWith(compareByDescending { it.sl_weight })
        val slResult = slAdBean.sl_result.sortedWith(compareByDescending { it.sl_weight })
        val slLock = slAdBean.sl_lock.sortedWith(compareByDescending { it.sl_weight })

        adBean.sl_open = slOpen.toMutableList()
        adBean.sl_back = slBack.toMutableList()

        adBean.sl_app_list = slAppList.toMutableList()
        adBean.sl_result = slResult.toMutableList()
        adBean.sl_lock = slLock.toMutableList()

        adBean.sl_show_num = slAdBean.sl_show_num
        adBean.sl_click_num = slAdBean.sl_click_num
        return adBean
    }

    /**
     * 取出排序后的广告ID
     */
    fun takeSortedAdIDSl(index: Int, slAdDetails: MutableList<SlDetailBean>): String {
        return slAdDetails.getOrNull(index)?.sl_id ?: ""
    }

    /**
     * 获取广告服务器数据
     */
    fun getAdServerDataSl(): SlAdBean {
        val serviceData: SlAdBean =
            if (Utils.isNullOrEmpty(mmkvSl.decodeString(Constant.ADVERTISING_SL_DATA))) {
                JsonUtil.fromJson(
                    ResourceUtils.readStringFromAssert("slAdData.json"),
                    object : TypeToken<
                            SlAdBean?>() {}.type
                )
            } else {
                JsonUtil.fromJson(
                    mmkvSl.decodeString(Constant.ADVERTISING_SL_DATA),
                    object : TypeToken<SlAdBean?>() {}.type
                )
            }
        return adSortingSl(serviceData)
    }

    /**
     * 是否达到阀值
     */
    fun isThresholdReached(): Boolean {
        val clicksCount = mmkvSl.decodeInt(Constant.CLICKS_SL_COUNT, 0)
        val showCount = mmkvSl.decodeInt(Constant.SHOW_SL_COUNT, 0)
        KLog.e("TAG", "clicksCount=${clicksCount}, showCount=${showCount}")
        KLog.e(
            "TAG",
            "sl_click_num=${getAdServerDataSl().sl_click_num}, getAdServerData().sl_show_num=${getAdServerDataSl().sl_show_num}"
        )
        if (clicksCount >= getAdServerDataSl().sl_click_num || showCount >= getAdServerDataSl().sl_show_num) {
            return true
        }
        return false
    }

    /**
     * 记录广告展示次数
     */
    fun recordNumberOfAdDisplaysSl() {
        var showCount = mmkvSl.decodeInt(Constant.SHOW_SL_COUNT, 0)
        showCount++
        MmkvUtils.set(Constant.SHOW_SL_COUNT, showCount)
    }

    /**
     * 记录广告点击次数
     */
    fun recordNumberOfAdClickSl() {
        var clicksCount = mmkvSl.decodeInt(Constant.CLICKS_SL_COUNT, 0)
        clicksCount++
        MmkvUtils.set(Constant.CLICKS_SL_COUNT, clicksCount)
    }

}