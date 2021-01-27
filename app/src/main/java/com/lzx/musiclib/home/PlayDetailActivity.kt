package com.lzx.musiclib.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogBehavior
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.lzx.musiclib.R
import com.lzx.musiclib.adapter.addItem
import com.lzx.musiclib.adapter.itemClicked
import com.lzx.musiclib.adapter.notifyDataSetChanged
import com.lzx.musiclib.adapter.removedData
import com.lzx.musiclib.adapter.setText
import com.lzx.musiclib.adapter.setup
import com.lzx.musiclib.formatTime
import com.lzx.musiclib.getSelfViewModel
import com.lzx.musiclib.loadImage
import com.lzx.musiclib.showToast
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.musiclib.weight.SpectrumDrawView
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.manager.PlaybackStage
import kotlinx.android.synthetic.main.activity_play_detail.btnNextSong
import kotlinx.android.synthetic.main.activity_play_detail.btnPlayMode
import kotlinx.android.synthetic.main.activity_play_detail.btnPlayState
import kotlinx.android.synthetic.main.activity_play_detail.btnPreSong
import kotlinx.android.synthetic.main.activity_play_detail.btnTime
import kotlinx.android.synthetic.main.activity_play_detail.progressText
import kotlinx.android.synthetic.main.activity_play_detail.seekBar
import kotlinx.android.synthetic.main.activity_play_detail.seekBarSpeed
import kotlinx.android.synthetic.main.activity_play_detail.seekBarVolume
import kotlinx.android.synthetic.main.activity_play_detail.songCover
import kotlinx.android.synthetic.main.activity_play_detail.songDesc
import kotlinx.android.synthetic.main.activity_play_detail.songName
import kotlinx.android.synthetic.main.activity_play_detail.timeText
import kotlinx.android.synthetic.main.activity_play_detail.tvSpeed
import kotlinx.android.synthetic.main.activity_play_detail.tvVolume


class PlayDetailActivity : AppCompatActivity() {

