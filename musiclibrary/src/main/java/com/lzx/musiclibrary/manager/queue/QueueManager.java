package com.lzx.musiclibrary.manager.queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.PlayMode;
import com.lzx.musiclibrary.helper.QueueHelper;
import com.lzx.musiclibrary.utils.AlbumArtCache;
import com.lzx.musiclibrary.utils.SPUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by xian on 2018/1/20.
 */

public class QueueManager implements SongQueue {

    private SongQueue mSongQueue;
    private Context mContext;
    private SongQueue.MetadataUpdateListener mListener;
    private PlayMode mPlayMode;

    public QueueManager(Context context, SongQueue.MetadataUpdateListener listener, PlayMode playMode) {
        this.mContext = context;
        this.mListener = listener;
        this.mPlayMode = playMode;
        mSongQueue = new NormalSongQueue(context, listener, playMode);
    }

    /**
     * 切换播放列表
     * @param isAudition 是否试听
     */
    public void switchSongQueue(boolean isAudition) {
        if (isAudition) {
            mSongQueue = new AuditionSongQueue(mContext, mListener, mPlayMode);
        } else {
            mSongQueue = new NormalSongQueue(mContext, mListener, mPlayMode);
        }
    }

    @Override
    public Context getContext() {
        return mSongQueue.getContext();
    }

    @Override
    public void updatePlayModel(PlayMode playModel) {
        mSongQueue.updatePlayModel(playModel);
    }

    @Override
    public void setUpRandomQueue() {
        mSongQueue.setUpRandomQueue();
    }

    @Override
    public void setListener(MetadataUpdateListener listener) {
        mSongQueue.setListener(listener);
    }

    /**
     * 获取播放列表
     *
     * @return list
     */
    @Override
    public List<SongInfo> getPlayingQueue() {
        return mSongQueue.getPlayingQueue();
    }

    /**
     * 获取当前索引
     *
     * @return index
     */
    @Override
    public int getCurrentIndex() {
        return mSongQueue.getCurrentIndex();
    }

    /**
     * 设置当前的播放列表
     *
     * @param newQueue     整个队列
     * @param currentIndex 当前第几首
     */
    @Override
    public void setCurrentQueue(List<SongInfo> newQueue, int currentIndex) {
        mSongQueue.setCurrentQueue(newQueue, currentIndex);
    }

    /**
     * 设置当前的播放列表 默认第一首
     *
     * @param newQueue 整个队列
     */
    @Override
    public void setCurrentQueue(List<SongInfo> newQueue) {
        setCurrentQueue(newQueue, -1);
    }

    /**
     * 添加一个音乐信息到队列中
     *
     * @param info 音乐信息
     */
    @Override
    public void addQueueItem(SongInfo info) {
        mSongQueue.addQueueItem(info);
    }

    @Override
    public void deleteQueueItem(SongInfo info, boolean isNeedToPlayNext) {
        mSongQueue.deleteQueueItem(info, isNeedToPlayNext);
    }

    /**
     * 得到列表长度
     *
     * @return 队列长度
     */
    @Override
    public int getCurrentQueueSize() {
        return mSongQueue.getCurrentQueueSize();
    }

    /**
     * 得到当前播放的音乐信息
     *
     * @return 音乐信息
     */
    @Override
    public SongInfo getCurrentMusic() {
        return mSongQueue.getCurrentMusic();
    }

    @Override
    public void setCurrentMusic(int currentIndex) {
        mSongQueue.setCurrentMusic(currentIndex);
    }

    /**
     * 转跳到指定位置
     *
     * @param amount 维度
     * @return boolean
     */
    @Override
    public boolean skipQueuePosition(int amount) {
        return mSongQueue.skipQueuePosition(amount);
    }

    /**
     * 更新音乐艺术家信息
     *
     * @param musicId
     * @param bitmap
     */
    @Override
    public void updateSongCoverBitmap(String musicId, Bitmap bitmap) {
        mSongQueue.updateSongCoverBitmap(musicId, bitmap);
    }

    /**
     * 设置当前的音乐item，用于播放
     *
     * @param musicId       音乐id
     * @param isJustPlay
     * @param isSwitchMusic
     */
    @Override
    public void setCurrentQueueItem(String musicId, boolean isJustPlay, boolean isSwitchMusic) {
        mSongQueue.setCurrentQueueItem(musicId, isJustPlay, isSwitchMusic);
    }

    /**
     * 设置当前的音乐item，用于播放
     *
     * @param index         队列下标
     * @param isJustPlay
     * @param isSwitchMusic
     */
    @Override
    public void setCurrentQueueIndex(int index, boolean isJustPlay, boolean isSwitchMusic) {
        mSongQueue.setCurrentQueueIndex(index, isJustPlay, isSwitchMusic);
    }

    /**
     * 得到上一首音乐信息
     *
     * @return SongInfo
     */
    @Override
    public SongInfo getPreMusicInfo() {
        return getNextOrPreMusicInfo(-1);
    }

    /**
     * 得到下一首音乐信息
     *
     * @return SongInfo
     */
    @Override
    public SongInfo getNextMusicInfo() {
        return getNextOrPreMusicInfo(1);
    }

    @Override
    public SongInfo getNextOrPreMusicInfo(int amount) {
        return mSongQueue.getNextOrPreMusicInfo(amount);
    }

    /**
     * 更新媒体信息
     */
    @Override
    public void updateMetadata() {
        mSongQueue.updateMetadata();
    }
}
