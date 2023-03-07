package com.hinnka.tsbrowser.ui.composable

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ui.composable.widget.BottomDrawerState
import com.hinnka.tsbrowser.ui.composable.widget.TSAppBar
import com.hinnka.tsbrowser.ui.composable.widget.TSBottomDrawer

val LocalMainDrawerState = staticCompositionLocalOf<BottomDrawerState> {
    error("main drawer state not found")
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun LabPage() {
    logD("MainPage start")
    val mainDrawerState = remember { BottomDrawerState() }

    TSBottomDrawer(drawerState = mainDrawerState) {
        CompositionLocalProvider(LocalMainDrawerState provides mainDrawerState) {
            Column(Modifier.background(MaterialTheme.colors.surface)) {
                TSAppBar(title = stringResource(id = R.string.lab))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Text(
                    //     text = stringResource(id = R.string.general),
                    //     modifier = Modifier.padding(16.dp),
                    //     color = MaterialTheme.colors.primary,
                    //     fontSize = 13.sp
                    // )

                    // ListItem(
                    //     secondaryText = {
                    //         Text(
                    //             text = if (LabSettings.dokitState.value) {
                    //                 stringResource(id = R.string.on)
                    //             } else {
                    //                 stringResource(id = R.string.off)
                    //             }
                    //         )
                    //     },
                    //     text = { Text(text = stringResource(id = R.string.dokit)) },
                    //     trailing = {
                    //         Switch(checked = LabSettings.dokitState.value, onCheckedChange = {
                    //             LabSettings.isEnableDokit = it
                    //             if(it){
                    //                 DoKit.show()
                    //             }else{
                    //                 DoKit.hide()
                    //             }
                    //         })
                    //     }
                    // )
                }
            }
        }
    }
    // Welcome()
    XLog.d("MainPage end")
}
