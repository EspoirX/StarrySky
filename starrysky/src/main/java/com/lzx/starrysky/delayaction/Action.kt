package com.lzx.starrysky.delayaction

import com.lzx.starrysky.provider.SongInfo

interface Action {
    fun call(songInfo: SongInfo?)
}
