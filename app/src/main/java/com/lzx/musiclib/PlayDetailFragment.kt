package com.lzx.musiclib

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
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
import com.lzx.starrysky.utils.MD5
import com.lzx.starrysky.utils.TimerTaskManager
import kotlinx.android.synthetic.main.fragment_play_detail.bgImage
import kotlinx.android.synthetic.main.fragment_play_detail.btnFastForward
import kotlinx.android.synthetic.main.fragment_play_detail.btnNextSong
import kotlinx.android.synthetic.main.fragment_play_detail.btnPlayMode
import kotlinx.android.synthetic.main.fragment_play_detail.btnPlayState
import kotlinx.android.synthetic.main.fragment_play_detail.btnPreSong
import kotlinx.android.synthetic.main.fragment_play_detail.btnRefrain
import kotlinx.android.synthetic.main.fragment_play_detail.btnRefrainJia
import kotlinx.android.synthetic.main.fragment_play_detail.btnRefrainJian
import kotlinx.android.synthetic.main.fragment_play_detail.btnRewind
import kotlinx.android.synthetic.main.fragment_play_detail.btnSongList
import kotlinx.android.synthetic.main.fragment_play_detail.btnSpeedFast
import kotlinx.android.synthetic.main.fragment_play_detail.btnSpeedSlow
import kotlinx.android.synthetic.main.fragment_play_detail.progressText
import kotlinx.android.synthetic.main.fragment_play_detail.relativeLayout
import kotlinx.android.synthetic.main.fragment_play_detail.seekBar
import kotlinx.android.synthetic.main.fragment_play_detail.songName
import kotlinx.android.synthetic.main.fragment_play_detail.timeText


class PlayDetailFragment : BaseFragment() {

