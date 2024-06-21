package com.hinnka.tsbrowser.ui.base

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.core.view.isVisible
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.ext.Orientation
import com.hinnka.tsbrowser.ext.orientation
import com.hinnka.tsbrowser.ui.view.VideoContainer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

open class BaseActivity : ComponentActivity() {
    lateinit var videoLayout: VideoContainer
    private val requestCode = AtomicInteger()
    private val callbackMap = mutableMapOf<Int, (resultCode: Int, data: Intent?) -> Unit>()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionCallback: (Map<String, Boolean>) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        videoLayout = VideoContainer(this)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissionCallback.invoke(it)
        }
    }

    suspend fun requestPermissions(permissions: Array<String>): Map<String, Boolean> {
        return suspendCancellableCoroutine { continuation ->
            permissionCallback = {
                continuation.resume(it)
            }
            permissionLauncher.launch(permissions)
        }
    }

    fun startActivityForResult(intent: Intent, callback: (resultCode: Int, data: Intent?) -> Unit) {
        val code = requestCode.getAndIncrement()
        try {
            startActivityForResult(intent, code)
            callbackMap[code] = callback
        } catch (e: Exception) {
            callback.invoke(Activity.RESULT_CANCELED, null)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackMap.remove(requestCode)?.invoke(resultCode, data)
    }



    private val gravitySensorListener = object: SensorEventListener {
        private var lastOrientation = -1
        override fun onSensorChanged(event: SensorEvent) {
            if (!videoLayout.isVisible) {
                return
            }

            // 根据手机屏幕的朝向角度，来设置内容的横竖屏，并且记录状态
            val orientation = event.orientation()
            if (lastOrientation != orientation){
                requestedOrientation = when(orientation){
                    Orientation.ReverseLandscape -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    Orientation.ReversePortrait -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    Orientation.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                lastOrientation = orientation
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }


    private var origOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // Configuration.ORIENTATION_UNDEFINED
    private val mFlags = mutableSetOf<Pair<Int, Int>>()
    fun showFullScreenView(view: View?, callback: WebChromeClient.CustomViewCallback?){
        XLog.d("进入全屏：$view")
        if (view == null) return

        App.instance.sensorManager.apply {
            registerListener(gravitySensorListener, getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_UI)
        }

        // origOrientation = requestedOrientation
        origOrientation = resources.configuration.orientation

        // 旋转屏幕
        // 如何判断自动旋转开关是否开启
        // https://linroid.com/2016/11/09/Pit-of-Android-Orientation/
        // if (!context.autoRotation() && activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
        //     activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // }
        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }else{
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // 保持屏幕常亮
        if (window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON == 0) {
            window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            mFlags.add(Pair(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 0))
        }
        // 开启Window级别的硬件加速
        if (window.attributes.flags and WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED == 0) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            mFlags.add(Pair(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, 0))
        }

        videoLayout.showFullScreen(window, view, callback)
    }
    fun hideFullScreenView(){
        XLog.d("取消全屏")
        videoLayout.hideFullScreen(window)

        App.instance.sensorManager.unregisterListener(gravitySensorListener)
        // if (requestedOrientation != origOrientation) {
        if(resources.configuration.orientation != origOrientation){
            requestedOrientation = origOrientation
        }

        if (mFlags.isNotEmpty()) {
            for (mPair in mFlags) {
                window.setFlags(mPair.first, mPair.second)
            }
            mFlags.clear()
        }
    }
}