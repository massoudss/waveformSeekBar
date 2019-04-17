package com.masoudss.lib

interface SeekBarOnProgressChanged {

    fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Int, fromUser: Boolean)
}