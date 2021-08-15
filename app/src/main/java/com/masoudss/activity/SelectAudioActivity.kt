package com.masoudss.activity

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.masoudss.R
import com.masoudss.adapter.AudioAdapter
import com.masoudss.model.AudioModel
import kotlinx.android.synthetic.main.activity_select_audio.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class SelectAudioActivity : AppCompatActivity() {

    private val audioList = ArrayList<AudioModel>()
    private val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_audio)
        initViews()
        loadAudioFiles()
    }

    private fun initViews() {
        audioRecyclerView.layoutManager = LinearLayoutManager(this)
        audioRecyclerView.adapter = AudioAdapter(this@SelectAudioActivity, audioList)
    }

    private fun loadAudioFiles() {

        doAsync {

            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        null,
                        null,
                        MediaStore.Audio.Media.DATE_ADDED + " DESC")

                while (cursor!!.moveToNext()) {
                    audioList.add(AudioModel(
                            title = cursor.getString(1),
                            artist = cursor.getString(2),
                            path = cursor.getString(3)
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

            uiThread {
                audioRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun onSelectAudio(audioModel: AudioModel) {
        val intent = Intent()
        intent.putExtra("path", audioModel.path)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
