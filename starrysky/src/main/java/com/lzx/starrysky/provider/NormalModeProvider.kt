package com.lzx.starrysky.provider

class NormalModeProvider : BaseMediaSourceProvider() {

    override fun getSongListImpl(): MutableList<SongInfo> {
        return songList
    }
}