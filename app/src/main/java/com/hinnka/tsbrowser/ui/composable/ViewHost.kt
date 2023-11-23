package com.hinnka.tsbrowser.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun ViewHost(
    viewHostState: ViewHostState = remember { ViewHostState() },
) {
    val isShow by viewHostState.isShow
    if (isShow) {
        viewHostState.view()?.invoke()
    }else{
        LocalFocusManager.current.clearFocus()
    }
}

class ViewHostState {
    private var _view: @Composable (() -> Unit)? = null
    private var _isShow = mutableStateOf(false)
    val isShow: State<Boolean> = _isShow

    fun view(): @Composable (() -> Unit)? {
        return _view
    }
    fun show(content: @Composable () -> Unit) {
        _view = content
        _isShow.value = true
    }
    fun close() {
        _isShow.value = false
        _view = null
    }
}
