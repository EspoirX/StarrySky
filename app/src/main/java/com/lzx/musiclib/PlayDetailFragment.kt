package com.lzx.musiclib

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.itemClicked
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.weight.dialog.CommonBehavior
import com.lzx.musiclib.weight.dialog.MaterialDialog
import com.lzx.musiclib.weight.dialog.createMaterialDialog
import com.lzx.musiclib.weight.dialog.getCustomView
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.playback.PlaybackStage
import com.lzx.starrysky.utils.TimerTaskManager
import kotlinx.android.synthetic.main.fragment_play_detail.btnFastForward
import kotlinx.android.synthetic.main.fragment_play_detail.btnNextSong
import kotlinx.android.synthetic.main.fragment_play_detail.btnPlayState
import kotlinx.android.synthetic.main.fragment_play_detail.btnPreSong
import kotlinx.android.synthetic.main.fragment_play_detail.btnRewind
import kotlinx.android.synthetic.main.fragment_play_detail.btnSongList
import kotlinx.android.synthetic.main.fragment_play_detail.btnSpeedFast
import kotlinx.android.synthetic.main.fragment_play_detail.btnSpeedSlow
import kotlinx.android.synthetic.main.fragment_play_detail.progressText
import kotlinx.android.synthetic.main.fragment_play_detail.relativeLayout
import kotlinx.android.synthetic.main.fragment_play_detail.seekBar
import kotlinx.android.synthetic.main.fragment_play_detail.songCover
import kotlinx.android.synthetic.main.fragment_play_detail.songName
import kotlinx.android.synthetic.main.fragment_play_detail.timeText


class PlayDetailFragment : BaseFragment() {

