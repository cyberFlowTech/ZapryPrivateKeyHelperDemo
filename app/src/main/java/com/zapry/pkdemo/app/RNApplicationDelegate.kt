package com.zapry.pkdemo.app

import android.content.Context
import android.util.Log
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.react.ReactNativeHost
import com.facebook.soloader.SoLoader
import com.zapry.pkdemo.rn.MimoReactNativeHost

private const val TAG = "RNApplicationDelegate"


//@Route(path = PageConst.App.APPLICATION_DELEGATE, name = "RNWalletApplicationDelegate")
class RNApplicationDelegate : AbsReactApplication(), ApplicationDelegate {

    fun init(context: Context?) {
        attachBaseContext(/*RNBaseContext*/(context))
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate() called")

        Fresco.initialize(this)
        SoLoader.init(this, false)
    }

    override fun getReactNativeHost(): ReactNativeHost = MimoReactNativeHost.getInstance(this)
}