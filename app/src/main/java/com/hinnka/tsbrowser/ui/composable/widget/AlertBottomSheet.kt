package com.hinnka.tsbrowser.ui.composable.widget

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class AlertBottomSheet(private val params: Params) {

    fun show() {
        drawerState.cancelable = params.cancelable
        drawerState.open {
            val scope = rememberCoroutineScope()
            Column(Modifier.padding(16.dp)) {
                if (params.title.isNotBlank()) {
                    Text(text = params.title, style = MaterialTheme.typography.h6, color = MaterialTheme.colors.onSurface)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .padding(vertical = 16.dp), contentAlignment = Alignment.CenterStart
                ) {
                    if(params.view != null){
                        params.view?.let { view ->
                            Box(modifier = Modifier
                                .heightIn(max = 400.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                )) {
                                view()
                            }
                        }
                    }else{
                        Text(
                            modifier = Modifier.alpha(0.8f),
                            text = params.message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onSurface,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.End, modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    if (params.negativeString.isNotBlank()) {
                        TextButton(onClick = {
                            params.negativeBlock()
                            scope.launch {
                                drawerState.close()
                            }
                        }) {
                            Text(text = params.negativeString, color = MaterialTheme.colors.onSurface)
                        }
                    }
                    if (params.positiveString.isNotBlank()) {
                        Button(modifier = Modifier.padding(start = 8.dp), onClick = {
                            params.positiveBlock()
                            scope.launch {
                                drawerState.close()
                            }
                        }) {
                            Text(text = params.positiveString, color = MaterialTheme.colors.onPrimary)
                        }
                    }
                }
            }
        }
    }

    data class Params(
        var title: String = "",
        var view: (@Composable () -> Unit)? = null,
        var message: String = "",
        var positiveString: String = "",
        var positiveBlock: () -> Unit = {},
        var negativeString: String = "",
        var negativeBlock: () -> Unit = {},
        var cancelable: Boolean = false,
    )

    class Builder(val context: Context) {
        private val params = Params()

        fun setTitle(@StringRes res: Int) {
            params.title = context.getString(res)
        }

        fun setTitle(title: String) {
            params.title = title
        }

        fun setView(view: @Composable () -> Unit) {
            params.view = view
        }

        fun setMessage(message: String) {
            params.message = message
        }

        fun setMessage(@StringRes res: Int) {
            params.message = context.getString(res)
        }

        fun setPositiveButton(@StringRes res: Int, block: () -> Unit) {
            params.positiveString = context.getString(res)
            params.positiveBlock = block
        }

        fun setNegativeButton(@StringRes res: Int, block: () -> Unit = {}) {
            params.negativeString = context.getString(res)
            params.negativeBlock = block
        }

        fun setCancelable(cancelable: Boolean) {
            params.cancelable = cancelable
        }

        fun show() {
            val sheet = AlertBottomSheet(params)
            sheet.show()
        }
    }

    companion object {
        val drawerState = BottomDrawerState()

        fun open(showScrim: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
            drawerState.open(showScrim, content)
        }

        suspend fun close() {
            drawerState.close()
        }

        suspend fun show(message: String,
                         actionLabel: String? = null,
                         duration: SnackbarDuration = SnackbarDuration.Long, onClick:() -> Unit = {}){
            drawerState.show(message, actionLabel, duration, onClick)
        }
    }
}