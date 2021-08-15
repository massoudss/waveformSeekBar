package com.masoudss.lib.utils

import android.content.Context
import com.masoudss.lib.exception.AmplitudaNotFoundException
import java.io.File
import java.lang.reflect.Constructor

internal object WaveformOptions {

    @JvmStatic
    @Throws(AmplitudaNotFoundException::class)
    fun getSampleFrom(context: Context, file: File, onSuccess:(samples: IntArray) -> Unit) {
        getSampleFrom(context, file.path, onSuccess)
    }

    @JvmStatic
    @Throws(AmplitudaNotFoundException::class)
    fun getSampleFrom(context: Context, path: String, onSuccess: (IntArray) -> Unit) {
        ExternalAmplituda.run(onSuccess, context, path)
    }

}