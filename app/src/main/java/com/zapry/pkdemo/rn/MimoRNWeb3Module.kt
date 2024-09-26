package com.zapry.pkdemo.rn

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import java.lang.ref.WeakReference

class MimoRNWeb3Module private constructor(context: ReactApplicationContext) :
    AbsReactNativeModule(context) {

    companion object {
        private const val TAG = "MimoRNWeb3Module"

        @Volatile
        private var weakInstance: WeakReference<MimoRNWeb3Module?>? = null

        fun createInstance(context: ReactApplicationContext): MimoRNWeb3Module {
            return MimoRNWeb3Module(context)
                .also {
                weakInstance = WeakReference(it)
            }
        }

        fun getInstance(): MimoRNWeb3Module? {
            return weakInstance?.get()
        }
    }

    override fun getName(): String = "MimoRNWeb3Module"

    @ReactMethod
    override fun resolveTask(resolvedId: String?, result: String?) {
        super.resolveTask(resolvedId, result)
    }

    @ReactMethod
    override fun rejectTask(resolvedId: String?, error: String?) {
        super.rejectTask(resolvedId, error)
    }
}