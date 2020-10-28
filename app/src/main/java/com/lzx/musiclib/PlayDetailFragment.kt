package com.lzx.musiclib

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.musiclib.weight.dialog.CommonBehavior
import com.lzx.musiclib.weight.dialog.MaterialDialog
import com.lzx.musiclib.weight.dialog.createMaterialDialog
import com.lzx.musiclib.weight.dialog.getCustomView
import com.lzx.musiclib.weight.dialog.lifecycleOwner
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.isRefrain
import com.lzx.starrysky.playback.PlaybackStage
import com.lzx.starrysky.utils.TimerTaskManager
import com.lzx.starrysky.utils.md5
import kotlinx.android.synthetic.main.fragment_play_detail.btnAccompaniment
import kotlinx.android.synthetic.main.fragment_play_detail.btnNextSong
import kotlinx.android.synthetic.main.fragment_play_detail.btnPlayMode
import kotlinx.android.synthetic.main.fragment_play_detail.btnPlayState
import kotlinx.android.synthetic.main.fragment_play_detail.btnPreSong
import kotlinx.android.synthetic.main.fragment_play_detail.btnSpeed
import kotlinx.android.synthetic.main.fragment_play_detail.btnVolume
import kotlinx.android.synthetic.main.fragment_play_detail.progressText
import kotlinx.android.synthetic.main.fragment_play_detail.seekBar
import kotlinx.android.synthetic.main.fragment_play_detail.songDesc
import kotlinx.android.synthetic.main.fragment_play_detail.songName
import kotlinx.android.synthetic.main.fragment_play_detail.timeText
import kotlinx.android.synthetic.main.fragment_play_detail.txtAccompaniment


class PlayDetailFragment : BaseFragment() {

