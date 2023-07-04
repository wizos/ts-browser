package com.hinnka.tsbrowser.ui.composable.main.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TSDrawer(drawerState: BottomDrawerState) {
    PrivacyContent()

    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        BackButton(drawerState = drawerState)
        // val tab by TabManager.currentTab
        // if (tab?.isHome == false){
            HomeButton(drawerState = drawerState)
        // } else {
        //     ForwardButton(drawerState = drawerState)
        // }
        AddBookmarkButton(drawerState = drawerState)
        ShareButton(drawerState = drawerState)
    }

    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.padding(8.dp)) {
        item { DarkModeItem(drawerState) }
        item {
            if (App.isSecretMode) {
                SecretItem()
            } else {
                IncognitoItem(drawerState)
            }
        }
        item { FindInPageItem(drawerState = drawerState) }
        item { DesktopItem(drawerState) }
        item { BookmarksItem() }
        item { HistoryItem() }
        item { DownloadsItem() }
        item { SettingsItem() }
    }
}
