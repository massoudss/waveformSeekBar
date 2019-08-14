package com.masoudss.lib

import android.os.Build
import androidx.annotation.RequiresApi
import com.masoudss.lib.soundParser.SoundFile
import java.io.File

object WaveformOptions {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun getSampleFrom(file: File): IntArray? {
        val soundFile = SoundFile.create(file.absolutePath)
        return soundFile?.frameGains
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun getSampleFrom(path: String): IntArray? {
        val soundFile = SoundFile.create(path)
        return soundFile?.frameGains
    }
}