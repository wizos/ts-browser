/*
 * Copyright (c) 2022 wizos
 * 项目：LoreadCompose
 * 邮箱：wizos@qq.com
 * 创建时间：2022-07-07 02:07:28
 */

package com.hinnka.tsbrowser.ext

import android.net.Uri
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.Schema
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.util.AUTOLINK_WEB_URL
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs


const val ASSET_BASE = "file:///android_asset/"
const val FILE_BASE = "file:"
const val PROXY_BASE = "file:///cookieless_proxy/"


fun String.isUrl(): Boolean {
    var inUrl = this.trim()
    if (inUrl.contains(" ")) {
        return false
    }
    if (inUrl.startsWith(Schema.VIEW_SOURCE)) {
        inUrl = inUrl.substring(12)
    }
    if (URLUtil.isValidUrl(inUrl)) {
        return true
    }
    if (inUrl.startsWith(Schema.VIEW_SOURCE)){
        return true
    }
    val matcher = AUTOLINK_WEB_URL.matcher(inUrl)
    if (matcher.matches()) {
        return true
    }
    return false
}


fun String.isAssetUrl(): Boolean {
    return startsWith("file:///android_asset/")
}
fun String.isDataUrl(): Boolean {
    return startsWith("data:", true)
}
fun String.isJavaScriptUrl(): Boolean {
    return startsWith("javascript:")
}
fun String.isAboutUrl(): Boolean {
    return startsWith("about:")
}
fun String.isM3U8Url(): Boolean {
    return isHttpOrHttpsUrl() && (endsWith(".m3u8", true) || contains(".m3u8?", true))
}
fun String.isHttpUrl(): Boolean {
    return length > 6 && substring(0, 7).equals("http://", ignoreCase = true)
}
fun String.isHttpsUrl(): Boolean {
    return length > 7 && substring(0, 8).equals("https://", ignoreCase = true)
}
fun String.isContentUrl(): Boolean {
    return startsWith("content:")
}

// ^https?://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]
// ^https?://([\w-]+(\.[\w-]+)*\/?)+(\?([\w\-\.,@?^=%&:\/~\+#]*)+)?$
// ^https?://[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)
val HTTP_OR_HTTPS_URL: Pattern = Pattern.compile("^https?://[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")
fun String.isHttpOrHttpsUrl(): Boolean {
    val matcher = HTTP_OR_HTTPS_URL.matcher(this)
    return matcher.matches()
}

fun String.isValidUrl(): Boolean {
    return isAssetUrl() ||
            // isFileUrl() ||
            isAboutUrl() ||
            isHttpUrl() ||
            isHttpsUrl() ||
            isJavaScriptUrl() ||
            isContentUrl()
}


fun String.toUrl(): String {
    if (isUrl()) {
        return URLUtil.guessUrl(this.trim())
    }
    return toSearchUrl()
}

fun String.toSearchUrl(): String {
    return String.format(Settings.searchEngine.value, this)
}

/**
 * 去掉链接的协议，最末尾的/,?,#部分
 */
// fun String.toShortestUrl(): String {
//     var similarUrl = this.trim()
//
//     if (this.startsWith(Contracts.SCHEMA_HTTP, true)) {
//         similarUrl = this.substring(7)
//     } else if (this.startsWith(Contract.SCHEMA_HTTPS, true)){
//         similarUrl = this.substring(8)
//     }
//     while (similarUrl.endsWith("/")) {
//         similarUrl = similarUrl.substring(0, similarUrl.length - 2)
//     }
//     while (similarUrl.endsWith("?")) {
//         similarUrl = similarUrl.substring(0, similarUrl.length - 2)
//     }
//     while (similarUrl.endsWith("#")) {
//         similarUrl = similarUrl.substring(0, similarUrl.length - 2)
//     }
//     return similarUrl
// }




