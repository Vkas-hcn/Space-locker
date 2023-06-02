package f

import com.blankj.utilcode.util.LogUtils
import sl.wo.ip.R
import sl.wo.ip.basesl.BaseActivity2
import sl.wo.ip.databinding.LayoutLockScreenBinding

class F: BaseActivity2<LayoutLockScreenBinding>() {
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