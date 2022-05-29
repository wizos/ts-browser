package com.hinnka.tsbrowser.download

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.mimeType
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.util.getSaveableName
import zlc.season.rxdownload4.RANGE_CHECK_HEADER
import zlc.season.rxdownload4.file
import zlc.season.rxdownload4.manager.*
import zlc.season.rxdownload4.recorder.RoomRecorder
import zlc.season.rxdownload4.task.Task
import java.io.File
import java.io.FileInputStream
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

class DownloadHandler(val context: Context) : DownloadListener {

    companion object {
        val showDownloadingBadge = mutableStateOf(false)
    }

    init {
        OpenReceiver.register(context)
    }

    override fun onDownloadStart(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimetype: String?,
        contentLength: Long
    ) {
        if (tryOpenStream(url, contentDisposition, mimetype)) {
            return
        }

        val guessName = getFileName(url, contentDisposition, mimetype)
        val downloadSize = if (contentLength > 0) {
            formatFileSize(context, contentLength)
        } else {
            context.getString(R.string.unknown_size)
        }

        requestPermissionIfNeeded {
            AlertBottomSheet.Builder(context).apply {
                setMessage(context.getString(R.string.download_message, guessName, downloadSize))
                setPositiveButton(android.R.string.ok) {
                    try {
                        download(url, guessName, mimetype)
                    } catch (e: Exception) {
                    }
                }
                setNegativeButton(android.R.string.cancel) {}
            }.show()
        }
    }

    private fun tryOpenStream(
        url: String,
        contentDisposition: String?,
        mimetype: String?
    ): Boolean {
        val type = mimetype ?: return false
        if (contentDisposition == null
            || !contentDisposition.regionMatches(0, "attachment", 0, 10, true)
        ) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(url), type)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
            }
        }
        return false
    }
    private val P_FILE_NAME = Pattern.compile("attachment;\\s*filename\\s*=\\s*(\"?)([^\"]*)\\1\\s*$", Pattern.CASE_INSENSITIVE)
    private val P_FILE_NAME2 = Pattern.compile("attachment;\\s*filename\\*\\s*=\\s*([^\"]*)'[^\"]*'([^\"]*)\\s*$", Pattern.CASE_INSENSITIVE)

    private fun getFileName(url: String = "", contentDisposition: String?, mimeType: String?): String {
        var fileNameByGuess: String? = null
        var encoding: String? = null

        // XLog.d("猜测的文件名为A：$mimeType -- $fileNameByGuess -- $contentDisposition")
        // 处理会把 epub 文件，识别为 bin 文件的 bug：https://blog.csdn.net/imesong/article/details/45568697
        if (!contentDisposition.isNullOrBlank()) {
            var matcher = P_FILE_NAME.matcher(contentDisposition)
            if (matcher.find()) {
                matcher.group(2)?.also {
                    fileNameByGuess = it
                }
            } else {
                matcher = P_FILE_NAME2.matcher(contentDisposition)
                if (matcher.find()) {
                    encoding = matcher.group(1)
                    matcher.group(2)?.also {
                        fileNameByGuess = it
                    }
                }
            }
            if (fileNameByGuess.isNullOrBlank() || fileNameByGuess?.contains(".") == false) {
                fileNameByGuess = URLUtil.guessFileName(url, contentDisposition, mimeType)
            }
        }

        if (fileNameByGuess.isNullOrBlank() || fileNameByGuess?.contains(".") == false) {
            // 从路径中获取
            fileNameByGuess = getFileName(url)
        }

        if(encoding.isNullOrBlank()){
            encoding = "UTF-8"
        }

        // XLog.d("猜测的文件名为B：$mimeType -- $fileNameByGuess -- $contentDisposition")
        try {
            fileNameByGuess = URLDecoder.decode(fileNameByGuess, encoding)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        if(fileNameByGuess.isNullOrBlank()){
            return System.currentTimeMillis().toString()
        }else{
            return fileNameByGuess!!
        }
    }

    /**
     * 从 url 中获取/与?之间的数据
     *
     * @param url 网址
     * @return 文件名(含后缀)
     */
    fun getFileName(url: String): String {
        var url = url
        try {
            url = URLDecoder.decode(url, StandardCharsets.UTF_8.displayName())
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            XLog.d("url 解码异常：$url")
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            XLog.d("url 解码异常：$url")
        }
        var fileName: String
        var end = url.lastIndexOf("?")
        if (end < 0) {
            end = url.length
        }
        val start = url.lastIndexOf("/", end)
        fileName = url.substring(start + 1, end)
        fileName = getSaveableName(fileName)

        // 减少文件名太长的情况
        if (fileName.length > 64) {
            fileName = fileName.substring(0, 64)
        }
        return fileName
    }

    private fun getTask(url: String, guessName: String, mimetype: String?): Task {
        val savePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.filesDir, Environment.DIRECTORY_DOWNLOADS)
        } else {
            File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
        }
        if (!savePath.exists()) {
            savePath.mkdir()
        }
        var saveName = guessName
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype)
        if (!ext.isNullOrBlank()) {
            if (!saveName.endsWith(ext)) {
                saveName += ext
            }
        }
        return Task(
            url = url,
            taskName = saveName,
            saveName = saveName,
            savePath = savePath.path
        )
    }

    fun downloadImage(url: String) {
        val guessName = URLUtil.guessFileName(url, null, null)
        download(url, guessName, null)
    }

    private fun download(url: String, guessName: String, mimetype: String?) {
        val headerMap = RANGE_CHECK_HEADER.toMutableMap()
        val cookie = CookieManager.getInstance().getCookie(url)
        if (!cookie.isNullOrEmpty()) {
            headerMap["Cookie"] = cookie
        }
        val manager = getTask(url, guessName, mimetype).manager(
            recorder = TSRecorder(),
            notificationCreator = DownloadNotificationCreator()
        )

        val disposable = manager
            .subscribe { status ->
                when (status) {
                    is Failed -> XLog.d("download error: ${status.throwable}")
                    is Downloading -> XLog.d("download state: ${status.progress}")
                    is Completed -> {
                        XLog.d("download finished")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            addToPublicDownloadDir(url)
                        }
                    }
                    else -> {
                    }
                }
            }
        manager.start()
        showDownloadingBadge.value = true
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addToPublicDownloadDir(url: String) {
        val resolver = context.contentResolver ?: return
        val file = url.file()
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val cursor = resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.TITLE
            ),
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
