package com.masoudss.lib

import linc.com.amplituda.Amplituda
import java.io.File

object WaveformOptions {

    private val amplituda by lazy { Amplituda() }

    @JvmStatic
    fun getSampleFrom(file: File, onSuccess:(samples: IntArray) -> Unit) {
        amplituda.fromFile(file)
            .amplitudesAsList {
                onSuccess(it.toIntArray())
            }
    }

    @JvmStatic
    fun getSampleFrom(path: String, onSuccess: (IntArray) -> Unit) {
        amplituda.fromPath(path)
            .amplitudesAsList {
                onSuccess(it.toIntArray())
            }
    }
}