    companion object {
        fun newInstance(channelId: Int): PlayDetailFragment {
            val fragment = PlayDetailFragment()
            val bundle = Bundle()
            bundle.putInt("channelId", channelId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_play_detail

    private var channelId: Int = 10

    private var viewModel: MusicViewModel? = null
    private var timerTaskManager = TimerTaskManager()
    private var dialog: MaterialDialog? = null
    private var refrainList = mutableListOf<String>()
    private var imageColorList = mutableListOf<Int>()

    //节拍坐标
    private var nextBeat = -1

    //节拍时间
    private var beatTime = String.format("%.2f", 60.00 / 120.00).toDouble()

    //节拍开始时间
    private var beatStartTime = String.format("%.2f", 0.50).toDouble()

    private var isStartRefraining: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun initView(view: View?) {
        channelId = arguments?.getInt("channelId") ?: 10
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        timerTaskManager.bindLifecycle(lifecycle)
        viewModel?.getSongList(channelId)
        viewModel?.songInfos?.observe(this, Observer {
            val songInfo = StarrySky.with().getNowPlayingSongInfo()
            val isPlaying = StarrySky.with().isPlaying()
            if (songInfo != null && !it.contains(songInfo)) {
                if (isPlaying) {
                    StarrySky.with().stopMusic()
                }
            }
            StarrySky.with().playMusic(it, 0)
        })

        refrainList.add("file:///android_asset/hglo1.ogg")
        refrainList.add("file:///android_asset/hglo2.ogg")
        refrainList.add("file:///android_asset/hglo3.ogg")
        refrainList.add("file:///android_asset/hglo4.ogg")
        refrainList.add("file:///android_asset/hglo5.ogg")
        refrainList.add("file:///android_asset/hglo6.ogg")
        refrainList.add("file:///android_asset/hglo7.ogg")
        refrainList.add("file:///android_asset/hglo8.ogg")

//       也可以用 SoundPool 来播放伴奏
//        val list = mutableListOf<Any>()
//        list.add(AssetResIdData(resId = R.raw.hglo1))
//        list.add(AssetResIdData(resId = R.raw.hglo2))
//        list.add(AssetResIdData(resId = R.raw.hglo3))
//        list.add(AssetResIdData(resId = R.raw.hglo4))
//        list.add(AssetResIdData(resId = R.raw.hglo5))
//        list.add(AssetResIdData(resId = R.raw.hglo6))
//        list.add(AssetResIdData(resId = R.raw.hglo7))
//        list.add(AssetResIdData(resId = R.raw.hglo8))
//        StarrySky.soundPool().loadSound(list)

        imageColorList.add(Color.RED)
        imageColorList.add(Color.YELLOW)
        imageColorList.add(Color.GREEN)
        imageColorList.add(Color.BLUE)
        imageColorList.add(Color.CYAN)
        imageColorList.add(Color.MAGENTA)
        imageColorList.add(Color.DKGRAY)
        imageColorList.add(Color.GRAY)

        StarrySky.with().playbackState().observe(this, Observer {
            if (it.songInfo.isRefrain()) {
                if (it.stage == PlaybackStage.PLAYING) {
                    nextBeat = -1
                }
                return@Observer
            }
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    songName?.text = it.songInfo?.songName
                    songDesc?.text = it.songInfo?.artist
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_pause)
                    timerTaskManager.startToUpdateProgress()
                }
                PlaybackStage.PAUSE,
                PlaybackStage.STOP -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_play)
                    timerTaskManager.stopToUpdateProgress()
                }
                PlaybackStage.ERROR -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_pause)
                    timerTaskManager.stopToUpdateProgress()
                    activity?.showToast("播放失败：" + it.errorMsg)
                }
                PlaybackStage.IDEA -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_play)
                    timerTaskManager.stopToUpdateProgress()
                }
            }
        })
        timerTaskManager.setUpdateProgressTask(Runnable {
            val position = StarrySky.with().getPlayingPosition()
            val duration = StarrySky.with().getDuration()
            if (seekBar.max.toLong() != duration) {
                seekBar.max = duration.toInt()
            }
            seekBar.progress = position.toInt()
            progressText.text = position.formatTime()
            timeText.text = " / " + duration.formatTime()
            setUpRefrain(position)
        })
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with().seekTo(seekBar.progress.toLong())
            }
        })
        btnVolume?.setOnClickListener {
            showControlDialog("Volume")
        }
        btnSpeed?.setOnClickListener {
            showControlDialog("Speed")
        }
        btnNextSong?.setOnClickListener {
            StarrySky.with().skipToNext()
        }
        btnPreSong?.setOnClickListener {
            StarrySky.with().skipToPrevious()
        }
        btnPlayState?.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().pauseMusic()
            } else {
                StarrySky.with().restoreMusic()
            }
        }
        //点击逻辑:顺序播放->列表循环->单曲播放->单曲循环->随机播放->顺序播放
        val repeatMode = StarrySky.with().getRepeatMode()
        when (repeatMode.repeatMode) {
            RepeatMode.REPEAT_MODE_NONE -> {
                if (repeatMode.isLoop) {
                    btnPlayMode?.setImageResource(R.drawable.bt_playpage_loop_press)
                } else {
                    btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
                }
            }
            RepeatMode.REPEAT_MODE_ONE -> btnPlayMode?.setImageResource(R.drawable.ic_danqu)
            RepeatMode.REPEAT_MODE_SHUFFLE -> btnPlayMode?.setImageResource(R.drawable.ic_shunji)
        }
        btnPlayMode.setOnClickListener {
            val model = StarrySky.with().getRepeatMode()
            when (model.repeatMode) {
                RepeatMode.REPEAT_MODE_NONE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, false)
                    btnPlayMode?.setImageResource(R.drawable.ic_danqu)
                    activity?.showToast("当前为单曲播放")
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, true)
                    btnPlayMode?.setImageResource(R.drawable.bt_playpage_loop_press)
                    activity?.showToast("列表循环")
                }
                RepeatMode.REPEAT_MODE_ONE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_SHUFFLE, false)
                    btnPlayMode?.setImageResource(R.drawable.ic_shunji)
                    activity?.showToast("随机播放")
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, true)
                    btnPlayMode?.setImageResource(R.drawable.ic_danqu)
                    activity?.showToast("单曲循环")
                }
                RepeatMode.REPEAT_MODE_SHUFFLE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, false)
                    btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
                    activity?.showToast("顺序播放")
                }
            }
        }
        if (StarrySky.with().isRefrainPlaying()) {
            txtAccompaniment?.text = "伴奏关"
            isStartRefraining = true
        } else {
            txtAccompaniment?.text = "伴奏开"
            isStartRefraining = false
        }
        btnAccompaniment?.setOnClickListener {
            isStartRefraining = if (StarrySky.with().isRefrainPlaying() || StarrySky.with().isRefrainBuffering()) {
                StarrySky.with().stopRefrain()
                txtAccompaniment?.text = "伴奏开"
                false
            } else {
                txtAccompaniment?.text = "伴奏关"
                true
            }
        }
    }

    private fun setUpRefrain(position: Long) {
        try {
            val playPosition = String.format("%f", position.toDouble() / 1000.00).toDouble()
            val next = String.format("%.0f", (playPosition - beatStartTime) / beatTime).toDouble()
            if (next < 0) return
            if (nextBeat == next.toInt()) return
            nextBeat = next.toInt()
            Log.i("当前节拍", "$nextBeat")
            when (nextBeat % 8) {
                0 -> showSound(0, playPosition)
                1 -> showSound(1, playPosition)
                2 -> showSound(2, playPosition)
                3 -> showSound(3, playPosition)
                4 -> showSound(4, playPosition)
                5 -> showSound(5, playPosition)
                6 -> showSound(6, playPosition)
                7 -> showSound(7, playPosition)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun showSound(i: Int, position: Double) {
        Log.i("当前节拍88", "节拍 $i   ->beatStartTime:$beatStartTime   ->position:$position")

        if (!isStartRefraining) return
        val url = refrainList[i]
        StarrySky.with().playRefrain(SongInfo(url.md5(), url))
        StarrySky.soundPool().playSound(i)
    }

    @SuppressLint("SetTextI18n")
    private fun showControlDialog(type: String) {
        dialog = activity?.createMaterialDialog(CommonBehavior(R.style.dialog_base_style,
            "gravity" to Gravity.BOTTOM,
            "windowAnimations" to R.style.select_popup_bottom,
            "realHeight" to 147.dp))?.show {
            cancelOnTouchOutside(true)
            noAutoDismiss()
            customView(R.layout.dialog_music_detail) {
                val customView = it.getCustomView() as ViewGroup
                val title = customView.findViewById<TextView>(R.id.title)
                val desc = customView.findViewById<TextView>(R.id.desc)
                val seekBar = customView.findViewById<SeekBar>(R.id.seekBar)

                if ("Volume" == type) {
                    seekBar.max = 100
                    title.text = "音量调节"
                    seekBar.progress = (StarrySky.with().getVolume() * 100f).toInt()
                    desc.text = "当前音量：" + seekBar.progress + " %"
                } else if ("Speed" == type) {
                    title.text = "调节速度"
                    seekBar.max = 200 //最大2倍速
                    seekBar.progress = StarrySky.with().getPlaybackSpeed().toInt() * 100
                    desc.text = "当前速度：" + seekBar.progress + " %"
                }
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if ("Volume" == type) {
                            StarrySky.with().setVolume(progress.toFloat() / 100f)
                            desc.text = "当前音量：$progress %"
                        } else if ("Speed" == type) {
                            StarrySky.with().onDerailleur(false, progress.toFloat() / 100)
                            desc.text = "当前速度：$progress %"
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
            lifecycleOwner(this@PlayDetailFragment)
        }
    }

    override fun unInitView() {
    }
}