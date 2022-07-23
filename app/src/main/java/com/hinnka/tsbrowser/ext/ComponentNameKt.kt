package com.hinnka.tsbrowser.ext

import android.content.ComponentName
import android.content.pm.PackageManager


fun ComponentName.getAppName(packageManager: PackageManager): CharSequence {
    val appInfo = packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA);
    val appName = appInfo.loadLabel(packageManager);
    return appName
}