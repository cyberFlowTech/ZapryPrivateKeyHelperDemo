package com.zapry.pkdemo.app

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.zapry.pkdemo.util.OnceRunner
import java.lang.ref.WeakReference

class WindowActionDelegateImpl private constructor(
    val activity: AppCompatActivity
) : IWindowActionDelegate {
    companion object {
        fun create(activity: AppCompatActivity): IWindowActionDelegate {
            return WindowActionDelegateImpl(activity)
        }
    }

    private var isWindowActive = false
    private val pendingOnGlobalLayoutListener =
        mutableListOf<WeakReference<ViewTreeObserver.OnGlobalLayoutListener>>()
    private val onceRunner = OnceRunner()

    override fun onCreate(savedInstanceState: Bundle?) {
        scheduleNextLayout { isWindowActive = true }
    }

    override fun onPause() {
        if (activity.isFinishing) {
            release()
        }
    }

    override fun onDestroy() {
        release()
    }

    private fun release() {
        onceRunner.run { onReleaseInner() }
    }

    private fun onReleaseInner() {
        pendingOnGlobalLayoutListener.forEach { r ->
            r.get()?.let { l ->
                activity.window.decorView.viewTreeObserver
                    .removeOnGlobalLayoutListener(l)
            }
        }
        isWindowActive = false
    }

    override fun runOnWindowActive(run: () -> Unit) {
        if (isWindowActive) {
            run.invoke()
        } else {
            scheduleNextLayout(run)
        }
    }

    private fun scheduleNextLayout(run: () -> Unit) {
        object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                activity.window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                run.invoke()
            }
        }.also {
            activity.window.decorView.viewTreeObserver.addOnGlobalLayoutListener(it)
            pendingOnGlobalLayoutListener.add(WeakReference(it))
        }
    }
}