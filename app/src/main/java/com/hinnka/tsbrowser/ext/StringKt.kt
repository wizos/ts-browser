package com.hinnka.tsbrowser.ext

import android.R.attr
import android.net.Uri
import android.util.Base64
import android.webkit.URLUtil
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.util.AUTOLINK_WEB_URL
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.R.attr.data
import com.hinnka.tsbrowser.Schema
import com.hinnka.tsbrowser.util.filterUnsavedSymbol
import java.lang.StringBuilder
import java.security.MessageDigest
import java.util.regex.Pattern
import kotlin.experimental.and


// fun String.isDataUrl(): Boolean {
//     val inUrl = this.trim()
//     return inUrl.startsWith("data:", true)
// }
// val HTTP_OR_HTTPS_URL: Pattern = Pattern.compile("^https?://[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")
// fun String.isHttpOrHttpsUrl(): Boolean {
//     val inUrl = this.trim()
//     val matcher = HTTP_OR_HTTPS_URL.matcher(inUrl)
//     if (matcher.matches()) {
//         return true
//     }
//     return false
// }
// fun String.isM3U8Url(): Boolean {
//     val inUrl = this.trim()
//     return inUrl.endsWith(".m3u8", true) || inUrl.contains(".m3u8?", true)
// }
// fun String.isUrl(): Boolean {
//     var inUrl = this.trim()
//     if (inUrl.contains(" ")) {
//         return false
//     }
//     if (inUrl.startsWith(Schema.VIEW_SOURCE)) {
//         inUrl = inUrl.substring(12)
//     }
//     if (URLUtil.isValidUrl(inUrl)) {
//         return true
//     }
//     val matcher = AUTOLINK_WEB_URL.matcher(inUrl)
//     if (matcher.matches()) {
//         return true
//     }
//     return false
// }
//
// fun String.toUrl(): String {
//     if (isUrl()) {
//         return URLUtil.guessUrl(this.trim())
//     }
//     return toSearchUrl()
// }
//
// fun String.toSearchUrl(): String {
//     return String.format(Settings.searchEngine.value, this)
// }

/**
 * 处理文件名中的特殊字符和表情，用于保存为文件
 */
fun String.toSavable(): String {
    // 因为有些title会用 html中的转义。所以这里要改过来
    // fileName = Html.fromHtml(fileName).toString();
    // fileName = EmojiParser.removeAllEmojis(fileName)
    val name = filterUnsavedSymbol().trim()
    if (name.isBlank()) {
        return System.currentTimeMillis().timestampFormatted("yyyyMMddHHmmss")
    }
    return name
}

/**
 * 过滤保存文件到存储器中会有问题的符号
 * @return
 */
fun String.filterUnsavedSymbol(): String {
    return this
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
        .replace("#", "_") // 系统下载器不允许有 # 符号
        .replace("&amp;", "_")
        .replace("&nbsp;", "_")
        .replace("&", "_")
        .replace("�", "_")
        .replace("\r", "_")
        .replace("\n", "_")
}

fun String.aesEncode(key: String): String {
    val secretKey = SecretKeySpec(key.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val bytes = cipher.doFinal(this.toByteArray())
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun String.aesDecode(key: String): String {
    val textBytes: ByteArray = Base64.decode(this, Base64.DEFAULT)
    val secretKey = SecretKeySpec(key.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    val bytes = cipher.doFinal(textBytes)
    return String(bytes)
}

fun String?.md5(): String? {
    this ?: return null
    val addSalt = "ts${this}browser"
    val md5 = MessageDigest.getInstance("MD5")
    val bytes = md5.digest(addSalt.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

val String.host: String?
    get() {
        val uri = if (URLUtil.isValidUrl(this)) {
            Uri.parse(this)
        } else {
            Uri.parse("https://$this")
        }
        return uri.host
    }