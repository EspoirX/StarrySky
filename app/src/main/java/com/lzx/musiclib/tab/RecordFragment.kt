package com.lzx.musiclib.tab

import android.Manifest
import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.lzx.basecode.AudioDecoder
import com.lzx.basecode.FocusInfo
import com.lzx.basecode.Playback
import com.lzx.basecode.SongInfo
import com.lzx.basecode.orDef
import com.lzx.basecode.showToast
import com.lzx.basecode.toSdcardPath
import com.lzx.musiclib.R
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.dp
import com.lzx.musiclib.viewmodel.MusicViewModel
import com.lzx.musiclib.weight.dialog.CommonBehavior
import com.lzx.musiclib.weight.dialog.createMaterialDialog
import com.lzx.musiclib.weight.dialog.getCustomView
import com.lzx.musiclib.weight.dialog.lifecycleOwner
import com.lzx.record.StarrySkyRecord
import com.lzx.record.recorder.RecordState
import com.lzx.record.recorder.SimpleRecorderCallback
import com.lzx.starrysky.playback.changePlaybackState
import com.qw.soul.permission.SoulPermission
import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import kotlinx.android.synthetic.main.fragment_recorder.btnAccVolume
import kotlinx.android.synthetic.main.fragment_recorder.btnAccompaniment
import kotlinx.android.synthetic.main.fragment_recorder.btnFinish
import kotlinx.android.synthetic.main.fragment_recorder.btnRecord
import kotlinx.android.synthetic.main.fragment_recorder.geci
import java.io.File

class RecordFragment : BaseFragment() {

    companion object {
        fun newInstance(): RecordFragment {
            return RecordFragment()
        }
    }

    private var viewModel: MusicViewModel? = null

    override fun getResourceId(): Int = R.layout.fragment_recorder

    override fun initView(view: View?) {
        val text = geci?.text.toString().replace("\\n", "\n")
        geci?.text = text

        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        viewModel?.downloadLiveData?.observe(this, {
            activity?.showToast("音频处理成功")
            setUpBtnEnabled(true)
            StarrySkyRecord.with().playBgMusic(it)
        })

        btnRecord?.setOnClickListener {
            SoulPermission.getInstance().checkAndRequestPermissions(Permissions.build(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object : CheckRequestPermissionsListener {
                    override fun onAllPermissionOk(allPermissions: Array<Permission>) {
                        when {
                            StarrySkyRecord.recorder?.getRecordState() == RecordState.STOPPED -> {
                                val path = "StarrySkyRecord".toSdcardPath()
                                StarrySkyRecord.with()
                                    .setRecordOutputFile(path)
                                    .setRecordCallback(object : SimpleRecorderCallback() {
                                        override fun onStart() {
                                            super.onStart()
                                            btnRecord?.setImageResource(R.drawable.b0c)
                                        }

                                        override fun onPause() {
                                            super.onPause()
                                            btnRecord?.setImageResource(R.drawable.afb)
                                        }

                                        override fun onSuccess(file: File?, time: Long) {
                                            super.onSuccess(file, time)
                                            btnRecord?.setImageResource(R.drawable.afb)
                                        }
                                    })
                                    .setRecordOutputFileName("录音.mp3")
                                    .startRecord()
                            }
                            StarrySkyRecord.recorder?.getRecordState() == RecordState.RECORDING -> {
                                StarrySkyRecord.recorder?.pauseRecording()
                            }
                            else -> {
                                StarrySkyRecord.recorder?.resumeRecording()
                            }
                        }
                    }

                    override fun onPermissionDenied(refusedPermissions: Array<Permission>) {
                    }
                })
        }
        btnFinish?.setOnClickListener {
            StarrySkyRecord.with().getBgPlayer()?.pause()
            StarrySkyRecord.recorder?.stopRecording()
        }
        btnAccVolume?.setOnClickListener { showControlDialog() }
        //伴奏
        btnAccompaniment?.setOnClickListener {
            val url = "https://github.com/EspoirX/lzxTreasureBox/raw/master/周杰伦-告白气球.mp3"
            StarrySkyRecord.with()
                .isNeedDownloadBgMusic(true)
                .setBgMusicFileName("周杰伦-告白气球.mp3")
                .playBgMusic(url)
        }

        StarrySkyRecord.with().getBgPlayer()?.setCallback(object : Playback.Callback {
            override fun onPlayerStateChanged(songInfo: SongInfo?, playWhenReady: Boolean, playbackState: Int) {
                activity?.showToast("播放：" + playbackState.changePlaybackState())
            }

            override fun onPlaybackError(songInfo: SongInfo?, error: String) {
                activity?.showToast("播放失败：$error")
            }

            override fun onFocusStateChange(info: FocusInfo) {
            }
        })
    }

    private fun setUpBtnEnabled(isEnabled: Boolean) {
        btnAccVolume?.isEnabled = isEnabled
        btnRecord?.isEnabled = isEnabled
        btnAccompaniment?.isEnabled = isEnabled
        btnFinish?.isEnabled = isEnabled
    }


    @SuppressLint("SetTextI18n")
    private fun showControlDialog() {
        activity?.createMaterialDialog(CommonBehavior(R.style.dialog_base_style,
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
                seekBar.max = 100
                title.text = "音量调节"

                val volume = StarrySkyRecord.with().getBgPlayer()?.getVolume().orDef()
                seekBar.progress = (volume * 100f).toInt()
                desc.text = "当前音量：" + seekBar.progress + " %"
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        StarrySkyRecord.with().getBgPlayer()?.setVolume(progress.toFloat() / 100f)
                        desc.text = "当前音量：$progress %"
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
            lifecycleOwner(this@RecordFragment)
        }
    }

    override fun unInitView() {
    }
}