    companion object {
        fun newInstance(songId: String?, type: String?, position: Int = 0): PlayDetailFragment {
            val fragment = PlayDetailFragment()
            val bundle = Bundle()
            bundle.putString("songId", songId)
            bundle.putString("type", type)
            bundle.putInt("position", position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_play_detail

    private var songId: String? = null
    private var type: String? = null
    private var position: Int = 0
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
        songId = arguments?.getString("songId")
        type = arguments?.getString("type")
        position = arguments?.getInt("position", 0) ?: 0
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        timerTaskManager.bindLifecycle(lifecycle)
        if (songId.isNullOrEmpty()) {
            activity?.finish()
            return
        }
        when (type) {
            "baidu" -> {
                viewModel?.getBaiduMusicUrl(songId!!)
            }
            "qq" -> {
                val songInfo = StarrySky.with().getPlayList().getOrNull(0)
                songInfo?.let {
                    initDetailUI(it)
                    StarrySky.with().playMusicByIndex(0)
                }
            }
            "flac" -> {
                val songInfo = StarrySky.with().getPlayList().getOrNull(position)
                songInfo?.let {
                    initDetailUI(it)
                    StarrySky.with().playMusicByInfo(it)
                }
            }
            else -> {
                val songInfo = StarrySky.with().getNowPlayingSongInfo()
                songInfo?.let {
                    initDetailUI(it)
                }
            }
        }

        refrainList.add("file:///android_asset/hglo1.ogg")
        refrainList.add("file:///android_asset/hglo2.ogg")
        refrainList.add("file:///android_asset/hglo3.ogg")
        refrainList.add("file:///android_asset/hglo4.ogg")
        refrainList.add("file:///android_asset/hglo5.ogg")
        refrainList.add("file:///android_asset/hglo6.ogg")
        refrainList.add("file:///android_asset/hglo7.ogg")
        refrainList.add("file:///android_asset/hglo8.ogg")

        imageColorList.add(Color.RED)
        imageColorList.add(Color.YELLOW)
        imageColorList.add(Color.GREEN)
        imageColorList.add(Color.BLUE)
        imageColorList.add(Color.CYAN)
        imageColorList.add(Color.MAGENTA)
        imageColorList.add(Color.DKGRAY)
        imageColorList.add(Color.GRAY)

        viewModel?.songInfoLiveData?.observe(this, Observer {
            initDetailUI(it)
            StarrySky.with().playMusicByInfo(it)
        })
        StarrySky.with().playbackState().observe(this, Observer {
            if (it.songInfo.isRefrain()) {
                if (it.stage == PlaybackStage.PLAYING) {
                    nextBeat = -1
                }
                return@Observer
            }
            if (dialog?.isShowing == true) {
                val recycleView = dialog?.getCustomView()?.findViewById<RecyclerView>(R.id.recycleView)
                recycleView?.adapter?.notifyDataSetChanged()
            }
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    it.songInfo?.let { info -> initDetailUI(info) }
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_pause)
                    timerTaskManager.startToUpdateProgress()
                    it.songInfo?.songId?.let { id -> songName?.startToScroll(id) }
                }
                PlaybackStage.PAUSE,
                PlaybackStage.STOP -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_play)
                    timerTaskManager.stopToUpdateProgress()
                    songName?.stop()
                }
                PlaybackStage.ERROR -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_pause)
                    timerTaskManager.stopToUpdateProgress()
                    activity?.showToast("播放失败：" + it.errorMsg)
                    songName?.stop()
                }
                PlaybackStage.IDEA -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_play)
                    timerTaskManager.stopToUpdateProgress()
                    songName?.stop()
                }
            }
        })
        timerTaskManager.setUpdateProgressTask(Runnable {
            val position = StarrySky.with().getPlayingPosition()
            val duration = StarrySky.with().getDuration()
            val buffered = StarrySky.with().getBufferedPosition()
            if (seekBar.max.toLong() != duration) {
                seekBar.max = duration.toInt()
            }
            seekBar.progress = position.toInt()
            seekBar.secondaryProgress = buffered.toInt()
            progressText.text = position.formatTime()
            timeText.text = duration.formatTime()
            setUpRefrain(position)
        })
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with().seekTo(seekBar.progress.toLong())
            }
        })
        btnRewind?.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().rewind()
            }
        }
        btnFastForward?.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().fastForward()
            }
        }
        btnSpeedSlow?.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().onDerailleur(true, 0.1f)
            }
        }
        btnSpeedFast?.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().onDerailleur(true, 1.1f)
            }
        }
        btnSongList?.setOnClickListener {
            showSongListDialog()
        }
        btnNextSong?.setOnClickListener {
            StarrySky.with().skipToNext()
        }
        btnPreSong?.setOnClickListener {
            StarrySky.with().skipToPrevious()
        }
        relativeLayout?.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().pauseMusic()
            } else {
                StarrySky.with().restoreMusic()
            }
        }
        //点击逻辑:顺序播放->列表循环->单曲播放->单曲循环->随机播放->倒序播放->倒序列表循环->顺序播放
        val repeatMode = StarrySky.with().getRepeatMode()
        when (repeatMode.repeatMode) {
            RepeatMode.REPEAT_MODE_NONE -> btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
            RepeatMode.REPEAT_MODE_ONE -> btnPlayMode?.setImageResource(R.drawable.ic_danqu)
            RepeatMode.REPEAT_MODE_SHUFFLE -> btnPlayMode?.setImageResource(R.drawable.ic_shunji)
            RepeatMode.REPEAT_MODE_REVERSE -> btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
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
                    btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
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
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_REVERSE, false)
                    btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
                    activity?.showToast("倒序播放")
                }
                RepeatMode.REPEAT_MODE_REVERSE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, false)
                    btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
                    activity?.showToast("顺序播放")
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_REVERSE, true)
                    btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
                    activity?.showToast("倒序列表循环")
                }
            }
        }

        if (StarrySky.with().isRefrainPlaying()) {
            btnRefrain?.text = "伴奏关"
            isStartRefraining = true
        } else {
            btnRefrain?.text = "伴奏开"
            isStartRefraining = false
        }
        btnRefrain?.setOnClickListener {
            isStartRefraining = if (StarrySky.with().isRefrainPlaying() || StarrySky.with().isRefrainBuffering()) {
                StarrySky.with().stopRefrain()
                btnRefrain?.text = "伴奏开"
                false
            } else {
                btnRefrain?.text = "伴奏关"
                true
            }
        }

        btnRefrainJia.setOnClickListener {
            val currVolume = StarrySky.with().getRefrainVolume()
            StarrySky.with().setRefrainVolume(currVolume + 0.1F)
        }
        btnRefrainJian.setOnClickListener {
            val currVolume = StarrySky.with().getRefrainVolume()
            StarrySky.with().setRefrainVolume(currVolume - 0.1F)
        }
    }

    private fun setUpRefrain(position: Long) {
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
    }

    private fun showSound(i: Int, position: Double) {
        Log.i("当前节拍88", "节拍 $i   ->beatStartTime:$beatStartTime   ->position:$position")
        bgImage.borderColor = imageColorList[i]
        bgImage.shadowColor = imageColorList[i]
        if (!isStartRefraining) return
        val url = refrainList[i]
        StarrySky.with().playRefrain(SongInfo(MD5.hexdigest(url), url))
    }

    private fun initDetailUI(it: SongInfo) {
        bgImage?.loadImage(it.songCover)
        songName?.setText(it.songId, it.songName)
        songName?.setScrollDuration(20000)
        timeText?.text = it.duration.formatTime()
        if (StarrySky.with().isPlaying() && StarrySky.with().getNowPlayingSongId() != it.songId) {
            StarrySky.with().stopMusic()
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
            lifecycleOwner(this@PlayDetailFragment)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpRepeatMode(playMode: TextView?) {
        val repeatMode = StarrySky.with().getRepeatMode()
        var playModeText = "顺序播放"
        when (repeatMode.repeatMode) {
            RepeatMode.REPEAT_MODE_NONE -> playModeText = if (repeatMode.isLoop) "列表循环" else "顺序播放"
            RepeatMode.REPEAT_MODE_ONE -> playModeText = if (repeatMode.isLoop) "单曲循环" else "单曲播放"
            RepeatMode.REPEAT_MODE_SHUFFLE -> playModeText = "随机播放"
            RepeatMode.REPEAT_MODE_REVERSE -> playModeText = if (repeatMode.isLoop) "倒序列表循环" else "倒序播放"
        }
        playMode?.text = playModeText + "（" + StarrySky.with().getPlayList().size + "）"
    }

    private fun setUpSongList(recycleView: RecyclerView?) {
        recycleView?.setup<SongInfo> {
            dataSource(StarrySky.with().getPlayList() ?: mutableListOf())
            adapter {
                addItem(R.layout.item_dialog_song_list) {
                    bindViewHolder { data, position, holder ->
                        val imgAnim = holder.findViewById<ImageView>(R.id.imgAnim)
                        val anim = imgAnim.drawable as AnimationDrawable
                        anim.start()
                        if (StarrySky.with().isCurrMusicIsPlaying(data?.songId!!)) {
                            imgAnim.visibility = View.VISIBLE
                        } else {
                            imgAnim.visibility = View.GONE
                        }
                        setText(
                            R.id.songName to data.songName,
                            R.id.singer to "-" + data.artist
                        )
                        itemClicked(View.OnClickListener {
                            StarrySky.with().playMusicByIndex(position)
                        })
                    }
                }
            }
        }
    }

    override fun unInitView() {
    }

}