    private var viewModel: MusicViewModel? = null
    private var songId: String = ""
    private var songList = mutableListOf<SongInfo>()
    private var dialog: MaterialDialog? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_detail)
        songId = intent.getStringExtra("songId")
        viewModel = getSelfViewModel {
            songList = getHomeMusic()
            val currSong = songList.filter { it.songId == songId }.getOrNull(0)
            if (currSong == null) {
                showToast("音频获取失败")
                finish()
                return@getSelfViewModel
            }
            initDetailUI(currSong)
        }
        //进度监听
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
        //状态监听
        StarrySky.with().playbackState().observe(this, { it ->
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    songName?.text = it.songInfo?.songName
                    songDesc?.text = it.songInfo?.artist
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_pause)
                    notifyDialogItem()
                }
                PlaybackStage.SWITCH -> { //切歌
                    it.songInfo?.let {
                        initDetailUI(it)
                    }
                }
                PlaybackStage.PAUSE,
                PlaybackStage.IDEA -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_play)
                }
                PlaybackStage.ERROR -> {
                    btnPlayState?.setImageResource(R.drawable.gdt_ic_play)
                    showToast("播放失败：" + it.errorMsg)
                }
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

        //速度SeekBar，seekBarSpeed配置最大速度是当前2倍
        seekBarSpeed.progress = StarrySky.with().getPlaybackSpeed().toInt() * 100
        tvSpeed.text = seekBarSpeed.progress.toString() + " %"
        seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                StarrySky.with().onDerailleur(false, progress.toFloat() / 100)
                tvSpeed.text = "$progress %"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        //音量SeekBar
        seekBarVolume.progress = (StarrySky.with().getVolume() * 100f).toInt()
        tvVolume.text = seekBarVolume.progress.toString() + " %"
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                StarrySky.with().setVolume(progress.toFloat() / 100f)
                tvVolume.text = "$progress %"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        //播放模式
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
        //点击逻辑:顺序播放->列表循环->单曲播放->单曲循环->随机播放->顺序播放
        btnPlayMode.setOnClickListener {
            val model = StarrySky.with().getRepeatMode()
            when (model.repeatMode) {
                RepeatMode.REPEAT_MODE_NONE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, false)
                    btnPlayMode?.setImageResource(R.drawable.ic_danqu)
                    showToast("单曲播放（不循环）")
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, true)
                    btnPlayMode?.setImageResource(R.drawable.bt_playpage_loop_press)
                    showToast("列表循环")
                }
                RepeatMode.REPEAT_MODE_ONE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_SHUFFLE, false)
                    btnPlayMode?.setImageResource(R.drawable.ic_shunji)
                    showToast("随机播放")
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, true)
                    btnPlayMode?.setImageResource(R.drawable.ic_danqu)
                    showToast("单曲循环")
                }
                RepeatMode.REPEAT_MODE_SHUFFLE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, false)
                    btnPlayMode?.setImageResource(R.drawable.ic_shunxu)
                    showToast("顺序播放")
                }
            }
        }

        //下一首
        btnNextSong?.setOnClickListener {
            StarrySky.with().skipToNext()
        }
        //上一首
        btnPreSong?.setOnClickListener {
            StarrySky.with().skipToPrevious()
        }
        //播放&暂停
        btnPlayState?.setOnClickListener {
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().pauseMusic()
            } else {
                StarrySky.with().restoreMusic()
            }
        }
        //播放列表
        btnTime?.setOnClickListener {
            showCustomViewDialog(BottomSheet(LayoutMode.WRAP_CONTENT))
        }
    }

    private fun showCustomViewDialog(dialogBehavior: DialogBehavior) {
        dialog = MaterialDialog(this, dialogBehavior).show {
            title(R.string.dialog_title)
            customView(R.layout.dialog_song_list, scrollable = true, horizontalPadding = true)
        }
        val customView = dialog?.getCustomView()
        val recycleView: RecyclerView? = customView?.findViewById(R.id.recycleView)
        val indexBtn: Button? = customView?.findViewById(R.id.indexBtn)
        indexBtn?.setOnClickListener {
            val index = StarrySky.with().getNowPlayingIndex()
            showToast("index = $index")
        }
        recycleView?.setup<SongInfo> {
            dataSource(songList)
            adapter {
                addItem(R.layout.item_dialog_song_list) {
                    bindViewHolder { data, position, holder ->
                        val btnClose = holder.findViewById<ImageView>(R.id.btnClose)
                        val imgAnim = holder.findViewById<SpectrumDrawView>(R.id.imgAnim)
                        imgAnim.setSpectrumColor(Color.RED)
                        setText(R.id.songName to data?.songName)
                        val isPlaying = StarrySky.with().isCurrMusicIsPlaying(data?.songId)
                        val isPause = StarrySky.with().isCurrMusicIsPaused(data?.songId)
                        when {
                            isPlaying -> {
                                imgAnim.startAnim()
                                imgAnim.visibility = View.VISIBLE
                            }
                            isPause -> {
                                imgAnim.stopAnim()
                                imgAnim.visibility = View.VISIBLE
                            }
                            else -> {
                                imgAnim.visibility = View.GONE
                            }
                        }
                        btnClose.setOnClickListener {
                            songList.remove(data)
                            recycleView.removedData(position)
                            StarrySky.with().removeSongInfo(data?.songId)
                        }
                        itemClicked {
                            StarrySky.with().playMusicByInfo(data)
                        }
                    }
                }
            }
        }
    }

    private fun initDetailUI(currSong: SongInfo) {
        songCover.loadImage(currSong.songCover)
        songName.text = currSong.songName
        songDesc.text = currSong.artist
    }

    private fun notifyDialogItem() {
        val customView = dialog?.getCustomView()
        val recycleView: RecyclerView? = customView?.findViewById(R.id.recycleView)
        recycleView?.notifyDataSetChanged<SongInfo>()
    }
}
