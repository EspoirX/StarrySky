package com.lzx.musiclib.user

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lzx.musiclib.R
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.utils.formatTime

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserHolder>() {

    private val list = mutableListOf<SongInfo>()

    var currPlayingIndex = 0

    fun submitList(list: MutableList<SongInfo>, isRefresh: Boolean) {
        if (isRefresh) {
            this.list.clear()
        }
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserHolder(view)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val info = list[position]
        holder.musicName.text = info.songName
        holder.musicSinger.text = info.artist

        holder.itemView.setOnClickListener {
            currPlayingIndex = position
            StarrySky.with().playMusic(list, position)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        val any = payloads.getOrNull(0) ?: return
        val info = list[position]
        val playingId = StarrySky.with().getNowPlayingSongId()
        if (info.songId != playingId) return
        val pair = any as Pair<*, *>
        val currPos = pair.first as Long
        val duration = pair.second as Long
        holder.time.text = currPos.formatTime() + " / " + duration.formatTime()
    }

    override fun getItemCount(): Int = list.size

    class UserHolder(holder: View) : RecyclerView.ViewHolder(holder) {
        val musicName: TextView = holder.findViewById(R.id.musicName)
        val musicSinger: TextView = holder.findViewById(R.id.musicSinger)
        val time: TextView = holder.findViewById(R.id.time)
    }
}