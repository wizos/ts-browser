package cc.ibooker.zkeepalivelib

import android.content.Context
import cc.ibooker.zkeepalivelib.manager.DozeManager
import cc.ibooker.zkeepalivelib.manager.ForeManager

/**
 * 进程保护管理类
 *
 * @author 邹峰立
 */
class ZKeepAlive private constructor() {

    companion object {
        var isKeepAlive: Boolean = true
        val instance: ZKeepAlive by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ZKeepAlive()
        }
    }

    fun register(context: Context) {
        isKeepAlive = true
        ForeManager.instance.startForeService(context)
    }

    fun unRegister(context: Context) {
        isKeepAlive = false
        ForeManager.instance.stopForeService(context)
    }

}