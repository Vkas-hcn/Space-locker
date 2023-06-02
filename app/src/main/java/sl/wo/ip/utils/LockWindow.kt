package sl.wo.ip.utils

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log.e
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import sl.wo.ip.R
import sl.wo.ip.appsl.App.Companion.mmkvSl
import sl.wo.ip.enevtsl.Constant
import sl.wo.ip.widget.VerifyCodeEditText
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.display.ScreenUtils

class LockWindow : VerifyCodeEditText.OnInputListener {
    companion object {
        fun getInstance() = InstanceHelper.lockWindowHelper
    }

    object InstanceHelper {
        val lockWindowHelper = LockWindow()
    }

    var mWindowManager: WindowManager? = null
    var wmParams: WindowManager.LayoutParams? = null
    var mFloatingLayout: View? = null

//    var wmParamsDialog: WindowManager.LayoutParams? = null
//    var mFloatingLayoutDialog: View? = null
    private lateinit var context: Context

    private lateinit var verifyCodeEditText: VerifyCodeEditText
    private lateinit var img1: ImageView
    private lateinit var img2: ImageView
    private lateinit var img3: ImageView
    private lateinit var img4: ImageView
    private lateinit var img5: ImageView
    private lateinit var img6: ImageView
    private lateinit var img7: ImageView
    private lateinit var img8: ImageView
    private lateinit var img9: ImageView
    private lateinit var img0: ImageView
    private lateinit var imgX: ImageView
    private lateinit var imgEn: ImageView
    private lateinit var forgetText: TextView
    private lateinit var topText: TextView
    private lateinit var conDialogTip: ConstraintLayout
    private lateinit var tvConfirm: TextView


    fun initWindow(context: Context) {
        if (mWindowManager != null) {
            return
        }
//        initWindowDialog(context)
        this.context = context
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //设置好悬浮窗的参数
        wmParams = params
        wmParams!!.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        wmParams!!.x = 0
        wmParams!!.y = 50
        wmParams!!.width = WindowManager.LayoutParams.MATCH_PARENT
        wmParams!!.height = WindowManager.LayoutParams.MATCH_PARENT
        wmParams!!.format = PixelFormat.OPAQUE
        wmParams!!.alpha = 1.0f
        processPasswordControls(context)
    }

    //设置可以显示在状态栏上
    //设置悬浮窗口长宽数据
    private val params: WindowManager.LayoutParams
        private get() {
            wmParams = WindowManager.LayoutParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wmParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                wmParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            //设置可以显示在状态栏上
            wmParams!!.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            val screenWidth = ScreenUtils.getScreenWidth()
            //设置悬浮窗口长宽数据
            wmParams!!.width = screenWidth
            wmParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
            //        wmParams.horizontalMargin=ScreenUtils.dip2px(10);
            return wmParams as WindowManager.LayoutParams
        }

