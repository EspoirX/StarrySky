package com.lzx.musiclib

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.utils.TimerTaskManager
import kotlinx.android.synthetic.main.activity_play_detail.Pause
import kotlinx.android.synthetic.main.activity_play_detail.playPause
import kotlinx.android.synthetic.main.activity_play_detail.seekBar
import kotlinx.android.synthetic.main.activity_play_detail.stop

class MainActivity : AppCompatActivity() {

    private var timerTaskManager = TimerTaskManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)

        playPause.setOnClickListener {
            val info = SongInfo("23", "http://re.aniic.com/files/Serchmaa%20-%20%E5%BF%83%E4%B9%8B%E5%AF%BB.mp3")
            info.songName = "大手大脚大声点"
            info.artist = "大声哭了多久"
            info.songCover = "https://media.zenfs.com/ko/setn.com.tw/78f6f4c7d5ac5aed9455c8eeb9924e93"
            StarrySky.with()?.playMusicByInfo(info)
            timerTaskManager.startToUpdateProgress()
        }
        Pause.setOnClickListener {
            StarrySky.with()?.pauseMusic()
            timerTaskManager.stopToUpdateProgress()
        }
        stop.setOnClickListener {
            StarrySky.with()?.stopMusic()
            timerTaskManager.stopToUpdateProgress()
        }
        timerTaskManager.bindLifecycle(lifecycle)
        timerTaskManager.setUpdateProgressTask(Runnable {
            val position = StarrySky.with()?.getPlayingPosition() ?: 0
            val duration = StarrySky.with()?.getDuration() ?: 0
            val buffered = StarrySky.with()?.getBufferedPosition() ?: 0
            if (seekBar.max.toLong() != duration) {
                seekBar.max = duration.toInt()
            }
            seekBar.progress = position.toInt()
            seekBar.secondaryProgress = buffered.toInt()
        })
        //进度条滑动
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with()?.seekTo(seekBar.progress.toLong())
            }
        })

        StarrySky.playbackState().observe(this, Observer {
            Log.i("XIAN", "playbackState = " + it.stage)
        })
    }
}