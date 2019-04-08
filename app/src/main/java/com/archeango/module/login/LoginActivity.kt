package com.archeango.module.login

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import cn.smssdk.EventHandler
import cn.smssdk.SMSSDK
import com.archeango.R
import com.archeango.base.BaseActivity
import com.archeango.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_login.*


/**
 *
 *
 * @author tangliang.autisl@bytedance.com
 * @date 2019/3/28.
 */
class LoginActivity : BaseActivity() {

    private var isDelete = false
    private var lastContentLength = 0
    private var isFirstEdit = true
    private var isTicking = false
    private var phone: String = ""

    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout() = R.layout.activity_login

    override fun doBusiness(mContext: Context) {
        initClick()
    }

    private var eventHandler: EventHandler = object : EventHandler() {
        override fun afterEvent(event: Int, result: Int, data: Any) {
            // afterEvent会在子线程被调用，因此如果后续有UI相关操作，需要将数据发送到UI线程
            val msg = Message()
            msg.arg1 = event
            msg.arg2 = result
            msg.obj = data
            Handler(Looper.getMainLooper(), object : Handler.Callback {
                override fun handleMessage(msg: Message): Boolean {
                    val event = msg.arg1
                    val result = msg.arg2
                    val data = msg.obj
                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            // 请注意，此时只是完成了发送验证码的请求，验证码短信还需要几秒钟之后才送达
                        } else {
                            showToast("获取验证码失败，请核对手机号是否正确")
                            (data as Throwable).printStackTrace()
                        }
                    } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        if (result == SMSSDK.RESULT_COMPLETE) {

                        } else {
                            showToast("验证失败")
                            (data as Throwable).printStackTrace()
                        }
                    }
                    // TODO 其他接口的返回结果也类似，根据event判断当前数据属于哪个接口
                    return false
                }
            }).sendMessage(msg)
        }
    }

    private fun initClick() {

        login_msg.setOnClickListener {
            phone = login_phone?.text?.toString()?.replace(" ", "").toString()
            if (phone == null || phone.length != 11) {
                return@setOnClickListener
            }
            val timer = TimeCount(60000, 1000)
            timer.start()
            login_psw.requestFocus()
            SMSSDK.registerEventHandler(eventHandler)
            SMSSDK.getVerificationCode("86", phone)
        }

        login_enter.setOnClickListener {
            /*val code = login_psw.text.toString()
            if (!fastClick() && !TextUtils.isEmpty(code)) {
                SMSSDK.submitVerificationCode("86", phone, login_psw.text.toString())
            }*/
            startActivity(DriverVerifyActivity::class.java)
        }

        login_phone.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && isFirstEdit) {
                editAnimator()
                isFirstEdit = false
            }
        }

        login_psw.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && isFirstEdit) {
                editAnimator()
                isFirstEdit = false
            }
        }


        login_phone.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(s: Editable?) {
                if (s == null || isTicking)
                    return
                if (login_phone.text.isNotEmpty() && login_psw.text.isNotEmpty()) {
                    login_enter.setBackgroundResource(R.drawable.bg_login_enter_enable)
                } else {
                    login_enter.setBackgroundResource(R.drawable.bg_login_enter)
                }
                if (s.length == 13) {
                    login_msg.setTextColor(resources.getColor(R.color.themecolor_1))
                    login_msg.isEnabled = true
                } else {
                    login_msg.setTextColor(resources.getColor(R.color.light_grey))
                    login_msg.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null || s.isEmpty())
                    return
                val sb = StringBuffer(s)
                //是否为输入状态
                isDelete = s.length <= lastContentLength
                //输入是第4，第9位，这时需要插入空格
                if (!isDelete && (s.length == 4 || s.length == 9)) {
                    if (s.length == 4) {
                        sb.insert(3, " ")
                    } else {
                        sb.insert(8, " ")
                    }
                    login_phone.setText(sb.toString())
                    //移动光标到最后面
                    login_phone.setSelection(sb.length)
                }
                //删除的位置到4，9时，剔除空格
                if (isDelete && (s.length == 4 || s.length == 9)) {
                    sb.deleteCharAt(sb.length - 1)
                    login_phone.setText(sb.toString())
                    //移动光标到最后面
                    login_phone.setSelection(sb.length)
                }
                lastContentLength = sb.length
            }
        })

        login_psw.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(s: Editable?) {
                if (login_phone.text.isNotEmpty() && login_psw.text.isNotEmpty()) {
                    login_enter.setBackgroundResource(R.drawable.bg_login_enter_enable)
                } else {
                    login_enter.setBackgroundResource(R.drawable.bg_login_enter)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

    }

    private fun editAnimator() {
        val height = CommonUtils.Px2Dp(this, login_status_logo.height?.toFloat())?.toFloat() * 3 / 2
        val animator: ObjectAnimator = ObjectAnimator.ofFloat(login_status_logo, "alpha", 1f, 0f)
        animator.duration = 500
        animator.start()
        val animator2: ObjectAnimator = ObjectAnimator.ofFloat(login_main, "translationY", -height)
        animator2.duration = 500
        animator2.start()
    }

    internal inner class TimeCount(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {

        override fun onTick(millisUntilinished: Long) {
            login_msg.isEnabled = false
            isTicking = true
            login_msg.text = "${millisUntilinished / 1000}s"
        }

        override fun onFinish() {
            login_msg.text = "重新发送"
            isTicking = false
            if (login_phone.text.length == 13) {
                login_msg.setTextColor(resources.getColor(R.color.themecolor_1))
                login_msg.isEnabled = true
            } else {
                login_msg.setTextColor(resources.getColor(R.color.light_grey))
                login_msg.isEnabled = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}