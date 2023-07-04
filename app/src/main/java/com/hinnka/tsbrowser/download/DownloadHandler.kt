package com.hinnka.tsbrowser.download

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.text.format.Formatter.formatFileSize
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.*
import com.hinnka.tsbrowser.tab.TabManager
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.util.HttpsUtils
import com.hinnka.tsbrowser.util.NotificationUtils
import com.hinnka.tsbrowser.util.copy2PublicDir
import com.hinnka.tsbrowser.util.save
import com.jeffmony.downloader.VideoDownloadManager
import com.jeffmony.downloader.model.VideoTaskItem
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import zlc.season.rxdownload4.RANGE_CHECK_HEADER
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.manager.*
import zlc.season.rxdownload4.task.Task
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

class DownloadHandler(val context: Context) : DownloadListener {

    companion object {
        val showDownloadingBadge = mutableStateOf(false)
    }

    init {
        OpenReceiver.register(context)
    }

    fun onDownloadWithDialog(url: String, userAgent: String?, contentDisposition: String?, mimetype: String?, contentLength: Long) {
        XLog.d("下载：$url, $contentDisposition, $mimetype, $contentLength")
        // if (tryOpenStream(url, contentDisposition, mimetype)) {
        //     return
        // }

        ioScope.launch {
            val guessName:String = if(url.isDataUrl()){
                url.hashCode().toString() + url.dataUrlToByteArray()?.ext()
            }else{
                guessFileName(url, contentDisposition, mimetype)
            }

            var downloadSize by mutableStateOf("")
            downloadSize = if (contentLength > 0) {
                formatFileSize(context, contentLength)
            } else {
                context.getString(R.string.unknown_size)
            }

            if (contentLength <= 0 && url.isHttpOrHttpsUrl()) {
                val requestBuilder = Request.Builder()
                requestBuilder.get().url(url)
                CookieManager.getInstance().getCookie(url)?.let {
                    requestBuilder.header("cookie", it)
                }
                userAgent?.let {
                    requestBuilder.header("user-agent", it)
                }
                mimetype?.let {
                    requestBuilder.header("accept", it)
                }
                val simpleNetworkClient = OkHttpClient.Builder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(15, TimeUnit.SECONDS) // 仅仅是建立TCP连接的超时时间, 不包含读写
                    .callTimeout(60, TimeUnit.SECONDS)
                    .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                    .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                    .followRedirects(true)
                    .followSslRedirects(true) // .retryOnConnectionFailure(true)
                    .build()


                val call = simpleNetworkClient.newCall(requestBuilder.build())
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            response.body?.let {
                                downloadSize = formatFileSize(context, it.contentLength())
                            }
                        }
                    }
                })
            }

            requestPermissionIfNeeded {
                AlertBottomSheet.Builder(context).apply {
                    setMessage(context.getString(R.string.download_message, guessName, downloadSize))
                    setPositiveButton(android.R.string.ok) {
                        try {
                            download(url, userAgent, mimetype, guessName)
                        } catch (e: Exception) {
                        }
                    }
                    setNegativeButton(android.R.string.cancel) {}
                }.show()
            }
        }
    }

    override fun onDownloadStart(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimetype: String?,
        contentLength: Long
    ) {
        XLog.d("下载：$url, $userAgent, $contentDisposition, $mimetype, $contentLength")
        onDownloadWithDialog(url, userAgent, contentDisposition, mimetype, contentLength)
    }

    private fun decodeFileName(fileName: String, encoding: String?): String{
        try {
            return URLDecoder.decode(fileName, encoding ?:"UTF-8")
        } catch (e: UnsupportedEncodingException) {
            XLog.e(e)
        }
        return ""
    }

    /**
     * 从 url 中获取/与?之间的数据
     *
     * @param url 网址
     * @return 文件名(含后缀)
     */
    fun String.guessFileName(): String {
        var end = lastIndexOf("?")
        if (end < 0) {
            end = length
        }
        val start = lastIndexOf("/", end)
        var name = substring(start + 1, end)
        if (!name.contains(".") && end > 0){
            name = substring(end + 1)
            XLog.d("猜测名字1：$name")
            name = URLUtil.guessFileName(this, "", "")
            XLog.d("猜测名字2：$name")
        }
        return name
    }

    private fun getTask(url: String, guessName: String, mimetype: String?): Task {
        val savePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(null), Environment.DIRECTORY_DOWNLOADS)
        } else {
            File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
        }
        if (!savePath.exists()) {
            savePath.mkdir()
        }
        var saveName = guessName
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype)
        val extIndex = saveName.lastIndexOf(".")

        if ( (extIndex < 0 || saveName.length - extIndex > 5) && !ext.isNullOrBlank() ){
            saveName += ext
        }
        return Task(
            url = url,
            taskName = saveName,
            saveName = saveName,
            savePath = savePath.path
        )
    }

    fun downloadImage(url: String) {
        download(url, TabManager.currentTab.value?.view?.settings?.userAgentString, null, guessFileName(url))
    }

    private fun download(url: String, userAgent: String?, mimetype: String?, guessName: String) {
        if(url.isDataUrl()){
            ioScope.launch {
                val bitmap:ByteArray? = url.dataUrlToByteArray()
                val fileType = bitmap?.ext() ?: ""
                if (bitmap != null){
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), guessName + fileType)
                        file.save(bitmap)
                        MainScope().launch {
                            App.snackBarHostState.showSnackbar(context.getString(R.string.download_succeeded))
                        }
                    }else{
                        App.instance.cacheDir?.let {
                            val file = File(it, guessName + fileType)
                            file.save(bitmap)
                            if (file.exists()){
                                copy2PublicDir(context, file, Environment.DIRECTORY_DOWNLOADS)
                                MainScope().launch {
                                    App.snackBarHostState.showSnackbar(context.getString(R.string.download_succeeded))
                                }
                            }else{
                                MainScope().launch {
                                    App.snackBarHostState.showSnackbar(context.getString(R.string.download_failed))
                                }
                            }
                        }
                    }
                }else{
                    MainScope().launch {
                        App.snackBarHostState.showSnackbar(context.getString(R.string.the_image_is_abnormal))
                    }
                }
            }
            return
        }else if(url.isM3U8Url()){
            byM3U8(context, url, guessName)
        }else {
            bySystem(context, url, userAgent, mimetype, guessName)
        }
    }

    // 方法1：跳转浏览器下载
    fun byBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = Uri.parse(url)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    fun byRxJava(url: String, fileName: String, mimetype: String?) {
        val headerMap = RANGE_CHECK_HEADER.toMutableMap()
        val cookie = CookieManager.getInstance().getCookie(url)
        if (!cookie.isNullOrEmpty()) {
            headerMap["Cookie"] = cookie
        }
        val manager = getTask(url, fileName, mimetype).manager(
            recorder = TSRecorder(),
            notificationCreator = DownloadNotificationCreator()
        )

        manager.subscribe { status ->
            when (status) {
                is Failed -> XLog.d("download error: ${status.throwable}")
                is Downloading -> XLog.d("download state: ${status.progress}")
                is Completed -> {
                    XLog.d("download finished")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        addToPublicDownloadDir(url)
                    }
                }
                else -> {}
            }
        }

        manager.start()
        showDownloadingBadge.value = true
    }

    // 作者：落英坠露 ,链接：https://www.jianshu.com/p/6e38e1ef203a
    private fun bySystem(context: Context, url: String, userAgent: String?, mimetype: String?, fileName: String) {
        // 方法2、使用系统的下载服务
        // 指定下载地址
        val request = DownloadManager.Request(Uri.parse(url))
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner()
        // 设置通知的显示类型，下载进行时和完成后显示通知
        // Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED 表示在下载过程中通知栏会一直显示该下载的Notification，在下载完成后该Notification会继续显示，直到用户点击该Notification或者消除该Notification。
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // 设置通知栏的标题，如果不设置，默认使用文件名
        // request.setTitle("This is title");
        // 设置通知栏的描述
        request.setDescription(url)
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true)
        // 允许在计费流量下下载
        request.setAllowedOverMetered(true)
        // 允许漫游时下载
        request.setAllowedOverRoaming(true)
        // 允许下载的网路类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

        val cookie = CookieManager.getInstance().getCookie(url)
        request.addRequestHeader("Cookie", cookie)
        request.addRequestHeader("User-Agent", userAgent);
        request.addRequestHeader("Accept", mimetype);
        // 设置下载文件保存的路径和文件名。
        // Content-disposition 是 MIME 协议的扩展，MIME 协议指示 MIME 用户代理如何显示附加的文件。当 Internet Explorer 接收到头时，它会激活文件下载对话框，它的文件名框自动填充了头中指定的文件名。（请注意，这是设计导致的；无法使用此功能将文档保存到用户的计算机上，而不向用户询问保存位置。）
        // XLog.i("下载", "文件名：" + fileName);
        // request.setDestinationInExternalFilesDir()
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        // 另外可选一下方法，自定义下载路径
        // request.setDestinationUri();
        // request.setDestinationInExternalFilesDir();
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // 添加一个下载任务
        downloadManager.enqueue(request)
    }

    private fun byM3U8(context: Context, url: String, fileName: String) {
        val mListener: com.jeffmony.downloader.listener.DownloadListener = object : com.jeffmony.downloader.listener.DownloadListener() {
            val channelId = NotificationUtils.getDownloadChannel(context)
            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(fileName)
                .setContentText(url) // .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setAutoCancel(true).setOngoing(true)

            // final NotificationCompat.Builder notification = NotificationUtils.getDownloadNotify(context, finalFileName, url);
            val notificationManager = NotificationManagerCompat.from(context)
            override fun onDownloadDefault(item: VideoTaskItem) {
                XLog.i("onDownloadDefault：$item")
            }

            override fun onDownloadPending(item: VideoTaskItem) {
                XLog.i("onDownloadPending：$item")
                notification
                    .setContentText(context.getString(R.string.pending))
                    .setProgress(0, 0, true)
                notificationManager.notify(item.url.hashCode(), notification.build())
            }

            override fun onDownloadPrepare(item: VideoTaskItem) {
                XLog.i("onDownloadPrepare：$item")
                notification.setProgress(0, 0, true)
                notificationManager.notify(item.url.hashCode(), notification.build())
            }

            override fun onDownloadStart(item: VideoTaskItem) {
                XLog.i("onDownloadStart：$item")
            }

            override fun onDownloadProgress(item: VideoTaskItem) {
                XLog.i("onDownloadProgress：$item")
                notification
                    .setContentText(null)
                    .setProgress(100, item.percent.toInt(), false)
                notificationManager.notify(item.url.hashCode(), notification.build())
            }

            override fun onDownloadSpeed(item: VideoTaskItem) {
                XLog.i("onDownloadSpeed：$item")
            }

            override fun onDownloadPause(item: VideoTaskItem) {
                XLog.i("onDownloadPause：$item")
            }

            override fun onDownloadError(item: VideoTaskItem) {
                XLog.i("onDownloadError：$item")
                notification
                    .setContentText(context.getString(R.string.download_fail))
                    .setProgress(0, 0, false).setOngoing(false)
                notificationManager.notify(item.url.hashCode(), notification.build())
            }

            override fun onDownloadSuccess(item: VideoTaskItem) {
                XLog.i("onDownloadSuccess：$item")
                //更新
                notification
                    .setContentText(context.getString(R.string.download_success))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setProgress(0, 0, false).setOngoing(false)
                notificationManager.notify(item.url.hashCode(), notification.build())
                val suffix = item.filePath.ext()
                XLog.d("视频文件后缀：$fileName  $suffix")

                item.filePath.asFile()?.tryRenameTo(Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + fileName + suffix)
            }
        }
        VideoDownloadManager.getInstance().setGlobalDownloadListener(mListener)
        val item = VideoTaskItem(url, "", fileName, "")
        VideoDownloadManager.getInstance().startDownload(item)
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addToPublicDownloadDir(url: String) {
        val resolver = context.contentResolver ?: return
        val file = url.file()
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val cursor = resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.TITLE),
            "${MediaStore.Downloads.TITLE}=\'${file.name}\' or ${MediaStore.Downloads.TITLE}=\'${file.nameWithoutExtension}\'",
            null,
            null
        )

        val downloadDetail = ContentValues().apply {
            put(MediaStore.Downloads.TITLE, file.name)
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.MIME_TYPE, file.mimeType)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = if (cursor != null && cursor.count > 0) {
            cursor.use { c ->
                c.moveToFirst()
                val id = c.getLong(c.getColumnIndex(MediaStore.Downloads._ID))
                ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id)
            }
        } else {
            resolver.insert(collection, downloadDetail) ?: return
        }
        resolver.openFileDescriptor(uri, "w", null)?.use { fd ->
            ParcelFileDescriptor.AutoCloseOutputStream(fd).use {
                val fis = FileInputStream(file)
                fis.channel.transferTo(0, file.length(), it.channel)
            }
        }
        downloadDetail.clear()
        downloadDetail.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, downloadDetail, null, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file.delete()
        }
    }

    private fun requestPermissionIfNeeded(completion: () -> Unit) {
        val activity = context as? BaseActivity
        activity?.let {
            it.lifecycleScope.launchWhenCreated {
                it.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                completion()
            }
        } ?: completion()
    }
}
