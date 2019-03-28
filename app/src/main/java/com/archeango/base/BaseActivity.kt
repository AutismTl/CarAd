package com.archeango.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast


/**
 * Activity基类
 */
abstract class BaseActivity : Activity() {


    /**
     * 是否沉浸状态栏
     */
    private var isSetStatusBar = true
    /**
     * 是否允许全屏
     */
    private var mAllowFullScreen = false
    /**
     * 是否禁止旋转屏幕
     */
    private var isAllowScreenRoate = true
    /**
     * 当前Activity渲染的视图View
     */
    private var mContextView: View? = null
    /**
     * 输出日志标志
     */
    protected val TAG = this.javaClass.simpleName
    /**
     * 是否输出日志信息
     */
    private val isDebug = true
    private val APP_NAME = "ControlHelper"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DLog(TAG + "BaseActivity-->onCreate()")
        try {
            val bundle = intent.extras
            initParms(bundle)
            mContextView = LayoutInflater.from(this)
                .inflate(bindLayout(), null)
            if (mAllowFullScreen) {
                this.window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                //                requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
            if (isSetStatusBar) {
                steepStatusBar()
            }
            setContentView(mContextView)
            if (!isAllowScreenRoate) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            doBusiness(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * [沉浸状态栏]
     */
    private fun steepStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            // 透明导航栏
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        }
    }

    /**
     * [初始化Bundle参数]
     *
     * @param parms
     */
    abstract fun initParms(parms: Bundle?)


    /**
     * [绑定布局]
     *
     * @return
     */
    abstract fun bindLayout(): Int

    /**
     * [业务操作]
     *
     * @param mContext
     */
    abstract fun doBusiness(mContext: Context)


    /**
     * [简化Toast]
     *
     * @param msg
     */
    protected fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    /**
     * [携带数据的页面跳转]
     *
     * @param clz
     * @param bundle
     */
    @JvmOverloads
    fun startActivity(clz: Class<*>, bundle: Bundle? = null) {
        val intent = Intent()
        intent.setClass(this, clz)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivity(intent)
    }


    /**
     * [含有Bundle通过Class打开编辑界面]
     *
     * @param cls
     * @param bundle
     * @param requestCode
     */
    fun startActivityForResult(
        cls: Class<*>, bundle: Bundle?,
        requestCode: Int
    ) {
        val intent = Intent()
        intent.setClass(this, cls)
        if (bundle != null) {
            intent.putExtras(bundle)
        }
        startActivityForResult(intent, requestCode)
    }

    fun startActivityForResult(cls: Class<*>, requestCode: Int) {
        val intent = Intent()
        intent.setClass(this, cls)
        startActivityForResult(intent, requestCode)
    }

    override fun onPause() {
        super.onPause()
        DLog("$TAG--->onPause()")
    }

    override fun onResume() {
        super.onResume()
        DLog("$TAG--->onResume()")
    }

    override fun onDestroy() {
        super.onDestroy()
        DLog("$TAG--->onDestroy()")
    }

    /**
     * [是否允许全屏]
     *
     * @param allowFullScreen
     */
    fun setAllowFullScreen(allowFullScreen: Boolean) {
        this.mAllowFullScreen = allowFullScreen
    }

    /**
     * [是否设置沉浸状态栏]
     *
     * @param isSetStatusBar
     */
    fun setSteepStatusBar(isSetStatusBar: Boolean) {
        this.isSetStatusBar = isSetStatusBar
    }

    /**
     * [是否禁止屏幕旋转]
     *
     * @param isAllowScreenRoate
     */
    fun setScreenRoate(isAllowScreenRoate: Boolean) {
        this.isAllowScreenRoate = isAllowScreenRoate
    }

    /**
     * [日志输出]
     *
     * @param msg
     */
    protected fun DLog(msg: String) {
        if (isDebug) {
            Log.w(APP_NAME, msg)
        }
    }

    /**
     * [防止快速点击]
     *
     * @return
     */
    protected fun fastClick(): Boolean {
        var lastClick: Long = 0
        if (System.currentTimeMillis() - lastClick <= 1000) {
            return false
        }
        lastClick = System.currentTimeMillis()
        return true
    }

}