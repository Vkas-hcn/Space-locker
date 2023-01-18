package com.vkas.spacelocker.appsl.slad


import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.vkas.spacelocker.R
import com.vkas.spacelocker.appsl.App
import com.vkas.spacelocker.bean.SlAdBean
import com.vkas.spacelocker.databinding.ActivityMainBinding
import com.vkas.spacelocker.enevtsl.Constant.logTagSl
import com.vkas.spacelocker.utils.KLog
import com.vkas.spacelocker.utils.RoundCornerOutlineProvider
import com.vkas.spacelocker.utils.SpaceLockerUtils
import com.vkas.spacelocker.utils.SpaceLockerUtils.getAdServerDataSl
import com.vkas.spacelocker.utils.SpaceLockerUtils.recordNumberOfAdClickSl
import com.vkas.spacelocker.utils.SpaceLockerUtils.recordNumberOfAdDisplaysSl
import com.vkas.spacelocker.utils.SpaceLockerUtils.takeSortedAdIDSl
import java.util.*

class SlLoadAppListAd {
    companion object {
        fun getInstance() = InstanceHelper.openLoadSl
    }

    object InstanceHelper {
        val openLoadSl = SlLoadAppListAd()
    }

    var appAdDataSl: NativeAd? = null

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
        KLog.d(logTagSl, "vpn--isLoading=${isLoadingSl}")
        if (isLoadingSl) {
            KLog.d(logTagSl, "vpn--广告加载中，不能再次加载")
            return
        }
        if (appAdDataSl == null) {
            isLoadingSl = true
            loadHomeAdvertisementSl(context, getAdServerDataSl())
        }
        if (appAdDataSl != null && !whetherAdExceedsOneHour(loadTimeSl)) {
            isLoadingSl = true
            appAdDataSl = null
            loadHomeAdvertisementSl(context, getAdServerDataSl())
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
     * 加载vpn原生广告
     */
    private fun loadHomeAdvertisementSl(context: Context, adData: SlAdBean) {
        val id = takeSortedAdIDSl(adIndexSl, adData.sl_app_list)
        KLog.d(logTagSl, "vpn---原生广告id=$id;权重=${adData.sl_app_list.getOrNull(adIndexSl)?.sl_weight}")

        val vpnNativeAds = AdLoader.Builder(
            context.applicationContext,
            id
        )
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
            .build()

        vpnNativeAds.withNativeAdOptions(adOptions)
        vpnNativeAds.forNativeAd {
            appAdDataSl = it
        }
        vpnNativeAds.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                val error =
                    """
           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """"
                isLoadingSl = false
                appAdDataSl = null
                KLog.d(logTagSl, "vpn---加载vpn原生加载失败: $error")

                if (adIndexSl < adData.sl_app_list.size - 1) {
                    adIndexSl++
                    loadHomeAdvertisementSl(context, adData)
                } else {
                    adIndexSl = 0
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                KLog.d(logTagSl, "vpn---加载vpn原生广告成功")
                loadTimeSl = Date().time
                isLoadingSl = false
                adIndexSl = 0
            }

            override fun onAdOpened() {
                super.onAdOpened()
                KLog.d(logTagSl, "vpn---点击vpn原生广告")
                recordNumberOfAdClickSl()
            }
        }).build().loadAd(AdRequest.Builder().build())
    }

    /**
     * 设置展示vpn原生广告
     */
    fun setDisplayHomeNativeAdSl(activity: AppCompatActivity, binding: ActivityMainBinding) {
        activity.runOnUiThread {
            appAdDataSl.let {
                if (it != null
                    && !whetherToShowSl
                    && activity.lifecycle.currentState == Lifecycle.State.RESUMED
                    && !App.isFrameDisplayed) {
                    val activityDestroyed: Boolean = activity.isDestroyed
                    if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        it.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater
                        .inflate(R.layout.layout_app_list_native, null) as NativeAdView
                    // 对应原生组件
                    setCorrespondingNativeComponentSl(it, adView)
                    binding.slAdFrame.removeAllViews()
                    binding.slAdFrame.addView(adView)
                    binding.appListAdSl = true
                    recordNumberOfAdDisplaysSl()
                    whetherToShowSl = true
                    App.nativeAdRefreshSl = false
                    appAdDataSl = null
                    KLog.d(logTagSl, "vpn--原生广告--展示")
                    //重新缓存
                    advertisementLoadingSl(activity)
                }
            }

        }
    }

    private fun setCorrespondingNativeComponentSl(nativeAd: NativeAd, adView: NativeAdView) {
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.bodyView = adView.findViewById(R.id.ad_body)

        (adView.headlineView as TextView).text = nativeAd.headline
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}