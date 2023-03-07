package com.hinnka.tsbrowser.ui.composable.main

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.webkit.WebView
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.IntOffset
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.BuildConfig
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.Schema
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.ext.*
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.tab.active
import com.hinnka.tsbrowser.ui.LocalViewModel
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.composable.widget.PopupMenu
import com.hinnka.tsbrowser.util.copy2Pictures
import com.hinnka.tsbrowser.util.save
import com.hinnka.tsbrowser.util.savePictures
import com.king.zxing.util.CodeUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import zlc.season.rxdownload4.download
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.task.Task
import java.io.File


@Composable
fun LongPressPopup() {
    val tabOpt by TabManager.currentTab
    val tab = tabOpt ?: return
    val density = LocalDensity.current
    val info by tab.longPressState

    PopupMenu(
        alignment = Alignment.TopStart,
        expanded = info.show,
        offset = density.run { IntOffset(info.xOffset, info.yOffset) },
        onDismissRequest = {
            info.hidePopup()
        },
    ) {

        when (info.type) {
            // a 标签 href 为 javascript: 的，类型为 UNKNOWN_TYPE
            // Email。href 为 mailto:wizos@qq.com, 此处值为：wizos@qq.com。但是 webview 拦截得到的url为 mailto:wizos@qq.com
            WebView.HitTestResult.EMAIL_TYPE -> {
                Copy(info)
            }
            // 拨号。href 为 tel:18333333333, 此处值为：18333333333。但是 webview 拦截得到的url为 tel:18333333333
            WebView.HitTestResult.PHONE_TYPE -> {
                Copy(info)
            }
            // 超链接
            WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                if (info.extra.startsWith(Schema.HTTP, true)
                    || info.extra.startsWith(Schema.HTTPS, true)
                    || info.extra.startsWith(Schema.VIEW_SOURCE, true)
                ){
                    OpenInNewTab(info)
                    OpenInBackgroundTab(info)
                }
                OpenWithExternalApp(info)
                CopyLink(info)
                ShareLink(info)
            }
            // 图片。无论是否带有链接，传的都是 src
            WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                if (info.extra.startsWith(Schema.HTTP, true)
                    || info.extra.startsWith(Schema.HTTPS, true)
                    || info.extra.startsWith(Schema.VIEW_SOURCE, true)
                ){
                    OpenInNewTab(info)
                    OpenInBackgroundTab(info)
                }
                OpenWithExternalApp(info)
                CopyLink(info)
                ShareLink(info)

                if (info.extra.startsWith(Schema.DATA, true)){
                    ShareImageData(info)
                    SaveImageData(info)
                    // SaveImageData2(info)
                    ParseQRImageData(info)
                } else {
                    ShareImage(info)
                    SaveImage(info)
                    ParseQRImage(info)
                }
            }
            else -> {
            }
        }
    }
}

@Composable
fun OpenInNewTab(info: LongPressInfo) {
    val context = LocalContext.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        TabManager.newTab(context).apply {
            loadUrl(info.extra)
            active()
        }
    }) {
        Text(text = stringResource(id = R.string.open_in_new_tab))
    }
}

@Composable
fun OpenInBackgroundTab(info: LongPressInfo) {
    val context = LocalContext.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        TabManager.newTab(context).apply {
            loadUrl(info.extra)
        }
    }) {
        Text(text = stringResource(id = R.string.open_in_background_tab))
    }
}

@Composable
fun OpenWithExternalApp(info: LongPressInfo) {
    val context = LocalContext.current
    val uri = Uri.parse(info.extra)
    var intent = Intent(Intent.ACTION_VIEW, uri)
    val activities: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

    val hideApp = setOf(BuildConfig.APPLICATION_ID)
    val targetIntents = ArrayList<Intent>()
    for (currentInfo in activities) {
        val packageName = currentInfo.activityInfo.packageName
        if (!hideApp.contains(packageName)) {
            val targetIntent = Intent(Intent.ACTION_VIEW, uri)
            targetIntent.setPackage(packageName)
            targetIntents.add(targetIntent)
        }
    }

    if (targetIntents.size != 0) {
        DropdownMenuItem(onClick = {
            info.hidePopup()
            intent = Intent.createChooser(targetIntents.removeAt(0), context.getString(R.string.open_with_external_app))
            if (targetIntents.size > 0){
                intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(arrayOf<Parcelable>()))
            }
            // 每次都要选择打开方式
            context.startActivity(intent)
        }) {
            Text(text = stringResource(id = R.string.open_with_external_app))
        }
    }
}

