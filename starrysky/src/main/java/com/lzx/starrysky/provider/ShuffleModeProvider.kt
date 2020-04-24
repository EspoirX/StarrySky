package com.lzx.starrysky.provider

class ShuffleModeProvider : BaseMediaSourceProvider() {


    override fun getSongListImpl(): MutableList<SongInfo> {
        val copyList = mutableListOf<SongInfo>()
        copyList.addAll(songList)
        copyList.shuffle()   //打乱顺序
        return copyList
    }
}