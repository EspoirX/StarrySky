package com.lzx.musiclib

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.manager.PlaybackStage
import kotlinx.android.synthetic.main.activiity_dynamic.cover
import kotlinx.android.synthetic.main.activiity_dynamic.progressText
import kotlinx.android.synthetic.main.activiity_dynamic.seekBar
import kotlinx.android.synthetic.main.activiity_dynamic.songName
import kotlinx.android.synthetic.main.activiity_dynamic.timeText

class DynamicDetailActivity : AppCompatActivity() {

    private var songInfo: SongInfo? = null
    private var rotationAnim: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activiity_dynamic)

        songInfo = intent.getParcelableExtra("songInfo")

        cover.loadImage(songInfo?.songCover)
        songName.text = songInfo?.songName

        rotationAnim = ObjectAnimator.ofFloat(cover, "rotation", 0f, 359f)
        rotationAnim?.interpolator = LinearInterpolator()
        rotationAnim?.duration = 20000
        rotationAnim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                rotationAnim?.start()
            }
        })
        rotationAnim?.start()

        StarrySky.with().playbackState().observe(this, {
            if (it.stage == PlaybackStage.IDEA && !it.isStop) {
                //重播
                StarrySky.with()
                    .skipMediaQueue(true)
                    .playMusicByInfo(songInfo)
            }
        })
        StarrySky.with().setOnPlayProgressListener(object : OnPlayProgressListener {
            @SuppressLint("SetTextI18n")
            override fun onPlayProgress(currPos: Long, duration: Long) {
                if (seekBar.max.toLong() != duration) {
                    seekBar.max = duration.toInt()
                }
                seekBar.progress = currPos.toInt()
                progressText.text = currPos.formatTime()
                timeText.text = " / " + duration.formatTime()
            }
        })
        //进度SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with().seekTo(seekBar.progress.toLong(), true)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        rotationAnim?.cancel()
        rotationAnim?.removeAllListeners()
        rotationAnim = null
    }
}