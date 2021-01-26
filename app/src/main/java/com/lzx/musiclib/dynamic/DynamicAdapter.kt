package com.lzx.musiclib.dynamic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.R
import com.lzx.musiclib.loadImage
import com.lzx.musiclib.navigationTo
import com.lzx.musiclib.weight.MomentAudioView
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky

class DynamicAdapter(val context: Context?) : RecyclerView.Adapter<DynamicAdapter.DynamicHolder>() {

    private val list = mutableListOf<SongInfo>()
    var listener: OnItemClickListener? = null

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
            listener?.itemClick(list, position)
        }
        holder.desc.setOnClickListener {
            StarrySky.with().playMusic(list, position)
            context?.navigationTo<DynamicDetailActivity>("songInfo" to info, "from" to "dynamic")
        }

    }

    override fun getItemCount(): Int = list.size

    class DynamicHolder(holder: View) : RecyclerView.ViewHolder(holder) {
        val userHeader: RCImageView = holder.findViewById(R.id.userHeader)
        val nickname: TextView = holder.findViewById(R.id.nickname)
        val desc: TextView = holder.findViewById(R.id.desc)
        val audioView: MomentAudioView = holder.findViewById(R.id.audioView)
    }

    interface OnItemClickListener {
        fun itemClick(list: MutableList<SongInfo>, position: Int)
    }
}