package com.cyberflow.mimolite.lib_private_key.ext

import android.os.Build
import android.os.Bundle


inline fun <reified T : Any> Bundle?.getParcelableExtraCompat(name: String, clazz: Class<T>): T? =
    if (this == null) {
        null
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelable(name, clazz)
    } else {
        this.getParcelable(name)
    }