private val P_REPEATED_SLASH = Pattern.compile("([^:]/)/", Pattern.CASE_INSENSITIVE)
fun String.formatUrl(): String {
    // 将 url 中除了://以外的 // 替换为 /
    var url = this.trim()
    var matcher = P_REPEATED_SLASH.matcher(url)
    while (matcher.find()) {
        url = matcher.replaceAll("$1")
        matcher = P_REPEATED_SLASH.matcher(url)
    }
    return url
}

private val P_URL_PATH = Pattern.compile("^https?://.*?/", Pattern.CASE_INSENSITIVE)
fun String.getPathWithParam(): String {
    return P_URL_PATH.matcher(this).replaceFirst("/")
}

// https://www.hao123.com/favicon.ico
fun String.guessFaviconUrl(): String? {
    if(isHttpOrHttpsUrl()){
        val uri = Uri.parse(this)
        return uri.scheme + "://" + uri.host + "/favicon.ico"
    }else{
        return null
    }
}

/**
 * Guesses canonical filename that a download would have, using
 * the URL and contentDisposition. File extension, if not defined,
 * is added based on the mimetype
 * @param url Url to the content
 * @param contentDisposition Content-Disposition HTTP header or `null`
 * @param mimeType Mime-type of the content or `null`
 *
 * @return suggested filename
 */
private val CONTENT_DISPOSITION_PATTERN = Pattern.compile("attachment;\\s*filename\\s*=\\s*(\"?)([^\"]*)\\1\\s*$", Pattern.CASE_INSENSITIVE)
// attachment;filename=%5Bwww.ghxi.com%5D%E5%93%94%E5%93%A9%E5%93%94%E5%93%A9_v7.1.2.apk;filename*=UTF-8''%5Bwww.ghxi.com%5D%E5%93%94%E5%93%A9%E5%93%94%E5%93%A9_v7.1.2.apk
private val pContentDisposition1 = Pattern.compile("attachment;\\s*filename\\s*=\\s*(\"?)([^\";]*)(\\1\\s*;|\\1\\s*\$)", Pattern.CASE_INSENSITIVE)
private val pContentDisposition2 = Pattern.compile("attachment;\\s*filename\\s*\\*\\s*=\\s*([^\";]*)'[^\"]*'([^\"]*)(\\s*;|\\s*\$)", Pattern.CASE_INSENSITIVE)
fun guessFileName(url: String?, contentDisposition: String? = null, mimeType: String? = null): String {
    var filename: String? = null
    var encoding: String? = null
    var extension: String? = null

    XLog.d("猜测文件名A：$url -- $mimeType -- $contentDisposition")
    // 1、优先依靠 contentDisposition 来解析名字
    // 处理会把 epub 文件，识别为 bin 文件的 bug：https://blog.csdn.net/imesong/article/details/45568697
    if (!contentDisposition.isNullOrBlank()) {
        var matcher = pContentDisposition1.matcher(contentDisposition)
        if (matcher.find()) {
            filename = matcher.group(2)
        } else {
            matcher = pContentDisposition2.matcher(contentDisposition)
            if (matcher.find()) {
                encoding = matcher.group(1)
                filename = matcher.group(2)
            }
        }
        XLog.d("猜测文件名B：$url -- $$filename")

        if (!filename.isNullOrBlank()) {
            val index = filename.lastIndexOf('/') + 1
            if (index > 0) {
                filename = filename.substring(index)
            }
            XLog.d("猜测文件名C：$url -- $$filename")
        }
        filename = filename?.let { decodeFileName(it, encoding) }
        XLog.d("猜测文件名E：$url -- $filename")
    }
    // If all the other http-related approaches failed, use the plain uri
    // 2、否则依靠 url 本身来解析名字
    if (!url.isNullOrBlank() && filename.isNullOrBlank()) {
        if(url.isDataUrl()){
            filename = url.hashCode().toString()// + url.dataUrlToByteArray()?.ext()
        }else{
            var decodedUrl: String? = Uri.decode(url)
            if (decodedUrl != null) {
                val queryIndex = decodedUrl.indexOf('?')
                // If there is a query string strip it, same as desktop browsers
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex)
                }
                if (!decodedUrl.endsWith("/")) {
                    val index = decodedUrl.lastIndexOf('/') + 1
                    if (index > 0) {
                        filename = decodedUrl.substring(index)
                        // fileName = decodeFileName(fileName, encoding)
                        XLog.d("猜测文件名D：$url -- $$filename")
                    }
                }
            }
        }
    }
    // Finally, if couldn't get filename from URI, get a generic filename
    if (filename == null) {
        filename = "download_file_" + url.hashCode().toString()
        XLog.d("猜测文件名F：$url -- $filename")
    }

    // Split filename between base and extension
    // Add an extension if filename does not have one
    val dotIndex = filename.indexOf('.')
    if (dotIndex < 0) {
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            if (extension == null) {
                if (mimeType.lowercase(Locale.ROOT).startsWith("text/")) {
                    if (mimeType.equals("text/html", ignoreCase = true)) {
                        extension = "html"
                    } else {
                        extension = "txt"
                    }
                }
            }
        }
    }
    // else {
    //     if (mimeType != null) {
    //         // Compare the last segment of the extension against the mime type.
    //         // If there's a mismatch, discard the entire extension.
    //         val lastDotIndex = filename.lastIndexOf('.')
    //         val typeFromExt: String? = MimeTypeMap.getSingleton().getMimeTypeFromExtension(filename.substring(lastDotIndex + 1))
    //         if (typeFromExt != null && !typeFromExt.equals(mimeType, ignoreCase = true)) {
    //             extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    //         }
    //     }
    //     if (extension == null) {
    //         extension = filename.substring(dotIndex)
    //     }
    //     filename = filename.substring(0, dotIndex)
    // }

    if(extension != null){
        return "$filename.$extension"
    }else{
        return filename
    }
}

