package com.lzx.musiclib.bean

class MusicGroup {
    var groupId: Int = 0
    var groupName: String? = null
    var chls: MutableList<MusicChannel> = mutableListOf()
}

class MusicChannel {
    var bgColor: String? = null
    var intro: String? = null
    var name: String? = null
    var cover: String? = null
    var songNum: Int = 0
    var id: Int = 0
}

