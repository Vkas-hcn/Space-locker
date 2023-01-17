package com.vkas.spacelocker.uisl.start

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.spacelocker.BR
import com.vkas.spacelocker.BuildConfig
import com.vkas.spacelocker.R
import com.vkas.spacelocker.appsl.App
import com.vkas.spacelocker.appsl.slad.SlLoadAppListAd
import com.vkas.spacelocker.appsl.slad.SlLoadLockAd
import com.vkas.spacelocker.appsl.slad.SlLoadOpenAd
import com.vkas.spacelocker.basesl.BaseActivity
import com.vkas.spacelocker.broadcast.SlBroadcastReceiver
import com.vkas.spacelocker.databinding.ActivityStartBinding
import com.vkas.spacelocker.enevtsl.Constant
import com.vkas.spacelocker.enevtsl.Constant.logTagSl
import com.vkas.spacelocker.service.LockService
import com.vkas.spacelocker.uisl.main.MainActivity
import com.vkas.spacelocker.utils.KLog
import com.vkas.spacelocker.utils.MmkvUtils
import com.vkas.spacelocker.utils.SpaceLockerUtils.getAppList
import com.vkas.spacelocker.utils.SpaceLockerUtils.isThresholdReached
import com.xuexiang.xui.widget.progress.HorizontalProgressView
import kotlinx.coroutines.*

class StartActivity : BaseActivity<ActivityStartBinding, StartViewModel>(),
    HorizontalProgressView.HorizontalProgressUpdateListener {
    companion object {
        var isCurrentPage: Boolean = false
    }
    private var jobOpenAdsSl: Job? = null
    private var liveJumpHomePage = MutableLiveData<Boolean>()
    private var liveJumpHomePage2 = MutableLiveData<Boolean>()
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_start
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
        isCurrentPage = intent.getBooleanExtra(Constant.RETURN_SL_CURRENT_PAGE, false)
    }

    override fun initToolbar() {
        super.initToolbar()
    }

    override fun initData() {
        super.initData()
        binding.horProViewSl.setProgressViewUpdateListener(this)
        binding.horProViewSl.setProgressDuration(8000)
        binding.horProViewSl.startProgressAnimation()
        liveEventBusFs()
        startServiceAndBroadcast()
        getAppList(this)
        getFirebaseDataFs()
        jumpHomePageData()
    }

    private fun liveEventBusFs() {
        LiveEventBus
            .get(Constant.OPEN_CLOSE_JUMP, Boolean::class.java)
            .observeForever {
                KLog.d(logTagSl, "关闭开屏内容-接收==${this.lifecycle.currentState}")
                if (this.lifecycle.currentState == Lifecycle.State.STARTED) {
                    jumpPage()
                }
            }
    }

    /**
     * 开启服务和广播
     */
    fun startServiceAndBroadcast() {
        val innerReceiver = SlBroadcastReceiver()
        //动态注册广播
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        //启动广播
        registerReceiver(innerReceiver, intentFilter)
        val intentOne = Intent(this, LockService::class.java)
        startService(intentOne)
    }

    private fun getFirebaseDataFs() {
        if (BuildConfig.DEBUG) {
            preloadedAdvertisement()
//            lifecycleScope.launch {
//                delay(500)
//                MmkvUtils.set(Constant.ADVERTISING_SL_DATA, ResourceUtils.readStringFromAssert("fsAdData1.json"))
//            }
            return
        } else {
            preloadedAdvertisement()
            val auth = Firebase.remoteConfig
            auth.fetchAndActivate().addOnSuccessListener {
                MmkvUtils.set(Constant.PROFILE_SL_DATA, auth.getString("FsServiceData"))
                MmkvUtils.set(Constant.PROFILE_SL_DATA_FAST, auth.getString("FsServiceDataSlst"))
                MmkvUtils.set(Constant.AROUND_SL_FLOW_DATA, auth.getString("FsAroundFlow_Data"))
                MmkvUtils.set(Constant.ADVERTISING_SL_DATA, auth.getString("FsAd_Data"))
            }
        }
    }


    private fun jumpHomePageData() {
        liveJumpHomePage2.observe(this, {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                KLog.e("TAG", "isBackDataFs==${App.isBackDataSl}")
                delay(300)
                if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                    jumpPage()
                }
            }
        })
        liveJumpHomePage.observe(this, {
            liveJumpHomePage2.postValue(true)
        })
    }

    /**
     * 跳转页面
     */
    private fun jumpPage() {
        // 不是后台切回来的跳转，是后台切回来的直接finish启动页
        if (!isCurrentPage) {
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

    /**
     * 加载广告
     */
    private fun loadAdvertisement() {
        // 开屏
        SlLoadOpenAd.getInstance().adIndexSl = 0
        SlLoadOpenAd.getInstance().advertisementLoadingSl(this)
        rotationDisplayOpeningAdSl()
        SlLoadAppListAd.getInstance().adIndexSl = 0
        SlLoadAppListAd.getInstance().advertisementLoadingSl(this)


        SlLoadLockAd.getInstance().adIndexSl = 0
        SlLoadLockAd.getInstance().advertisementLoadingSl(this)
//        // 首页原生
//        SlLoadVpnAd.getInstance().adIndexSl = 0
//        SlLoadVpnAd.getInstance().advertisementLoadingSl(this)
//        // 结果页原生
//        SlLoadResultAd.getInstance().adIndexSl = 0
//        SlLoadResultAd.getInstance().advertisementLoadingSl(this)
//        // 连接插屏
//        SlLoadConnectAd.getInstance().adIndexSl = 0
//        SlLoadConnectAd.getInstance().advertisementLoadingSl(this)
//        // 服务器页原生
//        SlLoadBackAd.getInstance().adIndexSl = 0
//        SlLoadBackAd.getInstance().advertisementLoadingSl(this)
    }

    /**
     * 轮训展示开屏广告
     */
    private fun rotationDisplayOpeningAdSl() {
        jobOpenAdsSl = lifecycleScope.launch {
            try {
                withTimeout(8000L) {
                    delay(1000L)
                    while (isActive) {
                        val showState = SlLoadOpenAd.getInstance()
                            .displayOpenAdvertisementSl(this@StartActivity)
                        if (showState) {
                            jobOpenAdsSl?.cancel()
                            jobOpenAdsSl = null
                        }
                        delay(1000L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                KLog.e("TimeoutCancellationException I'm sleeping $e")
                jumpPage()
            }
        }
    }

    /**
     * 预加载广告
     */
    private fun preloadedAdvertisement() {
        App.isAppOpenSameDaySl()
        if (isThresholdReached()) {
            KLog.d(logTagSl, "广告达到上线")
            lifecycleScope.launch {
                delay(2000L)
                liveJumpHomePage.postValue(true)
            }
        } else {
            loadAdvertisement()
        }
    }

    override fun onHorizontalProgressStart(view: View?) {

    }

    override fun onHorizontalProgressUpdate(view: View?, progress: Float) {
    }

    override fun onHorizontalProgressFinished(view: View?) {
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK
    }
}