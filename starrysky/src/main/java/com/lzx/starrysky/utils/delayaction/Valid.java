package com.lzx.starrysky.utils.delayaction;

import com.lzx.starrysky.provider.SongInfo;

public interface Valid {

    /**
     * 去执行验证前置行为，例如跳转到登录界面。
     * 但并未完成验证。所以需要在登陆成功时调用preCheck()再次检查）
     */
    void doValid(SongInfo songInfo, ValidCallback callback);

    interface ValidCallback {
        void finishValid();  //完成验证时回调

        void doActionDirect(); //执行执行验证后的 action
    }
}
