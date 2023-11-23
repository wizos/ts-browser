/*
 * Copyright (c) 2023 Wizos
 * Project: Loread
 * Email: wizos@qq.com
 * Create Time: 2023-08-19 09:46:59
 */

package com.hinnka.tsbrowser.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.isVisible
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.ext.setFullScreen

class VideoContainer: FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    val visibleState = mutableStateOf(false)
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    init {
        isVisible = false
        setBackgroundColor(Color.BLACK)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        visibleState.value = isVisible
    }

    fun showFullScreen(window: Window, view: View, callback: WebChromeClient.CustomViewCallback?){
        customViewCallback = callback

        if (isVisible) callback?.onCustomViewHidden()

        removeAllViews()
        addView(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        isVisible = true
        setFullScreen(window, true)
        try {
            keepScreenOn = true
        } catch (e: Exception) {
            XLog.e(e)
        }
    }

    fun hideFullScreen(window: Window){
        isVisible = false
        removeAllViews()
        setFullScreen(window, false)
        try {
            keepScreenOn = false
        } catch (e: Exception) {
            XLog.e(e)
            e.printStackTrace()
        }

        // 调用CustomViewCallback的onCustomViewHidden方法告知WebView退出全屏模式
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }
}