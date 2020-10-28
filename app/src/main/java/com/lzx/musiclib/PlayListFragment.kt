package com.lzx.musiclib

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.musiclib.weight.SpectrumDrawView
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.isRefrain
import com.lzx.starrysky.playback.PlaybackStage
import kotlinx.android.synthetic.main.activity_play_detail.songCover
import kotlinx.android.synthetic.main.fragment_recomment.recycleView

class PlayListFragment : BaseFragment() {
    companion object {
        fun newInstance(channelId: Int): PlayListFragment {
            val fragment = PlayListFragment()
            val bundle = Bundle()
            bundle.putInt("channelId", channelId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_playback_list

    private var viewModel: MusicViewModel? = null
    private var channelId: Int = 10

    override fun initView(view: View?) {
        channelId = arguments?.getInt("channelId") ?: 10
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
//        viewModel?.getSongList(channelId)
//        viewModel?.songInfos?.observe(this, Observer {
//            initRecycleView(it)
//        })

        StarrySky.with().playbackState().observe(this, Observer {
            if (it.songInfo.isRefrain()) {
                return@Observer
            }
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    initRecycleView(StarrySky.with().getPlayList())
                }
            }
        })
    }

    private fun initRecycleView(list: MutableList<SongInfo>) {
        recycleView?.setup<SongInfo> {
            dataSource(list)
            adapter {
                addItem(R.layout.item_hot_type_one) {
                    bindViewHolder { info, position, holder ->
                        val spectrumDrawView = holder.findViewById<SpectrumDrawView>(R.id.imgAnim)
                        setText(R.id.songName to info?.songName, R.id.songDesc to info?.artist)
                        val isPlaying = StarrySky.with().isCurrMusicIsPlaying(info?.songId!!)
                        val isPause = StarrySky.with().isCurrMusicIsPaused(info.songId)
                        when {
                            isPlaying -> {
                                spectrumDrawView.startAnim()
                                spectrumDrawView.visibility = View.VISIBLE
                            }
                            isPause -> {
                                spectrumDrawView.stopAnim()
                                spectrumDrawView.visibility = View.VISIBLE
                            }
                            else -> {
                                spectrumDrawView.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    override fun unInitView() {
    }
}