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

class RtmpFragment : BaseFragment() {

    companion object {
        fun newInstance(): RtmpFragment {
            return RtmpFragment()
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_recomment

    override fun initView(view: View?) {
        val list = mutableListOf<Pair<String, String>>()
        list.add(Pair("香港财经", "rtmp://202.69.69.180:443/webcast/bshdlive-pc"))
        list.add(Pair("韩国GoodTV", "rtmp://mobliestream.c3tv.com:554/live/goodtv.sdp"))
        list.add(Pair("韩国朝鲜日报", "rtmp://live.chosun.gscdn.com/live/tvchosun1.stream"))
        list.add(Pair("美国1", "rtmp://ns8.indexforce.com/home/mystream"))
        list.add(Pair("美国2", "rtmp://media3.scctv.net/live/scctv_800"))
        list.add(Pair("美国中文电视", "rtmp://media3.sinovision.net:1935/live/livestream"))
        list.add(Pair("湖南卫视", "rtmp://58.200.131.2:1935/livetv/hunantv"))

        val songList = mutableListOf<SongInfo>()
        list.forEach {
            val songInfo = SongInfo(it.second.md5(), it.second)
            songInfo.artist = it.first
            songInfo.songName = it.first
            songInfo.songCover = "https://img95.699pic.com/photo/50052/5059.jpg_wh300.jpg"
            songList.add(songInfo)
        }
        recycleView?.setup<SongInfo> {
            dataSource(songList)
            adapter {
                addItem(R.layout.item_recomment_channel) {
                    bindViewHolder { info, position, holder ->
                        val icon = holder.findViewById<RCImageView>(R.id.cover)
                        icon.loadImage(info?.songCover)
                        setText(R.id.title to info?.songName, R.id.desc to "RTMP", R.id.username to info?.artist)
                        itemClicked(View.OnClickListener {
                            StarrySky.with().updatePlayList(songList)
                            activity?.navigationTo<PlayDetailActivity>(
                                "songId" to info?.songId,
                                "position" to position,
                                "type" to "rtmp")
                        })
                    }
                }
            }
        }
    }

    override fun unInitView() {
    }
}