package com.hinnka.tsbrowser.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.persist.Settings

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary = Teal200,
    primaryVariant = Color.Black,
    onPrimary = Color.White,
    secondary = Teal200,
    surface = Color.Black,
    onSurface = Color.White,
    background = Color.Black,
    onBackground = Color.White,
)

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
    primary = Teal200,
    primaryVariant = Teal700,
    onPrimary = Color.White,
    secondary = Teal200,
    surface = Color.White,
    onSurface = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
)

val Colors.primaryLight: Color get() = if (isLight) PrimaryWhite else PrimaryDark


@Composable
fun TSBrowserTheme(
    // Dynamic color is available on Android 12+
    // useDynamicColors: Boolean = true,
    content: @Composable() () -> Unit
) {
    logD("TSBrowserTheme start")
    // val colors = if (App.isSecretMode || Settings.darkModeState.value) {
    //     DarkColorPalette
    // } else {
    //     LightColorPalette
    // }
    //
    // // val darkIcons = MaterialTheme.colors.isLight
    // val darkIcons = colors == LightColorPalette
    // val systemUiController = rememberSystemUiController()
    // SideEffect {
    //     systemUiController.setSystemBarsColor(
    //         color = Color.Transparent,
    //         darkIcons = darkIcons,
    //         isNavigationBarContrastEnforced = false // 当需要完全透明的背景时，系统是否应该确保导航栏有足够的对比度。仅在API 29+上支持。
    //     )
    // }
    //
    // MaterialTheme(
    //     colors = colors,
    //     typography = Typography,
    //     shapes = Shapes,
    //     content = content
    // )

    val useDarkTheme = App.isSecretMode || Settings.darkModeState.value
    val colors = if (App.isSecretMode || Settings.darkModeState.value) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    // val darkIcons = MaterialTheme.colors.isLight
    // val darkIcons = colors == LightColorPalette
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController, colors) {
        // navigationBarContrastEnforced - 当需要完全透明的背景时，系统是否应该确保导航栏有足够的对比度。仅在API 29+上支持。
        systemUiController.setSystemBarsColor(
            color = colors.background,
            // darkIcons = colorScheme.background.luminance() > 0.5,
            // isNavigationBarContrastEnforced = useDarkTheme,
            // transformColorForLightContent = { Color.Black.copy(alpha = 0.6F) }
        )
        // 该属性保存状态和导航栏图标+内容是否为“黑色”。
        systemUiController.systemBarsDarkContentEnabled = !useDarkTheme
        onDispose {}
    }

    CompositionLocalProvider {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                    // .statusBarsPadding()
                    // .navigationBarsPadding()
                    // .imePadding()
                    ,
                    color = colors.background,
                    contentColor = colors.onBackground,
                    content = content
                )
            }
        )
    }
    // SideEffect {
    //     systemUiController.setSystemBarsColor(
    //         color = Color.Transparent,
    //         darkIcons = darkIcons,
    //         isNavigationBarContrastEnforced = false // 当需要完全透明的背景时，系统是否应该确保导航栏有足够的对比度。仅在API 29+上支持。
    //     )
    // }
    //
    // MaterialTheme(
    //     colors = colors,
    //     typography = Typography,
    //     shapes = Shapes,
    //     content = content
    // )

    // val useDarkTheme = App.isSecretMode || Settings.darkModeState.value
    //
    // val colorScheme = when {
    //     useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
    //         val context = LocalContext.current
    //         if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    //     }
    //     useDarkTheme -> darkColorScheme()
    //     else -> lightColorScheme()
    // }
    //
    // val systemUiController = rememberSystemUiController()
    // DisposableEffect(systemUiController, useDarkTheme) {
    //     // navigationBarContrastEnforced - 当需要完全透明的背景时，系统是否应该确保导航栏有足够的对比度。仅在API 29+上支持。
    //     systemUiController.setSystemBarsColor(
    //         color = colorScheme.background,
    //         // darkIcons = colorScheme.background.luminance() > 0.5,
    //         // isNavigationBarContrastEnforced = useDarkTheme,
    //         // transformColorForLightContent = { Color.Black.copy(alpha = 0.6F) }
    //     )
    //     // 该属性保存状态和导航栏图标+内容是否为“黑色”。
    //     systemUiController.systemBarsDarkContentEnabled = !useDarkTheme
    //     onDispose {}
    // }
    //
    // CompositionLocalProvider(LocalSystemUiController provides systemUiController) {
    //     MaterialTheme(
    //         colorScheme = colorScheme,
    //         typography = Typography,
    //         content = {
    //             // A surface container using the 'background' color from the theme
    //             Surface(
    //                 modifier = Modifier.fillMaxSize()
    //                 .statusBarsPadding()
    //                 // .navigationBarsPadding()
    //                 // .imePadding()
    //                 ,
    //                 color = colorScheme.background,
    //                 contentColor = colorScheme.onBackground,
    //                 content = content
    //             )
    //         }
    //     )
    // }
}