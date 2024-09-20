package com.cyberflow.mimolite.lib_private_key.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

private const val TAG = "KeyboardUtil"

object KeyboardUtil {

    @JvmStatic
    fun show(et: EditText) {
        et.requestFocus()
        val context = et.context
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
    }
}