package a

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import sl.wo.ip.R
import b.B
import sl.wo.ip.enevtsl.Constant
import sl.wo.ip.utils.MmkvUtils
import sl.wo.ip.utils.SpaceLockerUtils
import sl.wo.ip.utils.SpaceLockerUtils.clearApplicationData
import com.xuexiang.xui.utils.Utils
import java.util.*

class K : Dialog, View.OnClickListener, A.OnInputListener {
    private var mContext: Activity? = null
    private var onForgetClickListener: OnForgetClickListener? = null
    private var onFinishClickListener: OnFinishClickListener? = null

    private lateinit var verifyCodeEditText: A
    private lateinit var topText: TextView
    private lateinit var forgetText: TextView
    private var fistPassword = ""
    private var secondPassword = ""
    private var isSetPassWord = false
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

    //错误次数
    var numberOfErrors = 0

    interface OnForgetClickListener {
        fun doForget()
    }
    interface OnFinishClickListener {
        fun doFinish()
    }
    constructor(context: Activity, isSetPassWord: Boolean = false) : super(context) {
        this.isSetPassWord = isSetPassWord
        this.mContext = context
        initView()
    }

    constructor(context: Activity, themeResId: Int) : super(context, R.style.dialog) {
        this.mContext = context
        initView()
    }

    fun setForgetButton(onClickListener: OnForgetClickListener?): K {
        this.onForgetClickListener = onClickListener
        return this
    }
    fun setFinishButton(onClickListener: OnFinishClickListener?): K {
        this.onFinishClickListener = onClickListener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        //设置主题透明，是dialog可以显示圆角
        Objects.requireNonNull(window)?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.layout_lock_screen)
        val dialogWindow = this.window
        dialogWindow!!.setGravity(Gravity.CENTER)
        dialogWindow
        // 添加动画
        dialogWindow.setWindowAnimations(R.style.dialogWindowAnim)
        // 获取对话框当前的参数值
        val lp = dialogWindow.attributes
        lp.width = context.resources.displayMetrics.widthPixels
        lp.height = context.resources.displayMetrics.heightPixels

