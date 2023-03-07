package com.hinnka.tsbrowser.ui.home

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

open class MainActivity: BaseActivity() {

    private val viewModel by viewModels<AppViewModel>()

    lateinit var videoLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logD("MainActivity onCreate")

        // 状态栏、导航栏沉浸式设置
        // 第二参数decorFitsSystemWindows表示是否沉浸，false 表示沉浸，true表示不沉浸
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Providers {
                TSBrowserTheme {
                    PageContainer("main") {
                        page("main") { MainPage() }
                        page("downloads") { DownloadPage() }
                        page("bookmarks") { BookmarkPage() }
                        page("history") { HistoryPage() }
                        page("settings") { SettingsPage() }
                        page("addFolder") { AddFolder(it?.get(0) as Bookmark) }
                        page("editBookmark") { EditBookmark(it?.get(0) as Bookmark) }
                    }
                    TSBottomDrawer(drawerState = AlertBottomSheet.drawerState)
                    AndroidView(factory = { context ->
                        FrameLayout(context).apply {
                            isVisible = false
                            videoLayout = this
                            videoLayout.setBackgroundColor(Color.BLACK);
                        }
                    })
                    ImeListener()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            TabManager.loadTabs(this@MainActivity)
            handleIntent(intent)
        }


        logD("MainActivity onCreate complete")
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

    @Composable
    fun ImeListener() {
        val viewModel = LocalViewModel.current
        val scope = rememberCoroutineScope()

        val view = LocalView.current
        view.setOnApplyWindowInsetsListener { v, insets ->
            val imeHeight =
                WindowInsetsCompat.toWindowInsetsCompat(insets).getInsets(
                    WindowInsetsCompat.Type.ime()
                )
            scope.launch {
                viewModel.imeHeightState.animateTo(imeHeight.bottom.toFloat())
            }
            insets
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        logD(
            "handleIntent: ${intent.action} ${
                intent.extras?.keySet()?.joinToString(",")
            } ${intent.data}"
        )
        when (intent.action) {
            Intent.ACTION_WEB_SEARCH -> {
                intent.getStringExtra(SearchManager.QUERY)?.also {
                    val appId = intent.getStringExtra(Browser.EXTRA_APPLICATION_ID)
                    // ACTION_WEB_SEARCH 的 EXTRA_NEW_SEARCH 数据关键字可以让你在新的浏览器tab中打开一个搜索，而不是在本tab中
                    val newSearch = intent.getBooleanExtra(SearchManager.EXTRA_NEW_SEARCH, false)
                    XLog.d("搜索A: $it $appId $newSearch")
                    handle(it, appId != packageName || newSearch)
                }

            }
            Intent.ACTION_PROCESS_TEXT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.also {
                        handle(it, false)
                    }
                }
            }
            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.also {
                    val appId = intent.getStringExtra(Browser.EXTRA_APPLICATION_ID)
                    val newSearch = intent.getBooleanExtra(SearchManager.EXTRA_NEW_SEARCH, false)
                    XLog.d("搜索B: $it $appId $newSearch")
                    handle(it, appId != packageName || newSearch)
                }
            }

            Intent.ACTION_VIEW -> {
                intent.data?.toString()?.also {
                    val appId = intent.getStringExtra(Browser.EXTRA_APPLICATION_ID)
                    XLog.d("搜索C: $it $appId")
                    handle(it, appId != packageName)
                }
            }

            "navigate" -> intent.getStringExtra("route")?.let {
                if (PageController.currentRoute.value != it) {
                    PageController.navigate(it)
                }
            }
            "shortcut" -> when (intent.getStringExtra("shortcutId")) {
                "new_tab" -> TabManager.newTab(this).apply {
                    goHome()
                    active()
                }
                "incognito" -> {
                    Settings.incognito = true
                    TabManager.newTab(this).apply {
                        goHome()
                        active()
                    }
                }
            }
        }
    }

    private fun handle(text: String, newTab: Boolean = false) {
        lifecycleScope.launchWhenResumed {
            TabManager.open(this@MainActivity, text.toUrl(), newTab)
        }
    }

    override fun onResume() {
        super.onResume()
        TabManager.onResume(viewModel.uiState.value)
    }

    override fun onPause() {
        super.onPause()
        TabManager.onPause()
    }

    override fun onBackPressed() {
        XLog.d("onBackPressed")
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        XLog.d("MainActivity 结果 $requestCode $resultCode")
        if (requestCode == viewModel.secretRequestCode && resultCode == RESULT_OK) {
            LocalStorage.isSecretVisited = true
            viewModel.updateDefaultBrowserBadgeState()
        }

        if (requestCode == viewModel.captureRequestCode && resultCode == RESULT_OK){
            val result = CameraScan.parseScanResult(data)
            if (!result.isNullOrBlank()){
                TabManager.open(this@MainActivity, result.toUrl())
            }
        }
    }
}
