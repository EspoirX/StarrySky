package com.lzx.musiclib

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.gcssloop.widget.RCImageView
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.itemClicked
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.manager.PlaybackStage
import kotlinx.android.synthetic.main.activity_main.card
import kotlinx.android.synthetic.main.activity_main.donutProgress
import kotlinx.android.synthetic.main.activity_main.dynamic
import kotlinx.android.synthetic.main.activity_main.recycleView
import kotlinx.android.synthetic.main.activity_main.songCover

class MainActivity : AppCompatActivity() {

    private var viewModel: MusicViewModel? = null
    private var rotationAnim: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        songCover?.loadImage("http://img01.jituwang.com/190613/256558-1Z613225P691.jpg")
        rotationAnim = ObjectAnimator.ofFloat(songCover, "rotation", 0f, 359f)
        rotationAnim?.interpolator = LinearInterpolator()
        rotationAnim?.duration = 20000
        rotationAnim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                rotationAnim?.start()
            }
        })

        viewModel = getSelfViewModel {
            val list = getHomeMusic()
            initRecycleView(list)
        }

        StarrySky.with().playbackState().observe(this, Observer {
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    rotationAnim?.start()
                    songCover?.loadImage(it.songInfo?.songCover)
                }
                PlaybackStage.IDEA,
                PlaybackStage.ERROR,
                PlaybackStage.PAUSE -> {
                    rotationAnim?.cancel()
                    if (it.stage == PlaybackStage.ERROR) {
                        showToast("播放失败，请查看log了解原因")
                    }
                }
            }
        })
        StarrySky.with().setOnPlayProgressListener(object : OnPlayProgressListener {
            override fun onPlayProgress(currPos: Long, duration: Long) {
                if (donutProgress.getMax().toLong() != duration) {
                    donutProgress.setMax(duration.toInt())
                }
                donutProgress.setProgress(currPos.toFloat())
            }
        })
        songCover?.setOnClickListener {
            StarrySky.with().getNowPlayingSongInfo()?.let {
                navigationTo<PlayDetailActivity>("songId" to it.songId)
            }
        }
        card?.setOnClickListener {
            navigationTo<CardActivity>()
        }
        dynamic?.setOnClickListener {
            StarrySky.with().stopMusic()
            StarrySky.closeNotification()
            navigationTo<DynamicActivity>()
        }
    }

    private fun initRecycleView(list: MutableList<SongInfo>) {
        recycleView?.setup<SongInfo> {
            dataSource(list)
            adapter {
                addItem(R.layout.item_home_music) {
                    bindViewHolder { info, position, holder ->
                        val icon = holder.findViewById<RCImageView>(R.id.cover)
                        icon.loadImage(info?.songCover)
                        setText(R.id.title to info?.songName, R.id.desc to info?.songName)
                        itemClicked {
                            StarrySky.with().playMusic(list, position)
                            navigationTo<PlayDetailActivity>("songId" to info?.songId)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (StarrySky.with().isPlaying()) {
            rotationAnim?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rotationAnim?.cancel()
        rotationAnim?.removeAllListeners()
        rotationAnim = null
    }
}
