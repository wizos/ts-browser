package com.hinnka.tsbrowser.web

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Message
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import android.security.KeyChainException
import android.view.KeyEvent
import android.webkit.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.adblock.AdBlocker
import com.hinnka.tsbrowser.ext.getAppName
import com.hinnka.tsbrowser.persist.LocalStorage
import com.hinnka.tsbrowser.ui.composable.widget.AlertBottomSheet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit


class TSWebClient(private val controller: UIController) : WebViewClient() {
    companion object {
        val localSchemes = arrayOf("http", "https", "ftp", "file", "about", "data", "javascript")
        val sslKeepLastDuration = TimeUnit.SECONDS.toMillis(15)
    }

    private var sslLastAllow = false
    private var sslLastTime = 0L

    private val interceptUrls = mutableSetOf<String>()

    /**
     * @return
     * 返回 true 表示你已经处理此次请求。
     * 返回 false 表示由 webview 自行处理（一般都是把此url加载出来）。
     * 返回 super.shouldOverrideUrlLoading(view, url); 这个返回的方法会调用父类方法，也就是跳转至手机浏览器
     */
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        XLog.d("shouldOverrideUrlLoading $uri")

        if (localSchemes.contains(uri.scheme)) {
            return controller.shouldOverrideUrlLoading(view, request)
        }
        var intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
        var componentName = intent.resolveActivity(view.context.packageManager)
        if (componentName == null) {
            intent.`package`?.let {
                intent = Intent.parseUri("market://search?q=pname:$it", Intent.URI_INTENT_SCHEME)
                componentName = intent.resolveActivity(view.context.packageManager)
            }
        }
        if (componentName == null) {
            return true
        }
        MainScope().launch {
            when(App.snackBarHostState.showSnackbar(
                view.context.getString(R.string.webpage_request_to_launch_x, componentName.getAppName(view.context.packageManager)),
                view.context.getString(android.R.string.ok),
                SnackbarDuration.Short
            )){
                SnackbarResult.ActionPerformed -> {
                    try {
                        view.context.startActivity(intent)
                    } catch (e: Exception) {
                        XLog.e(e)
                    }
                }
                else -> {}
            }
        }
        return true
    }

    // override fun onLoadResource(view: WebView, url: String) {
    //     super.onLoadResource(view, url)
    //     // XLog.e("onLoadResource $url")
    // }

    // private val gson = Gson()
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        // XLog.e("shouldInterceptRequest ${request.url} " )
        val host = request.url?.host ?: return null
        if (AdBlocker.shouldBlock(host)) {
            if (!interceptUrls.contains(host)) {
                LocalStorage.blockTimes++
                interceptUrls.add(host)
            }
            return AdBlocker.emptyResponse
        }
        return null
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        controller.onPageStarted(url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        controller.onPageFinished(url)
    }

    // override fun onReceivedError(
    //     view: WebView?,
    //     errorCode: Int,
    //     description: String?,
    //     failingUrl: String?
    // ) {
    //     XLog.e("onReceivedError 1 ")
    //     super.onReceivedError(view, errorCode, description, failingUrl)
    // }

    // override fun onReceivedError(
    //     view: WebView?,
    //     request: WebResourceRequest?,
    //     error: WebResourceError?
    // ) {
    //     XLog.e("onReceivedError 2 ")
    //     super.onReceivedError(view, request, error)
    // }
    //
    // override fun onReceivedHttpError(
    //     view: WebView,
    //     request: WebResourceRequest,
    //     response: WebResourceResponse
    // ) {
    //     XLog.e("http error ${request.url} ->${response.statusCode}: ${response.reasonPhrase}")
    // }

    /**
     * 通知主程序处理SSL客户端认证请求。如果需要提供密钥，主程序负责显示UI界面。
     * 有三个响应方法：proceed(), cancel() 和 ignore()。
     * 如果调用proceed()和cancel()，webview将会记住response，
     * 对相同的host和port地址不再调用 onReceivedClientCertRequest 方法。
     * 如果调用ignore()方法，webview则不会记住response。该方法在UI线程中执行，
     * 在回调期间，连接被挂起。默认cancel()，即无客户端认证
     */
    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        XLog.e("onReceivedSslError ")
        handler.proceed()
        // if (System.currentTimeMillis() - sslLastTime <= sslKeepLastDuration) {
        //     if (sslLastAllow) {
        //         handler.proceed()
        //     } else {
        //         handler.cancel()
        //     }
        //     return
        // }
        // AlertBottomSheet.Builder(view.context).apply {
        //     setTitle(R.string.warning)
        //     setMessage(R.string.insecue_message)
        //     setPositiveButton(android.R.string.yes) {
        //         sslLastAllow = true
        //         sslLastTime = System.currentTimeMillis()
        //         handler.proceed()
        //     }
        //     setNegativeButton(android.R.string.no) {
        //         sslLastAllow = false
        //         sslLastTime = System.currentTimeMillis()
        //         handler.cancel()
        //     }
        // }.show()
    }

    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest) {
        XLog.e("onReceivedClientCertRequest ${request.host}  ${request.keyTypes}  ${request.principals}")

        view?.apply {
            // KeyChain.choosePrivateKeyAlias(context as Activity, { alias ->
            //     if (alias == null) {
            //         request.ignore()
            //     } else {
            //         val privateKey: PrivateKey? = KeyChain.getPrivateKey(context, alias)
            //         val certificateChain: Array<X509Certificate>? = KeyChain.getCertificateChain(context, alias)
            //         request.proceed(privateKey, certificateChain)
            //     }
            // }, arrayOf<String>(request.keyTypes!![0]), null, null, -1, null)

            KeyChain.choosePrivateKeyAlias(context as Activity, { alias ->
                if (alias == null) {
                    request.ignore()
                } else {
                    val privateKey: PrivateKey?
                    val certificateChain: Array<X509Certificate>?
                    try {
                        privateKey = KeyChain.getPrivateKey(context, alias)
                        certificateChain = KeyChain.getCertificateChain(context, alias)
                        request.proceed(privateKey, certificateChain)
                    } catch (e: InterruptedException) {
                        request.ignore()
                    } catch (e: KeyChainException) {
                        request.ignore()
                    }
                }
            }, request.keyTypes, request.principals, request.host, request.port, null)
        }
    }

    override fun onReceivedLoginRequest(
        view: WebView,
        realm: String?,
        account: String?,
        args: String?
    ) {
        XLog.e("onReceivedLoginRequest ${realm}")
        super.onReceivedLoginRequest(view, realm, account, args)
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView,
        handler: HttpAuthHandler,
        host: String,
        realm: String
    ) {
        XLog.e("onReceivedHttpAuthRequest ${realm}")
        var user by mutableStateOf("")
        var password by mutableStateOf("")
        AlertBottomSheet.Builder(view.context).apply {
            setView {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = host, maxLines = 1, color = MaterialTheme.colors.onPrimary)
                    OutlinedTextField(value = user, onValueChange = {
                        user = it
                    }, placeholder = { Text(text = stringResource(id = R.string.username)) })
                    OutlinedTextField(value = password, onValueChange = {
                        password = it
                    }, placeholder = { Text(text = stringResource(id = R.string.password)) })
                }
            }
            setTitle(R.string.signin)
            setPositiveButton(android.R.string.ok) {
                handler.proceed(user, password)
            }
            setNegativeButton(android.R.string.cancel) {
                handler.cancel()
            }
        }.show()
    }

    override fun onFormResubmission(view: WebView, dontResend: Message, resend: Message) {
        XLog.e("onFormResubmission ${resend.target}")
        AlertBottomSheet.Builder(view.context).apply {
            setTitle(R.string.form_resubmission)
            setMessage(R.string.resend_data)
            setCancelable(false)
            setPositiveButton(android.R.string.yes) {
                resend.sendToTarget()
            }
            setNegativeButton(android.R.string.no) {
                dontResend.sendToTarget()
            }
        }.show()
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        controller.doUpdateVisitedHistory(url, isReload)
    }

    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        return super.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
        super.onUnhandledKeyEvent(view, event)
    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        super.onScaleChanged(view, oldScale, newScale)
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail?): Boolean {
        val activity = view.context as? Activity
        activity?.recreate()
        return true
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        super.onPageCommitVisible(view, url)
    }

    // override fun onReceivedError(
    //     view: WebView,
    //     request: WebResourceRequest,
    //     error: WebResourceError
    // ) {
    //     super.onReceivedError(view, request, error)
    //     if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_CODE)) {
    //         if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_DESCRIPTION)) {
    //             logE(
    //                 "onReceivedError: ${request.url} ${error.errorCode} ${error.description}"
    //             )
    //         }
    //     }
    // }
    //
    // override fun onSafeBrowsingHit(
    //     view: WebView,
    //     request: WebResourceRequest,
    //     threatType: Int,
    //     callback: SafeBrowsingResponseCompat
    // ) {
    //     super.onSafeBrowsingHit(view, request, threatType, callback)
    // }
}
