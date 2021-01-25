package com.lzx.musiclib.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.R
import com.lzx.musiclib.loadImage
import com.lzx.musiclib.weight.MomentAudioView
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky

class DynamicAdapter : RecyclerView.Adapter<DynamicAdapter.DynamicHolder>() {

    private val list = mutableListOf<SongInfo>()

    fun submitList(list: MutableList<SongInfo>, isRefresh: Boolean) {
        if (isRefresh) {
            this.list.clear()
        }
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DynamicHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dynamic, parent, false)
        return DynamicHolder(view)
    }

    override fun onBindViewHolder(holder: DynamicHolder, position: Int) {
        val info = list[position]
        holder.userHeader.loadImage(info.songCover)
        holder.nickname.text = info.artist
        holder.audioView.setVoiceInfo(info)

        val isPlaying = StarrySky.with().isCurrMusicIsPlaying(info.songId)
        holder.audioView.setUIState(isPlaying)
        holder.audioView.setOnClickListener {
            if (StarrySky.with().isCurrMusicIsPlaying(info.songId)) {
                StarrySky.with().pauseMusic()
            } else {
                StarrySky.with()
                    .openNotification(false)
                    .playMusic(list, position)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    class DynamicHolder(holder: View) : RecyclerView.ViewHolder(holder) {
        val userHeader: RCImageView = holder.findViewById(R.id.userHeader)
        val nickname: TextView = holder.findViewById(R.id.nickname)
        val audioView: MomentAudioView = holder.findViewById(R.id.audioView)
    }
}