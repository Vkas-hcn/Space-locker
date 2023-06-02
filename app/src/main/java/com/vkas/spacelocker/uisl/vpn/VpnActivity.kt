package com.vkas.spacelocker.uisl.vpn

import com.blankj.utilcode.util.LogUtils
import com.vkas.spacelocker.R
import com.vkas.spacelocker.basesl.BaseActivity2
import com.vkas.spacelocker.databinding.LayoutLockScreenBinding

class VpnActivity: BaseActivity2<LayoutLockScreenBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.layout_lock_screen
    }

    override fun setupViews() {
        binding.img1.setOnClickListener {
            LogUtils.e("TAG","111111")
            binding.edPass.setText("9",true)
        }
        binding.imgEn.setOnClickListener {
            LogUtils.e("TAG","en${binding.edPass.inputValue}")
        }    }

    override fun setupData() {
    }
}