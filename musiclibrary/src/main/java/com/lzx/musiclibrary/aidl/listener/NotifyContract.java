package com.lzx.musiclibrary.aidl.listener;

import com.lzx.musiclibrary.aidl.model.SongInfo;

/**
 * lzx
 * 2018/2/3
 */

public interface NotifyContract {
    /**
     * 统一通知播放状态改变
     */
    interface NotifyStatusChanged {
        void notify(SongInfo info, int index, int status, String errorMsg);
    }

    /**
     * 切歌
     */
    interface NotifyMusicSwitch {
        void notify(SongInfo info);
    }

    interface NotifyTimerTask {
        void notifyTimerTasFinish();

        void onTimerTick(long millisUntilFinished, long totalTime);
    }
}