@Composable
fun CopyLink(info: LongPressInfo) {
    val clipboardManager = LocalClipboardManager.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        clipboardManager.setText(AnnotatedString(info.extra))
    }) {
        Text(text = stringResource(id = R.string.copy_link))
    }
}

@Composable
fun Copy(info: LongPressInfo) {
    val clipboardManager = LocalClipboardManager.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        clipboardManager.setText(AnnotatedString(info.extra))
    }) {
        Text(text = stringResource(id = R.string.copy))
    }
}

@Composable
fun ShareLink(info: LongPressInfo) {
    val viewModel = LocalViewModel.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        viewModel.share(info.extra)
    }) {
        Text(text = stringResource(id = R.string.share_link))
    }
}

@Composable
fun SaveImage(info: LongPressInfo) {
    val context = LocalContext.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        DownloadHandler(context).downloadImage(info.extra)
    }) {
        Text(text = stringResource(id = R.string.download_image))
    }
}
@Composable
fun SaveImageData(info: LongPressInfo) {
    val context = LocalContext.current
    val viewModel = LocalViewModel.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        ioScope.launch {
            val bytes:ByteArray? = info.extra.dataUrlToByteArray()
            val fileType = bytes?.ext() ?: ""
            if (bytes == null){
                MainScope().launch {
                    viewModel.snackBarHostState.showSnackbar(context.getString(R.string.the_image_is_abnormal))
                }
            }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), System.currentTimeMillis().toString() + fileType)
                file.save(bytes)
                MainScope().launch {
                    viewModel.snackBarHostState.showSnackbar(context.getString(R.string.download_succeeded))
                }
            }else{
                App.instance.cacheDir?.let {
                    val file = File(it, System.currentTimeMillis().toString() + fileType)
                    file.save(bytes)
                    if (file.exists()){
                        copy2Pictures(context, file)
                        MainScope().launch {
                            viewModel.snackBarHostState.showSnackbar(context.getString(R.string.download_succeeded))
                        }
                    }else{
                        MainScope().launch {
                            viewModel.snackBarHostState.showSnackbar(context.getString(R.string.download_failed))
                        }
                    }
                }
            }
        }
    }) {
        Text(text = stringResource(id = R.string.download_image))
    }
}

@Composable
fun ShareImage(info: LongPressInfo) {
    val viewModel = LocalViewModel.current
    val context = LocalContext.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        Task(info.extra, savePath = App.instance.cacheDir.path).download()
            .doOnComplete {
                viewModel.share(info.extra.file())
            }.doOnError {
                MainScope().launch {
                    viewModel.snackBarHostState.showSnackbar(context.getString(R.string.share_failed))
                }
            }.subscribe()
    }) {
        Text(text = stringResource(id = R.string.share_image))
    }
}
@Composable
fun ShareImageData(info: LongPressInfo) {
    val viewModel = LocalViewModel.current
    val context = LocalContext.current
    DropdownMenuItem(onClick = {
        info.hidePopup()
        ioScope.launch {
            val bitmap:Bitmap? = info.extra.dataUrlToBitmap()
            if (bitmap == null){
                MainScope().launch {
                    viewModel.snackBarHostState.showSnackbar(context.getString(R.string.the_image_is_abnormal))
                }
            }else{
                App.instance.cacheDir?.let {
                    val file = File(it, System.currentTimeMillis().toString())
                    savePictures(file, bitmap)
                    if (file.exists()){
                        file.ext()?.let { ext ->
                            file.renameTo(File(file.absolutePath + ext))
                        }
                        viewModel.share(file)
                    }
                }
            }
        }
    }) {
        Text(text = stringResource(id = R.string.share_image))
    }
}

