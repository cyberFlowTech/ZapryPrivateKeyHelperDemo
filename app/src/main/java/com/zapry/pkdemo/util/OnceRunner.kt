package com.zapry.pkdemo.util

class OnceRunner {
    private var ran = false
    fun run(block: () -> Unit) {
        if (!ran) {
            ran = true
            block.invoke()
        }
    }
}