package com.hinnka.tsbrowser

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import androidx.compose.material.SnackbarHostState
import androidx.core.content.getSystemService
import cc.ibooker.zkeepalivelib.ZKeepAlive
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.LogLevel.*
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.Printer
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import com.hinnka.tsbrowser.ext.logD
import com.hinnka.tsbrowser.ext.logE
import com.hinnka.tsbrowser.persist.Bookmark
import com.hinnka.tsbrowser.persist.Favorites
import com.hinnka.tsbrowser.persist.Settings
import com.tencent.mmkv.MMKV
import io.reactivex.plugins.RxJavaPlugins
import zlc.season.rxdownload4.recorder.RxDownloadRecorder
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*


class App : Application() {
    val sensorManager by lazy { instance.getSystemService<SensorManager>()!! }
    val imm by lazy { instance.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    override fun onCreate() {
        super.onCreate()
        instance = this
        MMKV.initialize(this)
        initLog()
        XLog.d("$processName onCreate")
        configWebViewCacheDirWithAndroidP()
        RxJavaPlugins.setErrorHandler {
            logE("RxJava run error", throwable = it)
        }
        sendBroadcast(Intent("${packageName}.action.secret").apply {
            `package` = packageName
        })
        initBrowser()
        if(BuildConfig.DEBUG) ZKeepAlive.instance.register(this)

        if (Settings.keepAlive) {
            ZKeepAlive.instance.register(this)
        }

        // DoKit.Builder(this)
        //     // .productId(BuildConfig.DORAEMON_KIT_PRODUCT_ID)
        //     .build()

        RxDownloadRecorder
        XLog.d("$processName onCreate complete")
    }

    private fun initBrowser() {
        //FIXME hack for RxDownload init in sub process
        if (processName != packageName) {
            contentResolver.insert(Uri.parse("content://$packageName.claritypotion/start"), null)
        }
        Bookmark.init()
        Favorites.init()
    }

    private fun initLog(){
        val config = LogConfiguration.Builder()
            .logLevel(if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.INFO)
            // .tag(Contract.LOREAD)
            .enableStackTrace(1)
            // .stackTraceFormatter(SingleStackTraceFormatter())
            .build()

        val androidPrinter: Printer = AndroidPrinter()
        val filePrinter: Printer =
            FilePrinter.Builder(externalCacheDir.toString() + "/log/") // 指定保存日志文件的路径
                .flattener(ClassicFlattener())
                .fileNameGenerator(DateFileNameGenerator()) // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                // .backupStrategy(new NeverBackupStrategy())         // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
                // .shouldBackup()
                .cleanStrategy(FileLastModifiedCleanStrategy(3 * 24 * 3600 * 1000)) // 指定日志文件清除策略，默认为 NeverCleanStrategy()
                .build()
        // 初始化 XLog
        XLog.init(config, androidPrinter, filePrinter)
    }

    private fun configWebViewCacheDirWithAndroidP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            if (packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }
    }

    companion object {
        @JvmStatic
        lateinit var instance: App

        val snackBarHostState = SnackbarHostState()

        val processName: String by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) getProcessName() else try {
                @SuppressLint("PrivateApi")
                val activityThread = Class.forName("android.app.ActivityThread")
                val methodName = "currentProcessName"
                val getProcessName: Method = activityThread.getDeclaredMethod(methodName)
                getProcessName.invoke(null) as String
            } catch (e: ClassNotFoundException) {
                throw RuntimeException(e)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e)
            }
        }

        val isSecretMode: Boolean by lazy { processName.endsWith("secret") }

        @SuppressLint("ConstantLocale")
        val isCN: Boolean = Locale.getDefault().country.uppercase(Locale.ROOT) == "CN"
    }

    fun versionName(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (unused: PackageManager.NameNotFoundException) {
            ""
        }
    }

}
