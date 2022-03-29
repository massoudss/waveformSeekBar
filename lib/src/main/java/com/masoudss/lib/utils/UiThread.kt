package com.masoudss.lib.utils

import android.os.Handler
import android.os.Looper

val uiHandler: Handler
    get() = Handler(Looper.getMainLooper())

fun runOnUiThread(runnable: Runnable) {
    if (Thread.currentThread() == Looper.getMainLooper().thread) {
        runnable.run()
    } else {
        uiHandler.post(runnable)
    }
}