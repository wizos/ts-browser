package com.hinnka.tsbrowser.ui.composable.main

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.removeFromParent
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.main.bottom.BottomBar
import com.hinnka.tsbrowser.ui.composable.welcome.SecretWelcome
import com.hinnka.tsbrowser.ui.composable.widget.*
import com.hinnka.tsbrowser.ui.home.UIState
import kotlinx.coroutines.delay

val LocalMainDrawerState = staticCompositionLocalOf<BottomDrawerState> {
    error("main drawer state not found")
}

@Composable
fun MainPage() {
    logD("MainPage start")
    val mainDrawerState = remember { BottomDrawerState() }

    TSBottomDrawer(drawerState = mainDrawerState) {
        CompositionLocalProvider(LocalMainDrawerState provides mainDrawerState) {
            Column {
                // StatusBar()
                MainView()
            }
        }
    }
    // Welcome()
    XLog.d("MainPage end")
}

@Composable
fun Welcome() {
    var showSecret by remember { mutableStateOf(false) }
    val mnemonicNotSet = Settings.mnemonicState.value == null
    LaunchedEffect(key1 = mnemonicNotSet) {
        delay(500)
        showSecret = mnemonicNotSet
    }
    AnimatedVisibility(
        visible = showSecret,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(stiffness = 250f)
        ),
        exit = fadeOut() + slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(stiffness = 250f)
        )
    ) {
        TSBackHandler(onBack = {}) {
            SecretWelcome()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainView() {
    logD("MainView start")
    // val tab = TabManager.currentTab.value
    // val viewModel = LocalViewModel.current

    Column(modifier = Modifier.fillMaxSize().imePadding().navigationBarsPadding()) {
        Box(modifier = Modifier.weight(1f)) {
            WebView()
            NewTabView()
            CoverView()

            SnackbarHost(modifier = Modifier.align(Alignment.BottomCenter), hostState = App.snackBarHostState)
        }
        BottomBar()
    }
    logD("MainView end")
}
@Composable
fun WebView() {
    val tab = TabManager.currentTab.value
    val context = LocalContext.current
    TSBackHandler(
        enabled = tab?.canGoBackState?.value == true || (TabManager.tabs.size > 1 && tab?.isHome == true), //TabManager.tabs.size > 1 || tab?.isHome == false,
        onBack = {
            // tab?.onBackPressed()

            tab?.let {
                if (it.canGoBackState.value){
                    it.onBackPressed()
                }else{
                    if (it.isHome){
                        val index = TabManager.tabs.indexOf(tab)
                        TabManager.remove(it)
                        when {
                            TabManager.tabs.size > index -> {
                                TabManager.tabs[index].active()
                            }
                            TabManager.tabs.isNotEmpty() -> {
                                TabManager.tabs
                                    .last()
                                    .active()
                            }
                            else -> {
                            }
                        }
                    }else{
                        // it.goHome()
                        // it.view.clearHistory()
                        val index = TabManager.tabs.indexOf(tab) - 1
                        if (index < 0){
                            when {
                                TabManager.tabs.isNotEmpty() -> {
                                    TabManager.tabs
                                        .last()
                                        .active()
                                }
                                else -> {
                                    // TabManager
                                    //     .newTab(context)
                                    //     .apply {
                                    //         goHome()
                                    //         active()
                                    //     }
                                }
                            }
                        }else{
                            when {
                                TabManager.tabs.size > index -> {
                                    TabManager.tabs[index].active()
                                }
                                TabManager.tabs.isNotEmpty() -> {
                                    TabManager.tabs
                                        .last()
                                        .active()
                                }
                                else -> {
                                }
                            }
                        }

                    }
                }
            }

        }
    ) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                FrameLayout(it)
            },
            update = { tabContainer ->
                tab?.let {
                    tabContainer.removeAllViews()
                    it.view.removeFromParent()
                    tabContainer.addView(it.view)
                }
            }
        )
        LongPressPopup()
    }

}

@Composable
fun NewTabView() {
    val tab = TabManager.currentTab.value ?: return
    val viewModel = LocalViewModel.current
    val uiState = viewModel.uiState
    if (uiState.value == UIState.Main && tab.isHome) {
        HomePage()
    }
}

@Composable
fun CoverView() {
    val viewModel = LocalViewModel.current
    val uiState = viewModel.uiState
    TSBackHandler(
        enabled = uiState.value != UIState.Main,
        onBack = { viewModel.uiState.value = UIState.Main }) {
        when (uiState.value) {
            UIState.Search -> SearchList()
            UIState.TabList -> TabList()
            else -> {
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProgressIndicator() {
    val currentTab = TabManager.currentTab
    val progressState = currentTab.value?.progressState
    val newProgress = progressState?.value ?: 0f
    val progress: Float = if (newProgress > 0) {
        animateFloatAsState(targetValue = newProgress).value
    } else {
        newProgress
    }
    AnimatedVisibility(visible = progress > 0f && progress < 1f) {
        LinearProgressIndicator(
            progress = progress,
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )
    }
}
