package com.hinnka.tsbrowser.ext

import android.webkit.MimeTypeMap
import java.io.File

val File.mimeType: String?
    get() {
        val ext = MimeTypeMap.getFileExtensionFromUrl(absolutePath)
        return ext?.let {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
        }
    }

fun String.asFile(): File? {
    val file = File(this)
    if (file.isFile){
        return file
    }
    return null
}
fun File.tryRenameTo(destFileName: String): Boolean {
    //XLog.i("文件是否存在：" + srcFile.exists() + destFileName);
    val destFile = File(destFileName)
    destFile.parentFile?.let {
        if (!it.exists()){it.mkdirs()}
    }
    return this.renameTo(destFile)
}