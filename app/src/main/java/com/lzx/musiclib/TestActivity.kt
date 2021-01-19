package com.lzx.musiclib

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.intercept.AsyncInterceptor
import com.lzx.starrysky.intercept.InterceptorCallback
import com.lzx.starrysky.intercept.SyncInterceptor
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.utils.MainLooper
import kotlinx.android.synthetic.main.activity_test.cacheSwitch
import kotlinx.android.synthetic.main.activity_test.getAudioSessionId
import kotlinx.android.synthetic.main.activity_test.getNowPlayingIndex
import kotlinx.android.synthetic.main.activity_test.getNowPlayingSongInfo
import kotlinx.android.synthetic.main.activity_test.getPlayList
import kotlinx.android.synthetic.main.activity_test.getRepeatMode
import kotlinx.android.synthetic.main.activity_test.interceptor
import kotlinx.android.synthetic.main.activity_test.isSkipToNextEnabled
import kotlinx.android.synthetic.main.activity_test.isSkipToPreviousEnabled
import kotlinx.android.synthetic.main.activity_test.pauseMusic
import kotlinx.android.synthetic.main.activity_test.playMusic
import kotlinx.android.synthetic.main.activity_test.playMusicById
import kotlinx.android.synthetic.main.activity_test.playMusicByInfo
import kotlinx.android.synthetic.main.activity_test.playMusicByUrl
import kotlinx.android.synthetic.main.activity_test.querySongInfoInLocal
import kotlinx.android.synthetic.main.activity_test.restoreMusic
import kotlinx.android.synthetic.main.activity_test.seekBarPro
import kotlinx.android.synthetic.main.activity_test.seekBarSpeed
import kotlinx.android.synthetic.main.activity_test.seekBarVolume
import kotlinx.android.synthetic.main.activity_test.setRepeatMode
import kotlinx.android.synthetic.main.activity_test.skipToNext
import kotlinx.android.synthetic.main.activity_test.skipToPrevious
import kotlinx.android.synthetic.main.activity_test.soundPool
import kotlinx.android.synthetic.main.activity_test.stopMusic
import kotlinx.android.synthetic.main.activity_test.tvPro
import kotlinx.android.synthetic.main.activity_test.tvSpeed
import kotlinx.android.synthetic.main.activity_test.tvVolume

class TestActivity : AppCompatActivity() {

    val z = "https://github.com/EspoirX/lzxTreasureBox/raw/master/%E5%91%A8%E6%9D%B0%E4%BC%A6-%E5%91%8A%E7%99%BD%E6%B0%94%E7%90%83.mp3"
    val a = "http://musicall.bs2dl.yy.com/odgube404c8b5f2040538c13667ffe13e195_124493272196875776_1000377059_bs2_format.aac"
    val b = "https://musicall.bs2dl.yy.com/odgud9920364f39b4b4dba7176354bff4638_131088925343375188_44815391_bs2_format.aac"
    val c = "http://musicall.bs2dl.huanjuyun.com/odgu4718dd56e4fd4d679d057722e6c3262b_82863562902891919_44028220_bs2_format_1562780034.aac"
    val d = "http://musicall.bs2dl.yy.com/odgu235e603857254b9dbfb41f02dc7b1c90_131160036913908871_47464487_bs2_format.aac"
    val e = "http://musicall.bs2dl.yy.com/odguae65e25206cc43d78f563034d9bdc459_118382661783854139_45387239_bs2_format.aac"
    val f = "http://musicall.bs2dl.yy.com/odgud249a26ec99a478c838966cb69f30d5b_132815537382758232_45834629_bs2_format.aac"
    val g = "http://musicall.bs2dl.huanjuyun.com/odgu3ec10e9921484e8dbbef0e9245fabb56_73838702584411398_18655347_bs2_format_1562799252.aac"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val songList = mutableListOf<SongInfo>()
        songList.add(SongInfo("z", z, "z"))
        songList.add(SongInfo("a", a, "a"))
        songList.add(SongInfo("b", b, "b"))
        songList.add(SongInfo("c", c, "c"))
        songList.add(SongInfo("d", d, "d"))
        songList.add(SongInfo("e", e, "e"))
        songList.add(SongInfo("f", f, "f"))
        songList.add(SongInfo("g", g, "g"))

