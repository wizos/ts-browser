package com.hinnka.tsbrowser.web


import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView

private fun Context.fixForLollipop(): Context {
    return if (Build.VERSION.SDK_INT in Build.VERSION_CODES.LOLLIPOP..Build.VERSION_CODES.LOLLIPOP_MR1) {
        // applicationContext
        createConfigurationContext(Configuration())
    } else this
}

open class LollipopFixWebView: WebView {
    constructor(context: Context) : super(context.fixForLollipop())
    constructor(context: Context, attrs: AttributeSet?) : super(context.fixForLollipop(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.fixForLollipop(),
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context.fixForLollipop(), attrs, defStyleAttr, defStyleRes)

    @Suppress("DEPRECATION")
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        privateBrowsing: Boolean
    ) : super(context.fixForLollipop(), attrs, defStyleAttr, privateBrowsing)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }
}