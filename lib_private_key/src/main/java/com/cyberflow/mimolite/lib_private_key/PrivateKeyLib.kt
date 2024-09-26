package com.cyberflow.mimolite.lib_private_key

import android.annotation.SuppressLint
import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * @title
 * @author Darren.eth
 * @Date
 */
class PrivateKeyLib {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var userId: String

        fun initialize(context: Context, userId: String) {
            this.context = context
            this.userId = userId

            MMKV.initialize(context)
        }
    }
}