package com.lzx.musiclib.tab

import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.PlayDetailActivity
import com.lzx.musiclib.R
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.itemClicked
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.bean.MusicChannel
import com.lzx.musiclib.loadImage
import com.lzx.musiclib.navigationTo
import com.lzx.musiclib.viewmodel.MusicViewModel
import kotlinx.android.synthetic.main.fragment_recomment.recycleView

class RecommendFragment : BaseFragment() {

    companion object {
        fun newInstance(): RecommendFragment {
            return RecommendFragment()
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_recomment

    private var viewModel: MusicViewModel? = null

    override fun initView(view: View?) {
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        viewModel?.login()
        viewModel?.musicChannels?.observe(this, Observer {
            initRecycleView(it)
        })
    }

    private fun initRecycleView(list: MutableList<MusicChannel>) {
        recycleView.setup<MusicChannel> {
            dataSource(list)
            adapter {
                addItem(R.layout.item_recomment_channel) {
                    bindViewHolder { data, position, holder ->
                        val icon = holder.findViewById<RCImageView>(R.id.cover)
                        icon.loadImage(data?.cover)
                        setText(R.id.title to data?.name, R.id.desc to data?.intro, R.id.username to "歌曲数：" + data?.songNum)
                        itemClicked(View.OnClickListener {
                            takeIf { data?.songNum ?: 0 > 0 }?.let {
                                activity?.navigationTo<PlayDetailActivity>(
                                    "channelId" to data?.id)
                            }
                        })
                    }
                }
//                addItem(R.layout.item_recomment_song) {
//                    isForViewType { data, position -> !data?.songList.isNullOrEmpty() }
//                    bindViewHolder { data, position, holder ->
//                        val layout = holder.findViewById<LinearLayout>(R.id.songLayout)
//                        layout.removeAllViews()
//                        data?.songList?.forEachIndexed { index, songInfo ->
//                            if (index < 5) {
//                                val view = R.layout.item_recom_song.getViewObj(context)
//                                val icon = view.findViewById<RCImageView>(R.id.cover)
//                                icon.setMargins(if (index == 0) 15 else 0, right = 15)
//                                icon.loadImage(songInfo.songCover)
//                                view.setOnClickListener {
//                                    activity?.navigationTo<PlayDetailActivity>(
//                                        "songId" to songInfo.songId,
//                                        "type" to "baidu")
//                                }
//                                layout.addView(view)
//                            }
//                        }
//                    }
//                }
            }
        }
    }

    override fun unInitView() {
    }

}