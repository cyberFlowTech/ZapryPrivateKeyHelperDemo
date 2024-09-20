package com.cyberflow.mimolite.lib_private_key.ext

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * @title
 * @author Darren.eth
 * @Date
 */

private const val TAG = "CoroutineExt"

private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    val s = throwable.stackTraceToString()
    Log.e(TAG, s)
}

object IoScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO + coroutineExceptionHandler
}