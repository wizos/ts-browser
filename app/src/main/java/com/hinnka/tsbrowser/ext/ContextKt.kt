package com.hinnka.tsbrowser.ext

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import com.elvishew.xlog.XLog
import com.hinnka.tsbrowser.App
import com.hinnka.tsbrowser.R
import java.io.*

operator fun Context.get(@StringRes id: Int): String = getString(id)

val Context.notificationManager: NotificationManager?
    get() = getSystemService()

val Context.clipboardManager: ClipboardManager?
    get() = getSystemService()

fun Context.queryExtraActivities(uri: Uri): Intent?{
    var targetIntent: Intent? = null
    val intent = Intent(Intent.ACTION_VIEW, uri)
    val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    if (activities.isNotEmpty()){
        val targetIntents: ArrayList<Intent> = ArrayList()
        for (currentInfo in activities) {
            if(currentInfo.activityInfo.packageName.equals(applicationInfo.packageName, true)){
                continue
            }
            targetIntents.add(Intent(Intent.ACTION_VIEW, uri).setPackage(currentInfo.activityInfo.packageName))
        }

        if (targetIntents.isNotEmpty()){
            targetIntent = Intent.createChooser(
                targetIntents.removeAt(0),
                App.instance.getString(R.string.open_by_outer)
            ).apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            // targetIntent.putExtra(
            //     Intent.EXTRA_INITIAL_INTENTS,
            //     targetIntents.toTypedArray()
            // )
            // intent.putExtra(
            //     Intent.EXTRA_INITIAL_INTENTS,
            //     targetIntents.toArray<Parcelable>(arrayOf<Parcelable>())
            // )
        }
    }
    return targetIntent;
}

fun Context.queryExtraActivityIntents(uri: Uri): MutableList<Intent>{
    val intent = Intent(Intent.ACTION_VIEW, uri)
    val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    val targetIntents: MutableList<Intent> = mutableListOf()
    if (activities.isNotEmpty()){
        for (currentInfo in activities) {
            if(currentInfo.activityInfo.packageName.equals(applicationInfo.packageName, true)){
                continue
            }
            targetIntents.add(Intent(Intent.ACTION_VIEW, uri).setPackage(currentInfo.activityInfo.packageName))
        }
    }
    return targetIntents;
}

fun Context.streamFromLocalOrAssets(path: String): InputStream {
    val localFile = File(getExternalFilesDir(null), path)
    // XLog.d("文件是否在：" + path + "," + localFile.isFile)
    return if (localFile.isFile) {
        FileInputStream(localFile)
    } else {
        return try {
            assets.open(path)
        } catch (e: Exception) {
            XLog.e(e)
            ByteArrayInputStream(byteArrayOf())
        }
    }
}

fun Context.textFromLocalOrAssets(path: String): String {
    return this.streamFromLocalOrAssets(path).use {
        BufferedReader(InputStreamReader(it)).readText()
    }
}