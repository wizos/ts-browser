package com.hinnka.tsbrowser.ext

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.experimental.and


fun File.ext(): String? {
    return try {
        val fis = FileInputStream(this)
        val src = ByteArray(28)
        fis.read(src, 0, 28)
        return src.ext()
    } catch (e: IOException) {
        null
    }
}

fun ByteArray.ext(): String?{
    return try {
        val src = this.copyOfRange(0, 28)
        val stringBuilder = StringBuilder("")
        for (b in src) {
            val v = b.toInt() and 0xFF
            val hv = Integer.toHexString(v).uppercase()
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        val fileHeader = stringBuilder.toString()
        if (fileHeader.startsWith("FFD8FF")) {
            return ".jpeg"
        }
        if (fileHeader.startsWith("89504E47")) {
            return ".png"
        }
        if (fileHeader.startsWith("47494638")) {
            return ".gif"
        }
        if (fileHeader.startsWith("3C73766720")) {
            return ".svg"
        }
        if (fileHeader.startsWith("424D")) {
            return ".bmp"
        }
        if (fileHeader.startsWith("52494646")) {
            return ".webp"
        }
        if (fileHeader.startsWith("49492A00")) {
            ".tiff"
        } else null
    } catch (e: IOException) {
        null
    }
}

fun String.ext(): String {
    if (isBlank()) {
        return ""
    }
    val start = lastIndexOf(".")
    return if (start > 0) {
        substring(start)
    } else {
        ""
    }
}