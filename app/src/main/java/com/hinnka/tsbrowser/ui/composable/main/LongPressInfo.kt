package com.hinnka.tsbrowser.ui.composable.main

import android.graphics.Bitmap
import android.webkit.WebView
import com.hinnka.tsbrowser.tab.TabManager

data class LongPressInfo(
    val show: Boolean = false,
    val xOffset: Int = 0,
    val yOffset: Int = 0,
    val type: Int = WebView.HitTestResult.UNKNOWN_TYPE,
    val extra: String = "",
    var preview: Bitmap? = null
)

fun LongPressInfo.hidePopup() {
    TabManager.currentTab.value?.longPressState?.value = copy(show = false)
}
