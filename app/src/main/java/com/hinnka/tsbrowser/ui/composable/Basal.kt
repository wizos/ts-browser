import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat
import com.hinnka.tsbrowser.ui.LocalViewModel
import kotlinx.coroutines.launch

/*
 * Copyright (c) 2022 wizos
 * 项目：LoreadCompose
 * 邮箱：wizos@qq.com
 * 创建时间：2022-06-11 11:43:28
 */

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