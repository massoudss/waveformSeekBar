package com.masoudss.lib.utils

import android.content.Context
import java.io.File
import java.lang.reflect.Constructor

internal object WaveformOptions {

    @JvmStatic
    fun getSampleFrom(context: Context, file: File, onSuccess:(samples: IntArray) -> Unit) {
        getSampleFrom(context, file.path, onSuccess)
    }

    @JvmStatic
    fun getSampleFrom(context: Context, path: String, onSuccess: (IntArray) -> Unit) {
//        ExternalAmplituda.run(onSuccess, context, "/storage/emulated/0/Music/kygo.mp3")
        ExternalAmplituda.run(onSuccess, context, path)
    }
}