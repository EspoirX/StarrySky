package com.lzx.musiclib.tab

import android.Manifest
import android.util.Log
import android.view.View
import com.lzx.musiclib.R
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.showToast
import com.lzx.musiclib.toSdcardPath
import com.lzx.record.StarrySkyRecord
import com.lzx.record.recorder.RecorderCallback
import com.qw.soul.permission.SoulPermission
import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import kotlinx.android.synthetic.main.fragment_recorder.btnPlay
import kotlinx.android.synthetic.main.fragment_recorder.btnStart
import kotlinx.android.synthetic.main.fragment_recorder.btnStop
import java.io.File

class RecordFragment : BaseFragment() {

    companion object {
        fun newInstance(): RecordFragment {
            return RecordFragment()
        }
    }

    override fun getResourceId(): Int = R.layout.fragment_recorder

    override fun initView(view: View?) {
        btnStart?.setOnClickListener {
            SoulPermission.getInstance().checkAndRequestPermissions(Permissions.build(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object : CheckRequestPermissionsListener {
                    override fun onAllPermissionOk(allPermissions: Array<Permission>) {
                        val path = "StarrySkyRecord".toSdcardPath()
                        val file = "000remix.mp3".toSdcardPath()
                        StarrySkyRecord.with()
                            .setBgMusicUrl(file)
                            .setOutputFile(path)
                            .setRecordCallback(object : RecorderCallback {
                                override fun onStart() {
                                    activity?.showToast("onStart")
                                }

                                override fun onResume() {
                                    activity?.showToast("onResume")
                                }

                                override fun onReset() {
                                    activity?.showToast("onReset")
                                }

                                override fun onRecording(time: Long, volume: Int) {
                                    Log.i("XIAN", "onRecording time = $time volume = $volume")
                                }

                                override fun onPause() {
                                    activity?.showToast("onPause")
                                }

                                override fun onRemind(duration: Long) {
                                    activity?.showToast("onRemind")
                                }

                                override fun onSuccess(file: File?, time: Long) {
                                    activity?.showToast("onSuccess")
                                }

                                override fun onError(msg: String) {
                                    activity?.showToast(msg)
                                }

                                override fun onAutoComplete(file: String, time: Long) {
                                    activity?.showToast("onAutoComplete")
                                }
                            })
                            .setOutputFileName("12323.mp3")
                            .startRecord()
                    }

                    override fun onPermissionDenied(refusedPermissions: Array<Permission>) {
                    }
                })
        }

        btnStop?.setOnClickListener {
            StarrySkyRecord.recorder?.stopRecording()
        }

        btnPlay?.setOnClickListener {
            val file = "222.mp4".toSdcardPath()
            Log.i("XIAN", "file = " + file)
            StarrySkyRecord.with().setBgMusicUrl(file).player()?.playMusic()
        }


    }

    override fun unInitView() {
    }
}