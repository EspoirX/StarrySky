package com.lzx.musiclibrary.queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.bus.Bus;
import com.lzx.musiclibrary.bus.tags.BusTags;
import com.lzx.musiclibrary.bus.tags.QueueIndexUpdated;
import com.lzx.musiclibrary.constans.PlayMode;
import com.lzx.musiclibrary.helper.QueueHelper;
import com.lzx.musiclibrary.utils.AlbumArtCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 播放列表管理类
 * Playlist management class
 * create by lzx
 * time:2018/10/30
 */
public class PlayQueueManager implements IPlayQueue {

    private ArrayList<SongInfo> normalPlayList = new ArrayList<>();
    private ArrayList<SongInfo> randomPlayList = new ArrayList<>();
    private int mCurrentIndex;
    private Context mContext;

    public PlayQueueManager(Context context) {
        mContext = context;
    }

    public int getPlayMode() {
        return PlayMode.getInstance().getCurrPlayMode(mContext);
    }

    private boolean isRandomMode() {
        return getPlayMode() == PlayMode.PLAY_IN_RANDOM;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void setSongInfos(List<SongInfo> songInfos, int currentIndex) {
        int index = 0;
        if (currentIndex != -1) {
            index = currentIndex;
        }
        mCurrentIndex = Math.max(index, 0);
        normalPlayList.clear();
        randomPlayList.clear();
        normalPlayList.addAll(songInfos);
        randomPlayList = (ArrayList<SongInfo>) normalPlayList.clone();
        Collections.shuffle(randomPlayList);
        //通知播放列表更新了
        List<MediaSessionCompat.QueueItem> queueItems = QueueHelper.getQueueItems(getSongInfos());
        Bus.getInstance().post(queueItems, BusTags.onQueueUpdated);
    }

    @Override
    public void setSongInfos(List<SongInfo> songInfos) {
        setSongInfos(songInfos, -1);
    }

    @Override
    public List<SongInfo> getSongInfos() {
        if (isRandomMode()) {
            return randomPlayList;
        } else {
            return normalPlayList;
        }
    }

    @Override
    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    @Override
    public void addSongInfo(SongInfo songInfo) {
        if (songInfo == null) {
            return;
        }
        if (normalPlayList.contains(songInfo)) {
            return;
        }
        normalPlayList.add(songInfo);
        randomPlayList = (ArrayList<SongInfo>) normalPlayList.clone();
        Collections.shuffle(randomPlayList);
        //通知播放列表更新了
        List<MediaSessionCompat.QueueItem> queueItems = QueueHelper.getQueueItems(getSongInfos());
        Bus.getInstance().post(queueItems, BusTags.onQueueUpdated);
    }

    @Override
    public void deleteSongInfo(SongInfo songInfo, boolean isNeedToPlayNext) {
        if (songInfo == null) {
            return;
        }
        if (normalPlayList.size() == 0 || !normalPlayList.contains(songInfo)) {
            return;
        }
        normalPlayList.remove(songInfo);
        randomPlayList = (ArrayList<SongInfo>) normalPlayList.clone();
        Collections.shuffle(randomPlayList);
        //通知播放列表更新了
        List<MediaSessionCompat.QueueItem> queueItems = QueueHelper.getQueueItems(getSongInfos());
        Bus.getInstance().post(queueItems, BusTags.onQueueUpdated);
    }

    /**
     * 设置当前的音乐item，用于播放
     */
    public void setCurrentQueueItem(String musicId, boolean isJustPlay, boolean isSwitchMusic) {
        int index = QueueHelper.getMusicIndexOnQueue(getSongInfos(), musicId);
        setCurrentQueueIndex(index, isJustPlay, isSwitchMusic);
    }

    /**
     * 设置当前的音乐item，用于播放
     */
    private void setCurrentQueueIndex(int index, boolean isJustPlay, boolean isSwitchMusic) {
        if (index >= 0 && index < getSongInfos().size()) {
            mCurrentIndex = index;
            QueueIndexUpdated updated = new QueueIndexUpdated(mCurrentIndex, isJustPlay, isSwitchMusic);
            Bus.getInstance().post(updated, BusTags.onCurrentQueueIndexUpdated);
        }
    }

    @Override
    public int getSongInfosSize() {
        return normalPlayList.size();
    }

    @Override
    public SongInfo getCurrentSongInfo() {
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, normalPlayList) || !QueueHelper.isIndexPlayable(mCurrentIndex, randomPlayList)) {
            return null;
        }
        if (isRandomMode()) {
            return randomPlayList.get(mCurrentIndex);
        } else {
            return normalPlayList.get(mCurrentIndex);
        }
    }

    @Override
    public void setCurrentSong(int currentIndex) {
        if (normalPlayList.size() == 0) {
            return;
        }
        if (!QueueHelper.isIndexPlayable(currentIndex, normalPlayList)) {
            return;
        }
        this.mCurrentIndex = currentIndex;
    }

    @Override
    public void setCurrentSong(SongInfo songInfo) {
        if (normalPlayList.size() == 0 || songInfo == null) {
            return;
        }
        int index = QueueHelper.getMusicIndexOnQueue(getSongInfos(), songInfo.getSongId());
        if (index != -1) {
            mCurrentIndex = index;
        }
    }

    @Override
    public void updateSongCoverBitmap(String musicId, Bitmap bitmap) {
        SongInfo musicInfo = QueueHelper.getMusicInfoById(getSongInfos(), musicId);
        if (musicInfo == null) {
            return;
        }
        musicInfo.setSongCoverBitmap(bitmap);
        int index = getSongInfos().indexOf(musicInfo);
        if (isRandomMode()) {
            randomPlayList.set(index, musicInfo);
        } else {
            normalPlayList.set(index, musicInfo);
        }
    }

    @Override
    public SongInfo getPreMusicInfo(boolean isUpdateIndex) {
        return getNextOrPreMusicInfo(-1, isUpdateIndex);
    }

    @Override
    public SongInfo getNextMusicInfo(boolean isUpdateIndex) {
        return getNextOrPreMusicInfo(1, isUpdateIndex);
    }

    @Override
    public boolean hasNextSong() {
        if (getPlayMode() == PlayMode.PLAY_IN_ORDER) {
            return mCurrentIndex != getSongInfosSize() - 1;
        } else {
            return getSongInfosSize() > 1;
        }
    }

    @Override
    public boolean hasPreSong() {
        if (getPlayMode() == PlayMode.PLAY_IN_ORDER) {
            return mCurrentIndex != 0;
        } else {
            return getSongInfosSize() > 1;
        }
    }

    /**
     * 上一首或下一首
     */
    private SongInfo getNextOrPreMusicInfo(int amount, boolean isUpdateIndex) {
        SongInfo info = null;
        int playMode = getPlayMode();
        if (playMode == PlayMode.PLAY_IN_SINGLE_LOOP) {
            info = getCurrentSongInfo();
        } else if (playMode == PlayMode.PLAY_IN_RANDOM || playMode == PlayMode.PLAY_IN_FLASHBACK || playMode == PlayMode.PLAY_IN_LIST_LOOP) {
            if (skipQueuePosition(amount, isUpdateIndex)) {
                info = getCurrentSongInfo();
            }
        } else if (playMode == PlayMode.PLAY_IN_ORDER) {
            if (hasNextSong() && skipQueuePosition(amount, isUpdateIndex) || hasPreSong() && skipQueuePosition(amount, isUpdateIndex)) {
                info = getCurrentSongInfo();
            }
        }
        return info;
    }

    @Override
    public void updateMetadata() {
        SongInfo currentMusic = getCurrentSongInfo();
        if (currentMusic == null) {
            Bus.getInstance().post("", BusTags.onMetadataRetrieveError);
            return;
        }
        final String musicId = currentMusic.getSongId();
        SongInfo metadata = QueueHelper.getMusicInfoById(getSongInfos(), musicId);
        if (metadata == null) {
            throw new IllegalArgumentException("Invalid musicId " + musicId);
        }
        if (!TextUtils.isEmpty(metadata.getSongCover())) {
            String coverUri = metadata.getSongCover();
            //获取图片bitmap
            AlbumArtCache.getInstance().fetch(coverUri, new AlbumArtCache.FetchListener() {
                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    updateSongCoverBitmap(musicId, bitmap);
                    SongInfo currentMusic = getCurrentSongInfo();
                    if (currentMusic == null) {
                        return;
                    }
                    String currentPlayingId = currentMusic.getSongId();
                    if (musicId.equals(currentPlayingId)) {
                        Bus.getInstance().post(QueueHelper.getMusicInfoById(getSongInfos(), currentPlayingId), BusTags.onMetadataChanged);
                    }
                }
            });
        }
    }

    /**
     * 转跳到指定位置
     *
     * @param amount 维度
     * @return boolean
     */
    public boolean skipQueuePosition(int amount, boolean isUpdateIndex) {
        if (getSongInfos().size() == 0) {
            return false;
        } else {
            int index = mCurrentIndex + amount;
            if (index < 0) {
                // 在第一首歌曲是上一首，让你在第一首歌曲上
                int playModel = PlayMode.getInstance().getCurrPlayMode(mContext);
                if (playModel == PlayMode.PLAY_IN_FLASHBACK || playModel == PlayMode.PLAY_IN_LIST_LOOP) { //如果是倒序或者列表循环，则回去最后一首
                    index = getSongInfos().size() - 1;
                } else {
                    index = 0;
                }
            } else {
                //当在最后一首歌时点下一首将返回第一首个
                index %= getSongInfos().size();
            }
            if (!QueueHelper.isIndexPlayable(index, getSongInfos())) {
                return false;
            }
            if (isUpdateIndex) {
                mCurrentIndex = index;
            }
            return true;
        }
    }

    /**
     * 检查播放模式改变后下标是否正确
     */
    public void checkIndexForPlayMode(String mCurrentMediaId) {
        int currIndex = QueueHelper.getMusicIndexOnQueue(getSongInfos(), mCurrentMediaId);
        if (mCurrentIndex != currIndex) {
            mCurrentIndex = currIndex;
        }
    }
}
