package com.lzx.starrysky.utils.delayaction;

import com.lzx.starrysky.model.SongInfo;

public interface Valid {

    /**
     * 是否满足检验器的要求，如果不满足的话，则执行doValid()方法。如果满足，则执行目标action.call
     */
    boolean preCheck();

    /**
     * 去执行验证前置行为，例如跳转到登录界面。
     * 但并未完成验证。所以需要在登陆成功时调用preCheck()再次检查）
     */
    void doValid(SongInfo songInfo);
}
