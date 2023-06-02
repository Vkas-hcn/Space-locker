package com.vkas.spacelocker.uisl.start

import android.content.Intent
import android.content.IntentFilter
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.spacelocker.R
import com.vkas.spacelocker.appsl.App
import com.vkas.spacelocker.basesl.BaseActivity2
import com.vkas.spacelocker.broadcast.SlBroadcastReceiver
import com.vkas.spacelocker.databinding.ActivityStartBinding
import com.vkas.spacelocker.enevtsl.Constant
import com.vkas.spacelocker.enevtsl.Constant.logTagSl
import com.vkas.spacelocker.service.LockServiceNew
import com.vkas.spacelocker.uisl.main.MainActivity
import com.vkas.spacelocker.utils.SpaceLockerUtils.getAppList
import com.xuexiang.xui.widget.progress.HorizontalProgressView
import kotlinx.coroutines.*

class StartActivity : BaseActivity2<ActivityStartBinding>(),
    HorizontalProgressView.HorizontalProgressUpdateListener {
    companion object {
        var isCurrentPage: Boolean = false
    }

    private var jobOpenAdsSl: Job? = null
    private var liveJumpHomePage = MutableLiveData<Boolean>()
    private var liveJumpHomePage2 = MutableLiveData<Boolean>()

    override fun getLayoutId(): Int {
        return R.layout.activity_start
    }

    override fun setupViews() {
        isCurrentPage = intent.getBooleanExtra(Constant.RETURN_SL_CURRENT_PAGE, false)
    }

    override fun setupData() {
        binding.horProViewSl.setProgressViewUpdateListener(this)
        binding.horProViewSl.setProgressDuration(2000)
        binding.horProViewSl.startProgressAnimation()
        liveEventBusSl()
        startServiceAndBroadcast()
        getAppList(this)
        jumpHomePageData()
        preloadedAdvertisement()
    }



    private fun liveEventBusSl() {
        LiveEventBus
            .get(Constant.OPEN_CLOSE_JUMP, Boolean::class.java)
            .observeForever {
                LogUtils.d(logTagSl, "关闭开屏内容-接收==${this.lifecycle.currentState}")
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
        val intentOne = Intent(this, LockServiceNew::class.java)
        startService(intentOne)
    }

    private fun jumpHomePageData() {
        liveJumpHomePage2.observe(this, {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                LogUtils.e("TAG", "isBackDataSl==${App.isBackDataSl}")
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
     * 预加载广告
     */
    private fun preloadedAdvertisement() {
        lifecycleScope.launch {
            delay(2000L)
            liveJumpHomePage.postValue(true)
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