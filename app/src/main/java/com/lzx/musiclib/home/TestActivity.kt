package com.lzx.musiclib.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.lzx.musiclib.R
import com.lzx.musiclib.showToast
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.StarrySkyPlayer
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.intercept.InterceptCallback
import com.lzx.starrysky.intercept.InterceptorThread
import com.lzx.starrysky.intercept.StarrySkyInterceptor
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.utils.MainLooper
import com.lzx.starrysky.utils.formatTime
import com.lzx.starrysky.utils.md5
import kotlinx.android.synthetic.main.activity_test.cacheSwitch
import kotlinx.android.synthetic.main.activity_test.closeN
import kotlinx.android.synthetic.main.activity_test.dash
import kotlinx.android.synthetic.main.activity_test.delete
import kotlinx.android.synthetic.main.activity_test.flac
import kotlinx.android.synthetic.main.activity_test.getAudioSessionId
import kotlinx.android.synthetic.main.activity_test.getNowPlayingIndex
import kotlinx.android.synthetic.main.activity_test.getNowPlayingSongInfo
import kotlinx.android.synthetic.main.activity_test.getPlayList
import kotlinx.android.synthetic.main.activity_test.getRepeatMode
import kotlinx.android.synthetic.main.activity_test.interceptor
import kotlinx.android.synthetic.main.activity_test.isSkipToNextEnabled
import kotlinx.android.synthetic.main.activity_test.isSkipToPreviousEnabled
import kotlinx.android.synthetic.main.activity_test.m3u8Btn
import kotlinx.android.synthetic.main.activity_test.newPlayer1
import kotlinx.android.synthetic.main.activity_test.newPlayer2
import kotlinx.android.synthetic.main.activity_test.notifySwitch
import kotlinx.android.synthetic.main.activity_test.openN
import kotlinx.android.synthetic.main.activity_test.pauseMusic
import kotlinx.android.synthetic.main.activity_test.playMusic
import kotlinx.android.synthetic.main.activity_test.playMusicById
import kotlinx.android.synthetic.main.activity_test.playMusicByInfo
import kotlinx.android.synthetic.main.activity_test.playMusicByUrl
import kotlinx.android.synthetic.main.activity_test.querySongInfoInLocal
import kotlinx.android.synthetic.main.activity_test.replay
import kotlinx.android.synthetic.main.activity_test.restoreMusic
import kotlinx.android.synthetic.main.activity_test.rtmpBtn
import kotlinx.android.synthetic.main.activity_test.seekBarPro
import kotlinx.android.synthetic.main.activity_test.seekBarSpeed
import kotlinx.android.synthetic.main.activity_test.seekBarVolume
import kotlinx.android.synthetic.main.activity_test.setRepeatMode
import kotlinx.android.synthetic.main.activity_test.skipToNext
import kotlinx.android.synthetic.main.activity_test.skipToPrevious
import kotlinx.android.synthetic.main.activity_test.soundPool
import kotlinx.android.synthetic.main.activity_test.stopMusic
import kotlinx.android.synthetic.main.activity_test.stopNewPlayer1
import kotlinx.android.synthetic.main.activity_test.stopNewPlayer2
import kotlinx.android.synthetic.main.activity_test.tvPro
import kotlinx.android.synthetic.main.activity_test.tvSpeed
import kotlinx.android.synthetic.main.activity_test.tvVolume
import kotlinx.android.synthetic.main.activity_test.updateList


open class TestActivity : AppCompatActivity() {

    val z =
        "https://github.com/EspoirX/lzxTreasureBox/raw/master/%E5%91%A8%E6%9D%B0%E4%BC%A6-%E5%91%8A%E7%99%BD%E6%B0%94%E7%90%83.mp3"
    val a = "https://github.com/EspoirX/lzxTreasureBox/raw/master/a.aac"
    val b = "https://github.com/EspoirX/lzxTreasureBox/raw/master/b.aac"
    val c = "https://github.com/EspoirX/lzxTreasureBox/raw/master/c.aac"
    val d = "https://github.com/EspoirX/lzxTreasureBox/raw/master/d.aac"
    val e = "https://github.com/EspoirX/lzxTreasureBox/raw/master/e.aac"
    val f = "https://github.com/EspoirX/lzxTreasureBox/raw/master/f.aac"
    val g = "https://github.com/EspoirX/lzxTreasureBox/raw/master/g.aac"

