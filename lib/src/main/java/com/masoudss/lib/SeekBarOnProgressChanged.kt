package com.masoudss.lib

interface SeekBarOnProgressChanged {
    fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean)
}