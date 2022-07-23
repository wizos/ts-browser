package com.hinnka.tsbrowser.ui.composable.main.bottom

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.widget.EditText
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.persist.IconMap
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.widget.NewTextField
import com.hinnka.tsbrowser.ui.composable.widget.TSTextField
import com.hinnka.tsbrowser.ui.home.UIState
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddressTextField(modifier: Modifier, uiState: MutableState<UIState>) {
    val viewModel = LocalViewModel.current
    val text = viewModel.addressText
    val tab = TabManager.currentTab.value
    val url = tab?.urlState?.value
    val title = tab?.titleState?.value
    val icon = IconMap[url ?: ""]
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val focusRequester = FocusRequester()
    if (uiState.value != UIState.Search) {
        focusManager.clearFocus()
    }
    val context = LocalContext.current

    val placeholder = if (title?.isNotBlank() == true && title != url) {
        title
    } else if (URLUtil.isNetworkUrl(url)) {
        url
    } else
        stringResource(id = R.string.address_bar)

    fun onGo() {
        uiState.value = UIState.Main
        viewModel.onGo(text.value.text, context)
        text.value = TextFieldValue()
        focusManager.clearFocus()
    }

    TSTextField(
        modifier = modifier.focusRequester(focusRequester),
        text = text,
        placeholder = placeholder ?: stringResource(id = R.string.address_bar),
        onEnter = { onGo() },
        onFocusChanged = { state ->
            if (state.isFocused) {
                uiState.value = UIState.Search
            } else if (uiState.value == UIState.Search) {
                XLog.d("search mode enabled")
                scope.launch {
                    focusRequester.requestFocus()
                }
            }
        },
        leadingIcon = when (uiState.value) {
            UIState.Search -> {
                { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") }
            }
            else -> {
                if(icon!=null){ {
                    Image(
                        bitmap = icon.asImageBitmap(),
                        contentDescription = url,
                        modifier = Modifier.size(20.dp),
                    )
                }
                }else{
                    null
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go)
    )
}