    val test = "http://shunaier.oss-cn-beijing.aliyuncs.com/4h-test-img/system-c3dc731aa1dc4877a0e386fa8d0073f6-YT.aac"

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

        val m3u8 = mutableListOf<Pair<String, String>>()
        m3u8.add(Pair("CRI汉语环球", "http://sk.cri.cn/hyhq.m3u8"))
        m3u8.add(Pair("CRI环球资讯", "http://sk.cri.cn/nhzs.m3u8"))
        m3u8.add(Pair("CRI劲曲调频", "http://sk.cri.cn/887.m3u8"))
        m3u8.add(Pair("CRI怀旧金曲", "http://sk.cri.cn/oldies.m3u8"))
        m3u8.add(Pair("CRI客家之声", "http://sk.cri.cn/hakka.m3u8"))
        m3u8.add(Pair("CRI闽南之音", "http://sk.cri.cn/minnan.m3u8"))
        m3u8.add(Pair("CRI世界华声", "http://sk.cri.cn/hxfh.m3u8"))
        m3u8.add(Pair("CRl News", "http://sk.cri.cn/905.m3u8"))
        m3u8.add(Pair("CRI EZFM", "http://sk.cri.cn/915.m3u8"))
        m3u8.add(Pair("CRI Nairobi 91.9", "http://sk.cri.cn/kenya.m3u8"))
        m3u8.add(Pair("CRI music", "http://sk.cri.cn/am1008.m3u8"))

        val m3u8List = mutableListOf<SongInfo>()
        m3u8.forEach {
            val songInfo = SongInfo(it.second.md5(), it.second)
            songInfo.artist = it.first
            songInfo.songName = it.first
            songInfo.songCover = "https://blog.xmcdn.com/wp-content/uploads/2014/07/%E5%BD%95%E9%9F%B3.jpg"
            m3u8List.add(songInfo)
        }

        val rtmp = mutableListOf<Pair<String, String>>()
        rtmp.add(Pair("香港财经", "rtmp://202.69.69.180:443/webcast/bshdlive-pc"))
        rtmp.add(Pair("韩国GoodTV", "rtmp://mobliestream.c3tv.com:554/live/goodtv.sdp"))
        rtmp.add(Pair("韩国朝鲜日报", "rtmp://live.chosun.gscdn.com/live/tvchosun1.stream"))
        rtmp.add(Pair("美国1", "rtmp://ns8.indexforce.com/home/mystream"))
        rtmp.add(Pair("美国2", "rtmp://media3.scctv.net/live/scctv_800"))
        rtmp.add(Pair("美国中文电视", "rtmp://media3.sinovision.net:1935/live/livestream"))
        rtmp.add(Pair("湖南卫视", "rtmp://58.200.131.2:1935/livetv/hunantv"))

        val rtmpList = mutableListOf<SongInfo>()
        rtmp.forEach {
            val songInfo = SongInfo(it.second.md5(), it.second)
            songInfo.artist = it.first
            songInfo.songName = it.first
            songInfo.songCover = "https://img95.699pic.com/photo/50052/5059.jpg_wh300.jpg"
            rtmpList.add(songInfo)
        }

        playMusicById?.setOnClickListener {
            StarrySky.with().playMusicById("z")
        }

