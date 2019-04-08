package com.archeango.module.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import com.archeango.R
import com.archeango.base.BaseActivity
import com.archeango.utils.CommonUtils
import com.baidu.ocr.sdk.OCR
import com.baidu.ocr.sdk.OnResultListener
import com.baidu.ocr.sdk.exception.OCRError
import com.baidu.ocr.sdk.model.AccessToken
import com.baidu.ocr.sdk.model.OcrRequestParams
import com.baidu.ocr.sdk.model.OcrResponseResult
import com.baidu.ocr.ui.camera.CameraActivity
import kotlinx.android.synthetic.main.activity_driver_verify.*
import kotlinx.android.synthetic.main.common_titlebar.*
import org.json.JSONObject
import java.io.File

/**
 *
 *
 * @author tangliang.autisl@bytedance.com
 * @date 2019/4/2.
 */
class DriverVerifyActivity : BaseActivity() {

    private val REQUEST_CODE_VEHICLE_LICENSE = 120
    private val REQUEST_CODE_DRIVING_LICENSE = 121
    private var plateNumber: String? = null
    private var driverName: String? = null
    private var IDNumber: String? = null
    private var carType: String? = null
    private var recommendCode: String? = null

    override fun initParms(parms: Bundle?) {

    }

    override fun bindLayout() = R.layout.activity_driver_verify

    override fun doBusiness(mContext: Context) {
        initView()
        initOCR()
        initClick()
    }

    private fun initView() {
        title_text.text = "司机认证"
    }

    private fun initClick() {
        verify_jiashi_picture.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            // 设置临时存储
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(application).absolutePath)
            // 调用除银行卡，身份证等识别的activity
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL)
            startActivityForResult(intent, REQUEST_CODE_DRIVING_LICENSE)

        }

        verify_xingshi_picture.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            // 设置临时存储
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, getSaveFile(application).absolutePath)
            // 调用除银行卡，身份证等识别的activity
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL)
            startActivityForResult(intent, REQUEST_CODE_VEHICLE_LICENSE)
        }

        //当password获得焦点时
        //输入法不覆盖登陆按钮
        verify_recommendCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                CommonUtils.addLayoutListener(verify_root, verify_enter)
            }
        }

    }

    private fun getSaveFile(context: Context) = File(context.externalCacheDir, "pic.jpg")


    private fun initOCR() {
        OCR.getInstance(this).initAccessToken(object : OnResultListener<AccessToken> {
            override fun onResult(result: AccessToken) {
                // 调用成功，返回AccessToken对象
                val token = result.accessToken
                Log.w("ocr", "success " + token)
            }

            override fun onError(error: OCRError) {
                // 调用失败，返回OCRError子类SDKError对象
                Log.w("ocr", "error")
            }
        }, applicationContext)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 获取调用参数
        val contentType = data?.getStringExtra(CameraActivity.KEY_CONTENT_TYPE)
        // 通过临时文件获取拍摄的图片
        val filePath = getSaveFile(applicationContext).absolutePath
        // 行驶证识别参数设置
        val param = OcrRequestParams()
        // 设置image参数
        param.imageFile = File(filePath)
        // 设置其他参数
        param.putParam("detect_direction", true)

        // 驾驶证识别
        if (requestCode == REQUEST_CODE_DRIVING_LICENSE && resultCode == Activity.RESULT_OK) {
            verify_jiashi_picture.setImageDrawable(Drawable.createFromPath(filePath))
            OCR.getInstance(this).recognizeDrivingLicense(param, object : OnResultListener<OcrResponseResult> {
                override fun onResult(result: OcrResponseResult) {
                    val data: JSONObject = JSONObject(result.jsonRes)?.optJSONObject("words_result")
                    //driverName = data?.optJSONObject("姓名")?.optString("words")
                    IDNumber = data?.optJSONObject("证号")?.optString("words")
                    IDNumber?.let {
                        if (it.length < 15 || it.length > 20) {
                            IDNumber = null
                            showToast("识别错误，请重新上传")
                            return@onResult
                        }
                    }
                    jiashiStatusChange()
                    showToast(result.jsonRes)
                }


                override fun onError(error: OCRError) {
                    if (!CommonUtils.isNetworkAvailable(this@DriverVerifyActivity)) {
                        runOnUiThread { showToast("网络错误") }
                    } else {
                        runOnUiThread { showToast("识别错误，请重新上传") }
                    }

                }
            })

        }


        // 行驶证识别
        if (requestCode == REQUEST_CODE_VEHICLE_LICENSE && resultCode == Activity.RESULT_OK) {
            verify_xingshi_picture.setImageDrawable(Drawable.createFromPath(filePath))
            OCR.getInstance(this).recognizeVehicleLicense(param, object : OnResultListener<OcrResponseResult> {
                override fun onResult(result: OcrResponseResult) {
                    val data: JSONObject = JSONObject(result.jsonRes)?.optJSONObject("words_result")
                    plateNumber = data?.optJSONObject("号牌号码")?.optString("words")
                    if (!isCarNumber(plateNumber)) {
                        plateNumber = null
                        showToast("识别错误，请重新上传")
                        return
                    }
                    driverName = data?.optJSONObject("所有人")?.optString("words")
                    carType = data?.optJSONObject("车辆类型")?.optString("words")
                    xingshiStatusChange()
                    showToast(result.jsonRes)
                }

                override fun onError(error: OCRError) {
                    if (!CommonUtils.isNetworkAvailable(this@DriverVerifyActivity)) {
                        runOnUiThread { showToast("网络错误") }
                    } else {
                        runOnUiThread { showToast("识别错误，请重新上传") }
                    }
                }
            })
        }

    }

    private fun buildBitmap(file: File?): Bitmap? {
        if (file == null || file.length() == 0L) {
            return null
        }
        val ops = BitmapFactory.Options()
        ops.inDensity = 320 // xhdpi
        val res = this.resources
        ops.inTargetDensity = (res.displayMetrics.density * 160f).toInt()
        var bitmap: Bitmap? = null
        try {
            bitmap = BitmapFactory.decodeFile(file.absolutePath, ops)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return bitmap
    }

    private fun xingshiStatusChange() {
        verify_xingshi_mb.visibility = View.GONE
        verify_xingshi_mbt.visibility = View.GONE
        verify_xingshi_status.text = "认证成功"
        verify_xingshi_status.setBackgroundResource(R.drawable.bg_verify_enable)
        verify_xingshi_picture.isClickable = false
        verify_enter.isEnabled = !verify_jiashi_picture.isClickable
    }

    private fun jiashiStatusChange() {
        verify_jiashi_mb.visibility = View.GONE
        verify_jiashi_mbt.visibility = View.GONE
        verify_jiashi_status.text = "认证成功"
        verify_jiashi_status.setBackgroundResource(R.drawable.bg_verify_enable)
        verify_jiashi_picture.isClickable = false
        verify_enter.isEnabled = !verify_xingshi_picture.isClickable
    }

    private fun isCarNumber(carNumber: String?): Boolean {
        val carNumberRegex =
            "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})"
        carNumber?.let {
            return carNumber.matches(carNumberRegex.toRegex())
        }
        return false
    }
}