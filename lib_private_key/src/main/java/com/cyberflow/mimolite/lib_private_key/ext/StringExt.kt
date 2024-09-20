package com.cyberflow.mimolite.lib_private_key.ext

import com.cyberflow.mimolite.lib_private_key.BuildConfig

/**
 * @title
 * @author Darren.eth
 * @Date
 */
fun String?.masked(): String? {
    return if (BuildConfig.DEBUG) {
        "*** ${this.orEmpty().length} bytes ***"
    } else {
        this
    }
}