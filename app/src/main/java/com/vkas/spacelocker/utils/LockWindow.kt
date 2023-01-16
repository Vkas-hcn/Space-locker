package com.vkas.spacelocker.utils

import android.app.Activity
import android.content.Context
import com.vkas.spacelocker.utils.KLog.e
import com.vkas.spacelocker.utils.KLog
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.vkas.spacelocker.R
import com.vkas.spacelocker.appsl.App
import com.vkas.spacelocker.appsl.App.Companion.mmkvSl
import com.vkas.spacelocker.enevtsl.Constant
import com.vkas.spacelocker.uisl.main.MainActivity
import com.vkas.spacelocker.widget.LockerDialog
import com.vkas.spacelocker.widget.VerifyCodeEditText
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.XUtil
import com.xuexiang.xutil.app.ActivityUtils
import com.xuexiang.xutil.display.DensityUtils.dip2px
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

    var wmParamsDialog: WindowManager.LayoutParams? = null
    var mFloatingLayoutDialog: View? = null
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


    fun initWindow(context: Context) {
        if (mWindowManager != null) {
            return
        }
        initWindowDialog(context)
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
        topText = mFloatingLayout?.findViewById(R.id.tv_set_password)!!
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
        forgetText.setOnClickListener { v: View? ->
            App.forgotPassword = Constant.SKIP_TO_FORGET_PASSWORD
            ActivityUtils.startActivity(MainActivity::class.java)
            closeThePasswordBox()
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
            if(mFloatingLayoutDialog?.windowToken !=null){
                mWindowManager!!.removeView(mFloatingLayoutDialog)
            }
            mWindowManager = null
            MmkvUtils.set(Constant.NUMBER_OF_ERRORS, 0)
        }

    }

    /**
     * 重置密码弹框
     */
    fun resetPasswordPopup() {
        mWindowManager?.addView(mFloatingLayoutDialog, wmParamsDialog)
        MmkvUtils.set(Constant.NUMBER_OF_ERRORS, 0)
    }


    fun initWindowDialog(context: Context) {
        if (mWindowManager != null) {
            return
        }
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //设置好悬浮窗的参数
        wmParamsDialog = paramsDialog
        //这里 如果不设置透明  会导致添加的悬浮框带有黑边 布局文件方面  我将自定义布局没有放在最顶层  而是在外面又套了一层 达到我需要的效果 避免类似自定义dialog宽高显示不正常的情况
        wmParamsDialog!!.format = PixelFormat.TRANSPARENT
        wmParamsDialog!!.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
//        wmParamsDialog!!.x = 300
//        wmParamsDialog!!.y = 300
        wmParamsDialog!!.width = WindowManager.LayoutParams.MATCH_PARENT
        wmParamsDialog!!.height = WindowManager.LayoutParams.MATCH_PARENT
        wmParamsDialog!!.format = PixelFormat.OPAQUE
        wmParamsDialog!!.alpha = 1.0f
        processDialogControls(context)
    }

    //设置可以显示在状态栏上
    //设置悬浮窗口长宽数据
    private val paramsDialog: WindowManager.LayoutParams
        private get() {
            wmParamsDialog = WindowManager.LayoutParams()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wmParamsDialog!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                wmParamsDialog!!.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            //设置可以显示在状态栏上
            wmParamsDialog!!.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            val screenWidth = ScreenUtils.getScreenWidth()
            val screenHeight = ScreenUtils.getScreenHeight()

            //设置悬浮窗口长宽数据
            wmParamsDialog!!.width = screenWidth
            wmParamsDialog!!.height = screenHeight
                    wmParams?.horizontalMargin= dip2px(10f).toFloat();
            return wmParamsDialog as WindowManager.LayoutParams
        }

    /**
     * 处理弹框控件
     */
    private fun processDialogControls(context: Context) {
        val inflater = LayoutInflater.from(context)
        mFloatingLayoutDialog = inflater.inflate(R.layout.dialog_tips, null)
        //寻找控件
        val tvTips:TextView = mFloatingLayoutDialog?.findViewById(R.id.tv_tips)!!
        val tvCancel:TextView = mFloatingLayoutDialog?.findViewById(R.id.tv_cancel)!!
        val tvConfirm:TextView = mFloatingLayoutDialog?.findViewById(R.id.tv_confirm)!!

        tvTips.text = context.getString(R.string.password_error4_times)
        tvCancel.setOnClickListener { v: View? ->
            if (mWindowManager != null) {
                mWindowManager!!.removeView(mFloatingLayoutDialog)
                displayErrorView(false)
                verifyCodeEditText.clearInputValue()
            }
        }
        tvConfirm.setOnClickListener { v: View? ->
            App.forgotPassword = Constant.SKIP_TO_ERROR_PASSWORD
            ActivityUtils.startActivity(MainActivity::class.java)
            closeThePasswordBox()
        }
    }
}