@Composable
fun ParseQRImage(info: LongPressInfo) {
    TabManager.currentTab.value?.let{
        it.view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(it.view.drawingCache, 0, 0, it.view.drawingCache.width, it.view.drawingCache.height)
        val qrResult:String? = CodeUtils.parseCode(bitmap)
        it.view.destroyDrawingCache()
        if (!qrResult.isNullOrBlank()){
            val context = LocalContext.current
            val viewModel = LocalViewModel.current
            val clipboardManager = LocalClipboardManager.current
            DropdownMenuItem(onClick = {
                info.hidePopup()
                Task(info.extra, savePath = App.instance.cacheDir.path).download()
                    .doOnComplete {
                        val result:String? = CodeUtils.parseCode(info.extra.file().absolutePath)
                        XLog.d("二维码：$result")
                        if (result.isNullOrBlank()){
                            // Toast.makeText(context, context.getString(R.string.unable_to_recognize_the_qr_code), Toast.LENGTH_LONG).show()
                            MainScope().launch {
                                viewModel.snackBarHostState.showSnackbar(context.getString(R.string.unable_to_recognize_the_qr_code))
                            }
                        }else{
                            AlertBottomSheet.Builder(context).apply {
                                setTitle(context.getString(R.string.the_result_of_qr_code_recognition))
                                setMessage(result)
                                setPositiveButton(R.string.copy) {
                                    clipboardManager.setText(AnnotatedString(qrResult))
                                }
                                if(result.isUrl()){
                                    val url = result.toUrl()
                                    setNegativeButton(R.string.open) {
                                        TabManager.open(context, url, true)
                                    }
                                    val targetIntent = App.instance.queryExtraActivities(Uri.parse(url))
                                    if(targetIntent != null){
                                        // 每次都要选择打开方式
                                        App.instance.startActivity(targetIntent)
                                    }
                                    // val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    // val activities: List<ResolveInfo> = App.instance.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                                    // if (activities.isNotEmpty()){
                                    //     val targetIntents: ArrayList<Intent> = ArrayList()
                                    //     for (currentInfo in activities) {
                                    //         if(currentInfo.activityInfo.packageName.equals(App.instance.applicationInfo.packageName, true)){
                                    //             continue
                                    //         }
                                    //         targetIntents.add(Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(currentInfo.activityInfo.packageName))
                                    //     }
                                    //
                                    //     if (targetIntents.isNotEmpty()){
                                    //         val targetIntent = Intent.createChooser(
                                    //             targetIntents.removeAt(0),
                                    //             App.instance.getString(R.string.open_by_outer)
                                    //         ).apply {
                                    //             putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
                                    //             flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    //         }
                                    //         setNeutralButton(R.string.open_by_outer) {
                                    //             // 每次都要选择打开方式
                                    //             App.instance.startActivity(targetIntent)
                                    //         }
                                    //     }
                                    // }
                                }
                            }.show()
                        }
                    }.doOnError {
                        // Toast.makeText(context, context.getString(R.string.the_image_is_abnormal), Toast.LENGTH_LONG).show()
                        MainScope().launch {
                            viewModel.snackBarHostState.showSnackbar(context.getString(R.string.the_image_is_abnormal))
                        }
                    }.subscribe()
            }) {
                Text(text = stringResource(id = R.string.identify_qr_code))
            }
        }
    }
}


// @Composable
// fun ParseQRImage2(info: LongPressInfo) {
//     TabManager.currentTab.value?.let{
//         it.view.buildDrawingCache()
//         val bitmap = Bitmap.createBitmap(it.view.drawingCache, 0, 0, it.view.drawingCache.width, it.view.drawingCache.height)
//         val qrResult:String? = CodeUtils.parseCode(bitmap)
//         it.view.destroyDrawingCache()
//         if (!qrResult.isNullOrBlank()){
//             val context = LocalContext.current
//             val clipboardManager = LocalClipboardManager.current
//             DropdownMenuItem(onClick = {
//                 info.hidePopup()
//                 AlertBottomSheet.Builder(context).apply {
//                     setTitle(context.getString(R.string.the_result_of_qr_code_recognition))
//                     setMessage(qrResult)
//                     setPositiveButton(R.string.copy) {
//                         clipboardManager.setText(AnnotatedString(qrResult))
//                     }
//                     if(qrResult.isUrl()){
//                         val url = qrResult.toUrl()
//                         setNegativeButton(R.string.open) {
//                             TabManager.open(context, url, true)
//                         }
//                         val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                         val activities: List<ResolveInfo> = App.instance.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
//                         if (activities.isNotEmpty()){
//                             val targetIntents: ArrayList<Intent> = java.util.ArrayList()
//                             for (currentInfo in activities) {
//                                 if(currentInfo.activityInfo.packageName.equals(App.instance.applicationInfo.packageName, true)){
//                                     continue
//                                 }
//                                 targetIntents.add(Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(currentInfo.activityInfo.packageName))
//                                 XLog.d("内容1：" + currentInfo.activityInfo.packageName)
//                             }
//
//                             if (targetIntents.isNotEmpty()){
//                                 val targetIntent = Intent.createChooser(
//                                     targetIntents.removeAt(0),
//                                     App.instance.getString(R.string.open_by_outer)
//                                 ).apply {
//                                     putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
//                                     flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                                 }
//                                 setNeutralButton(R.string.open_by_outer) {
//                                     // 每次都要选择打开方式
//                                     App.instance.startActivity(targetIntent)
//                                 }
//                             }
//                         }
//                     }
//                 }.show()
//             }) {
//                 Text(text = stringResource(id = R.string.identify_qr_code))
//             }
//         }
//     }
// }

