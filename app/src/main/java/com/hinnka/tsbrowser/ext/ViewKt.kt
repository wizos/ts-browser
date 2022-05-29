package com.hinnka.tsbrowser.ext

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun View.removeFromParent() {
    val parent = parent as? ViewGroup
    parent?.removeView(this)
}

fun View.setFullScreen(window: Window, enable: Boolean) {
    val controller = WindowCompat.getInsetsController(window, this)
    // val controller = ViewCompat.getWindowInsetsController(this)
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
    if (enable) {
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.hide(WindowInsetsCompat.Type.ime())
    } else {
        controller.show(WindowInsetsCompat.Type.statusBars())
        controller.show(WindowInsetsCompat.Type.navigationBars())
        controller.show(WindowInsetsCompat.Type.ime())
    }
}

val View.activity: Activity?
    get() = context as? Activity

fun Modifier.tap(block: (Offset) -> Unit): Modifier {
    return pointerInput(Unit) {
        detectTapGestures(onTap = block)
    }
}

fun Modifier.longPress(block: (Offset) -> Unit): Modifier {
    return pointerInput(Unit) {
        detectTapGestures(onLongPress = block)
    }
}
