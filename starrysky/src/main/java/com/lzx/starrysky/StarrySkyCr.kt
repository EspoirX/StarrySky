package com.lzx.starrysky

import com.lzx.starrysky.service.ServiceBinder


/**
 * 支持多个实例的单例模式
 *
 */
class StarrySkyCr {

    companion object {
        val CLIENT_COUNT = 10 //默认10个，最多32个

        private val sInstances = arrayOfNulls<StarrySkyCr>(CLIENT_COUNT)

        @Volatile
        private var sInstanceMask = 0

        fun getInstance(client: Int): StarrySkyCr? {
            val mask = 1 shl client
            if (sInstanceMask and mask == 0) {
                synchronized(StarrySkyCr::class.java) {
                    if (sInstanceMask and mask == 0) {
                        sInstances[client] = StarrySkyCr()
                        sInstanceMask = sInstanceMask or mask
                    }
                }
            }
            return sInstances[client]
        }
    }

    private var binder: ServiceBinder? = null

    init {

    }

}