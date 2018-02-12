// IOnPlayerEventListener.aidl
package com.lzx.musiclibrary.aidl.listener;

import com.lzx.musiclibrary.aidl.model.SongInfo;

interface IOnPlayerEventListener {

        /**
         * 切换歌曲
         */
        void onMusicSwitch(out SongInfo music);

        /**
         * 继续播放
         */
        void onPlayerStart();

        /**
         * 暂停播放
         */
        void onPlayerPause();

        void onBuffering(boolean isFinishBuffer);

        /**
         * 播放完成
         */
        void onPlayCompletion();


        void onError(String errorMsg);
}
