package com.lzx.musiclib.example

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.lzx.musiclib.R
import com.lzx.musiclib.example.MusicRequest.RequestCallback
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.common.PlaybackStage
import com.lzx.starrysky.control.OnPlayerEventListener
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.TimerTaskManager

class PlayDetailActivity : AppCompatActivity() {
    private lateinit var title: TextView
    private lateinit var progress_text: TextView
    private lateinit var time_text: TextView
    private lateinit var cover: ImageView
    private lateinit var mSeekBar: SeekBar
    private lateinit var mRecyclerView: RecyclerView
    private var mListPlayAdapter: ListPlayAdapter? = null
    private var mTimerTask: TimerTaskManager? = null
    private lateinit var playMode: TextView
    private val isSettingShuffleMode = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)
        title = findViewById(R.id.title)
        cover = findViewById(R.id.cover)
        mSeekBar = findViewById(R.id.seek_bar)
        progress_text = findViewById(R.id.progress_text)
        time_text = findViewById(R.id.time_text)
        mRecyclerView = findViewById(R.id.recycle_view)
        playMode = findViewById(R.id.play_mode)
        mRecyclerView.setLayoutManager(LinearLayoutManager(this))
        mListPlayAdapter = ListPlayAdapter(this)
        mRecyclerView.setAdapter(mListPlayAdapter)
        mTimerTask = TimerTaskManager()
        StarrySky.with().addPlayerEventListener(object : OnPlayerEventListener {
            override fun onMusicSwitch(songInfo: SongInfo) {}
            override fun onPlayerStart() {}
            override fun onPlayerPause() {}
            override fun onPlayerStop() {}
            override fun onPlayCompletion(songInfo: SongInfo) {}
            override fun onBuffering() {}
            override fun onError(errorCode: Int, errorMsg: String) {}
        })

        //状态监听
        StarrySky.with().playbackState().observe(this, Observer { playbackStage: PlaybackStage? ->
            if (playbackStage == null) {
                return@Observer
            }
            updateUIInfo(playbackStage)
            when (playbackStage.getStage()) {
                PlaybackStage.NONE -> {
                    title.setText("播放详情页示例")
                    Log.i("PlayDetailActivity", "NONE")
                }
                PlaybackStage.START -> {
                    Log.i("PlayDetailActivity", "START")
                    val info = StarrySky.with().getNowPlayingSongInfo()
                    mListPlayAdapter?.notifyDataSetChanged()
                    mTimerTask?.startToUpdateProgress()
                }
                PlaybackStage.SWITCH -> {
                    val songInfo: SongInfo? = playbackStage.getSongInfo()
                    Log.i("PlayDetailActivity", "SWITCH = " + songInfo?.songName)
                }
                PlaybackStage.PAUSE -> {
                    Log.i("PlayDetailActivity", "PAUSE")
                    mTimerTask!!.stopToUpdateProgress()
                    mListPlayAdapter!!.notifyDataSetChanged()
                }
                PlaybackStage.STOP -> {
                    Log.i("PlayDetailActivity", "STOP")
                    mTimerTask!!.stopToUpdateProgress()
                }
                PlaybackStage.COMPLETION -> {
                    Log.i("PlayDetailActivity", "COMPLETION")
                    mTimerTask!!.stopToUpdateProgress()
                    mSeekBar.setProgress(0)
                    progress_text.setText("00:00")
                }
                PlaybackStage.BUFFERING -> Log.i("PlayDetailActivity", "BUFFERING")
                PlaybackStage.ERROR -> {
                    mTimerTask!!.stopToUpdateProgress()
                    Toast.makeText(this, playbackStage.getErrorMessage(), Toast.LENGTH_SHORT)
                            .show()
                }
                else -> {
                }
            }
        })

        //进度更新
        mTimerTask!!.setUpdateProgressTask {
            val position = StarrySky.with().getPlayingPosition()
            val duration = StarrySky.with().getDuration()
            val buffered = StarrySky.with().getBufferedPosition()
            if (mSeekBar.getMax().toLong() != duration) {
                mSeekBar.setMax(duration.toInt())
            }
            //Log.i("PlayDetailActivity", "duration = " + duration);
            mSeekBar.setProgress(position.toInt())
            mSeekBar.setSecondaryProgress(buffered.toInt())
            progress_text.setText(
                    ListPlayAdapter.formatMusicTime(position) + "/" + ListPlayAdapter.formatMusicTime(duration))
            time_text.setText(ListPlayAdapter.formatMusicTime(duration))
            mListPlayAdapter!!.notifyDataSetChanged()
        }

        //进度条滑动
        mSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with().seekTo(seekBar.progress.toLong())
            }
        })

        //点击逻辑
        //顺序播放->列表循环->单曲播放->单曲循环->随机播放->倒序播放->倒序列表循环->顺序播放
        val repeatMode = StarrySky.with().getRepeatMode()
        when (repeatMode.repeatMode) {
            RepeatMode.REPEAT_MODE_NONE -> if (repeatMode.isLoop) {
                playMode.text = "单曲播放"
            } else {
                playMode.text = "列表循环"
            }
            RepeatMode.REPEAT_MODE_ONE -> if (repeatMode.isLoop) {
                playMode.text = "随机播放"
            } else {
                playMode.text = "单曲循环"
            }
            RepeatMode.REPEAT_MODE_SHUFFLE -> {
                playMode.text = "倒序播放"
            }
            RepeatMode.REPEAT_MODE_REVERSE -> if (repeatMode.isLoop) {
                playMode.text = "顺序播放"
            } else {
                playMode.text = "倒序列表循环"
            }
        }

        playMode.setOnClickListener(View.OnClickListener { v: View? ->
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
        })

        //获取数据
        val musicRequest = MusicRequest()
        musicRequest.requestSongList(object : RequestCallback {
            override fun onSuccess(list: MutableList<SongInfo>) {
                StarrySky.with().updatePlayList(list)
                mListPlayAdapter?.songInfos = list
            }
        })
    }

    private fun updateUIInfo(playbackStage: PlaybackStage) {
        val songInfo = playbackStage.getSongInfo()
        if (songInfo != null) {
            title!!.text = songInfo.songName
            Glide.with(this).load(songInfo.songCover).into(cover!!)
        } else {
            title!!.text = "播放详情页示例"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimerTask!!.removeUpdateProgressTask()
    }
}