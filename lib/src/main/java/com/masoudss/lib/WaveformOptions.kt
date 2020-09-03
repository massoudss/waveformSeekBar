package com.masoudss.lib

import android.os.Build
import androidx.annotation.RequiresApi
import com.masoudss.lib.soundParser.SoundFile
import linc.com.amplituda.Amplituda
import java.io.File

object WaveformOptions {

    private val amplituda by lazy { Amplituda() }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @JvmStatic
    fun getSampleFrom(file: File, onSuccess:(samples: IntArray) -> Unit) {
        amplituda.fromFile(file)
            .amplitudesAsList {
                onSuccess(it.toIntArray())
            }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @JvmStatic
    fun getSampleFrom(path: String, onSuccess: (IntArray) -> Unit) {
        amplituda.fromPath(path)
            .amplitudesAsList {
                onSuccess(it.toIntArray())
            }
    }

    fun addCustomExtension(extension: String) = SoundFile.addCustomExtension(extension)

    fun removeCustomExtension(extension: String) = SoundFile.removeCustomExtension(extension)

    fun addCustomExtensions(extensions: List<String>) = SoundFile.addCustomExtensions(extensions)

    fun removeCustomExtensions(extensions: List<String>) = SoundFile.removeCustomExtensions(extensions)
}