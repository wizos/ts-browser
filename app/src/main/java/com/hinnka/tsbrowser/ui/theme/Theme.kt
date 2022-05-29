package com.hinnka.tsbrowser.ui.theme

import android.annotation.SuppressLint
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.persist.Settings

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary = Teal200,
    primaryVariant = Color.Black,
    secondary = Teal200,
    onPrimary = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
)

private val LightColorPalette = lightColors(
    primary = Teal200,
    primaryVariant = Teal700,
    secondary = Teal200,
    surface = Color.White,
)

val Colors.primaryLight: Color get() = if (isLight) PrimaryWhite else PrimaryDark


@Composable
fun TSBrowserTheme(content: @Composable() () -> Unit) {
    logD("TSBrowserTheme start")
    val colors = if (App.isSecretMode || Settings.darkModeState.value) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    // val darkIcons = MaterialTheme.colors.isLight
    val darkIcons = colors == LightColorPalette
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = darkIcons,
            isNavigationBarContrastEnforced = false // 当需要完全透明的背景时，系统是否应该确保导航栏有足够的对比度。仅在API 29+上支持。
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}