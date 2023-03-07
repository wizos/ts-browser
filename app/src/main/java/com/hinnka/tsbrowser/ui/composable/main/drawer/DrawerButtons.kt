package com.hinnka.tsbrowser.ui.composable.main.drawer

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.BookmarkType
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel
import kotlinx.coroutines.launch


@Composable
fun RowScope.BackButton() {
    val tab by TabManager.currentTab
    IconButton(
        onClick = {
            tab?.onBackPressed()
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        enabled = tab?.canGoBackState?.value == true,
    ) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
    }
}

@Composable
fun RowScope.ForwardButton() {
    val tab by TabManager.currentTab
    IconButton(
        onClick = {
            tab?.goForward()
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        enabled = tab?.canGoForwardState?.value == true,
    ) {
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Forward")
    }
}

@Composable
fun RowScope.ShareButton(drawerState: com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState) {
    val viewModel = LocalViewModel.current
    val tab by TabManager.currentTab
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            viewModel.share(tab?.urlState?.value ?: "")

            scope.launch {
                drawerState.close()
            }
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        enabled = !tab?.urlState?.value.equals("about:blank"),
    ) {
        Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share")
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RowScope.AddBookmarkButton(drawerState: com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState) {
    val tab by TabManager.currentTab
    val url = tab?.urlState?.value ?: return
    val title = tab?.titleState?.value ?: stringResource(id = R.string.untiled)
    // val bookmark = remember { mutableStateOf(Bookmark.findByUrl(url)) }
    var bookmark = Bookmark.findByUrl(url)

    val scope = rememberCoroutineScope()
    IconButton(
        onClick = {
            if (bookmark == null) {
                bookmark = Bookmark(
                    url = url,
                    name = title,
                    type = BookmarkType.Url
                ).apply {
                    Bookmark.root.addChild(this)
                }
            } else {
                bookmark?.remove()
                bookmark = null
            }
            scope.launch {
                drawerState.close()
            }
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        enabled = !tab?.urlState?.value.equals("about:blank"),
    ) {
        if(!tab?.urlState?.value.equals("about:blank")){
            Icon(
                imageVector = if (bookmark != null) Icons.Default.BookmarkAdded else Icons.Outlined.BookmarkAdd,
                contentDescription = "Bookmark",
                tint = if (bookmark != null) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
            )
        }else{
            Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = "Bookmark")
        }
    }
}