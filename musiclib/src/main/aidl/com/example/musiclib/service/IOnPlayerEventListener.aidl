// IOnPlayerEventListener.aidl
package com.example.musiclib.service;
import com.example.musiclib.model.MusicInfo;
// Declare any non-default types here with import statements

interface IOnPlayerEventListener {
     /**
         * 切换歌曲
         */
        void onMusicChange(inout MusicInfo music);

        /**
         * 继续播放
         */
        void onPlayerStart();

        /**
         * 暂停播放
         */
        void onPlayerPause();

        /**
         * 更新进度
         */
        void onProgress(int progress);

        /**
         * 缓冲百分比
         */
        void onBufferingUpdate(int percent);

        /**
         * 更新定时停止播放时间
         */
        void onTimer(long remain);
}
