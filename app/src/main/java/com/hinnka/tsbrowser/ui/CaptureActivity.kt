package com.hinnka.tsbrowser.ui

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.google.zxing.Result
import com.hinnka.tsbrowser.R
import com.hinnka.tsbrowser.ext.toUrl
import com.hinnka.tsbrowser.tab.TabManager
import com.king.zxing.CameraScan
import com.king.zxing.CameraScan.OnScanResultCallback
import com.king.zxing.DefaultCameraScan
import com.king.zxing.util.CodeUtils
import com.king.zxing.util.LogUtils
import com.king.zxing.util.PermissionUtils


class CaptureActivity : AppCompatActivity(), OnScanResultCallback {
    var previewView: PreviewView? = null
    var ivFlashlight: View? = null

    private var cameraScan: CameraScan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_code_activity)
        initUI()
    }

    /**
     * 初始化
     */
    private fun initUI() {
        previewView = findViewById(R.id.previewView)

        ivFlashlight = findViewById(R.id.ivFlashlight)
        ivFlashlight!!.setOnClickListener { onClickFlashlight() }

        val photoAlbumButton = findViewById<ImageView>(R.id.ivPhotoAlbum)
        photoAlbumButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            photoAlbumLauncher.launch(intent)
        }

        initCameraScan()
        startCamera()
    }

    // var photoAlbumCallback: (String) -> Unit = {}
    private val photoAlbumLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // 得到图片的全路径
                val uri: Uri? = result.data?.data
                uri?.let {
                    BitmapFactory.decodeStream(contentResolver.openInputStream(it))?.let { bitmap ->
                        CodeUtils.parseCode(bitmap)?.let { qrResult ->
                            finish()
                            TabManager.open(this, qrResult.toUrl(), true)
                        }
                    }
                }
                // XLog.d("扫图 data = $uri , " )
            }
        }

    /**
     * 点击手电筒
     */
    private fun onClickFlashlight() {
        toggleTorchState()
    }

    /**
     * 初始化CameraScan
     */
    private fun initCameraScan() {
        cameraScan = DefaultCameraScan(this, previewView!!)
        (cameraScan as DefaultCameraScan).setOnScanResultCallback(this)
    }

    /**
     * 启动相机预览
     */
    private fun startCamera() {
        if (cameraScan != null) {
            if (PermissionUtils.checkPermission(this, Manifest.permission.CAMERA)) {
                cameraScan!!.startCamera()
            } else {
                LogUtils.d("checkPermissionResult != PERMISSION_GRANTED")
                PermissionUtils.requestPermission(
                    this,
                    Manifest.permission.CAMERA,
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * 释放相机
     */
    private fun releaseCamera() {
        if (cameraScan != null) {
            cameraScan!!.release()
        }
    }

    /**
     * 切换闪光灯状态（开启/关闭）
     */
    private fun toggleTorchState() {
        if (cameraScan != null) {
            val isTorch = cameraScan!!.isTorchEnabled
            cameraScan!!.enableTorch(!isTorch)
            if (ivFlashlight != null) {
                ivFlashlight!!.isSelected = !isTorch
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            requestCameraPermissionResult(permissions, grantResults)
        }
    }

    /**
     * 请求Camera权限回调结果
     * @param permissions
     * @param grantResults
     */
    private fun requestCameraPermissionResult(permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtils.requestPermissionsResult(
                Manifest.permission.CAMERA,
                permissions,
                grantResults
            )
        ) {
            startCamera()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        releaseCamera()
        super.onDestroy()
    }

    /**
     * 接收扫码结果回调
     * @param result 扫码结果
     * @return 返回true表示拦截，将不自动执行后续逻辑，为false表示不拦截，默认不拦截
     */
    override fun onScanResultCallback(result: Result): Boolean {
        return false
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 0X86
    }
}