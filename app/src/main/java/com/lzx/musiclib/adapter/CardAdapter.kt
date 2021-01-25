package com.lzx.musiclib.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.DynamicDetailActivity
import com.lzx.musiclib.R
import com.lzx.musiclib.loadImage
import com.lzx.musiclib.navigationTo
import com.lzx.starrysky.SongInfo

class CardAdapter(private val context: Activity?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<SongInfo>()

    fun submitList(list: MutableList<SongInfo>, isRefresh: Boolean) {
        if (isRefresh) {
            this.list.clear()
        }
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun getList() = list

    fun getItem(position: Int): SongInfo? = list.getOrNull(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val info = list.getOrNull(position)
        val cardHolder = holder as CardHolder
        cardHolder.cover.loadImage(info?.songCover)
        cardHolder.headerImg.loadImage("https://i2.gqxz.com/uploads/202009/14/200914110924764.jpg")
        cardHolder.songName.text = info?.songName
        cardHolder.singer.text = info?.artist
        cardHolder.itemView.setOnClickListener {
            context?.navigationTo<DynamicDetailActivity>("songInfo" to info)
        }
    }

    class CardHolder(holder: View) : RecyclerView.ViewHolder(holder) {
        val cover: RCImageView = holder.findViewById(R.id.cover)
        val headerImg: RCImageView = holder.findViewById(R.id.headerImg)
        val songName: TextView = holder.findViewById(R.id.songName)
        val singer: TextView = holder.findViewById(R.id.singer)

    }

    override fun getItemCount(): Int = list.size
}




