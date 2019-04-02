package com.archeango

import android.content.Context
import android.os.Bundle
import cn.smssdk.EventHandler
import cn.smssdk.SMSSDK
import com.archeango.base.BaseActivity
import com.archeango.module.login.LoginActivity


class MainActivity : BaseActivity() {
    override fun initParms(parms: Bundle?) {
    }

    override fun bindLayout() = R.layout.activity_main

    override fun doBusiness(mContext: Context) {
        startActivity(LoginActivity::class.java)
        finish()

    }
}
