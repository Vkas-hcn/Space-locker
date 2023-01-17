package com.vkas.spacelocker.appsl.slad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
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
import com.xuexiang.xutil.net.JsonUtil
import java.util.*
class SlLoadOpenAd {
    companion object {
        fun getInstance() = InstanceHelper.openLoadSl
    }

    object InstanceHelper {
        val openLoadSl = SlLoadOpenAd()
    }

    var appAdDataSl: Any? = null

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
        KLog.d(logTagSl, "open--isLoading=${isLoadingSl}")

        if (isLoadingSl) {
            KLog.d(logTagSl, "open--广告加载中，不能再次加载")
            return
        }

        if (appAdDataSl == null) {
            isLoadingSl = true
            loadStartupPageAdvertisementSl(context, getAdServerDataSl())
        }
        if (appAdDataSl != null && !whetherAdExceedsOneHour(loadTimeSl)) {
            isLoadingSl = true
            appAdDataSl = null
            loadStartupPageAdvertisementSl(context, getAdServerDataSl())
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
     * 加载启动页广告
     */
    private fun loadStartupPageAdvertisementSl(context: Context, adData: SlAdBean) {
        if (adData.sl_open.getOrNull(adIndexSl)?.sl_type == "screen") {
            loadStartInsertAdSl(context, adData)
        } else {
            loadOpenAdvertisementSl(context, adData)
        }
    }

    /**
     * 加载开屏广告
     */
    private fun loadOpenAdvertisementSl(context: Context, adData: SlAdBean) {
        KLog.e("loadOpenAdvertisementSl", "adData().sl_open=${JsonUtil.toJson(adData.sl_open)}")
        KLog.e(
            "loadOpenAdvertisementSl",
            "id=${JsonUtil.toJson(takeSortedAdIDSl(adIndexSl, adData.sl_open))}"
        )

        val id = takeSortedAdIDSl(adIndexSl, adData.sl_open)

        KLog.d(logTagSl, "open--开屏广告id=$id;权重=${adData.sl_open.getOrNull(adIndexSl)?.sl_weight}")
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            id,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    loadTimeSl = Date().time
                    isLoadingSl = false
                    appAdDataSl = ad

                    KLog.d(logTagSl, "open--开屏广告加载成功")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingSl = false
                    appAdDataSl = null
                    if (adIndexSl < adData.sl_open.size - 1) {
                        adIndexSl++
                        loadStartupPageAdvertisementSl(context, adData)
                    } else {
                        adIndexSl = 0
                    }
                    KLog.d(logTagSl, "open--开屏广告加载失败: " + loadAdError.message)
                }
            }
        )
    }


    /**
     * 开屏广告回调
     */
    private fun advertisingOpenCallbackSl() {
        if (appAdDataSl !is AppOpenAd) {
            return
        }
        (appAdDataSl as AppOpenAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                //取消全屏内容
                override fun onAdDismissedFullScreenContent() {
                    KLog.d(logTagSl, "open--关闭开屏内容")
                    whetherToShowSl = false
                    appAdDataSl = null
                    if (!App.whetherBackgroundSl) {
                        LiveEventBus.get<Boolean>(Constant.OPEN_CLOSE_JUMP)
                            .post(true)
                    }
                }

                //全屏内容无法显示时调用
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    whetherToShowSl = false
                    appAdDataSl = null
                    KLog.d(logTagSl, "open--全屏内容无法显示时调用")
                }

                //显示全屏内容时调用
                override fun onAdShowedFullScreenContent() {
                    appAdDataSl = null
                    whetherToShowSl = true
                    recordNumberOfAdDisplaysSl()
                    adIndexSl = 0
                    KLog.d(logTagSl, "open---开屏广告展示")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    KLog.d(logTagSl, "open---点击open广告")
                    recordNumberOfAdClickSl()
                }
            }
    }

    /**
     * 展示Open广告
     */
    fun displayOpenAdvertisementSl(activity: AppCompatActivity): Boolean {
        if (appAdDataSl == null) {
            KLog.d(logTagSl, "open---开屏广告加载中。。。")
            return false
        }
        if (whetherToShowSl || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagSl, "open---前一个开屏广告展示中或者生命周期不对")
            return false
        }
        if (appAdDataSl is AppOpenAd) {
            advertisingOpenCallbackSl()
            (appAdDataSl as AppOpenAd).show(activity)
        } else {
            startInsertScreenAdCallbackSl()
            (appAdDataSl as InterstitialAd).show(activity)
        }
        return true
    }

    /**
     * 加载启动页插屏广告
     */
    private fun loadStartInsertAdSl(context: Context, adData: SlAdBean) {
        val adRequest = AdRequest.Builder().build()
        val id = takeSortedAdIDSl(adIndexSl, adData.sl_open)
        KLog.d(
            logTagSl,
            "open--插屏广告id=$id;权重=${adData.sl_open.getOrNull(adIndexSl)?.sl_weight}"
        )

        InterstitialAd.load(
            context,
            id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let { KLog.d(logTagSl, "open---连接插屏加载失败=$it") }
                    isLoadingSl = false
                    appAdDataSl = null
                    if (adIndexSl < adData.sl_open.size - 1) {
                        adIndexSl++
                        loadStartupPageAdvertisementSl(context, adData)
                    } else {
                        adIndexSl = 0
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    loadTimeSl = Date().time
                    isLoadingSl = false
                    appAdDataSl = interstitialAd
                    KLog.d(logTagSl, "open--启动页插屏加载完成")
                }
            })
    }

    /**
     * StartInsert插屏广告回调
     */
    private fun startInsertScreenAdCallbackSl() {
        if (appAdDataSl !is InterstitialAd) {
            return
        }
        (appAdDataSl as InterstitialAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    KLog.d(logTagSl, "open--插屏广告点击")
                    recordNumberOfAdClickSl()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    KLog.d(logTagSl, "open--关闭StartInsert插屏广告${App.isBackDataSl}")
                    if (!App.whetherBackgroundSl) {
                        LiveEventBus.get<Boolean>(Constant.OPEN_CLOSE_JUMP)
                            .post(true)
                    }
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
                    adIndexSl = 0
                    KLog.d(logTagSl, "open----插屏show")
                }
            }
    }
}