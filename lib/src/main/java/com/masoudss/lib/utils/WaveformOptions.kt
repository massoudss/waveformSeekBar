package com.masoudss.lib.utils

import android.content.Context
import linc.com.amplituda.Amplituda
import linc.com.amplituda.AmplitudaProcessingOutput
import linc.com.amplituda.exceptions.AmplitudaException
import java.io.File

internal object WaveformOptions {

    @JvmStatic
    fun getSampleFrom(context: Context, pathOrUrl: String, onSuccess: (IntArray) -> Unit) {
        handleAmplitudaOutput<String>(Amplituda(context).processAudio(pathOrUrl), onSuccess)
    }

    @JvmStatic
    fun getSampleFrom(context: Context, resource: Int, onSuccess: (IntArray) -> Unit) {
        handleAmplitudaOutput<Int>(Amplituda(context).processAudio(resource), onSuccess)
    }

    private fun <T> handleAmplitudaOutput(
        amplitudaOutput: AmplitudaProcessingOutput<*>,
        onSuccess: (IntArray) -> Unit
    ) {
        val result = amplitudaOutput.get { exception: AmplitudaException ->
            exception.printStackTrace()
        }
        onSuccess(result.amplitudesAsList().toTypedArray().toIntArray())
    }

}