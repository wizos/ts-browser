package com.hinnka.tsbrowser.ui

import ImeListener
import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.toUrl
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.LocalStorage
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.AppViewModel
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.composable.LabPage
import com.hinnka.tsbrowser.ui.composable.bookmark.AddFolder
import com.hinnka.tsbrowser.ui.composable.bookmark.BookmarkPage
import com.hinnka.tsbrowser.ui.composable.bookmark.EditBookmark
import com.hinnka.tsbrowser.ui.composable.download.DownloadPage
import com.hinnka.tsbrowser.ui.composable.history.HistoryPage
import com.hinnka.tsbrowser.ui.composable.main.MainPage
import com.hinnka.tsbrowser.ui.composable.settings.SettingsPage
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.composable.widget.page.PageContainer
import com.hinnka.tsbrowser.ui.composable.widget.page.PageController
import com.hinnka.tsbrowser.ui.composable.widget.TSBottomDrawer
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme
import com.king.zxing.CameraScan
import kotlinx.coroutines.launch

open class LabActivity: BaseActivity() {
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logD("LabActivity onCreate")

        // 状态栏、导航栏沉浸式设置
        // 第二参数decorFitsSystemWindows表示是否沉浸，false 表示沉浸，true表示不沉浸
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Providers {
                TSBrowserTheme {
                    PageContainer("lab") {
                        page("lab") { LabPage() }
                    }
                    TSBottomDrawer(drawerState = AlertBottomSheet.drawerState)
                    ImeListener()
                }
            }
        }

        logD("LabActivity onCreate complete")
    }

    @Composable
    fun Providers(content: @Composable () -> Unit) {
        logD("Providers start")
        CompositionLocalProvider(
            LocalViewModel provides viewModel,
            LocalLifecycleOwner provides this,
            content = content
        )
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        XLog.d("onBackPressed")
        super.onBackPressed()
    }
}
