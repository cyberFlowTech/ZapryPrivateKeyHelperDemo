package com.zapry.pkdemo

import android.app.Application
import com.cyberflow.mimolite.lib_private_key.PrivateKeyLib
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.zapry.pkdemo.app.RNApplicationDelegate
import com.zapry.pkdemo.rn.MimoReactNativeHost

/**
 * @title
 * @author Darren.eth
 * @Date
 */
class App : Application(), ReactApplication {

    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        PrivateKeyLib.initialize(this, TEST_USER_ID)

        val rnApp = RNApplicationDelegate()
        rnApp.init(this)
        rnApp.onCreate()

    }

    override fun getReactNativeHost(): ReactNativeHost {
        return MimoReactNativeHost.getInstance(this)
    }

}