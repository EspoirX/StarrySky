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
import kotlinx.android.synthetic.main.fragment_recomment.recycleView

class FlacFragment : BaseFragment() {

    companion object {
        fun newInstance(): FlacFragment {
            return FlacFragment()
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_recomment

    override fun initView(view: View?) {
        val list = mutableListOf<SongInfo>()
        val songInfo1 = SongInfo()
        songInfo1.songId = "11111"
        songInfo1.songUrl = "https://github.com/EspoirX/lzxTreasureBox/raw/master/%E6%83%B3%E4%B8%8D%E5%88%B0.flac"
        songInfo1.songName = "庄心妍-想不到"
        songInfo1.artist = "庄心妍"
        songInfo1.songCover = "https://y.gtimg.cn/music/photo_new/T001R300x300M000003Cn3Yh16q1MO.jpg?max_age=2592000"

        val songInfo2 = SongInfo()
        songInfo2.songId = "222222"
        songInfo2.songUrl = "https://github.com/EspoirX/lzxTreasureBox/raw/master/%E8%99%8E%E4%BA%8C%20-%20%E5%8D%B3%E4%BD%BF%E7%9F%A5%E9%81%93.flac"
        songInfo2.songName = "虎二 - 即使知道"
        songInfo2.artist = "虎二"
        songInfo2.songCover = "https://y.gtimg.cn/music/photo_new/T023R750x750M000001TzotW0Ie8TI.jpg?max_age=2592000"

        list.add(songInfo1)
        list.add(songInfo2)

        recycleView?.setup<SongInfo> {
            dataSource(list)
            adapter {
                addItem(R.layout.item_recomment_channel) {
                    bindViewHolder { info, position, holder ->
                        val icon = holder.findViewById<RCImageView>(R.id.cover)
                        icon.loadImage(info?.songCover)
                        setText(R.id.title to info?.songName, R.id.desc to "FLAC 无损", R.id.username to info?.artist)
                        itemClicked(View.OnClickListener {
                            StarrySky.with().updatePlayList(list)
                            activity?.navigationTo<PlayDetailActivity>(
                                "songId" to info?.songId,
                                "position" to position,
                                "type" to "flac")
                        })
                    }
                }
            }
        }
    }

    override fun unInitView() {
    }
}