fun String.guessFileNameFromUrl1(): String {
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

/**
 * 从 url 中获取/与?之间的数据
 *
 * @return 文件名(含后缀)
 */
fun String.guessFileNameFromUrl(): String? {
    var decodedUrl: String? = Uri.decode(this)
    // try {
    //     decodedUrl = URLDecoder.decode(this, StandardCharsets.UTF_8.displayName())
    // } catch (e: UnsupportedEncodingException) {
    //     XLog.w("url 解码异常：$this")
    // } catch (e: IllegalArgumentException) {
    //     XLog.w("url 解码异常：$this")
    // }
    if (decodedUrl != null) {
        val queryIndex = decodedUrl.indexOf('?')
        // If there is a query string strip it, same as desktop browsers
        if (queryIndex > 0) {
            decodedUrl = decodedUrl.substring(0, queryIndex)
        }
        if (!decodedUrl.endsWith("/")) {
            val index = decodedUrl.lastIndexOf('/') + 1
            if (index > 0) {
                return decodedUrl.substring(index)
                // fileName = decodeFileName(fileName, encoding)
            }
        }
    }
    return null
}
fun String.getFileNameFromUrl(): String {
    var fileName: String? = guessFileNameFromUrl()?.apply {
        toSavable()
    }
    // 减少文件名太长的情况
    fileName = fileName?.apply {
        if (length > 64){
            substring(0,64)
        }
    }
    return fileName ?: abs(hashCode()).toString()
}


private fun decodeFileName(fileName: String, encoding: String?): String{
    try {
        return URLDecoder.decode(fileName, encoding ?:"UTF-8")
    } catch (e: UnsupportedEncodingException) {
        XLog.e(e)
    }
    return ""
}


// suspend fun String.dataUrlToByteArray(): ByteArray? {
//     return withContext(Dispatchers.IO) {
//         try {
//             Base64Utils.getInstance().decode(this@dataUrlToByteArray.substring(this@dataUrlToByteArray.indexOf(",") + 1))
//         } catch (e: Exception) {
//             XLog.e("decode bitmap failed $e")
//             null
//         }
//     }
// }

private val P_URL = Regex("(?:http(s)?://)?[\\w.-]+(?:\\.[\\w.-]+)+[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=.]+")
fun findUrl(text: String?):String? {
    if (text.isNullOrBlank())
        return null

    val value = P_URL.find(text)?.groupValues

    if (value !=null){
        if (!value.first().isHttpOrHttpsUrl()) return Schema.HTTP + value.first().trim()
        return value.first()
    }
    return null
}
fun join(title: String?, url: String?): String {
    if (title == null) {
        return url ?: ""
    }
    if (url == null) {
        return title
    }
    if (title == url) {
        return title
    }
    return "$title $url"
}
fun join(title: CharSequence?, url: String?): String {
    return join(title?.toString() ?: "", url)
}
private val P_UTM = Pattern.compile("(?<=[?&])utm_(?:source|medium|campaign|id|oi)=.*?(?:&|\$)", Pattern.CASE_INSENSITIVE)
fun removeUtm(url: String?): String {
    if (url.isNullOrBlank()){
        return ""
    }else{
        var url = url.trim()
        var matcher = P_UTM.matcher(url)
        while (matcher.find()) {
            url = matcher.replaceAll("")
            matcher = P_UTM.matcher(url)
        }
        url = url.trimEnd('?', '&')
        return url
    }
}


/**
 * Encodes the passed String as UTF-8 using an algorithm that's compatible
 * with JavaScript's `encodeURIComponent` function. Returns
 * `null` if the String is `null`.
 *
 * @param s The String to be encoded
 * @return the encoded String
 */
fun String.encodeURIComponent(): String? {
    val result: String? = try {
        URLEncoder.encode(this, StandardCharsets.UTF_8.displayName())
            .replace("\\+".toRegex(), "%20")
            .replace("%21".toRegex(), "!")
            .replace("%27".toRegex(), "'")
            .replace("%28".toRegex(), "(")
            .replace("%29".toRegex(), ")")
            .replace("%7E".toRegex(), "~")
    } // This exception should never occur.
    catch (e: UnsupportedEncodingException) {
        this
    }
    return result
}

/**
 * Decodes the passed UTF-8 String using an algorithm that's compatible with
 * JavaScript's `decodeURIComponent` function. Returns
 * `null` if the String is `null`.
 *
 * @param s The UTF-8 encoded String to be decoded
 * @return the decoded String
 */
fun String.decodeURIComponent(): String? {
    val result = try {
        URLDecoder.decode(this, "UTF-8")
    } // This exception should never occur.
    catch (e: UnsupportedEncodingException) {
        this
    }
    return result
}

/**
 * 只对url中的汉字部分进行 urlEncode。
 */
fun String.encodeURIChineseChar(): String {
    val result = try {
        val sb = StringBuilder()
        var i = 0
        while (i < length) {
            val c = this[i]
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(URLEncoder.encode(c.toString(), "UTF-8"))
            } else {
                sb.append(c)
            }
            i++
        }
        sb.toString()
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        this
    }
    return result
}


/**
 * 获取截断（逐渐增长）
 */
fun String.getKnots(): List<String> {
    val hostList: MutableList<String> = mutableListOf()
    if (!isHttpOrHttpsUrl()){
        return hostList
    }
    val uri = Uri.parse(this)
    var host = uri.host
    if (host!!.contains(".")) {
        val slices = host.split(".").toTypedArray()
        var i = 0
        val size = slices.size
        while (i + 1 < size) {
            host = slices.copyOfRange(i, size).joinToString(".")
            hostList.add(host)
            i++
        }
    } else {
        hostList.add(host)
    }
    return hostList
}