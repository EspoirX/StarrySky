package com.lzx.musiclib

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import com.lzx.starrysky.StarrySky
import com.lzx.basecode.isRefrain
import com.lzx.starrysky.playback.PlaybackStage
import kotlinx.android.synthetic.main.activity_play_detail.songCover
import kotlinx.android.synthetic.main.activity_play_detail.viewPager
import java.io.ByteArrayOutputStream
import java.io.InputStream


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

        val assetFileDescriptor: AssetFileDescriptor = getAssets().openFd("hglo1.ogg")
        assets?.open("hglo1.ogg")?.let {
            readAsBytes(it)?.let {

            }
        }
    }

    private fun readAsBytes(inputStream: InputStream): ByteArray? {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            val byteArray = ByteArray(2048)
            while (true) {
                val count = inputStream.read(byteArray, 0, 2048)
                if (count <= 0) {
                    break
                } else {
                    byteArrayOutputStream.write(byteArray, 0, count)
                }
            }
            return byteArrayOutputStream.toByteArray()
        }
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
