package com.hinnka.tsbrowser.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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
    useDynamicColors: Boolean = true,
    // enableEdgeToEdge: (Boolean, Int, Int) -> Unit,
    content: @Composable() () -> Unit
) {
    logD("TSBrowserTheme start")


    val useDarkTheme = App.isSecretMode || Settings.darkModeState.value
    val colors = if (useDarkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    // val darkIcons = MaterialTheme.colors.isLight
    // val darkIcons = colors == LightColorPalette

    // val colorScheme = when {
    //     useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
    //         val context = LocalContext.current
    //         if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    //     }
    //     useDarkTheme -> darkColorScheme()
    //     else -> lightColorScheme()
    // }

    // https://stackoverflow.com/questions/65610216/how-to-change-statusbar-color-in-jetpack-compose
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity  = view.context as Activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // activity.window.navigationBarColor = colorScheme.primary.copy(alpha = 0.08f).compositeOver(colorScheme.surface.copy()).toArgb()
                activity.window.navigationBarColor = colors.primary.copy(alpha = 0.08f).compositeOver(colors.surface.copy()).toArgb()
                // activity.window.statusBarColor = colorScheme.background.toArgb()
                activity.window.statusBarColor = colors.background.toArgb()
                WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !useDarkTheme
                WindowCompat.getInsetsController(activity.window, view).isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }
    // val systemUiController = rememberSystemUiController()
    // DisposableEffect(systemUiController) {
    //     // navigationBarContrastEnforced - 当需要完全透明的背景时，系统是否应该确保导航栏有足够的对比度。仅在API 29+上支持。
    //     systemUiController.setSystemBarsColor(
    //         color = colors.background,
    //         // darkIcons = colorScheme.background.luminance() > 0.5,
    //         // isNavigationBarContrastEnforced = useDarkTheme,
    //         // transformColorForLightContent = { Color.Black.copy(alpha = 0.6F) }
    //     )
    //     // 该属性保存状态和导航栏图标+内容是否为“黑色”。
    //     systemUiController.systemBarsDarkContentEnabled = !useDarkTheme
    //     // enableEdgeToEdge.invoke(useDarkTheme, lightScrim, darkScrim)
    //     onDispose {}
    // }

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

    CompositionLocalProvider {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    // .imePadding()
                    ,
                    color = colors.background,
                    contentColor = colors.onBackground,
                    content = content
                )
            }
        )
    }
    // MaterialTheme(
    //     colors = colors,
    //     typography = Typography,
    //     shapes = Shapes,
    //     content = content
    // )

}