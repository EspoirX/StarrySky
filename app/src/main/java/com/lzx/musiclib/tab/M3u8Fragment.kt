package com.lzx.musiclib.tab

import android.view.View
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.PlayDetailActivity
import com.lzx.musiclib.R
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.itemClicked
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.loadImage
import com.lzx.musiclib.navigationTo
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.utils.md5
import kotlinx.android.synthetic.main.fragment_recomment.recycleView

class M3u8Fragment : BaseFragment() {

    companion object {
        fun newInstance(): M3u8Fragment {
            return M3u8Fragment()
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_recomment

    override fun initView(view: View?) {
        val list = mutableListOf<Pair<String, String>>()
        list.add(Pair("CRI汉语环球", "http://sk.cri.cn/hyhq.m3u8"))
        list.add(Pair("CRI环球资讯", "http://sk.cri.cn/nhzs.m3u8"))
        list.add(Pair("CRI劲曲调频", "http://sk.cri.cn/887.m3u8"))
        list.add(Pair("CRI怀旧金曲", "http://sk.cri.cn/oldies.m3u8"))
        list.add(Pair("CRI客家之声", "http://sk.cri.cn/hakka.m3u8"))
        list.add(Pair("CRI闽南之音", "http://sk.cri.cn/minnan.m3u8"))
        list.add(Pair("CRI世界华声", "http://sk.cri.cn/hxfh.m3u8"))
        list.add(Pair("CRl News", "http://sk.cri.cn/905.m3u8"))
        list.add(Pair("CRI EZFM", "http://sk.cri.cn/915.m3u8"))
        list.add(Pair("CRI Nairobi 91.9", "http://sk.cri.cn/kenya.m3u8"))
        list.add(Pair("CRI music", "http://sk.cri.cn/am1008.m3u8"))

        val songList = mutableListOf<SongInfo>()
        list.forEach {
            val songInfo = SongInfo(it.second.md5(), it.second)
            songInfo.artist = it.first
            songInfo.songName = it.first
            songInfo.songCover = "https://blog.xmcdn.com/wp-content/uploads/2014/07/%E5%BD%95%E9%9F%B3.jpg"
            songList.add(songInfo)
        }
        recycleView?.setup<SongInfo> {
            dataSource(songList)
            adapter {
                addItem(R.layout.item_recomment_channel) {
                    bindViewHolder { info, position, holder ->
                        val icon = holder.findViewById<RCImageView>(R.id.cover)
                        icon.loadImage(info?.songCover)
                        setText(R.id.title to info?.songName, R.id.desc to "HLS", R.id.username to info?.artist)
                        itemClicked(View.OnClickListener {
                            StarrySky.with().updatePlayList(songList)
                            activity?.navigationTo<PlayDetailActivity>(
                                "songId" to info?.songId,
                                "position" to position,
                                "type" to "m3u8")
                        })
                    }
                }
            }
        }
    }

    override fun unInitView() {
    }

}