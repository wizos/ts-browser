package com.hinnka.tsbrowser.ui

import ImeListener
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.composable.LabPage
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.composable.widget.page.PageContainer
import com.hinnka.tsbrowser.ui.composable.widget.TSBottomDrawer
import com.hinnka.tsbrowser.ui.theme.TSBrowserTheme

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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        XLog.d("onBackPressed")
        super.onBackPressed()
    }
}
