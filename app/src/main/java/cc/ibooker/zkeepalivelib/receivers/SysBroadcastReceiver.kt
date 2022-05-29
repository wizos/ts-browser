package cc.ibooker.zkeepalivelib.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cc.ibooker.zkeepalivelib.ZKeepAlive
import cc.ibooker.zkeepalivelib.manager.ForeManager

/**
 * 监听系统广播
 *
 * @author 邹峰立
 */
class SysBroadcastReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (ZKeepAlive.isKeepAlive) {
            context?.run {
                // 启动前置服务
                ForeManager.instance.startForeService(context)
            }
        }
    }

}