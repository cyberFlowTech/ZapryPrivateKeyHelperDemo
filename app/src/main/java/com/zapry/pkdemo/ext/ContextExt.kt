package com.zapry.pkdemo.ext

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager

/**
 * @title
 * @author Darren.eth
 * @Date
 */
fun Context.versionCode(): Long = try {
    packageManager.getPackageInfoCompat(packageName, 0).versionCodeCompat()
} catch (e: PackageManager.NameNotFoundException) {
    e.printStackTrace()
    0
}