package com.lzx.starrysky.delayaction

import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.provider.SongInfo
import com.lzx.starrysky.utils.StarrySkyUtils

class PlayValidManager {

    private val mValidQueue = mutableListOf<Valid>()
    private var action: Action? = null
    private var validIndex = 0
    private var doCallAfterAction = true

    companion object {
        @Volatile
        private var instance: PlayValidManager? = null

        fun get(): PlayValidManager {
            return instance ?: synchronized(this) {
                instance ?: PlayValidManager().also { instance = it }
            }
        }
    }

    fun addValid(valid: Valid) {
        if (!mValidQueue.contains(valid)) {
            mValidQueue.add(valid)
        }
    }

    fun setAction(action: Action?): PlayValidManager {
        if (action != null) {
            this.action = action
        }
        return this
    }

    fun getValidQueue(): List<Valid> {
        return mValidQueue
    }

    fun doCall(songInfo: SongInfo?) {
        StarrySkyUtils.log("doCall#validIndex = $validIndex")
        //执行验证
        if (validIndex < mValidQueue.size) {
            val valid = mValidQueue[0]
            valid.doValid(songInfo, object : Valid.ValidCallback {
                override fun finishValid() {
                    validIndex++
                    StarrySkyUtils.log("doCall#  validIndex++ ")
                    doCall(songInfo)
                }

                override fun doActionDirect() {
                    StarrySkyUtils.log("直接执行 Action")
                    doAction(songInfo) //直接执行
                }
            })
        } else {
            //执行action
            doAction(songInfo, true)
        }
    }

    fun doAction(songInfo: SongInfo?) {
        doAction(songInfo, false)
    }

    private fun doAction(songInfo: SongInfo?, doCallAfterAction: Boolean) {
        if (action != null) {
            this.doCallAfterAction = doCallAfterAction
            action?.call(songInfo)
            validIndex = 0
            StarrySkyUtils.log("doAction#validIndex = $validIndex")
        }
    }

    fun doCall(mediaId: String?) {
        val songInfo = mediaId?.let { StarrySky.get().mediaQueueProvider.getSongInfo(it) }
        doCall(songInfo)
    }

    fun resetValidIndex() {
        validIndex = 0
    }

    fun clear() {
        validIndex = 0
        mValidQueue.clear()
    }
}