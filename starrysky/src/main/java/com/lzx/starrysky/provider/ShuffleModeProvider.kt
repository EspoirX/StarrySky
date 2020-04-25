package com.lzx.starrysky.provider

class ShuffleModeProvider : BaseMediaSourceProvider() {

    override fun getSongList(): MutableList<SongInfo> {
        val list = mutableListOf<SongInfo>()
        songSources.forEach {
            list.add(it.value)
        }
        list.shuffle()   //打乱顺序
        return list
    }
}