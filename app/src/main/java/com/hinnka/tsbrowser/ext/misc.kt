package com.hinnka.tsbrowser.ext

import com.elvishew.xlog.XLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
    logE("$context execute error, thread: ${Thread.currentThread().name}", throwable = throwable)
}

val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)
val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

inline fun logD(vararg message: Any?) {
    XLog.d("TSBrowser" + message.contentDeepToString())
}

inline fun logE(vararg message: Any?, throwable: Throwable = Throwable()) {
    XLog.e("TSBrowser" + message.contentDeepToString() + throwable)
}