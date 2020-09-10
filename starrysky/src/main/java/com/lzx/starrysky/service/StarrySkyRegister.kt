package com.lzx.starrysky.service

import com.lzx.starrysky.cache.ICache
import com.lzx.starrysky.imageloader.ImageLoaderStrategy
import com.lzx.starrysky.playback.Playback

class StarrySkyRegister {
    var playback: Playback? = null
    var imageLoader: ImageLoaderStrategy? = null
    var cache: ICache? = null
}