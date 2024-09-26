package com.zapry.pkdemo.rn

import android.util.Log
import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager

private const val TAG = "MimoReactNativePackage"

class MimoReactNativePackage : ReactPackage {
    override fun createNativeModules(p0: ReactApplicationContext): MutableList<NativeModule> {
        Log.i(TAG, "createNativeModules() called with: p0 = $p0")
        val list = mutableListOf<NativeModule>()
        val iterator = list.iterator()
        iterator.forEach {
            if (it is AbsReactNativeModule) {
                Log.i(TAG, "createNativeModules() called remove last MimoReactNativeModule")
                // 原来添加的MimoReactNative
                iterator.remove()
            }
        }
        list.add(MimoReactNativeModule.createInstance(p0))
        list.add(WalletSdkReactModule.createInstance(p0))
        list.add(MimoRNWeb3Module.createInstance(p0))
        return list
    }

    override fun createViewManagers(p0: ReactApplicationContext) =
        mutableListOf<ViewManager<View, ReactShadowNode<*>>>()
}