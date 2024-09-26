package com.zapry.pkdemo.app

import android.os.Bundle

interface IWindowActionDelegate : IWindowActionApi {
    fun onCreate(savedInstanceState: Bundle?)
    fun onPause()
    fun onDestroy()
}

interface IWindowActionApi {
    fun runOnWindowActive(run: () -> Unit)
}