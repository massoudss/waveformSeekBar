package com.masoudss.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.masoudss.R
import com.masoudss.activity.SelectAudioActivity
import com.masoudss.model.AudioModel
import kotlinx.android.synthetic.main.item_audio.view.*

class AudioAdapter(private val activity: SelectAudioActivity, private val audioList: ArrayList<AudioModel>) :
        RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        return AudioViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_audio, parent, false))
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.itemView.title.text = "${audioList[position].title}\n${audioList[position].artist}".trim()
    }

    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                activity.onSelectAudio(audioList[adapterPosition])
            }
        }
    }


}