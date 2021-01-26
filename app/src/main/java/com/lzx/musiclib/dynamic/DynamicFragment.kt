package com.lzx.musiclib.dynamic

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzx.musiclib.R
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.getSelfViewModel
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import kotlinx.android.synthetic.main.fragment_card.recycleView

class DynamicFragment : BaseFragment() {
    companion object {
        fun newInstance(type: String) = DynamicFragment().apply {
            arguments = Bundle().apply {
                putString("type", type)
            }
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_card

    private var dynamicAdapter: DynamicAdapter? = null
    private var viewModel: MusicViewModel? = null
    private var type: String? = null

    override fun initView(view: View?) {
        type = arguments?.getString("type")
        recycleView.layoutManager = LinearLayoutManager(activity)
        recycleView.adapter = DynamicAdapter(context).also { dynamicAdapter = it }

        viewModel = getSelfViewModel {
            dynamicLiveData.observe(this@DynamicFragment, {
                dynamicAdapter?.submitList(it, true)
            })
        }

        viewModel?.getDynamicMusicList(type)

        StarrySky.with().playbackState().observe(this, {
            dynamicAdapter?.notifyDataSetChanged()
        })

        dynamicAdapter?.listener = object : DynamicAdapter.OnItemClickListener {
            override fun itemClick(list: MutableList<SongInfo>, position: Int) {
                (activity as DynamicActivity).showVoiceBar()
                val info = list[position]
                if (StarrySky.with().isCurrMusicIsPlaying(info.songId)) {
                    StarrySky.with().pauseMusic()
                } else {
                    StarrySky.with().playMusic(list, position)
                }
            }

        }
    }

    override fun unInitView() {
    }
}