        val soundPoolList = mutableListOf<String>()
        soundPoolList.add("hglo1.ogg")
        soundPoolList.add("hglo2.ogg")
        soundPoolList.add("hglo3.ogg")
        soundPoolList.add("hglo4.ogg")
        soundPoolList.add("hglo5.ogg")
        soundPoolList.add("hglo6.ogg")
        soundPoolList.add("hglo7.ogg")
        soundPoolList.add("hglo8.ogg")

        playMusicById?.setOnClickListener {
            StarrySky.with().playMusicById("z")
        }
        playMusicByUrl?.setOnClickListener {
            StarrySky.with().playMusicByUrl(z)
        }
        playMusicByInfo?.setOnClickListener {
            StarrySky.with().playMusicByInfo(SongInfo("a", a))
        }
        playMusic?.setOnClickListener {
            StarrySky.with().playMusic(songList, 0)
        }
        pauseMusic?.setOnClickListener {
            StarrySky.with().pauseMusic()
        }
        restoreMusic?.setOnClickListener {
            StarrySky.with().restoreMusic()
        }
        stopMusic?.setOnClickListener {
            StarrySky.with().stopMusic()
        }
        skipToNext?.setOnClickListener {
            StarrySky.with().skipToNext()
        }
        skipToPrevious?.setOnClickListener {
            StarrySky.with().skipToPrevious()
        }
        setRepeatMode?.setOnClickListener {
            val mode = StarrySky.with().getRepeatMode()
            when (mode.repeatMode) {
                RepeatMode.REPEAT_MODE_NONE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, false)
                }
                RepeatMode.REPEAT_MODE_ONE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_SHUFFLE, false)
                }
                RepeatMode.REPEAT_MODE_SHUFFLE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_REVERSE, false)
                }
                RepeatMode.REPEAT_MODE_REVERSE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, false)
                }
            }
            getRepeatModelImpl()
        }
        getRepeatMode?.setOnClickListener {
            getRepeatModelImpl()
        }
        getPlayList?.setOnClickListener {
            val list = StarrySky.with().getPlayList()
            showToast(list.size.toString())
        }
        getNowPlayingSongInfo?.setOnClickListener {
            val info = StarrySky.with().getNowPlayingSongInfo()
            showToast(info?.songId)
        }
        getNowPlayingIndex?.setOnClickListener {
            val index = StarrySky.with().getNowPlayingIndex()
            showToast(index.toString())
        }

        isSkipToNextEnabled?.setOnClickListener {
            val isSkipToNextEnabled = StarrySky.with().isSkipToNextEnabled()
            showToast("isSkipToNextEnabled = $isSkipToNextEnabled")
        }
        isSkipToPreviousEnabled?.setOnClickListener {
            val isSkipToPreviousEnabled = StarrySky.with().isSkipToPreviousEnabled()
            showToast("isSkipToPreviousEnabled = $isSkipToPreviousEnabled")
        }
        getAudioSessionId?.setOnClickListener {
            val getAudioSessionId = StarrySky.with().getAudioSessionId()
            showToast("getAudioSessionId = $getAudioSessionId")
        }
        querySongInfoInLocal?.setOnClickListener {
            val list = StarrySky.with().querySongInfoInLocal(this)
            showToast("size = ${list.size}")
        }
        cacheSwitch?.text = if (StarrySky.isOpenCache()) "缓存开" else "缓存关"
        cacheSwitch?.setOnClickListener {
            StarrySky.with().cacheSwitch(!StarrySky.isOpenCache())
            cacheSwitch?.text = if (StarrySky.isOpenCache()) "缓存开" else "缓存关"
        }
        interceptor?.setOnClickListener {
            StarrySky.with()
                .addInterceptor(InterceptorA())
                .addInterceptor(InterceptorB())
                .playMusic(songList, 0)
        }
        var index = 0
        soundPool?.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().stopMusic()
            }

            StarrySky.soundPool()?.prepareForAssets(soundPoolList) {
                if (index > soundPoolList.lastIndex) {
                    index = 0
                }
                it.playSound(index)
                index++
            }
        }

        StarrySky.with().setOnPlayProgressListener(lifecycle, object : OnPlayProgressListener {
            @SuppressLint("SetTextI18n")
            override fun onPlayProgress(currPos: Long, duration: Long) {
                if (seekBarPro.max.toLong() != duration) {
                    seekBarPro.max = duration.toInt()
                }
                seekBarPro.progress = currPos.toInt()
                tvPro.text = "进度：" + currPos.formatTime() + " / " + duration.formatTime()
            }
        })
        seekBarPro.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with().seekTo(seekBar.progress.toLong())
            }
        })

        seekBarVolume.progress = (StarrySky.with().getVolume() * 100f).toInt()
        tvVolume.text = "音量：" + seekBarVolume.progress + " %"
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                StarrySky.with().setVolume(progress.toFloat() / 100f)
                tvVolume.text = "音量：$progress %"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        //seekBarSpeed配置最大速度是当前2倍
        seekBarSpeed.progress = StarrySky.with().getPlaybackSpeed().toInt() * 100
        tvSpeed.text = "音速：" + seekBarSpeed.progress + " %"
        seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                StarrySky.with().onDerailleur(false, progress.toFloat() / 100)
                tvSpeed.text = "音速：$progress %"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        StarrySky.with().playbackState().observe(this, {
            if (it.stage == PlaybackStage.PLAYING) {
                seekBarVolume.progress = (StarrySky.with().getVolume() * 100f).toInt()
                tvVolume.text = "音量：" + seekBarVolume.progress + " %"

                seekBarSpeed.progress = StarrySky.with().getPlaybackSpeed().toInt() * 100
                tvSpeed.text = "音速：" + seekBarSpeed.progress + " %"
            } else if (it.stage == PlaybackStage.SWITCH) {
                showToast("切歌:last=" + it.lastSongInfo?.songName + " curr=" + it.songInfo?.songName)
            } else if (it.stage == PlaybackStage.IDEA) {
                seekBarPro.progress = 0
                tvPro.text = "进度："
                seekBarVolume.progress = 0
                tvVolume.text = "音量："
                seekBarSpeed.progress = 0
                tvSpeed.text = "音速："
            }
        })
    }

    private fun getRepeatModelImpl() {
        val mode = StarrySky.with().getRepeatMode()
        val result = when (mode.repeatMode) {
            RepeatMode.REPEAT_MODE_NONE -> "顺序播放"
            RepeatMode.REPEAT_MODE_ONE -> "单曲播放"
            RepeatMode.REPEAT_MODE_SHUFFLE -> "随机播放"
            RepeatMode.REPEAT_MODE_REVERSE -> "倒序播放"
            else -> ""
        }
        showToast("当前：$result")
    }

    private class InterceptorA : AsyncInterceptor() {
        override fun process(songInfo: SongInfo?, callback: InterceptorCallback) {
            val isInMainThread = MainLooper.instance.isInMainThread()
            Log.i("TestActivity", "InterceptorA#isInMainThread = $isInMainThread")
            callback.onContinue(songInfo)
        }

        override fun getTag(): String = "InterceptorA"
    }

    private class InterceptorB : SyncInterceptor {
        override fun process(songInfo: SongInfo?): SongInfo? {
            val isInMainThread = MainLooper.instance.isInMainThread()
            Log.i("TestActivity", "InterceptorA#isInMainThread = $isInMainThread")
            return songInfo
        }

        override fun getTag(): String = "InterceptorB"
    }
}