    /**
     * 处理密码控件
     */
    private fun processPasswordControls(context: Context) {
        val inflater = LayoutInflater.from(context)
        mFloatingLayout = inflater.inflate(R.layout.layout_lock_screen, null)
        //寻找控件
        verifyCodeEditText = mFloatingLayout?.findViewById(R.id.ed_pass)!!
        verifyCodeEditText.setOnInputListener(this)
        img1 = mFloatingLayout?.findViewById(R.id.img_1)!!
        img2 = mFloatingLayout?.findViewById(R.id.img_2)!!
        img3 = mFloatingLayout?.findViewById(R.id.img_3)!!
        img4 = mFloatingLayout?.findViewById(R.id.img_4)!!
        img5 = mFloatingLayout?.findViewById(R.id.img_5)!!
        img6 = mFloatingLayout?.findViewById(R.id.img_6)!!
        img7 = mFloatingLayout?.findViewById(R.id.img_7)!!
        img8 = mFloatingLayout?.findViewById(R.id.img_8)!!
        img9 = mFloatingLayout?.findViewById(R.id.img_9)!!
        img0 = mFloatingLayout?.findViewById(R.id.img_0)!!
        imgX = mFloatingLayout?.findViewById(R.id.img_x)!!
        imgEn = mFloatingLayout?.findViewById(R.id.img_en)!!
        forgetText = mFloatingLayout?.findViewById(R.id.tv_forget)!!
        forgetText.visibility =View.GONE
        topText = mFloatingLayout?.findViewById(R.id.tv_set_password)!!

        conDialogTip = mFloatingLayout?.findViewById(R.id.con_dialog_tip)!!
        tvConfirm =mFloatingLayout?.findViewById(R.id.tv_confirm)!!

        img1.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("1", false)
        }
        img2.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("2", false)
        }
        img3.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("3", false)
        }
        img4.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("4", false)
        }
        img5.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("5", false)
        }
        img6.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("6", false)
        }
        img7.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("7", false)
        }
        img8.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("8", false)
        }
        img9.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("9", false)
        }
        img0.setOnClickListener { v: View? ->
            verifyCodeEditText.setText("0", false)
        }
        imgX.setOnClickListener { v: View? ->
            verifyCodeEditText.clearInputValue()
        }
        imgEn.setOnClickListener { v: View? ->
            verifyCodeEditText.onKeyDelete()
        }

        conDialogTip.setOnClickListener {}

        tvConfirm.setOnClickListener {
            conDialogTip.visibility = View.GONE
            displayErrorView(false)
            verifyCodeEditText.clearInputValue()
        }
    }

    override fun onComplete(input: String?) {
        e("TAG", "onComplete========")
        val data = mmkvSl.decodeString(Constant.LOCK_CODE_SL, "")
        if (Utils.isNullOrEmpty(data)) {
            return
        }
        e("TAG", "inputValue----->${verifyCodeEditText.inputValue}")
        if (verifyCodeEditText.inputValue == data) {
            closeThePasswordBox()
        } else {
            var num = mmkvSl.decodeInt(Constant.NUMBER_OF_ERRORS, 0)
            num += 1
            MmkvUtils.set(Constant.NUMBER_OF_ERRORS, num)
            if (num > 3) {
                resetPasswordPopup()
            }
            displayErrorView(true)
        }
    }

    override fun onChange(input: String?) {
    }

    override fun onClear() {
        displayErrorView(false)
    }

    /**
     * 显示错误view
     */
    private fun displayErrorView(isErrorView: Boolean) {
        if (isErrorView) {
            verifyCodeEditText.setPasswordErrorColor()
            img0.setImageResource(R.mipmap.ic_0_dis)
            img1.setImageResource(R.mipmap.ic_1_dis)
            img2.setImageResource(R.mipmap.ic_2_dis)
            img3.setImageResource(R.mipmap.ic_3_dis)
            img4.setImageResource(R.mipmap.ic_4_dis)
            img5.setImageResource(R.mipmap.ic_5_dis)
            img6.setImageResource(R.mipmap.ic_6_dis)
            img7.setImageResource(R.mipmap.ic_7_dis)
            img8.setImageResource(R.mipmap.ic_8_dis)
            img9.setImageResource(R.mipmap.ic_9_dis)
            imgX.setImageResource(R.mipmap.ic_x_dis)
            imgEn.setImageResource(R.mipmap.ic_en_dis)
        } else {
            verifyCodeEditText.setPasswordNormalColor()
            img0.setImageResource(R.mipmap.ic_0)
            img1.setImageResource(R.mipmap.ic_1)
            img2.setImageResource(R.mipmap.ic_2)
            img3.setImageResource(R.mipmap.ic_3)
            img4.setImageResource(R.mipmap.ic_4)
            img5.setImageResource(R.mipmap.ic_5)
            img6.setImageResource(R.mipmap.ic_6)
            img7.setImageResource(R.mipmap.ic_7)
            img8.setImageResource(R.mipmap.ic_8)
            img9.setImageResource(R.mipmap.ic_9)
            imgX.setImageResource(R.mipmap.ic_x)
            imgEn.setImageResource(R.mipmap.ic_en)
        }
    }

    /**
     * 展示密码框
     */
    fun showPasswordBox() {
        // 添加悬浮窗的视图
        if (mWindowManager != null) {
            mWindowManager!!.addView(mFloatingLayout, wmParams)
            MmkvUtils.set(Constant.NUMBER_OF_ERRORS, 0)
        }
    }

    /**
     * 关闭密码框
     */
    fun closeThePasswordBox() {
        //移除悬浮窗
        if (mWindowManager != null) {
            mWindowManager!!.removeView(mFloatingLayout)
            mWindowManager = null
            MmkvUtils.set(Constant.NUMBER_OF_ERRORS, 0)
        }

    }

    /**
     * 重置密码弹框
     */
    private fun resetPasswordPopup() {
        conDialogTip.visibility = View.VISIBLE
        MmkvUtils.set(Constant.NUMBER_OF_ERRORS, 0)
    }
}