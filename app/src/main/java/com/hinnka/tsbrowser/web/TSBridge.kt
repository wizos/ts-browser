package com.hinnka.tsbrowser.web

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.JavascriptInterface
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ext.*
import com.hinnka.tsbrowser.persist.IconMap
import com.hinnka.tsbrowser.persist.Settings
import kotlinx.coroutines.launch

class TSBridge(val webView: TSWebView) {
    private val ANY = "any"
    private val PORTRAIT_PRIMARY = "portrait-primary"
    private val PORTRAIT_SECONDARY = "portrait-secondary"
    private val LANDSCAPE_PRIMARY = "landscape-primary"
    private val LANDSCAPE_SECONDARY = "landscape-secondary"
    private val PORTRAIT = "portrait"
    private val LANDSCAPE = "landscape"

    @SuppressLint("SourceLockedOrientationActivity")
    @JavascriptInterface
    fun requestScreenOrientation(orientation: String) {
        val activity = webView.activity ?: return
        when (orientation) {
            ANY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
            LANDSCAPE_PRIMARY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            PORTRAIT_PRIMARY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            LANDSCAPE -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            PORTRAIT -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
            LANDSCAPE_SECONDARY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
            PORTRAIT_SECONDARY -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            }
        }
    }

    @JavascriptInterface
    fun isDarkMode(): Boolean {
        return Settings.darkMode
    }

    @JavascriptInterface
    fun downFile(url: String?){
        url?.let {
            webView.downloadHandler.onDownloadWithDialog(it, userAgent = webView.settings.userAgentString)
        }
    }


    @JavascriptInterface
    fun runStyleFile(paths: String?) {
        XLog.d("加载样式文件：$paths")
        mainScope.launch {
            paths?.let {
                val js: String = "var styleEl = document.createElement('style'); styleEl.setAttribute('type', 'text/css'); styleEl.appendChild(document.createTextNode(LoreadBridge.readText('$it'))); document.head.appendChild(styleEl);"
                webView.evaluateJavascript(js, null)
            }
        }
    }
    @JavascriptInterface
    fun evaluateJs(js: String?) {
        XLog.d("执行 js：$js")
        mainScope.launch {
            js?.let {
                webView.evaluateJavascript(it, null)
            }
        }
    }
    @JavascriptInterface
    fun runJsFile(paths: String?) {
        mainScope.launch {
            paths?.let {
                App.instance.textFromLocalOrAssets(it).let { js ->
                    webView.evaluateJavascript(js, null)
                }
            }
        }
    }

    @JavascriptInterface
    fun readText(paths: String?):String {
        if(paths.isNullOrBlank()){
            XLog.e("paths 为空")
            return ""
        }else{
            return App.instance.textFromLocalOrAssets(paths)
        }
    }

    @JavascriptInterface
    fun onFetchIconHref(url: String?, href: String?) {
        XLog.e("取到iconurl：" + url + ", " + href)
        if(url.isNullOrBlank() || href.isNullOrBlank()){
            XLog.e("url 为空")
        }else{
            val key = url.host ?: ""
            if (key.isNotBlank()) {
                mainScope.launch {
                    IconMap.fetchIconUrl(App.instance, key, href)
                }
            }
        }
    }
    @JavascriptInterface
    fun onFetchIconData(url:String?, data: String?) {
        XLog.e("取到icon数据：" + url + ", " + data)
        if(url.isNullOrBlank() || data.isNullOrBlank()){
            XLog.e("数据 为空")
        }else{
            val key = url.host ?: ""
            if (key.isNotBlank()) {
                ioScope.launch {
                    val bitmap: Bitmap? = data.dataUrlToBitmap()
                    if(bitmap != null){
                        IconMap.save(key, bitmap)
                    }else{
                        IconMap.ready(url, key)
                    }
                }
            }
        }
    }
    @JavascriptInterface
    fun onFetchIconFail(url:String?) {
        XLog.e("取到 url：" + url)
        if(url.isNullOrBlank()){
            XLog.e("url 为空")
        }else{
            val key = url.host ?: ""
            if (key.isNotBlank()) {
                mainScope.launch {
                    IconMap.fetch(App.instance, key, Uri.parse(url).scheme ?: "https")
                }
            }
        }
    }
}