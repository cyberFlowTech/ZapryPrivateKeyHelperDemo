package com.zapry.pkdemo.rn

import android.content.Context
import com.facebook.react.ReactInstanceManager

/**
 * Flipper 主要用于开发和调试阶段，它可以帮助你更快地定位和解决问题，但也增加了额外的资源消耗。因此，不应包含在生产版本的应用中。
 * 生产版此类所有函数都是空实现
 * Class responsible of loading Flipper inside your React Native application. This is the release
 * flavor of it so it's empty as we don't want to load Flipper.
 */
object ReactNativeFlipper {
    fun initializeFlipper(context: Context?, reactInstanceManager: ReactInstanceManager?) {
        // Do nothing as we don't want to initialize Flipper on Release.
    }

    fun stop() {
        // 正式版不做任何操作
    }
}