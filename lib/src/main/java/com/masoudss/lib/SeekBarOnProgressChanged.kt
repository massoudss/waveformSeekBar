package com.masoudss.lib

fun interface SeekBarOnProgressChanged {

    fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean)
}