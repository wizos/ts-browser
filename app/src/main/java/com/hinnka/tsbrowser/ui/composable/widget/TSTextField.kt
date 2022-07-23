package com.hinnka.tsbrowser.ui.composable.widget

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.ui.home.UIState
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TSTextField(
    modifier: Modifier = Modifier,
    text: MutableState<TextFieldValue>,
    placeholder: String = "",
    onEnter: () -> Unit = {},
    onFocusChanged: (FocusState) -> Unit = {},
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(onGo = { onEnter() }),
    onValueChanged: () -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        OutlinedTextField(
            value = text.value,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = Color.Black,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { event ->
                    if (event.key == Key.Enter) {
                        onEnter()
                        return@onKeyEvent true
                    }
                    false
                }
                .onFocusChanged { state ->
                    onFocusChanged(state)
                },
            leadingIcon = leadingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            onValueChange = {
                text.value = it
                onValueChanged()
            },
        )
    }
}



@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NewTextField(
    modifier: Modifier = Modifier,
    text: MutableState<TextFieldValue>,
    placeholder: String = "",
    onEnter: () -> Unit = {},
    onFocusChanged: (FocusState) -> Unit = {},
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(onGo = { onEnter() }),
    onValueChanged: () -> Unit = {}
) {

    val holder = remember { mutableStateOf(placeholder) }
    val darkMode = remember { Settings.darkModeState }
    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { event ->
                    if (event.key == Key.Enter) {
                        onEnter()
                        return@onKeyEvent true
                    }
                    false
                }
                .onFocusChanged { onFocusChanged(it) },
            factory = { context ->
                val editText = EditText(context)
                editText.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                // editText.backgroundTintList =
                //     ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
                editText.setText(text.value.text)
                editText.maxLines = 1
                editText.setSingleLine()
                editText.inputType = 0x00000001
                editText.imeOptions = EditorInfo.IME_ACTION_GO
                editText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        //执行对应的操作
                        onEnter()
                        true
                    } else false
                }
                // editText.textSize = 13.dp
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }
                    override fun afterTextChanged(s: Editable?) {
                        text.value = TextFieldValue(s.toString())
                    }
                })
                editText
            },
        ){
            it.hint = holder.value
            if (darkMode.value) {
                it.setTextColor(android.graphics.Color.WHITE)
                it.setHintTextColor(8947848)
            } else {
                it.setTextColor(android.graphics.Color.BLACK)
                it.setHintTextColor(10395294)
            }
        }
    }
}