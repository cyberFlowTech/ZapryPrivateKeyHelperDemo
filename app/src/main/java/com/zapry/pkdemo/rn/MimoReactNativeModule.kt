package com.zapry.pkdemo.rn

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import java.lang.ref.WeakReference

open class MimoReactNativeModule private constructor(context: ReactApplicationContext) :
    AbsReactNativeModule(context) {
    companion object {

        @Volatile
        private var weakInstance: WeakReference<MimoReactNativeModule?>? = null

        fun createInstance(context: ReactApplicationContext): MimoReactNativeModule {
            return MimoReactNativeModule(context).also {
                weakInstance = WeakReference(it)
            }
        }

        fun getInstance(): MimoReactNativeModule? {
            return weakInstance?.get()
        }
    }

    override fun getName() = "MimoReactNativeModule"

    @ReactMethod
    override fun action(action: String, params: String?, promise: Promise) {
        super.action(action, params, promise)
    }
}