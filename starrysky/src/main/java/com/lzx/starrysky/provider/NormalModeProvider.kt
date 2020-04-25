package com.lzx.starrysky.provider

class NormalModeProvider : BaseMediaSourceProvider() {

    override fun getSongList(): MutableList<SongInfo> {
        val list = mutableListOf<SongInfo>()
        songSources.forEach {
            list.add(it.value)
        }
        return list
    }

}