    companion object {
        fun newInstance(songId: String?, type: String?): PlayDetailFragment {
            val fragment = PlayDetailFragment()
            val bundle = Bundle()
            bundle.putString("songId", songId)
            bundle.putString("type", type)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_play_detail

    private var songId: String? = null
    private var type: String? = null
    private var viewModel: MusicViewModel? = null
    private var timerTaskManager = TimerTaskManager()
    private var dialog: MaterialDialog? = null


    @SuppressLint("SetTextI18n")
    override fun initView(view: View?) {
        songId = arguments?.getString("songId")
        type = arguments?.getString("type")
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        timerTaskManager.bindLifecycle(lifecycle)
        if (songId.isNullOrEmpty()) {
            activity?.finish()
            return
        }
        if (type == "baidu") {
            viewModel?.getBaiduMusicUrl(songId!!)
        } else if (type == "qq") {
            val songInfo = StarrySky.with()?.getPlayList()?.getOrNull(0)
            if (songInfo != null) {
                initDetailUI(songInfo)
                StarrySky.with()?.playMusicByIndex(0)
            }
        }
        viewModel?.songInfoLiveData?.observe(this, Observer {
            initDetailUI(it)
            StarrySky.with()?.playMusicByInfo(it)
        })
        StarrySky.with()?.playbackState()?.observe(this, Observer {
            if (dialog?.isShowing == true) {
                val recycleView = dialog?.getCustomView()?.findViewById<RecyclerView>(R.id.recycleView)
                recycleView?.adapter?.notifyDataSetChanged()
            }
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    it.songInfo?.let { info -> initDetailUI(info) }
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_pause)
                    timerTaskManager.startToUpdateProgress()
                }
                PlaybackStage.PAUSE,
                PlaybackStage.STOP -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_play)
                    timerTaskManager.stopToUpdateProgress()
                }
                PlaybackStage.BUFFERING -> {

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
            val position = StarrySky.with()?.getPlayingPosition()
            val duration = StarrySky.with()?.getDuration()
            val buffered = StarrySky.with()?.getBufferedPosition()
            if (seekBar.max.toLong() != duration) {
                seekBar.max = duration?.toInt() ?: 0
            }
            seekBar.progress = position?.toInt() ?: 0
            seekBar.secondaryProgress = buffered?.toInt() ?: 0
            progressText.text = position?.formatTime()
            timeText.text = duration?.formatTime()
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with()?.seekTo(seekBar.progress.toLong())
            }
        })
        btnRewind?.setOnClickListener {
            if (StarrySky.with()?.isPlaying() == true) {
                StarrySky.with()?.rewind()
            }
        }
        btnFastForward?.setOnClickListener {
            if (StarrySky.with()?.isPlaying() == true) {
                StarrySky.with()?.fastForward()
            }
        }
        btnSpeedSlow?.setOnClickListener {
            if (StarrySky.with()?.isPlaying() == true) {
                StarrySky.with()?.onDerailleur(true, 0.1f)
            }
        }
        btnSpeedFast?.setOnClickListener {
            if (StarrySky.with()?.isPlaying() == true) {
                StarrySky.with()?.onDerailleur(true, 1.1f)
            }
        }
        btnSongList?.setOnClickListener {
            showSongListDialog()
        }
        btnNextSong?.setOnClickListener {
            StarrySky.with()?.skipToNext()
        }
        btnPreSong?.setOnClickListener {
            StarrySky.with()?.skipToPrevious()
        }
        relativeLayout?.setOnClickListener {
            if (StarrySky.with()?.isPlaying() == true) {
                StarrySky.with()?.pauseMusic()
            } else {
                StarrySky.with()?.restoreMusic()
            }
        }
    }

    private fun initDetailUI(it: SongInfo) {
        songCover?.loadImage(it.songCover)
        songName?.text = it.songName
        timeText?.text = it.duration.formatTime()
        if (StarrySky.with()?.isPlaying() == true && StarrySky.with()?.getNowPlayingSongId() != it.songId) {
            StarrySky.with()?.stopMusic()
        }
    }

    private fun showSongListDialog() {
        dialog = activity?.createMaterialDialog(CommonBehavior(R.style.dialog_base_style,
            "gravity" to Gravity.BOTTOM,
            "windowAnimations" to R.style.select_popup_bottom,
            "realHeight" to 453.dp.toInt()))?.show {
            cancelOnTouchOutside(true)
            customView(R.layout.dialog_song_list) {
                val customView = it.getCustomView() as ViewGroup
                val playModel = customView.findViewById<TextView>(R.id.playModel)
                val recycleView = customView.findViewById<RecyclerView>(R.id.recycleView)
                setUpRepeatMode(playModel)
                setUpSongList(recycleView)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpRepeatMode(playMode: TextView?) {
        val repeatMode = StarrySky.with()?.getRepeatMode()
        var playModeText = "顺序播放"
        when (repeatMode?.repeatMode) {
            RepeatMode.REPEAT_MODE_NONE -> playModeText = if (repeatMode.isLoop) "列表循环" else "顺序播放"
            RepeatMode.REPEAT_MODE_ONE -> playModeText = if (repeatMode.isLoop) "单曲循环" else "单曲播放"
            RepeatMode.REPEAT_MODE_SHUFFLE -> playModeText = "随机播放"
            RepeatMode.REPEAT_MODE_REVERSE -> playModeText = if (repeatMode.isLoop) "倒序列表循环" else "倒序播放"
        }
        playMode?.text = playModeText + "（" + StarrySky.with()?.getPlayList()?.size + "）"
    }

    private fun setUpSongList(recycleView: RecyclerView?) {
        recycleView?.setup<SongInfo> {
            dataSource(StarrySky.with()?.getPlayList() ?: mutableListOf())
            adapter {
                addItem(R.layout.item_dialog_song_list) {
                    bindViewHolder { data, position, holder ->
                        val imgAnim = holder.findViewById<ImageView>(R.id.imgAnim)
                        val anim = imgAnim.drawable as AnimationDrawable
                        anim.start()
                        if (StarrySky.with()?.isCurrMusicIsPlaying(data?.songId!!) == true) {
                            imgAnim.visibility = View.VISIBLE
                        } else {
                            imgAnim.visibility = View.GONE
                        }
                        setText(
                            R.id.songName to data?.songName,
                            R.id.singer to "-" + data?.artist
                        )
                        itemClicked(View.OnClickListener {
                            StarrySky.with()?.playMusicByIndex(position)
                        })
                    }
                }
            }
        }
    }

    override fun unInitView() {
    }

}