        verifyCodeEditText = findViewById(R.id.ed_pass)
        verifyCodeEditText.setOnInputListener(this)
        topText = findViewById(R.id.tv_set_password)
        forgetText = findViewById(R.id.tv_forget)
        forgetText.setOnClickListener(this)
        if (this.isSetPassWord) {
            topText.visibility = View.VISIBLE
            forgetText.visibility = View.GONE
        } else {
            topText.visibility = View.GONE
            forgetText.visibility = View.VISIBLE
        }
        img0 = findViewById(R.id.img_0)
        img0.setOnClickListener(this)
        img1 = findViewById(R.id.img_1)
        img1.setOnClickListener(this)
        img2 = findViewById(R.id.img_2)
        img2.setOnClickListener(this)
        img3 = findViewById(R.id.img_3)
        img3.setOnClickListener(this)
        img4 = findViewById(R.id.img_4)
        img4.setOnClickListener(this)
        img5 = findViewById(R.id.img_5)
        img5.setOnClickListener(this)
        img6 = findViewById(R.id.img_6)
        img6.setOnClickListener(this)
        img7 = findViewById(R.id.img_7)
        img7.setOnClickListener(this)
        img8 = findViewById(R.id.img_8)
        img8.setOnClickListener(this)
        img9 = findViewById(R.id.img_9)
        img9.setOnClickListener(this)
        imgX = findViewById(R.id.img_x)
        imgX.setOnClickListener(this)
        imgEn = findViewById(R.id.img_en)
        imgEn.setOnClickListener(this)
        setCanceledOnTouchOutside(false)
        B.isFrameDisplayed = true
        this.setCancelable(false)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.img_0 -> {
                verifyCodeEditText.setText("0", false)
            }
            R.id.img_1 -> {
                verifyCodeEditText.setText("1", false)
            }
            R.id.img_2 -> {
                verifyCodeEditText.setText("2", false)
            }
            R.id.img_3 -> {
                verifyCodeEditText.setText("3", false)
            }
            R.id.img_4 -> {
                verifyCodeEditText.setText("4", false)
            }
            R.id.img_5 -> {
                verifyCodeEditText.setText("5", false)
            }
            R.id.img_6 -> {
                verifyCodeEditText.setText("6", false)
            }
            R.id.img_7 -> {
                verifyCodeEditText.setText("7", false)
            }
            R.id.img_8 -> {
                verifyCodeEditText.setText("8", false)
            }
            R.id.img_9 -> {
                verifyCodeEditText.setText("9", false)
            }
            R.id.img_x -> {
                verifyCodeEditText.clearInputValue()
            }
            R.id.img_en -> {
                verifyCodeEditText.onKeyDelete()
            }
            R.id.tv_forget -> {
                dismiss()
                B.isFrameDisplayed =false
                if (onForgetClickListener != null) {
                    onForgetClickListener!!.doForget()
                }
            }
        }
    }

    /**
     * 设置密码
     */
    private fun setPassword(input: String?) {
        if (Utils.isNullOrEmpty(fistPassword)) {
            fistPassword = input.toString()
            topText.text = context.getString(R.string.input_password_again)
            verifyCodeEditText.clearInputValue()
            return
        } else {
            secondPassword = input.toString()
            if (secondPassword != fistPassword) {
                topText.text = context.getString(R.string.wrong_password)
                verifyCodeEditText.setPasswordErrorColor()
                return
            } else {
                dismiss()
                B.isFrameDisplayed = false
                if (B.forgotPassword == Constant.SKIP_TO_ERROR_PASSWORD) {
                    this.mContext?.let { it ->
                        J(it)
                            .setMessage(context.getString(R.string.confirm_you_encrypt))
                            ?.setConfirmTv("Sure")
                            ?.setCancelButton(object : J.OnCancelClickListener {
                                override fun doCancel() {
                                    B.forgotPassword = Constant.SKIP_TO_ERROR_PASSWORD
//                                    MmkvUtils.set(Constant.LOCK_CODE_SL, "")
                                    K(it, true).show()
                                }
                            })
                            ?.setConfirmButton(object : J.OnConfirmClickListener {
                                override fun doConfirm() {
                                    dismiss()
                                    MmkvUtils.set(Constant.LOCK_CODE_SL, secondPassword)
                                    B.forgotPassword = Constant.SKIP_TO_NORMAL_PASSWORD
                                    B.isFrameDisplayed = false
                                    MmkvUtils.set(Constant.STORE_LOCKED_APPLICATIONS, "")
                                    SpaceLockerUtils.appList.forEach {slAppBean->
                                        slAppBean.isLocked = false
                                    }
                                    LiveEventBus.get<Boolean>(Constant.REFRESH_LOCK_LIST)
                                            .post(true)
                                }
                            })
                            ?.show()
                    }
                } else {
                    MmkvUtils.set(Constant.LOCK_CODE_SL, secondPassword)
                    B.whetherEnteredSuccessPassword = true
                    LiveEventBus.get<Boolean>(Constant.REFRESH_LOCK_LIST)
                        .post(true)
                    this.mContext?.let {
                        J(it, true)
                            .setMessage(context.getString(R.string.set_password_successful))
                            ?.setConfirmTv("Got it")
                            ?.show()
                    }
                }
            }
        }
    }

    /**
     * 判断密码
     */
    private fun judgePassword(input: String?) {
        val data = B.mmkvSl.decodeString(Constant.LOCK_CODE_SL, "")
        if (Utils.isNullOrEmpty(data)) {
            return
        }
        LogUtils.e("TAG", "inputValue----->${verifyCodeEditText.inputValue}")
        if (input == data) {
            dismiss()
            B.isFrameDisplayed = false
            if (onFinishClickListener != null) {
                onFinishClickListener!!.doFinish()
            }
        } else {
            numberOfErrors += 1
            if (numberOfErrors > 3) {
                this.mContext?.let {
                    J(it)
                        .setMessage(context.getString(R.string.password_error4_times))
                        ?.setCancelButton(object : J.OnCancelClickListener {
                            override fun doCancel() {
                                displayErrorView(false)
                                verifyCodeEditText.clearInputValue()
                            }
                        })
                        ?.setConfirmButton(object : J.OnConfirmClickListener {
                            override fun doConfirm() {
                                dismiss()
                                displayErrorView(false)
                                B.isFrameDisplayed = false
                                clearApplicationData()
                                K(mContext!!, true).show()
                            }
                        })
                        ?.show()
                    numberOfErrors = 0
                }
            }
            displayErrorView(true)
        }
    }

    override fun onComplete(input: String?) {
        LogUtils.e("TAG", "onComplete----->${input}")
        if (B.forgotPassword == Constant.SKIP_TO_NORMAL_PASSWORD) {
            if (Utils.isNullOrEmpty(B.mmkvSl.getString(Constant.LOCK_CODE_SL, ""))) {
                setPassword(input)
            } else {
                judgePassword(input)
            }
        }
        if(B.forgotPassword == Constant.SKIP_TO_ERROR_PASSWORD){
            setPassword(input)
        }
        if(B.forgotPassword == Constant.SKIP_TO_FORGET_PASSWORD){
            setPassword(input)
        }
    }

    override fun onChange(input: String?) {
        displayErrorView(false)
    }

    override fun onClear() {
        if (!Utils.isNullOrEmpty(fistPassword)) {
            topText.text = context.getString(R.string.input_password_again)
            verifyCodeEditText.setPasswordNormalColor()
            return
        }
    }

    /**
     * 显示错误view
     */
    private fun displayErrorView(isErrorView: Boolean) {
        val imageResources = if (isErrorView) {
            verifyCodeEditText.setPasswordErrorColor()
            listOf(
                R.mipmap.ic_0_dis, R.mipmap.ic_1_dis, R.mipmap.ic_2_dis, R.mipmap.ic_3_dis,
                R.mipmap.ic_4_dis, R.mipmap.ic_5_dis, R.mipmap.ic_6_dis, R.mipmap.ic_7_dis,
                R.mipmap.ic_8_dis, R.mipmap.ic_9_dis, R.mipmap.ic_x_dis, R.mipmap.ic_en_dis
            )
        } else {
            verifyCodeEditText.setPasswordNormalColor()
            listOf(
                R.mipmap.ic_0, R.mipmap.ic_1, R.mipmap.ic_2, R.mipmap.ic_3,
                R.mipmap.ic_4, R.mipmap.ic_5, R.mipmap.ic_6, R.mipmap.ic_7,
                R.mipmap.ic_8, R.mipmap.ic_9, R.mipmap.ic_x, R.mipmap.ic_en
            )
        }
        listOf(img0, img1, img2, img3, img4, img5, img6, img7, img8, img9, imgX, imgEn)
            .zip(imageResources)
            .forEach { (imageView, imageResource) ->
                imageView.setImageResource(imageResource)
            }
    }

}