package com.lzx.starrysky.delayaction

import com.lzx.starrysky.provider.SongInfo

interface Valid {

    /**
     * 去执行验证前置行为，例如跳转到登录界面。
     * 但并未完成验证。所以需要在登陆成功时调用preCheck()再次检查）
     */
    fun doValid(songInfo: SongInfo?, callback: ValidCallback)

    interface ValidCallback {
        fun finishValid()   //完成验证时回调

        fun doActionDirect()  //执行执行验证后的 action
    }
}
