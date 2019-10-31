package com.lzx.starrysky.common

import android.text.TextUtils
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.provider.SongInfo
import java.util.HashMap

class PlaybackStage constructor(private val stage: String?, private val songInfo: SongInfo?) {

    companion object {
        const val NONE = "NONE"
        const val START = "START"
        const val PAUSE = "PAUSE"
        const val STOP = "STOP"
        const val SWITCH = "SWITCH"
        const val COMPLETION = "COMPLETION"
        const val BUFFERING = "BUFFERING"
        const val ERROR = "ERROR"

        internal fun buildNone(): PlaybackStage {
            return Builder()
                .setState(NONE)
                .setSongId("")
                .build()
        }
    }

    private val cacheMap = HashMap<Key, PlaybackStage>()
    private var errorCode = -1
    private var errorMessage = ""
    private val stateKey: StateKey

    init {
        stateKey = StateKey()
    }

    fun getStage(): String? {
        return stage
    }

    fun getSongInfo(): SongInfo? {
        return songInfo
    }

    fun getErrorCode(): Int {
        return errorCode
    }

    fun getErrorMessage(): String {
        return errorMessage
    }

    private fun setErrorCode(errorCode: Int) {
        this.errorCode = errorCode
    }

    private fun setErrorMessage(errorMessage: String) {
        this.errorMessage = errorMessage
    }

    class Builder {
        private var stage: String? = null
        private var songInfo: SongInfo? = null
        private var errorCode = -1
        private var errorMessage = ""

        internal fun setState(stage: String?): Builder {
            this.stage = stage
            return this
        }

        internal fun setErrorCode(errorCode: Int): Builder {
            this.errorCode = errorCode
            return this
        }

        internal fun setErrorMsg(errorMessage: String): Builder {
            this.errorMessage = errorMessage
            return this
        }

        internal fun setSongId(songId: String?): Builder {
            if (!songId.isNullOrEmpty()) {
                this.songInfo = StarrySky.get().mediaQueueProvider.getSongInfo(songId)
            }
            return this
        }

        internal fun build(): PlaybackStage {
            val playbackStage = PlaybackStage(stage, songInfo)
            playbackStage.setErrorCode(errorCode)
            playbackStage.setErrorMessage(errorMessage)
            return playbackStage
        }
    }

    internal fun buildStart(songId: String): PlaybackStage? {
        check(!TextUtils.isEmpty(songId)) { "songId is null" }
        val key = stateKey[START, songId]
        var stage: PlaybackStage? = cacheMap[key]
        if (stage == null) {
            stage = Builder()
                .setState(START)
                .setSongId(songId)
                .build()
            cacheMap[key] = stage
        } else {
            key.offer()
        }
        return stage
    }

    internal fun buildPause(songId: String): PlaybackStage? {
        check(!TextUtils.isEmpty(songId)) { "songId is null" }
        val key = stateKey[PAUSE, songId]
        var stage: PlaybackStage? = cacheMap[key]
        if (stage == null) {
            stage = Builder()
                .setState(PAUSE)
                .setSongId(songId)
                .build()
        } else {
            key.offer()
        }
        return stage
    }

    internal fun buildStop(songId: String): PlaybackStage? {

        check(!TextUtils.isEmpty(songId)) { "songId is null" }
        val key = stateKey[STOP, songId]
        var stage: PlaybackStage? = cacheMap[key]
        if (stage == null) {
            stage = Builder()
                .setState(STOP)
                .setSongId(songId)
                .build()
        } else {
            key.offer()
        }
        return stage
    }

    internal fun buildCompletion(songId: String): PlaybackStage? {

        check(!TextUtils.isEmpty(songId)) { "songId is null" }
        val key = stateKey[COMPLETION, songId]
        var stage: PlaybackStage? = cacheMap[key]
        if (stage == null) {
            stage = Builder()
                .setState(COMPLETION)
                .setSongId(songId)
                .build()
        } else {
            key.offer()
        }
        return stage
    }

    internal fun buildBuffering(songId: String): PlaybackStage? {
        val key = stateKey[BUFFERING, songId]
        var stage: PlaybackStage? = cacheMap[key]
        if (stage == null) {
            stage = Builder()
                .setState(BUFFERING)
                .setSongId(songId)
                .build()
        } else {
            key.offer()
        }
        return stage
    }

    internal fun buildError(songId: String, errorCode: Int, errorMsg: String): PlaybackStage? {
        check(!TextUtils.isEmpty(songId)) { "songId is null" }
        val key = stateKey[ERROR, songId, errorCode, errorMessage]
        var stage: PlaybackStage? = cacheMap[key]
        if (stage == null) {
            stage = Builder()
                .setState(ERROR)
                .setSongId(songId)
                .setErrorCode(errorCode)
                .setErrorMsg(errorMsg)
                .build()
        } else {
            key.offer()
        }
        return stage
    }

    internal fun buildSwitch(songId: String): PlaybackStage? {
        check(!TextUtils.isEmpty(songId)) { "songId is null" }
        val key = stateKey[SWITCH, songId]
        var stage: PlaybackStage? = cacheMap[key]
        if (stage == null) {
            stage = Builder()
                .setState(SWITCH)
                .setSongId(songId)
                .build()
        } else {
            key.offer()
        }
        return stage
    }

    private class StateKey : BaseKey<Key>() {

        internal operator fun get(
            state: String, songId: String, errorCode: Int = 0, errorMsg: String = ""
        ): Key {
            val result = super.get()
            result!!.init(state, songId, errorCode, errorMsg)
            return result
        }

        override fun create(): Key {
            return Key(this)
        }
    }

    private class Key internal constructor(private val pool: StateKey) {

        internal var state = NONE
        internal var songId = ""
        internal var errorCode = 0
        internal var errorMsg = ""

        internal fun init(state: String, songId: String, errorCode: Int, errorMsg: String) {
            this.state = state
            this.songId = songId
            this.errorCode = errorCode
            this.errorMsg = errorMsg
        }

        internal fun offer() {
            pool.offer(this)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Key

            if (pool != other.pool) return false
            if (state != other.state) return false
            if (songId != other.songId) return false
            if (errorCode != other.errorCode) return false
            if (errorMsg != other.errorMsg) return false

            return true
        }

        override fun hashCode(): Int {
            var result = pool.hashCode()
            result = 31 * result + state.hashCode()
            result = 31 * result + songId.hashCode()
            result = 31 * result + errorCode
            result = 31 * result + errorMsg.hashCode()
            return result
        }
    }
}