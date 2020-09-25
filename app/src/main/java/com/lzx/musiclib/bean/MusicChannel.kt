package com.lzx.musiclib.bean

import com.lzx.starrysky.SongInfo

class MusicChannel {
    var contentId: String? = null
    var cover: String? = null
    var id: String? = null
    var rcmdtemplate: String? = null
    var title: String? = null
    var username: String? = null
    var songList = mutableListOf<SongInfo>()
}