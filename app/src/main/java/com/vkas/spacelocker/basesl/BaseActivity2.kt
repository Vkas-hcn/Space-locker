package com.vkas.spacelocker.basesl

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity2<T : ViewDataBinding> : AppCompatActivity()  {
    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindView()
        ActivityStackManager.addActivity(this)
        setupViews()
        setupData()
    }

    private fun bindView() {
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this
    }

    protected abstract fun getLayoutId(): Int

    protected abstract fun setupViews()
    protected abstract fun setupData()
    override fun onDestroy() {
        super.onDestroy()
        ActivityStackManager.removeActivity(this)
    }

}