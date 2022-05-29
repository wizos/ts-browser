package cc.ibooker.zkeepalivelib.manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import cc.ibooker.zkeepalivelib.ZKeepAlive

/**
 * Android 6.0 Doze模式管理类
 *
 * @author 邹峰立
 */
class DozeManager private constructor() {
    companion object {
        val instance: DozeManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { DozeManager() }
    }

    /**
     * 针对N以上的Doze模式
     */
    @SuppressLint("BatteryLife")
    fun isIgnoreBatteryOption(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ZKeepAlive.isKeepAlive) {
            try {
                val intent = Intent()
                val packageName = context.packageName
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}