package com.lzx.musiclib.example

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import com.bumptech.glide.Glide
import com.lzx.musiclib.R
import com.lzx.musiclib.TestApplication
import com.lzx.musiclib.example.MusicRequest.RequestCallback
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.common.PlaybackStage
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.TimerTaskManager
import kotlinx.android.synthetic.main.activity_play_detail.*

class PlayDetailActivity : AppCompatActivity() {

    private var mListPlayAdapter: ListPlayAdapter? = null
    private var mTimerTask: TimerTaskManager? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)
        recycleView.layoutManager = LinearLayoutManager(this)
        mListPlayAdapter = ListPlayAdapter(this)
        recycleView.adapter = mListPlayAdapter
        mTimerTask = TimerTaskManager()
        //状态监听
        initListener()
        //进度更新
        initProgress()
        //播放模式
        initRepeatMode()
        //变速
        initDerailleur()
        //音量
        initVolume()
        //基础操作
        initBaseAction()
        //获取数据
        val musicRequest = MusicRequest()
        musicRequest.requestSongList(object : RequestCallback {
            override fun onSuccess(list: MutableList<SongInfo>) {
                StarrySky.with().updatePlayList(list)
                mListPlayAdapter?.songInfos = list
            }
        })
    }

    private fun initListener() {
        StarrySky.with().playbackState().observe(this, Observer { playbackStage: PlaybackStage? ->
            if (playbackStage == null) {
                return@Observer
            }
            updateUIInfo(playbackStage)
            when (playbackStage.getStage()) {
                PlaybackStage.NONE -> {
                    songName.text = "播放详情页示例"
                    Log.i("PlayDetailActivity", "NONE")
                }
                PlaybackStage.START -> {
                    val info = StarrySky.with().getNowPlayingSongInfo()
                    Log.i("PlayDetailActivity", "START = " + info?.songName)
                    playPause.text = "暂停"
                    mListPlayAdapter?.notifyDataSetChanged()
                    mTimerTask?.startToUpdateProgress()
                }
                PlaybackStage.SWITCH -> {
                    val songInfo: SongInfo? = playbackStage.getSongInfo()
                    Log.i("PlayDetailActivity", "SWITCH = " + songInfo?.songName)
                }
                PlaybackStage.PAUSE -> {
                    Log.i("PlayDetailActivity", "PAUSE")
                    playPause.text = "播放"
                    mTimerTask?.stopToUpdateProgress()
                    mListPlayAdapter?.notifyDataSetChanged()
                }
                PlaybackStage.STOP -> {
                    Log.i("PlayDetailActivity", "STOP")
                    playPause.text = "播放"
                    mTimerTask?.stopToUpdateProgress()
                }
                PlaybackStage.COMPLETION -> {
                    Log.i("PlayDetailActivity", "COMPLETION")
                    mTimerTask?.stopToUpdateProgress()
                    seekBar.progress = 0
                    progressText.text = "00:00"
                }
                PlaybackStage.BUFFERING -> Log.i("PlayDetailActivity", "BUFFERING")
                PlaybackStage.ERROR -> {
                    mTimerTask?.stopToUpdateProgress()
                    Toast.makeText(this, playbackStage.getErrorMessage(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initProgress() {
        mTimerTask?.setUpdateProgressTask {
            val position = StarrySky.with().getPlayingPosition()
            val duration = StarrySky.with().getDuration()
            val buffered = StarrySky.with().getBufferedPosition()
            if (seekBar.max.toLong() != duration) {
                seekBar.max = duration.toInt()
            }
            seekBar.progress = position.toInt()
            seekBar.secondaryProgress = buffered.toInt()
            progressText.text =
                ListPlayAdapter.formatMusicTime(position) + "/" + ListPlayAdapter.formatMusicTime(
                    duration)
            timeText.text = ListPlayAdapter.formatMusicTime(duration)
            mListPlayAdapter?.notifyDataSetChanged()
        }
        //进度条滑动
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with().seekTo(seekBar.progress.toLong())
            }
        })
    }

    private fun initRepeatMode() {
        //点击逻辑:顺序播放->列表循环->单曲播放->单曲循环->随机播放->倒序播放->倒序列表循环->顺序播放
        val repeatMode = StarrySky.with().getRepeatMode()
        when (repeatMode.repeatMode) {
            RepeatMode.REPEAT_MODE_NONE -> if (repeatMode.isLoop) {
                playMode.text = "列表循环"
            } else {
                playMode.text = "顺序播放"
            }
            RepeatMode.REPEAT_MODE_ONE -> if (repeatMode.isLoop) {
                playMode.text = "单曲循环"
            } else {
                playMode.text = "单曲播放"
            }
            RepeatMode.REPEAT_MODE_SHUFFLE -> {
                playMode.text = "随机播放"
            }
            RepeatMode.REPEAT_MODE_REVERSE -> if (repeatMode.isLoop) {
                playMode.text = "倒序列表循环"
            } else {
                playMode.text = "倒序播放"
            }
        }
        playMode.setOnClickListener {
            val model = StarrySky.with().getRepeatMode()
            when (model.repeatMode) {
                RepeatMode.REPEAT_MODE_NONE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, false)
                    playMode.text = "单曲播放"
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, true)
                    playMode.text = "列表循环"
                }
                RepeatMode.REPEAT_MODE_ONE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_SHUFFLE, false)
                    playMode.text = "随机播放"
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, true)
                    playMode.text = "单曲循环"
                }
                RepeatMode.REPEAT_MODE_SHUFFLE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_REVERSE, false)
                    playMode.text = "倒序播放"
                }
                RepeatMode.REPEAT_MODE_REVERSE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, false)
                    playMode.text = "顺序播放"
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_REVERSE, true)
                    playMode.text = "倒序列表循环"
                }
            }
        }
    }

    private fun initDerailleur() {
        derailleur.setOnClickListener {
            StarrySky.with().onDerailleur(true, 1.5f)
        }
    }

    private fun initVolume() {
        volumeJia.setOnClickListener {
            val currVolume = StarrySky.with().getVolume()
            StarrySky.with().setVolume(currVolume + 1)
        }
        volumeJian.setOnClickListener {
            val currVolume = StarrySky.with().getVolume()
            StarrySky.with().setVolume(currVolume - 1)
        }
    }

    private fun initBaseAction() {
        stop.setOnClickListener {
            StarrySky.with().stopMusic()
        }
        prepare.setOnClickListener {
            StarrySky.with().prepare()
        }
        next.setOnClickListener {
            if (StarrySky.with().isSkipToNextEnabled()) {
                StarrySky.with().skipToNext()
            } else {
                Toast.makeText(TestApplication.context, "已经最后一首了", Toast.LENGTH_SHORT).show()
            }
        }
        previous.setOnClickListener {
            if (StarrySky.with().isSkipToPreviousEnabled()) {
                StarrySky.with().skipToPrevious()
            } else {
                Toast.makeText(TestApplication.context, "已经是第一首了", Toast.LENGTH_SHORT).show()
            }
        }
        playPause.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().pauseMusic()
                playPause.text = "播放"
            } else {
                StarrySky.with().restoreMusic()
                playPause.text = "暂停"
            }
        }
        fastForward.setOnClickListener {
            StarrySky.with().fastForward()
        }
        rewind.setOnClickListener {
            StarrySky.with().rewind()
        }
    }

    private fun updateUIInfo(playbackStage: PlaybackStage) {
        val songInfo = playbackStage.getSongInfo()
        if (songInfo != null) {
            songName.text = songInfo.songName
            Glide.with(this).load(songInfo.songCover).into(cover)
        } else {
            songName.text = "播放详情页示例"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimerTask?.removeUpdateProgressTask()
    }
}