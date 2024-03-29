package com.vkas.spacelocker.uisl.websl

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import com.vkas.spacelocker.BR
import com.vkas.spacelocker.R
import com.vkas.spacelocker.basesl.BaseActivity
import com.vkas.spacelocker.basesl.BaseViewModel
import com.vkas.spacelocker.databinding.ActivityWebSlBinding
import com.vkas.spacelocker.enevtsl.Constant

class WebSlActivity : BaseActivity<ActivityWebSlBinding, BaseViewModel>() {
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_web_sl
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.webTitleSl.imgLeft.visibility = View.VISIBLE
        binding.webTitleSl.imgLeft.setImageResource(R.drawable.ic_title_back)

        binding.webTitleSl.imgLeft.setOnClickListener {
            finish()
        }
        binding.webTitleSl.imgMiddle.visibility = View.GONE
    }

    override fun initData() {
        super.initData()
        binding.ppWebSl.loadUrl(Constant.PRIVACY_SL_AGREEMENT)
        binding.ppWebSl.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            }

            override fun onPageFinished(view: WebView, url: String) {
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.proceed()
            }
        }

        binding.ppWebSl.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler, error: SslError
            ) {
                handler.proceed()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (Constant.PRIVACY_SL_AGREEMENT == url) {
                    view.loadUrl(url)
                } else {
                    // 系统处理
                    return super.shouldOverrideUrlLoading(view, url)
                }
                return true
            }
        }


    }


    //点击返回上一页面而不是退出浏览器
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.ppWebSl.canGoBack()) {
            binding.ppWebSl.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        binding.ppWebSl.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        binding.ppWebSl.clearHistory()
        (binding.ppWebSl.parent as ViewGroup).removeView(binding.ppWebSl)
        binding.ppWebSl.destroy()
        super.onDestroy()
    }
}