// @Composable
// fun ParseQRImage3(info: LongPressInfo) {
//     var qrResult by remember { mutableStateOf("")}
//     info.preview?.let{
//         ioScope.launch {
//             qrResult = CodeUtils.parseCode(it) ?: ""
//         }
//     }
//     if (qrResult.isNotBlank()){
//         val context = LocalContext.current
//         val clipboardManager = LocalClipboardManager.current
//         DropdownMenuItem(onClick = {
//             info.hidePopup()
//             AlertBottomSheet.Builder(context).apply {
//                 setTitle(context.getString(R.string.the_result_of_qr_code_recognition))
//                 setMessage(qrResult)
//                 setPositiveButton(android.R.string.copy) {
//                     clipboardManager.setText(AnnotatedString(qrResult))
//                 }
//                 if(qrResult.isUrl()){
//                     val url = qrResult.toUrl()
//                     setNegativeButton(R.string.open) {
//                         TabManager.open(context, url, true)
//                     }
//                     val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                     val activities: List<ResolveInfo> = App.instance.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
//                     if (activities.isNotEmpty()){
//                         setNeutralButton(R.string.open_by_outer) {
//                             // 每次都要选择打开方式
//                             App.instance.startActivity(
//                                 Intent.createChooser(
//                                     intent,
//                                     App.instance.getString(R.string.open_by_outer)
//                                 ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
//                             )
//                         }
//                     }
//                 }
//             }.show()
//         }) {
//             Text(text = stringResource(id = R.string.identify_qr_code))
//         }
//     }
// }

@Composable
fun ParseQRImageData(info: LongPressInfo) {
    TabManager.currentTab.value?.let{
        var qrResult by remember { mutableStateOf("")}
        ioScope.launch {
            val bitmap:Bitmap? = info.extra.dataUrlToBitmap()
            if (bitmap != null){
                qrResult = CodeUtils.parseCode(bitmap) ?: ""
            }else{
                qrResult = ""
            }
        }

        if (qrResult.isNotBlank()){
            val context = LocalContext.current
            val clipboardManager = LocalClipboardManager.current
            DropdownMenuItem(onClick = {
                info.hidePopup()
                AlertBottomSheet.Builder(context).apply {
                    setTitle(context.getString(R.string.the_result_of_qr_code_recognition))
                    setMessage(qrResult)
                    setPositiveButton(R.string.copy) {
                        clipboardManager.setText(AnnotatedString(qrResult))
                    }
                    if(qrResult.isUrl()){
                        val url = qrResult.toUrl()
                        setNegativeButton(R.string.open) {
                            TabManager.open(context, qrResult.toUrl(), true)
                        }
                        val targetIntents: MutableList<Intent> = App.instance.queryExtraActivityIntents(Uri.parse(url))
                        if (targetIntents.isNotEmpty()){
                            val targetIntent = Intent.createChooser(
                                targetIntents.removeAt(0),
                                App.instance.getString(R.string.open_by_outer)
                            ).apply {
                                putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            setNeutralButton(R.string.open_by_outer) {
                                // 每次都要选择打开方式
                                App.instance.startActivity(targetIntent)
                            }
                        }
                    }
                }.show()
            }) {
                Text(text = stringResource(id = R.string.identify_qr_code))
            }
        }
    }
}