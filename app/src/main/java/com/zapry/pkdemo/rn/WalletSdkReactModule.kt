package com.zapry.pkdemo.rn

import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import java.lang.ref.WeakReference

class WalletSdkReactModule private constructor(context: ReactApplicationContext) :
    AbsReactNativeModule(context) {

    companion object {
        private const val TAG = "WalletSdkReactModule"

        @Volatile
        private var weakInstance: WeakReference<WalletSdkReactModule?>? = null

        fun createInstance(context: ReactApplicationContext): WalletSdkReactModule {
            return WalletSdkReactModule(context).also {
                weakInstance = WeakReference(it)
            }
        }

        fun getInstance(): WalletSdkReactModule? {
            return weakInstance?.get()
        }
    }

    override fun getName(): String = "WalletSdkRNModule"

    @ReactMethod
    override fun action(action: String, params: String?, promise: Promise) {
        Log.d(
            TAG,
            "action() called with: action = $action, params = $params, promise = $promise"
        )
        super.action(action, params, promise)
    }
}