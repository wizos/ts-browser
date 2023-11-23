package com.hinnka.tsbrowser.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.hinnka.tsbrowser.ui.composable.ViewHostState

val LazyViewHostState by lazy { ViewHostState() }
val LocalViewHostState = staticCompositionLocalOf<ViewHostState> {
    error("no ViewHostState provided")
}