        val player = StarrySkyPlayer.create()
            .setAutoManagerFocus(false)
        playMusicByUrl?.setOnClickListener {
//            StarrySky.with().playMusicByUrl(test)
            player.with().playMusicByUrl(test)
        }
        playMusicByInfo?.setOnClickListener {
//            StarrySky.with().playMusicByInfo(SongInfo("a", a))
            StarrySky.with().playMusicByInfo(SongInfo("a", "http://ting6.yymp3.net:82/new14/zhangyj/5.mp3"))
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
                .addInterceptor(InterceptorB(), InterceptorThread.IO)
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
        notifySwitch?.setOnClickListener {
            val type = StarrySky.getNotificationType()
            if (type == INotification.SYSTEM_NOTIFICATION) {
                StarrySky.changeNotification(INotification.CUSTOM_NOTIFICATION)
                showToast("当前使用自定义通知栏")
            } else {
                StarrySky.changeNotification(INotification.SYSTEM_NOTIFICATION)
                showToast("当前使用系统通知栏")
            }
        }
        updateList?.setOnClickListener {
            val list = mutableListOf<SongInfo>()
            songList.add(SongInfo("f", f, "f"))
            songList.add(SongInfo("g", g, "g"))
            StarrySky.with().updatePlayList(list)

            val size = StarrySky.with().getPlayList().size
            showToast("size = $size")
        }

        flac?.setOnClickListener {
            val list = mutableListOf<SongInfo>()
            val info = SongInfo()
            info.songId = "11111"
            info.songUrl = "https://github.com/EspoirX/lzxTreasureBox/raw/master/%E6%83%B3%E4%B8%8D%E5%88%B0.flac"
            info.songName = "庄心妍-想不到"
            info.artist = "庄心妍"
            info.songCover = "https://y.gtimg.cn/music/photo_new/T001R300x300M000003Cn3Yh16q1MO.jpg?max_age=2592000"
            list.add(info)
            StarrySky.with().playMusic(list, 0)
        }

        dash?.setOnClickListener {
            val info = SongInfo()
            info.songId = "32313"
            info.songUrl = "https://storage.googleapis.com/wvmedia/clear/hevc/tears/tears.mpd"
            StarrySky.with().playMusicByInfo(info)
        }

        m3u8Btn?.setOnClickListener {
            StarrySky.with().playMusic(m3u8List, 0)
        }

        rtmpBtn?.setOnClickListener {
            StarrySky.with().playMusic(rtmpList, 0)
        }
        delete?.setOnClickListener {
            val list = StarrySky.with().getPlayList()
            StarrySky.with().removeSongInfo(list.getOrNull(1)?.songId)
            showToast("已删除")
        }
        newPlayer1?.setOnClickListener {
            val info = SongInfo(a.md5(), a)
//            StarrySky.newPlayer(0)?.play(info, true)
        }
        newPlayer2?.setOnClickListener {
            val info = SongInfo(b.md5(), b)
//            StarrySky.newPlayer(1)?.play(info, true)
        }
        stopNewPlayer1?.setOnClickListener {
//            StarrySky.newPlayer(0)?.stop()
        }
        stopNewPlayer2?.setOnClickListener {
//            StarrySky.newPlayer(1)?.stop()
        }
        closeN?.setOnClickListener {
            StarrySky.closeNotification()
        }
        openN?.setOnClickListener {
            StarrySky.openNotification()
        }
        replay?.setOnClickListener {
            StarrySky.with().replayCurrMusic()
        }

        StarrySky.with().setOnPlayProgressListener(object : OnPlayProgressListener {
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

        StarrySky.with().playbackState().observe(this) {
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    seekBarVolume.progress = (StarrySky.with().getVolume() * 100f).toInt()
                    tvVolume.text = "音量：" + seekBarVolume.progress + " %"

                    seekBarSpeed.progress = StarrySky.with().getPlaybackSpeed().toInt() * 100
                    tvSpeed.text = "音速：" + seekBarSpeed.progress + " %"
                }
                PlaybackStage.SWITCH -> {
                    showToast("切歌:last=" + it.lastSongInfo?.songName + " curr=" + it.songInfo?.songName)
                }
                PlaybackStage.IDLE -> {
                    seekBarPro.progress = 0
                    tvPro.text = "进度："
                    seekBarVolume.progress = 0
                    tvVolume.text = "音量："
                    seekBarSpeed.progress = 0
                    tvSpeed.text = "音速："
                }
            }
        }
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

    private class InterceptorA : StarrySkyInterceptor() {
        override fun process(songInfo: SongInfo?, callback: InterceptCallback) {
            val isInMainThread = MainLooper.instance.isInMainThread()
            Log.i("TestActivity", "InterceptorA#isInMainThread = $isInMainThread")
            callback.onNext(songInfo)
        }

        override fun getTag(): String = "InterceptorA"
    }

    private class InterceptorB : StarrySkyInterceptor() {
        override fun process(songInfo: SongInfo?, callback: InterceptCallback) {
            val isInMainThread = MainLooper.instance.isInMainThread()
            Log.i("TestActivity", "InterceptorA#isInMainThread = $isInMainThread")
            callback.onNext(songInfo)
        }

        override fun getTag(): String = "InterceptorB"
    }
}