package com.vkas.spacelocker.appsl.slad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.spacelocker.appsl.App
import com.vkas.spacelocker.bean.SlAdBean
import com.vkas.spacelocker.enevtsl.Constant
import com.vkas.spacelocker.enevtsl.Constant.logTagSl
import com.vkas.spacelocker.utils.KLog
import com.vkas.spacelocker.utils.SpaceLockerUtils
import com.vkas.spacelocker.utils.SpaceLockerUtils.getAdServerDataSl
import com.vkas.spacelocker.utils.SpaceLockerUtils.recordNumberOfAdClickSl
import com.vkas.spacelocker.utils.SpaceLockerUtils.recordNumberOfAdDisplaysSl
import com.vkas.spacelocker.utils.SpaceLockerUtils.takeSortedAdIDSl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
class SlLoadLockAd {
    companion object {
        fun getInstance() = InstanceHelper.openLoadSl
    }

    object InstanceHelper {
        val openLoadSl = SlLoadLockAd()
    }

    var appAdDataSl: InterstitialAd? = null

    // 是否正在加载中
    var isLoadingSl = false

    //加载时间
    private var loadTimeSl: Long = Date().time

    // 是否展示
    var whetherToShowSl = false

    // openIndex
    var adIndexSl = 0

    /**
     * 广告加载前判断
     */
    fun advertisementLoadingSl(context: Context) {
        App.isAppOpenSameDaySl()
        if (SpaceLockerUtils.isThresholdReached()) {
            KLog.d(logTagSl, "广告达到上线")
            return
        }
        KLog.d(logTagSl, "connect--isLoading=${isLoadingSl}")

        if (isLoadingSl) {
            KLog.d(logTagSl, "connect--广告加载中，不能再次加载")
            return
        }

        if (appAdDataSl == null) {
            isLoadingSl = true
            loadConnectAdvertisementSl(context, getAdServerDataSl())
        }
        if (appAdDataSl != null && !whetherAdExceedsOneHour(loadTimeSl)) {
            isLoadingSl = true
            appAdDataSl = null
            loadConnectAdvertisementSl(context, getAdServerDataSl())
        }
    }

    /**
     * 广告是否超过过期（false:过期；true：未过期）
     */
    private fun whetherAdExceedsOneHour(loadTime: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour
    }


    /**
     * 加载首页插屏广告
     */
    private fun loadConnectAdvertisementSl(context: Context, adData: SlAdBean) {
        val adRequest = AdRequest.Builder().build()
        val id = takeSortedAdIDSl(adIndexSl, adData.sl_lock)
        KLog.d(
            logTagSl,
            "connect--插屏广告id=$id;权重=${adData.sl_lock.getOrNull(adIndexSl)?.sl_weight}"
        )

        InterstitialAd.load(
            context,
            id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let { KLog.d(logTagSl, "connect---连接插屏加载失败=$it") }
                    isLoadingSl = false
                    appAdDataSl = null
                    if (adIndexSl < adData.sl_lock.size - 1) {
                        adIndexSl++
                        loadConnectAdvertisementSl(context, adData)
                    } else {
                        adIndexSl = 0
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loadTimeSl = Date().time
                    isLoadingSl = false
                    appAdDataSl = interstitialAd
                    adIndexSl = 0
                    KLog.d(logTagSl, "connect---连接插屏加载成功")
                }
            })
    }

    /**
     * connect插屏广告回调
     */
    private fun connectScreenAdCallback() {
        appAdDataSl?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    KLog.d(logTagSl, "connect插屏广告点击")
                    recordNumberOfAdClickSl()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    KLog.d(logTagSl, "关闭connect插屏广告=${App.isBackDataSl}")
                    LiveEventBus.get<Boolean>(Constant.PLUG_SL_ADVERTISEMENT_SHOW)
                        .post(App.isBackDataSl)

                    appAdDataSl = null
                    whetherToShowSl = false
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    KLog.d(logTagSl, "Ad failed to show fullscreen content.")
                    appAdDataSl = null
                    whetherToShowSl = false
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    KLog.e("TAG", "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    appAdDataSl = null
                    recordNumberOfAdDisplaysSl()
                    // Called when ad is shown.
                    whetherToShowSl = true
                    KLog.d(logTagSl, "connect----show")
                }
            }
    }

    /**
     * 展示Connect广告
     */
    fun displayConnectAdvertisementSl(activity: AppCompatActivity): Boolean {
        if (appAdDataSl == null) {
            KLog.d(logTagSl, "connect--插屏广告加载中。。。")
            return false
        }
        if (whetherToShowSl || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagSl, "connect--前一个插屏广告展示中或者生命周期不对")
            return false
        }
        connectScreenAdCallback()
        activity.lifecycleScope.launch(Dispatchers.Main) {
            (appAdDataSl as InterstitialAd).show(activity)
        }
        return true
    }
}