package com.lzx.musiclib.example

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lzx.musiclib.R
import com.lzx.musiclib.example.ListPlayAdapter.ListPlayHolder
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.provider.SongInfo
import java.util.*

class ListPlayAdapter(private val mContext: Context) : RecyclerView.Adapter<ListPlayHolder>() {
    private val mSongInfos: MutableList<SongInfo> = ArrayList()

    var songInfos: MutableList<SongInfo>?
        get() = mSongInfos
        set(songInfos) {
            mSongInfos.clear()
            mSongInfos.addAll(songInfos!!)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListPlayHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_play_list, viewGroup, false)
        return ListPlayHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListPlayHolder, position: Int) {
        val songInfo = mSongInfos[position]
        Glide.with(mContext).load(songInfo.songCover).into(holder.cover)
        holder.title.text = songInfo.songName
        val progress = StarrySky.with().getPlayingPosition()
        val duration = StarrySky.with().getDuration()
        if (StarrySky.with().isCurrMusicIsPlaying(songInfo.songId)) {
            holder.itemView.setBackgroundColor(Color.GREEN)
            holder.state.text = "状态:播放中    " + formatMusicTime(progress) + "/" + formatMusicTime(duration)
        } else if (StarrySky.with().isCurrMusicIsPaused(songInfo.songId)) {
            holder.itemView.setBackgroundColor(Color.GREEN)
            holder.state.text = "状态:暂停中    " + formatMusicTime(progress) + "/" + formatMusicTime(duration)
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE)
            holder.state.text = ""
        }
        holder.itemView.setOnClickListener { v: View? ->
            if (StarrySky.with().isCurrMusicIsPlaying(songInfo.songId)) {
                StarrySky.with().pauseMusic()
            } else {
                StarrySky.with().playMusicByIndex(position)
            }
        }
        holder.itemView.setOnLongClickListener { v: View? ->
            if (StarrySky.with().isCurrMusicIsPlaying(songInfo.songId)) {
                StarrySky.with().playMusicByIndex(position + 1)
            }
            StarrySky.with().removeSongInfo(songInfo.songId)
            notifyItemRemoved(position)
            mSongInfos.removeAt(position)
            Toast.makeText(mContext, "移除歌曲：" + songInfo.songName, Toast.LENGTH_SHORT).show()
            false
        }
    }

    override fun onBindViewHolder(holder: ListPlayHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        val songInfo = mSongInfos[position]
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val progress = StarrySky.with().getPlayingPosition()
            val duration = StarrySky.with().getDuration()
            if (StarrySky.with().isCurrMusicIsPlaying(songInfo.songId)) {
                holder.state.text = "状态:播放中    " + formatMusicTime(progress) + "/" + formatMusicTime(duration)
            } else if (StarrySky.with().isCurrMusicIsPaused(songInfo.songId)) {
                holder.state.text = "状态:暂停中    " + formatMusicTime(progress) + "/" + formatMusicTime(duration)
            } else {
                holder.state.text = ""
            }
        }
    }

    fun updateItemProgress(position: Int) {
        notifyItemChanged(position, "position")
    }

    override fun getItemCount(): Int {
        return mSongInfos.size
    }

    inner class ListPlayHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cover: ImageView
        var title: TextView
        var state: TextView

        init {
            cover = itemView.findViewById(R.id.cover)
            title = itemView.findViewById(R.id.title)
            state = itemView.findViewById(R.id.state)
        }
    }

    companion object {
        private const val RED = -0x7f80
        private const val BLUE = -0x7f7f01
        private const val CYAN = -0x7f0001
        private const val GREEN = -0x7f0080
        fun formatMusicTime(duration: Long): String {
            var time = ""
            val minute = duration / 60000
            val seconds = duration % 60000
            val second = Math.round(seconds.toInt() / 1000.toFloat()).toLong()
            if (minute < 10) {
                time += "0"
            }
            time += "$minute:"
            if (second < 10) {
                time += "0"
            }
            time += second
            return time
        }
    }

}