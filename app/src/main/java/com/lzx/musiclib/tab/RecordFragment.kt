package com.lzx.musiclib.tab

import android.Manifest
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.lzx.musiclib.R
import com.lzx.musiclib.base.BaseFragment
import com.lzx.musiclib.toSdcardPath
import com.lzx.record.RecordConst
import com.lzx.record.StarrySkyRecord
import com.lzx.record.recorder.RecordState
import com.qw.soul.permission.SoulPermission
import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import kotlinx.android.synthetic.main.fragment_recorder.btnStart
import kotlinx.android.synthetic.main.fragment_recorder.btnStop

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
                        StarrySkyRecord.with().startRecord(path, "lzx", RecordConst.FORMAT_M4A)
                    }

                    override fun onPermissionDenied(refusedPermissions: Array<Permission>) {
                    }
                })
        }

        btnStop?.setOnClickListener {
            StarrySkyRecord.with().stopRecording(false)
        }

        StarrySkyRecord.with().recordState().observe(this, Observer {
            when (it.state) {
                RecordState.STATE_START -> {
                    Log.i("XIAN", "state = STATE_START")
                }
                RecordState.STATE_ERROR -> {
                    Log.i("XIAN", "state = STATE_ERROR msg = " + it.error?.message)
                }
                RecordState.STATE_IDEA -> {
                    Log.i("XIAN", "state = STATE_IDEA")
                }
                RecordState.STATE_PAUSE -> {
                    Log.i("XIAN", "state = STATE_PAUSE")
                }
                RecordState.STATE_PROGRESS -> {
//                    Log.i("XIAN", "state = STATE_PROGRESS pro = " + it.recordMills + " amplitude = " + it.amplitude)
                }
                RecordState.STATE_PROCESSING -> {
                    Log.i("XIAN", "state = STATE_PROCESSING")
                }
                RecordState.STATE_PROCESSING_FINISH -> {
                    Log.i("XIAN", "state = STATE_PROCESSING_FINISH")
                }
                RecordState.STATE_STOP -> {
                    Log.i("XIAN", "state = STATE_STOP info = " + it.recordInfo)
                }
            }
        })
    }

    override fun unInitView() {
    }
}