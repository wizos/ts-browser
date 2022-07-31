package com.hinnka.tsbrowser.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.elvishew.xlog.XLog
import java.io.*
import kotlin.experimental.and


/**
 * 过滤保存文件到存储器中会有问题的符号
 * @param str
 * @return
 */
fun filterUnsavedSymbol(str: String): String {
    return str
        .replace("\\", "")
        .replace("/", "")
        .replace(":", "")
        .replace("*", "")
        .replace("?", "")
        .replace("\"", "")
        .replace("<", "")
        .replace(">", "")
        .replace("|", "")
        .replace("%", "_")
        .replace("#", "_")
        .replace("&amp;", "_")
        .replace("&nbsp;", "_")
        .replace("&", "_")
        .replace("�", "_")
        .replace("\r", "_")
        .replace("\n", "_")
}

fun getSaveableName(fileName: String): String {
    // 因为有些title会用 html中的转义。所以这里要改过来
    // fileName = Html.fromHtml(fileName).toString();
    var fileName = fileName
    // fileName = EmojiParser.removeAllEmojis(fileName)
    fileName = filterUnsavedSymbol(fileName).trim()
    if (fileName == "") {
        fileName = System.currentTimeMillis().toString()
    }
    return fileName.trim { it <= ' ' }
}

/**
 * https://zhuanlan.zhihu.com/p/172493773
 * https://www.jianshu.com/p/b3595fc1f9be
 */
fun save2SDPictures(mContext: Context, name: String, bmp: Bitmap): Uri? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val destFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name)
        savePictures(destFile, bmp)
        var uri: Uri? = null
        if (destFile.exists()) uri = Uri.parse("file://" + destFile.absolutePath)
        return uri
    } else {
        // 检查SD卡状态是否为正常挂载
        val contentUri: Uri = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        }
        //创建 ContentValues 对象，准备插入数据
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg") //文件格式
        contentValues.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis())
        contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, name) //文件名字
        contentValues.put(MediaStore.Images.ImageColumns.RELATIVE_PATH, "Pictures")
        val fileUri = mContext.contentResolver.insert(contentUri, contentValues) ?: return null
        savePictures(mContext, fileUri, bmp)
    }
}

fun savePictures(destFile: File, bmp: Bitmap): File? {
    destFile.parentFile?.let {
        if (!it.exists()){
            if (!it.mkdirs()) {
                return null
            }
        }
    }

    try {
        val fos = FileOutputStream(destFile)
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        XLog.e("保存到相册错误")
        e.printStackTrace()
        return null
    }
    return destFile
}

fun File.inject(bmp: Bitmap): File? {
    this.parentFile?.let {
        if (!it.exists()){
            if (!it.mkdirs()) {
                return null
            }
        }
    }

    try {
        val fos = FileOutputStream(this)
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        XLog.e("保存到相册错误")
        e.printStackTrace()
        return null
    }
    return this
}

fun File.inject(bmp: ByteArray): File? {
    this.parentFile?.let {
        if (!it.exists()){
            if (!it.mkdirs()) {
                return null
            }
        }
    }

    try {
        val fos = FileOutputStream(this)
        fos.write(bmp)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        XLog.e("保存到相册错误")
        e.printStackTrace()
        return null
    }
    return this
}


fun savePictures(mContext: Context, fileUri: Uri, bmp: Bitmap): Uri? {
    // android Q
    var outputStream: OutputStream? = null
    try {
        outputStream = mContext.contentResolver.openOutputStream(fileUri)
        if (outputStream != null) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
        }
        return fileUri
    } catch (e: Exception) {
        e.printStackTrace()
        // 失败的时候，删除此 uri 记录
        mContext.contentResolver.delete(fileUri, null, null)
        return null
    } finally {
        try {
            outputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
            // ignore
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun copy2Pictures(mContext: Context, oriFile:File){
    val values = ContentValues()
    values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, oriFile.name)
    values.put(MediaStore.Files.FileColumns.TITLE, oriFile.name)
    values.put(MediaStore.Files.FileColumns.MIME_TYPE, "image/*")
    values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

    val resolver: ContentResolver = mContext.contentResolver
    val insertUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    var ist: InputStream? = null
    var ost: OutputStream? = null
    try {
        ist = FileInputStream(oriFile)
        if (insertUri != null) {
            ost = resolver.openOutputStream(insertUri)
        }
        if (ost != null) {
            val buffer = ByteArray(4096)
            var byteCount = 0
            while (ist.read(buffer).also { byteCount = it } != -1) {
                ost.write(buffer, 0, byteCount)
            }
        }
    } catch (e: IOException) {
    } finally {
        try {
            ist?.close()
            ost?.close()
        } catch (e: IOException) {
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun copy2PublicDir(mContext: Context, oriFile:File, dir:String){
    val values = ContentValues()
    values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, oriFile.name)
    values.put(MediaStore.Files.FileColumns.TITLE, oriFile.name)
    values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, dir)

    val resolver: ContentResolver = mContext.contentResolver
    val insertUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    var ist: InputStream? = null
    var ost: OutputStream? = null
    try {
        ist = FileInputStream(oriFile)
        if (insertUri != null) {
            ost = resolver.openOutputStream(insertUri)
        }
        if (ost != null) {
            val buffer = ByteArray(4096)
            var byteCount = 0
            while (ist.read(buffer).also { byteCount = it } != -1) {
                ost.write(buffer, 0, byteCount)
            }
        }
    } catch (e: IOException) {
    } finally {
        try {
            ist?.close()
            ost?.close()
        } catch (e: IOException) {
        }
    }
}
