package com.vkas.spacelocker.uisl.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.core.net.toUri
import com.vkas.spacelocker.BR
import com.vkas.spacelocker.R
import com.vkas.spacelocker.basesl.BaseActivity
import com.vkas.spacelocker.databinding.ActivityMainBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.vkas.spacelocker.bean.SlAppBean
import com.vkas.spacelocker.utils.SpaceLockerUtils
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.spacelocker.appsl.App
import com.vkas.spacelocker.broadcast.SlBroadcastReceiver
import com.vkas.spacelocker.enevtsl.Constant
import com.vkas.spacelocker.uisl.websl.WebSlActivity
import com.vkas.spacelocker.utils.KLog
import com.vkas.spacelocker.utils.MmkvUtils
import com.vkas.spacelocker.widget.LockerDialog
import com.vkas.spacelocker.widget.PasswordDialog
import com.vkas.spacelocker.widget.SlLockeringDialog
import com.xuexiang.xutil.tip.ToastUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    private lateinit var appListBean: MutableList<SlAppBean>
    private lateinit var appListAdapter: AppListAdapter
    private var lockFrameJob: Job? = null
    private var liveLock = MutableLiveData<Bundle>()
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_main
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.presenter = SLClick()
        binding.inMainTitle.imgLeft.visibility = View.GONE
        binding.inMainTitle.imgMiddle.setImageResource(R.mipmap.ic_applock)
        binding.inMainTitle.imgRight.setImageResource(R.drawable.ic_title_settting_sl)
        binding.inMainTitle.imgRight.setOnClickListener {
            KLog.e("TAG", "inMainTitle-----")
            binding.sidebarShowsSL = binding.sidebarShowsSL != true

        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            SpaceLockerUtils.isOnRight = when (checkedId) {
                R.id.radio_button0 -> 0
                R.id.radio_button1 -> 1
                else -> 0
            }
            sortApplicationToEmptyList()
        }
    }

    override fun initData() {
        super.initData()
        liveEventBusReceive()
        viewModel.whetherPopUpPasswordSettingBox(this)
        initRecyclerView()
        createBroadcast()
    }

    override fun initViewObservable() {
        super.initViewObservable()
        viewModel.liveLock.observe(this, {
            showLockFrame(it)
        })


    }
    private fun liveEventBusReceive() {
        LiveEventBus
            .get(Constant.REFRESH_LOCK_LIST, Boolean::class.java)
            .observeForever {
                sortApplicationToEmptyList()
            }
    }
    private fun initRecyclerView() {
        appListBean = ArrayList()
        SpaceLockerUtils.isOnRight = 0
        appListBean = SpaceLockerUtils.appList
        KLog.e("TAG","appList---2--${SpaceLockerUtils.appList.size}")

        var typeEnum = false
        appListBean.forEach {
            if (it.isLocked) {
                typeEnum = true
            }
        }
        binding.dataEmpty = typeEnum
        appListAdapter = AppListAdapter(appListBean)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recAppList.layoutManager = layoutManager
        binding.recAppList.adapter = appListAdapter
        appListAdapter.addChildClickViewIds(R.id.img_down_state)
        appListAdapter.setOnItemChildClickListener { _, _, position ->
            if (!Settings.canDrawOverlays(this@MainActivity)
            ) {
                requestAlertWindowPermission()
                return@setOnItemChildClickListener
            }
            if(!hasPackageUseStatusPermission()){
                requestPackageUseStatusPermission()
                return@setOnItemChildClickListener
            }
            if(appListAdapter.data[position].isLocked){
                viewModel.clickToPopPasswordBox(this,appListAdapter,position)
            }else{
                showLockFrame(position)
            }
        }
    }
    /**
     * 加锁弹框
     */
    private fun showLockFrame(position: Int) {
        lockFrameJob = lifecycleScope.launch {
            SlLockeringDialog(this@MainActivity).show()
            delay(2000)
            appListBean.getOrNull(position)?.isLocked =
                appListBean.getOrNull(position)?.isLocked != true
            sortApplicationToEmptyList()
            viewModel.storeLockedApplications(appListBean)
        }
    }
    /**
     * 应用列表排序,至空
     */
    private fun sortApplicationToEmptyList(){
        val list = appListBean
            .asSequence()
            .distinctBy { it.packageNameSl }
            .sortedByDescending { it.installTime }
            .sortedBy { it.isLocked }
            .toMutableList()
        var typeEnum = false
        if (SpaceLockerUtils.isOnRight == 0) {
            appListBean.forEach {
                if (it.isLocked) {
                    typeEnum = true
                }
            }
            binding.dataEmpty = typeEnum
        } else {
            binding.dataEmpty = true
        }
        appListBean= list
        appListAdapter.setList(appListBean)
    }
    /**
     * 创建广播
     */
    private fun createBroadcast() {
        //创建广播
        val innerReceiver = SlBroadcastReceiver()
        //动态注册广播
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        //启动广播
        registerReceiver(innerReceiver, intentFilter)
    }

    private fun Context.hasPackageUseStatusPermission(): Boolean {
        return try {
            val appOpsManager =
                ContextCompat.getSystemService(this, AppOpsManager::class.java) ?: return false
            val mode: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    packageName
                )
            } else {
                appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    private fun Context.requestPackageUseStatusPermission() {
        runCatching {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = "package:${applicationContext.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    private fun Context.requestAlertWindowPermission() {
        runCatching {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = "package:${applicationContext.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    inner class SLClick {
        fun clickMain() {
            if (binding.sidebarShowsSL == true) {
                binding.sidebarShowsSL = false
            }
        }

        fun toSetPassword() {
            App.forgotPassword = Constant.SKIP_TO_NORMAL_PASSWORD
            binding.sidebarShowsSL = false
            LockerDialog(this@MainActivity)
                .setMessage(getString(R.string.are_you_sure_to_reset_them))
                ?.setConfirmButton(object : LockerDialog.OnConfirmClickListener {
                    override fun doConfirm() {
                        SpaceLockerUtils.clearApplicationData()
                        appListAdapter.notifyDataSetChanged()
                        PasswordDialog(this@MainActivity, true).show()
                    }
                })
                ?.show()
        }

        fun clickMainMenu() {

        }

        fun toContactUs() {
            val uri = Uri.parse("mailto:${Constant.MAILBOX_SL_ADDRESS}")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            runCatching {
                startActivity(intent)
            }.onFailure {
                ToastUtils.toast("Please set up a Mail account")
            }
        }

        fun toPrivacyPolicy() {
            startActivity(WebSlActivity::class.java)
        }

        fun toShare() {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(
                Intent.EXTRA_TEXT,
                Constant.SHARE_SL_ADDRESS + this@MainActivity.packageName
            )
            intent.type = "text/plain"
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if(appListBean.isEmpty()){
            appListBean = SpaceLockerUtils.appList
            sortApplicationToEmptyList()
        }
        if (App.forgotPassword == Constant.SKIP_TO_NORMAL_PASSWORD) {
            KLog.e("TAG","onResume-----1")
            viewModel.noPasswordSetPassword(this)
        }
        if (App.forgotPassword == Constant.SKIP_TO_ERROR_PASSWORD) {
            KLog.e("TAG","onResume-----2")
            viewModel.showSettingPasswordPopUp(this)
        }
        if (App.forgotPassword == Constant.SKIP_TO_FORGET_PASSWORD) {
            KLog.e("TAG","onResume-----3")
            viewModel.showClearPasswordPopUp(this, appListAdapter,-1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.isFrameDisplayed =false
    }
}