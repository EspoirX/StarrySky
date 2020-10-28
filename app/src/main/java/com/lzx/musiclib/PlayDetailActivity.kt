package com.lzx.musiclib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.isRefrain
import com.lzx.starrysky.playback.PlaybackStage
import kotlinx.android.synthetic.main.activity_play_detail.songCover
import kotlinx.android.synthetic.main.activity_play_detail.viewPager

class PlayDetailActivity : AppCompatActivity() {

    private var channelId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)
        channelId = intent.getIntExtra("channelId", 10)
        viewPager?.removeAllViews()
        viewPager?.removeAllViewsInLayout()
        viewPager?.adapter = PlayDetailAdapter(supportFragmentManager, channelId)

        StarrySky.with().playbackState().observe(this, Observer {
            if (it.songInfo.isRefrain()) {
                return@Observer
            }
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    songCover?.loadImage(it.songInfo?.songCover)
                }
            }
        })
    }

    class PlayDetailAdapter(fm: FragmentManager, channelId: Int) : FragmentPagerAdapter(fm) {
        private val fragmentList = mutableListOf<Fragment>()
        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return if (position == 0) {
                "详情"
            } else {
                "列表"
            }
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        init {
            fragmentList.add(PlayDetailFragment.newInstance(channelId))
            fragmentList.add(PlayListFragment.newInstance(channelId))
        }
    }

}
