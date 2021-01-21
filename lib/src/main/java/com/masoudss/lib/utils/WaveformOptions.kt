package com.masoudss.lib.utils

import android.content.Context
import linc.com.amplituda.Amplituda
import java.io.File

internal object WaveformOptions {

    private var amplituda: Amplituda? = null

    @JvmStatic
    fun init(context: Context) {
        if(amplituda == null) {
            amplituda = Amplituda(context)
        }
    }

    @JvmStatic
    fun getSampleFrom(file: File, onSuccess:(samples: IntArray) -> Unit) {
        amplituda!!.fromFile(file)
            .amplitudesAsList {
                onSuccess(it.toIntArray())
            }
    }

    @JvmStatic
    fun getSampleFrom(path: String, onSuccess: (IntArray) -> Unit) {
        amplituda!!.fromPath(path)
            .amplitudesAsList {
                onSuccess(it.toIntArray())
            }
    }
}