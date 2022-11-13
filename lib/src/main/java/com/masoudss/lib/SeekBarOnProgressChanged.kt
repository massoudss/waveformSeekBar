package com.masoudss.lib

interface SeekBarOnProgressChanged {
    fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean)
}
interface TimelineOnProgressChanged {
    fun onProgressChanged(waveformTimeline: WaveformTimeline, progress: Float, fromUser: Boolean)
}