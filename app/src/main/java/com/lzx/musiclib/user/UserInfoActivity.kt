package com.lzx.musiclib.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lzx.musiclib.R
import com.lzx.musiclib.getSelfViewModel
import com.lzx.musiclib.loadImage
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.utils.orDef
import kotlinx.android.synthetic.main.activity_user.bgImage
import kotlinx.android.synthetic.main.activity_user.recycleView

class UserInfoActivity : AppCompatActivity() {

    private var userAdapter: UserAdapter? = null
    private var viewModel: MusicViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        bgImage.loadImage("https://up.zhuoku.org/pic/ba/5b/a0/ba5ba00c78aafbd57ba5021615b46d8a.jpg")

        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = UserAdapter().also { userAdapter = it }

        viewModel = getSelfViewModel {
            val list = getUserMusicList()
            userAdapter?.submitList(list, true)
        }

        StarrySky.with().playbackState().observe(this, {
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    userAdapter?.currPlayingIndex = StarrySky.with().getNowPlayingIndex()
                    userAdapter?.notifyDataSetChanged()
                }
            }
        })

        StarrySky.with().setOnPlayProgressListener(object : OnPlayProgressListener {
            override fun onPlayProgress(currPos: Long, duration: Long) {
                userAdapter?.notifyItemChanged(userAdapter?.currPlayingIndex.orDef(), Pair(currPos, duration))
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        StarrySky.with().stopMusic()
    }
}