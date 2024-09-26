package com.zapry.pkdemo.ext

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

internal fun PackageManager.getPackageInfoCompat(packageName: String, flag: Long): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flag))
    } else {
        getPackageInfo(packageName, flag.toInt())
    }
}

internal fun PackageInfo.versionCodeCompat() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    this.longVersionCode
} else {
    versionCode.toLong()
}