/*
 *  Twidere X
 *
 *  Copyright (C) TwidereProject and Contributors
 * 
 *  This file is part of Twidere X.
 * 
 *  Twidere X is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Twidere X is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Twidere X. If not, see <http://www.gnu.org/licenses/>.
 */
package com.hinnka.tsbrowser.ext

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Suppress("DEPRECATION")
fun View.addSystemUiVisibility(systemUiVisibility: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        this.systemUiVisibility = this.systemUiVisibility or systemUiVisibility
    }
}

@Suppress("DEPRECATION")
fun View.removeSystemUiVisibility(systemUiVisibility: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        this.systemUiVisibility = this.systemUiVisibility and systemUiVisibility.inv()
    }
}

fun View.removeFromParent() {
    val parent = parent as? ViewGroup
    parent?.removeView(this)
}

fun View.setFullScreen(window: Window, enable: Boolean) {
    val controller = WindowCompat.getInsetsController(window, this)
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    // controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
    // controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    if (enable) {
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.hide(WindowInsetsCompat.Type.ime())
    } else {
        controller.show(WindowInsetsCompat.Type.statusBars())
        controller.show(WindowInsetsCompat.Type.navigationBars())
    }
}

val View.activity: Activity?
    get() = context as? Activity

fun View.asBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(
        width, height, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

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