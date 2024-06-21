package com.hinnka.tsbrowser.web

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.hinnka.tsbrowser.*
import com.hinnka.tsbrowser.download.DownloadHandler
import com.hinnka.tsbrowser.ext.*
import com.hinnka.tsbrowser.persist.Settings
import com.hinnka.tsbrowser.ui.base.BaseActivity
import com.hinnka.tsbrowser.ui.composable.main.LongPressInfo
import com.hinnka.tsbrowser.ui.home.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume


val longPressType = arrayOf(
    WebView.HitTestResult.EMAIL_TYPE,
    WebView.HitTestResult.PHONE_TYPE,
    WebView.HitTestResult.SRC_ANCHOR_TYPE)
val longPressImg = arrayOf(WebView.HitTestResult.IMAGE_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)

@SuppressLint("SetJavaScriptEnabled")
class TSWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FastScrollWebView(context, attrs), UIController, LifecycleOwner {
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private val lifecycleRegistry = LifecycleRegistry(context as LifecycleOwner)
    // private var fullScreenView: ViewGroup? = null
    // private var origOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    val downloadHandler = DownloadHandler(context)
    var dataListener: WebDataListener? = null


    var goForwardUrls = mutableListOf<String>()


    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(event: MotionEvent) {
                val type = hitTestResult.type
                val extra = hitTestResult.extra
                logD("webview hitResult", type, extra)

                if(longPressType.contains(hitTestResult.type)){
                    dataListener?.longPressState?.value =
                        LongPressInfo(true, event.x.toInt(), event.y.toInt(), type, extra ?: "")
                }else if(longPressImg.contains(hitTestResult.type)){
                    buildDrawingCache()
                    dataListener?.longPressState?.value =
                        LongPressInfo(true, event.x.toInt(), event.y.toInt(), type, extra ?: "", Bitmap.createBitmap(drawingCache, 0, 0, drawingCache.width, drawingCache.height))
                    destroyDrawingCache()
                }
            }
        })

    init {
        setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }

    init {
        isFocusableInTouchMode = true
        isFocusable = true
        isHorizontalScrollBarEnabled = true
        isVerticalScrollBarEnabled = true
        scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        isScrollbarFadingEnabled = true
        isScrollContainer = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        }
        isSaveEnabled = true

        settings.apply {
            allowContentAccess = true
            // 允许访问文件
            allowFileAccess = true
            // 通过 file url 加载的 Javascript 读取其他的本地文件 .建议关闭
            allowFileAccessFromFileURLs = false
            // 允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源
            allowUniversalAccessFromFileURLs = true
            builtInZoomControls = true
            databaseEnabled = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            domStorageEnabled = true
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            setSupportZoom(true)

            // 安全浏览策略：访问不良网站会拦截并发出警告
            if (Build.VERSION.SDK_INT >= 26) safeBrowsingEnabled = false

            setGeolocationEnabled(true)
            setGeolocationDatabasePath(File(context.filesDir, GeoDB).path)
            setSupportMultipleWindows(true)
            addJavascriptInterface(TSBridge(this@TSWebView), LoreadBridge)

            userAgentString = Settings.userAgent.value
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptThirdPartyCookies(this, Settings.acceptThirdPartyCookies)

        // initForWeb()

        setDarkMode(Settings.darkMode)
        setIncognito(Settings.incognito)

        setDownloadListener(downloadHandler)

        webChromeClient = TSChromeClient(this)
        webViewClient = TSWebClient(this)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    // init {
    //     val cookieManager = CookieManager.getInstance()
    //     cookieManager.setAcceptThirdPartyCookies(this, Settings.acceptThirdPartyCookies)
    // }

    fun setDarkMode(dark: Boolean) {
        if (dark) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
            }
        }
    }

    fun setIncognito(value: Boolean) {
        if (value) {
            CookieManager.getInstance().setAcceptCookie(false)
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            clearHistory()
            clearCache(true)
            clearFormData()
            settings.savePassword = false
            settings.saveFormData = false
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.savePassword = true
            settings.saveFormData = true
        }
    }

    override fun loadUrl(url: String) {
        if (Settings.dnt) {
            super.loadUrl(url, mapOf("DNT" to "1"))
        } else {
            super.loadUrl(url)
        }
    }

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) {
        if (Settings.dnt) {
            additionalHttpHeaders["DNT"] = "1"
        }
        super.loadUrl(url, additionalHttpHeaders)
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        super.loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause() {
        super.onPause()
        evaluateJavascript("if(window.localStream){window.localStream.stop();}", null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun onDestroy() {
        val parent = parent as? ViewGroup
        parent?.removeView(this)
        stopLoading()
        onPause()
        removeAllViews()
        if (Settings.incognito) {
            clearHistory()
        }
        destroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun handleLongPress(event: MotionEvent) {
        val type = hitTestResult.type
        val extra = hitTestResult.extra
        logD("webview hitResult", type, extra)
        dataListener?.longPressState?.value =
            LongPressInfo(true, event.x.toInt(), event.y.toInt(), type, extra ?: "")
    }

    fun generatePreview() {
        if (width == 0 || height == 0) {
            return
        }
        val currentUrl = url
        ioScope.launch {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            if (currentUrl == "about:blank") {
                val window = (context as? Activity)?.window ?: return@launch
                val insets = ViewCompat.getRootWindowInsets(window.decorView)
                val height = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
                canvas.translate(0f, -1f * height)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val windowBitmap = Bitmap.createBitmap(window.decorView.width, window.decorView.height, Bitmap.Config.RGB_565)
                    PixelCopy.request(window, windowBitmap, {}, handler)
                    canvas.drawBitmap(windowBitmap, 0f, 0f, null)
                } else {
                    canvas.drawBitmap(window.decorView.drawingCache, 0f, 0f, null)
                }
            } else {
                canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())
                draw(canvas)
            }
            dataListener?.previewState?.value = bitmap
            dataListener?.updateInfo()
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url.toString()
        if(goForwardUrls.isNotEmpty() && uri == goForwardUrls.first() && canGoForward()){
            goForward()
            goForwardUrls.removeFirst()
            return true
        }else{
            goForwardUrls.clear()
            return false
        }
    }
    override fun onProgressChanged(progress: Int) {
        dataListener?.progressState?.value = progress * 1.0f / 100
    }

    override fun onReceivedTitle(title: String?) {
        // 修复 web title 获取为空的问题
        dataListener?.titleState?.value = if (title == "about:blank") context.getString(R.string.new_tab) else title ?: copyBackForwardList().currentItem?.title ?: ""
    }

    override fun onReceivedIcon(icon: Bitmap?) {
        dataListener?.onReceivedIcon(icon)
    }

    override fun onShowCustomView(
        view: View,
        requestedOrientation: Int,
        callback: WebChromeClient.CustomViewCallback
    ) {
        // origOrientation = requestedOrientation
        // post {
        //     val resolver = context.contentResolver
        //     val autoRotationOff = android.provider.Settings.System.getInt(
        //         resolver,
        //         android.provider.Settings.System.ACCELEROMETER_ROTATION
        //     ) == 0
        //     if (autoRotationOff && activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
        //         activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //     }
        // }
        // view.removeFromParent()
        // if (fullScreenView != null) {
        //     callback.onCustomViewHidden()
        // }
        //
        // val parentView = (context as? MainActivity)?.videoLayout
        // parentView?.removeAllViews()
        // parentView?.addView(view, FrameLayout.LayoutParams(-1, -1))
        // parentView?.isVisible = true
        // try {
        //     parentView?.keepScreenOn = true
        // } catch (e: Exception) {
        //     e.printStackTrace()
        // }
        // (context as? MainActivity)?.window?.let { fullScreenView?.setFullScreen(it, true) }
        // fullScreenView = parentView

        (context as? MainActivity)?.showFullScreenView(view, callback)
    }

    override fun onHideCustomView() {
        (context as? MainActivity)?.hideFullScreenView()

        // try {
        //     fullScreenView?.keepScreenOn = false
        // } catch (e: Exception) {
        //     e.printStackTrace()
        // }
        // // fullScreenView?.setFullScreen(false)
        // (context as? MainActivity)?.window?.let { fullScreenView?.setFullScreen(it, true) }
        // fullScreenView?.isVisible = false
        // fullScreenView?.removeAllViews()
        // if (activity?.requestedOrientation != origOrientation) {
        //     activity?.requestedOrientation = origOrientation
        // }
        // fullScreenView = null
        //
        // //如果开启
        // if (App.instance.imm.isActive) {
        //     //关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
        //     // App.instance.imm.toggleSoftInput(
        //     //     InputMethodManager.SHOW_IMPLICIT,
        //     //     InputMethodManager.HIDE_NOT_ALWAYS
        //     // )
        //     App.instance.imm.hideSoftInputFromWindow(applicationWindowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        // }
    }

    override fun onCreateWindow(resultMsg: Message): Boolean {
        dataListener?.onCreateWindow(resultMsg)
        return true
    }

    override fun onCloseWindow() {
        dataListener?.onCloseWindow()
    }

    override suspend fun requestPermissions(permissions: Array<String>): Map<String, Boolean> {
        val activity = context as? BaseActivity
        return activity?.requestPermissions(permissions) ?: mapOf()
    }

    override suspend fun showFileChooser(fileChooserParams: WebChromeClient.FileChooserParams): Array<Uri>? {
        val intent = fileChooserParams.createIntent()
        val activity = context as? BaseActivity ?: return null
        return suspendCancellableCoroutine { continuation ->
            activity.startActivityForResult(intent) { resultCode, data ->
                val uris = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                continuation.resume(uris)
            }
        }
    }

    override fun onPageStarted(url: String, favicon: Bitmap?) {
        // 修复 web title 获取为空的问题
        dataListener?.titleState?.value = copyBackForwardList().currentItem?.title.toString()
    }

    override fun onPageFinished(url: String) {
        // 修复 web title 获取为空的问题
        if (dataListener?.titleState?.value == null) {
            dataListener?.titleState?.value = copyBackForwardList().currentItem?.title.toString()
        } else if (dataListener?.titleState?.value == "about:blank") {
            dataListener?.titleState?.value = context.getString(R.string.new_tab)
        }

        evaluateJavascript(App.instance.textFromLocalOrAssets(JsFilePath.loaderWeb), null)
    }

    override fun doUpdateVisitedHistory(url: String, isReload: Boolean) {
        dataListener?.doUpdateVisitedHistory(url, isReload)
    }

}