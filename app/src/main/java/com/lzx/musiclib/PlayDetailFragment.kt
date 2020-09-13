package com.lzx.musiclib

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.lzx.musiclib.base.BaseFragment

class PlayDetailFragment : BaseFragment() {

    companion object {
        fun newInstance(songId: String?): PlayDetailFragment {
            val fragment = PlayDetailFragment()
            val bundle = Bundle()
            bundle.putString("songId", songId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_play_detail

    private var songId:String?=null
    private var viewModel: MusicViewModel? = null

    override fun initView(view: View?) {
        songId = arguments?.getString("songId")
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        if (songId.isNullOrEmpty()){
            activity?.finish()
            return
        }
        viewModel?.getBaiduMusicUrl(songId!!)
    }

    override fun unInitView() {
    }

}