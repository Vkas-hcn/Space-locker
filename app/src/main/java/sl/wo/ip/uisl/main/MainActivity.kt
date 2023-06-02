package sl.wo.ip.uisl.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import sl.wo.ip.R
import sl.wo.ip.databinding.ActivityMainBinding
import androidx.recyclerview.widget.LinearLayoutManager
import sl.wo.ip.bean.SlAppBean
import sl.wo.ip.utils.SpaceLockerUtils
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import sl.wo.ip.appsl.App
import sl.wo.ip.basesl.BaseActivity2
import sl.wo.ip.broadcast.SlBroadcastReceiver
import sl.wo.ip.enevtsl.Constant
import sl.wo.ip.uisl.websl.WebSlActivity
import sl.wo.ip.utils.DebounceUtil
import sl.wo.ip.widget.LockerDialog
import sl.wo.ip.widget.PasswordDialog
import sl.wo.ip.widget.SlLockeringDialog
import com.xuexiang.xutil.tip.ToastUtils
import kotlinx.coroutines.*


class MainActivity : BaseActivity2<ActivityMainBinding>() {
    private lateinit var appListBean: MutableList<SlAppBean>
    private lateinit var appListAdapter: AppListAdapter
    private var lockFrameJob: Job? = null


    private var jobRepeatClick: Job? = null


    //点击下标
    private var positionApp: Int = 0

    // 跳转后弹框
    private var bounceBoxAfterJump = false

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun setupViews() {
        binding.presenter = SLClick()
        binding.inMainTitle.imgLeft.visibility = View.GONE
        binding.inMainTitle.imgMiddle.setImageResource(R.mipmap.ic_applock)
        binding.inMainTitle.imgRight.setImageResource(R.drawable.ic_title_settting_sl)
        binding.inMainTitle.imgRight.setOnClickListener {
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

    override fun setupData() {
        liveEventBusReceive()
        initRecyclerView()
        createBroadcast()
        MainViewFun.liveLock.observe(this, {
            App.whetherEnteredSuccessPassword = true
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



    private fun clickFun(position: Int) {
        LogUtils.e("TAG", "setOnItemChildClickListener-11111")
        if (!Settings.canDrawOverlays(this@MainActivity)
        ) {
            LogUtils.e("TAG", "setOnItemChildClickListener-q")

            requestAlertWindowPermission()
            return
        }
        if (!hasPackageUseStatusPermission()) {
            LogUtils.e("TAG", "setOnItemChildClickListener-d")

            requestPackageUseStatusPermission()
            return
        }
        LogUtils.e("TAG", "setOnItemChildClickListener-22222")
        positionApp = position
        lockClickJudgment()
    }

    private fun initRecyclerView() {
        appListBean = ArrayList()
        SpaceLockerUtils.isOnRight = 0
        appListBean = SpaceLockerUtils.appList
        appListAdapter = AppListAdapter(appListBean, this)
        MainViewFun.whetherPopUpPasswordSettingBox(this, appListAdapter)
        LogUtils.e("TAG", "appList---2--${SpaceLockerUtils.appList.size}")
        var typeEnum = false
        appListBean.forEach {
            if (it.isLocked) {
                typeEnum = true
            }
        }
        binding.dataEmpty = typeEnum
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.recAppList.layoutManager = layoutManager
        binding.recAppList.adapter = appListAdapter

        appListAdapter.setOnItemClickListener(object : AppListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                clickFun(position)
            }
        })
    }

    /**
     * 加锁点击判断
     */
    private fun lockClickJudgment() {
        showLockFrame(positionApp)
    }

    /**
     * 加锁弹框
     */
    private fun showLockFrame(position: Int) {
        lockFrameJob = lifecycleScope.launch {
            SlLockeringDialog(this@MainActivity).show()
            delay(1000)
            appListBean.getOrNull(position)?.isLocked =
                appListBean.getOrNull(position)?.isLocked != true
            sortApplicationToEmptyList()
            MainViewFun.storeLockedApplications(appListBean)
        }
    }

    /**
     * 应用列表排序，将锁定的应用排在最后
     */
    private fun sortApplicationToEmptyList() {
        LogUtils.e("TAG", "应用列表排序，至空")

        val appList = SpaceLockerUtils.appList.toMutableList()

        // 通过包名去重
        val distinctList = appList.distinctBy { it.packageNameSl }

        // 分离锁定的应用和非锁定的应用
        val lockedApps = distinctList.filter { it.isLocked }
        val unlockedApps = distinctList.filterNot { it.isLocked }

        // 按安装时间降序排序
        val sortedUnlockedApps = unlockedApps.sortedByDescending { it.installTime }

        // 合并锁定的应用和非锁定的应用
        val sortedList = sortedUnlockedApps + lockedApps

        var isLockedExist = false

        if (SpaceLockerUtils.isOnRight == 0) {
            // 检查是否存在被锁定的应用
            isLockedExist = lockedApps.isNotEmpty()
        }

        binding.dataEmpty = if (SpaceLockerUtils.isOnRight == 0) isLockedExist else true

        appListAdapter.addAdapterData(sortedList.toMutableList())

        SpaceLockerUtils.appList = sortedList.toMutableList()

        binding.recAppList.scrollToPosition(0)
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

    private fun Context.hasPackageUseStatusPermission2(): Boolean {
        return try {
            val appOpsManager = ContextCompat.getSystemService(this, AppOpsManager::class.java)
            if (appOpsManager == null) {
                return false
            }

            val uid = applicationInfo.uid
            val packageName = packageName
            val opstr = AppOpsManager.OPSTR_GET_USAGE_STATS
            val mode: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(opstr, uid, packageName)
            } else {
                appOpsManager.checkOpNoThrow(opstr, uid, packageName)
            }

            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }



    private fun Context.requestPackageUseStatusPermission() {
        runCatching {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    data = "package:${applicationContext.packageName}".toUri()
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            App.whetherJumpPermission = true
        }
    }

    private fun Context.requestAlertWindowPermission() {
        runCatching {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = "package:${applicationContext.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            App.whetherJumpPermission = true
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
                        App.whetherJumpPermission = true
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
            val intent = Intent(this@MainActivity, WebSlActivity::class.java)
            startActivity(intent)
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


    /**
     * 首页弹框
     */
    private fun homeFrame() {
        if (appListBean.isEmpty()) {
            sortApplicationToEmptyList()
        }
        if (App.forgotPassword == Constant.SKIP_TO_NORMAL_PASSWORD) {
            LogUtils.e("TAG", "onResume-----1")
            MainViewFun.noPasswordSetPassword(this)
        }
        if (App.forgotPassword == Constant.SKIP_TO_ERROR_PASSWORD) {
            LogUtils.e("TAG", "onResume-----2")
            MainViewFun.showSettingPasswordPopUp(this)
        }
        if (App.forgotPassword == Constant.SKIP_TO_FORGET_PASSWORD) {
            LogUtils.e("TAG", "onResume-----3")
            MainViewFun.showClearPasswordPopUp(this, appListAdapter, -1)
        }
    }

    override fun onResume() {
        super.onResume()
        homeFrame()
    }

    override fun onDestroy() {
        super.onDestroy()
        App.isFrameDisplayed = false
        App.whetherEnteredSuccessPassword = false
    }


}