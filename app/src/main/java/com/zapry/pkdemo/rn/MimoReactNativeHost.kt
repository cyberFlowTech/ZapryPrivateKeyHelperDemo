package com.zapry.pkdemo.rn

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultReactNativeHost
import com.zapry.pkdemo.BuildConfig

class MimoReactNativeHost(application: Application) : DefaultReactNativeHost(application) {
    override fun getPackages(): MutableList<ReactPackage> {
        val packages = PackageList(this).packages
        packages.add(MimoReactNativePackage())
        return packages
    }

    override fun getUseDeveloperSupport() = BuildConfig.DEBUG

    override fun getJSMainModuleName() = "index"

    override val isHermesEnabled = true

    override val isNewArchEnabled = false

    companion object {
        @Volatile
        private var instance: MimoReactNativeHost? = null

        fun getInstance(application: Application): MimoReactNativeHost {
            if (instance == null) {
                instance = MimoReactNativeHost(application)
            }
            return instance!!
        }
    }
}