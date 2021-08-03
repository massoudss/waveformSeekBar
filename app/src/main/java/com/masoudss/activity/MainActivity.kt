package com.masoudss.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.masoudss.R
import com.masoudss.lib.*
import com.masoudss.lib.utils.Utils
import com.masoudss.lib.utils.WaveGravity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQ_CODE_PICK_SOUND_FILE = 1
    private val REQ_CODE_STORAGE_PERMMISION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        waveformSeekBar.apply {
            progress = 33.2F
            waveWidth = Utils.dp(this@MainActivity, 5)
            waveGap = Utils.dp(this@MainActivity, 2)
            waveMinHeight = Utils.dp(this@MainActivity, 5)
            waveCornerRadius = Utils.dp(this@MainActivity, 2)
            waveGravity = WaveGravity.CENTER
            waveBackgroundColor = ContextCompat.getColor(this@MainActivity, R.color.white)
            waveProgressColor = ContextCompat.getColor(this@MainActivity, R.color.blue)
            sample = getDummyWaveSample()
            onProgressChanged = object : SeekBarOnProgressChanged {
                override fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Float, fromUser: Boolean) {
                    if (fromUser)
                        waveProgress.progress = progress.toInt()
                }
            }
        }

        waveWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveformSeekBar.waveWidth = progress / 100F * Utils.dp(this@MainActivity, 20)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        waveCornerRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveformSeekBar.waveCornerRadius = progress / 100F * Utils.dp(this@MainActivity, 10)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        waveGap.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveformSeekBar.waveGap = progress / 100F * Utils.dp(this@MainActivity, 10)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        waveProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveformSeekBar.progress = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        waveMaxProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                waveProgress.max = progress
                waveformSeekBar.maxProgress = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        gravityRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            val radioButton = gravityRadioGroup.findViewById(checkedId) as RadioButton
            val index = gravityRadioGroup.indexOfChild(radioButton)
            waveformSeekBar.waveGravity = when (index) {
                0 -> WaveGravity.TOP
                1 -> WaveGravity.CENTER
                else -> WaveGravity.BOTTOM
            }
        }

        waveColorRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            val radioButton = waveColorRadioGroup.findViewById(checkedId) as RadioButton
            val index = waveColorRadioGroup.indexOfChild(radioButton)
            waveformSeekBar.waveBackgroundColor = when (index) {
                0 -> ContextCompat.getColor(this, R.color.pink)
                1 -> ContextCompat.getColor(this, R.color.yellow)
                else -> ContextCompat.getColor(this, R.color.white)
            }
        }

        progressColorRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            val radioButton = progressColorRadioGroup.findViewById(checkedId) as RadioButton
            val index = progressColorRadioGroup.indexOfChild(radioButton)
            waveformSeekBar.waveProgressColor = when (index) {
                0 -> ContextCompat.getColor(this, R.color.red)
                1 -> ContextCompat.getColor(this, R.color.blue)
                else -> ContextCompat.getColor(this, R.color.green)
            }
        }

        icGithub.setOnClickListener {
            val url = "https://github.com/massoudss/waveformSeekBar"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        icImport.setOnClickListener {
            checkStoragePermission()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == REQ_CODE_PICK_SOUND_FILE && resultCode == Activity.RESULT_OK) {
            val path = data.getStringExtra("path")

            val progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setMessage(getString(R.string.message_waiting))
            progressDialog.show()


            doAsync {
                waveformSeekBar.setSampleFrom(path!!)

                uiThread {
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasReadPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            val hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            val permissions = ArrayList<String>()

            if (hasReadPermission != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)

            if (hasWritePermission != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permissions.isNotEmpty())
                requestPermissions(permissions.toTypedArray(), REQ_CODE_STORAGE_PERMMISION)
            else
                launchSelectAudioActivity()

        } else
            launchSelectAudioActivity()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQ_CODE_STORAGE_PERMMISION) {
            var denied = false
            for (i in permissions.indices)
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    denied = true
                    break
                }

            if (denied)
                Toast.makeText(this@MainActivity, getString(R.string.permission_error), Toast.LENGTH_SHORT).show()
            else
                launchSelectAudioActivity()

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    private fun launchSelectAudioActivity() {
        val intent = Intent(this@MainActivity, SelectAudioActivity::class.java)
        startActivityForResult(intent, REQ_CODE_PICK_SOUND_FILE)
    }

    private fun getDummyWaveSample(): IntArray {
        val data = IntArray(50)
        for (i in data.indices)
            data[i] = 0

        return data
    }


}
