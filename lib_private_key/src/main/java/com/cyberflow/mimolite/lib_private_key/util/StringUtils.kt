package com.cyberflow.mimolite.lib_private_key.util

import android.util.Base64

/**
 * @title
 * @author Darren.eth
 * @Date
 */

object StringUtils {
    fun encodeToBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    fun decodeFromBase64(base64String: String): ByteArray {
        return Base64.decode(base64String, Base64.DEFAULT)
    }

    fun wrapBase64(data: String): String {
        return encodeToBase64(data.toByteArray(Charsets.UTF_8))
    }

    fun unwrapBase64(base64String: String): String {
        return decodeFromBase64(base64String).toString(Charsets.UTF_8)
    }
}