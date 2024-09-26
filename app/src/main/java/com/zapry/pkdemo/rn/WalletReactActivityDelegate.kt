package com.zapry.pkdemo.rn

import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.defaults.DefaultReactActivityDelegate

class WalletReactActivityDelegate(
    activity: ReactActivity,
    mainComponentName: String,
    fabricEnabled: Boolean,
    val onCreateOptions: (() -> Bundle)
) : DefaultReactActivityDelegate(
    activity,
    mainComponentName,
    fabricEnabled
) {
    override fun getLaunchOptions(): Bundle {
        return onCreateOptions()
    }

    override fun getReactNativeHost(): ReactNativeHost =
        (plainActivity.applicationContext as ReactApplication).reactNativeHost
}