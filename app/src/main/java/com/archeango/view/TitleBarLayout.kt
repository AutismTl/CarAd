package com.archeango.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import com.archeango.R

/**
 * Created by 唐亮 on 2017/7/30.
 */
class TitleBarLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private val button: Button

    init {
        LayoutInflater.from(context).inflate(R.layout.common_titlebar, this)
        button = findViewById<View>(R.id.title_menu) as Button
        button.setOnClickListener { (getContext() as Activity).finish() }
    }
}
