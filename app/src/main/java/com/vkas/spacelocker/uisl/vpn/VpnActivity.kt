package com.vkas.spacelocker.uisl.vpn

import android.os.Bundle
import com.vkas.spacelocker.BR
import com.vkas.spacelocker.R
import com.vkas.spacelocker.basesl.BaseActivity
import com.vkas.spacelocker.basesl.BaseViewModel
import com.vkas.spacelocker.databinding.ActivityStartBinding
import com.vkas.spacelocker.databinding.LayoutLockScreenBinding
import com.vkas.spacelocker.uisl.start.StartViewModel
import com.vkas.spacelocker.utils.KLog

class VpnActivity: BaseActivity<LayoutLockScreenBinding, BaseViewModel>() {
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.layout_lock_screen
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initData() {
        super.initData()
        binding.img1.setOnClickListener {
            KLog.e("TAG","111111")
            binding.edPass.setText("9",true)
        }
        binding.imgEn.setOnClickListener {
            KLog.e("TAG","en${binding.edPass.inputValue}")
        }
    }
}