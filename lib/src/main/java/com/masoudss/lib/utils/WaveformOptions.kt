package com.masoudss.lib.utils

import android.content.Context
import linc.com.amplituda.Amplituda
import linc.com.amplituda.AmplitudaProcessingOutput
import linc.com.amplituda.exceptions.AmplitudaException

internal object WaveformOptions {

    @JvmStatic
    fun getSampleFrom(context: Context, pathOrUrl: String, onSuccess: (IntArray) -> Unit) {
        handleAmplitudaOutput(Amplituda(context).processAudio(pathOrUrl), onSuccess)
    }

    @JvmStatic
    fun getSampleFrom(context: Context, resource: Int, onSuccess: (IntArray) -> Unit) {
        handleAmplitudaOutput(Amplituda(context).processAudio(resource), onSuccess)
    }

    private fun handleAmplitudaOutput(
        amplitudaOutput: AmplitudaProcessingOutput<*>,
        onSuccess: (IntArray) -> Unit
    ) {
        val result = amplitudaOutput.get { exception: AmplitudaException ->
            exception.printStackTrace()
        }
        onSuccess(result.amplitudesAsList().toTypedArray().toIntArray())
    }

}
