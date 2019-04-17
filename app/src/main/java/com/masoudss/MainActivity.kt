package com.masoudss

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.Utils
import com.masoudss.lib.WaveGravity
import com.masoudss.lib.WaveformSeekBar
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.net.Uri


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        waveformSeekBar.progress = 33
        waveformSeekBar.waveWidth = Utils.dp(this,5)
        waveformSeekBar.waveGap = Utils.dp(this,2)
        waveformSeekBar.waveMinHeight = Utils.dp(this,5)
        waveformSeekBar.waveCornerRadius = Utils.dp(this,2)
        waveformSeekBar.waveGravity = WaveGravity.CENTER
        waveformSeekBar.waveBackgroundColor = ContextCompat.getColor(this,R.color.white)
        waveformSeekBar.waveProgressColor = ContextCompat.getColor(this,R.color.blue)
        waveformSeekBar.sample = Utils.getDummyWaveSample()
        waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
            override fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    waveProgress.progress = progress
            }
        }

        waveWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveformSeekBar.waveWidth = progress/100F*Utils.dp(this@MainActivity,20)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        waveCornerRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveformSeekBar.waveCornerRadius = progress/100F*Utils.dp(this@MainActivity,10)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        waveGap.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveformSeekBar.waveGap = progress/100F*Utils.dp(this@MainActivity,10)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        waveProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveformSeekBar.progress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        gravityRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            val radioButton = gravityRadioGroup.findViewById(checkedId) as RadioButton
            val index = gravityRadioGroup.indexOfChild(radioButton)
            waveformSeekBar.waveGravity = when (index){
                0 -> WaveGravity.TOP
                1 -> WaveGravity.CENTER
                else -> WaveGravity.BOTTOM
            }
        }

        waveColorRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            val radioButton = waveColorRadioGroup.findViewById(checkedId) as RadioButton
            val index = waveColorRadioGroup.indexOfChild(radioButton)
            waveformSeekBar.waveBackgroundColor = when (index){
                0 -> ContextCompat.getColor(this,R.color.pink)
                1 -> ContextCompat.getColor(this,R.color.yellow)
                else -> ContextCompat.getColor(this,R.color.white)
            }
        }

        progressColorRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            val radioButton = progressColorRadioGroup.findViewById(checkedId) as RadioButton
            val index = progressColorRadioGroup.indexOfChild(radioButton)
            waveformSeekBar.waveProgressColor = when (index){
                0 -> ContextCompat.getColor(this,R.color.red)
                1 -> ContextCompat.getColor(this,R.color.blue)
                else -> ContextCompat.getColor(this,R.color.green)
            }
        }

        icGithub.setOnClickListener {
            val url = "https://github.com/massoudss/waveformSeekBar"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }
}
