package com.lzx.musiclib.example

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.lzx.musiclib.R
import com.lzx.musiclib.example.MusicRequest.RequestCallback
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.StarrySky.Companion.with
import com.lzx.starrysky.common.PlaybackStage
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.TimerTaskManager

class ListPlayExampleActivity : AppCompatActivity() {
    private var mListPlayAdapter: ListPlayAdapter? = null
    private var mTimerTask: TimerTaskManager? = null
    private lateinit var playPause: TextView
    private lateinit var playMode: TextView
    private val isSettingShuffleMode = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listplay_example)
        playPause = findViewById(R.id.play_pause)
        playMode = findViewById(R.id.play_mode)
        val recyclerView = findViewById<RecyclerView>(R.id.recycle_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        mListPlayAdapter = ListPlayAdapter(this)
        recyclerView.adapter = mListPlayAdapter
        mTimerTask = TimerTaskManager()


        //状态监听
        with().playbackState().observe(this, Observer { playbackStage: PlaybackStage? ->
            if (playbackStage == null) {
                return@Observer
            }
            when (playbackStage.getStage()) {
                PlaybackStage.NONE -> playPause.setText("播放/暂停")
                PlaybackStage.START -> {
                    mListPlayAdapter!!.notifyDataSetChanged()
                    mTimerTask!!.startToUpdateProgress()
                    playPause.setText("暂停")
                }
                PlaybackStage.PAUSE -> {
                    mTimerTask!!.stopToUpdateProgress()
                    playPause.setText("播放")
                    mListPlayAdapter!!.notifyDataSetChanged()
                }
                PlaybackStage.STOP -> {
                    mTimerTask!!.stopToUpdateProgress()
                    playPause.setText("播放")
                }
                PlaybackStage.COMPLETION -> {
                    mTimerTask!!.stopToUpdateProgress()
                    playPause.setText("播放/暂停")
                }
                PlaybackStage.BUFFERING -> playPause.setText("缓存中")
                PlaybackStage.ERROR -> {
                    mTimerTask!!.stopToUpdateProgress()
                    playPause.setText("播放/暂停")
                    Toast.makeText(this, playbackStage.getErrorMessage(), Toast.LENGTH_SHORT)
                            .show()
                }
                else -> {
                }
            }
        })
        //        playMode.setOnClickListener(v -> {
//            int repeatMode = StarrySky.with().getRepeatMode();
//            int shuffleMode = StarrySky.with().getShuffleMode();
//            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE) {
//                StarrySky.with().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
//                playMode.setText("单曲循环"); //当前是顺序播放，设置为单曲循环
//            }
//            else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
//                StarrySky.with().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
//                playMode.setText("列表循环"); //当前是单曲循环，设置为列表循环
//            }
//            else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL && !isSettingShuffleMode) {
//                playMode.setText("随机播放"); //当前是列表循环，设置为随机播放
//                StarrySky.with().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
//                isSettingShuffleMode = true;
//            } else if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
//                playMode.setText("顺序播放");  //当前是随机播放，设置为顺序播放
//                StarrySky.with().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
//                StarrySky.with().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
//                isSettingShuffleMode = false;
//            }
//        });
        //进度监听
        mTimerTask!!.setUpdateProgressTask {
            val songInfo = with().getNowPlayingSongInfo()
            val position = mListPlayAdapter!!.songInfos?.indexOf(songInfo)?:0
            mListPlayAdapter!!.updateItemProgress(position)
        }
        //上一首
        findViewById<View>(R.id.previous).setOnClickListener { v: View? -> with().skipToPrevious() }
        //播放/暂停
        playPause.setOnClickListener(View.OnClickListener { v: View? ->
            val text = playPause.getText().toString()
            if (text == "播放/暂停") {
                mListPlayAdapter?.songInfos?.let { with().playMusic(it, 0) }
            } else if (text == "暂停") {
                with().pauseMusic()
            } else if (text == "播放") {
                with().restoreMusic()
            }
        })
        //停止
        findViewById<View>(R.id.stop).setOnClickListener { v: View? -> with().stopMusic() }
        //下一首
        findViewById<View>(R.id.next).setOnClickListener { v: View? -> with().skipToNext() }


        //获取数据
        val musicRequest = MusicRequest()
        musicRequest.requestSongList(object : RequestCallback {
            override fun onSuccess(list: MutableList<SongInfo>) {
                StarrySky.with().updatePlayList(list)
                mListPlayAdapter?.songInfos = list
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimerTask!!.removeUpdateProgressTask()
    }
}