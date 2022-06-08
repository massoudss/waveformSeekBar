package com.masoudss.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.masoudss.activity.SelectAudioActivity
import com.masoudss.databinding.ItemAudioBinding
import com.masoudss.model.AudioModel

class AudioAdapter(
    private val activity: SelectAudioActivity,
    private val audioList: ArrayList<AudioModel>
) :
    RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        return AudioViewHolder(
            ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.binding.title.text =
            "${audioList[position].title}\n${audioList[position].artist}".trim()
    }

    inner class AudioViewHolder(val binding: ItemAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                activity.onSelectAudio(audioList[adapterPosition])
            }
        }
    }
}