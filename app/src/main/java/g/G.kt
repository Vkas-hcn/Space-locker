package g

import android.graphics.Bitmap
import android.net.http.SslError
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import sl.wo.ip.R
import sl.wo.ip.basesl.BaseActivity2
import sl.wo.ip.databinding.ActivityWebSlBinding
import sl.wo.ip.enevtsl.Constant

class G : BaseActivity2<ActivityWebSlBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_web_sl
    }

    override fun setupViews() {
        binding.webTitleSl.imgLeft.visibility = View.VISIBLE
        binding.webTitleSl.imgLeft.setImageResource(R.drawable.ic_title_back)

        binding.webTitleSl.imgLeft.setOnClickListener {
            finish()
        }
        binding.webTitleSl.imgMiddle.visibility = View.GONE
    }

    override fun setupData() {
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