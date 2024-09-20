package com.cyberflow.mimolite.lib_private_key

import android.annotation.SuppressLint
import android.content.Context

/**
 * @title
 * @author Darren.eth
 * @Date
 */
class PrivateKeyApp {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var userId: String

        fun initialize(context: Context, userId: String) {
            this.context = context
            this.userId = userId
        }
    }
}