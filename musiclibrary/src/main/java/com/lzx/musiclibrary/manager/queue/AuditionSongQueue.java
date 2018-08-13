package com.lzx.musiclibrary.manager.queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.PlayMode;
import com.lzx.musiclibrary.helper.QueueHelper;
import com.lzx.musiclibrary.utils.AlbumArtCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class AuditionSongQueue implements SongQueue {

    private List<SongInfo> mAuditionQueue; //试听列表

    private int mCurrentIndex;
    private SongQueue.MetadataUpdateListener mListener;
    private PlayMode mPlayMode;
    private List<SongInfo> mNormalOrderQueue;
    private boolean isPlayRandomModel = false;
    private Context mContext;

    public AuditionSongQueue(Context context, SongQueue.MetadataUpdateListener listener, PlayMode playMode) {
        mAuditionQueue = Collections.synchronizedList(new ArrayList<SongInfo>());
        mNormalOrderQueue = Collections.synchronizedList(new ArrayList<SongInfo>());
        mCurrentIndex = 0;
        mListener = listener;
        this.mPlayMode = playMode;
        mContext = context;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void updatePlayModel(PlayMode playModel) {
        this.mPlayMode = playModel;
        setUpRandomQueue();
    }

    @Override
    public void setUpRandomQueue() {
        isPlayRandomModel = mPlayMode.getCurrPlayMode(mContext) == PlayMode.PLAY_IN_RANDOM;
        if (isPlayRandomModel) {
            mNormalOrderQueue.clear();
            mNormalOrderQueue.addAll(mAuditionQueue);
            Collections.shuffle(mAuditionQueue); //洗牌算法打乱顺序
        } else {
            if (mNormalOrderQueue.size() != 0) {
                SongInfo songInfo = getCurrentMusic();
                mAuditionQueue.clear();
                mAuditionQueue.addAll(mNormalOrderQueue);
                mCurrentIndex = QueueHelper.getMusicIndexOnQueue(mAuditionQueue, songInfo.getSongId());
                mNormalOrderQueue.clear();
            }
        }
    }

    @Override
    public void setListener(SongQueue.MetadataUpdateListener listener) {
        mListener = listener;
    }

    @Override
    public List<SongInfo> getPlayingQueue() {
        return isPlayRandomModel ? mNormalOrderQueue : mAuditionQueue;
    }

    @Override
    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    @Override
    public void setCurrentQueue(List<SongInfo> newQueue, int currentIndex) {
        int index = 0;
        if (currentIndex != -1) {
            index = currentIndex;
        }
        mCurrentIndex = Math.max(index, 0);
        mAuditionQueue.clear();
        mAuditionQueue.addAll(newQueue);

        setUpRandomQueue();

        //通知播放列表更新了
        List<MediaSessionCompat.QueueItem> queueItems = QueueHelper.getQueueItems(mAuditionQueue);
        if (mListener != null) {
            mListener.onQueueUpdated(queueItems, mAuditionQueue);
        }
    }

    @Override
    public void setCurrentQueue(List<SongInfo> newQueue) {
        setCurrentQueue(newQueue, -1);
    }

    @Override
    public void addQueueItem(SongInfo info) {
        if (mAuditionQueue.contains(info)) {
            return;
        }
        mAuditionQueue.add(info);

        setUpRandomQueue();

        //通知播放列表更新了
        List<MediaSessionCompat.QueueItem> queueItems = QueueHelper.getQueueItems(mAuditionQueue);
        if (mListener != null) {
            mListener.onQueueUpdated(queueItems, mAuditionQueue);
        }
    }

    @Override
    public void deleteQueueItem(SongInfo info, boolean isNeedToPlayNext) {
        if (mAuditionQueue.size() == 0) {
            return;
        }
        if (!mAuditionQueue.contains(info)) {
            return;
        }
        mAuditionQueue.remove(info);

        setUpRandomQueue();

        List<MediaSessionCompat.QueueItem> queueItems = QueueHelper.getQueueItems(mAuditionQueue);
        if (mListener != null) {
            mListener.onQueueUpdated(queueItems, mAuditionQueue);
            //播放下一首
            if (isNeedToPlayNext) {
                mListener.onCurrentQueueIndexUpdated(mCurrentIndex, false, true);
            }
        }
    }

    @Override
    public int getCurrentQueueSize() {
        if (mAuditionQueue == null) {
            return 0;
        }
        return mAuditionQueue.size();
    }

    @Override
    public SongInfo getCurrentMusic() {
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, mAuditionQueue)) {
            return null;
        } else {
            return mAuditionQueue.get(mCurrentIndex);
        }
    }

    @Override
    public void setCurrentMusic(int currentIndex) {
        if (mAuditionQueue.size() == 0) {
            return;
        }
        if (!QueueHelper.isIndexPlayable(currentIndex, mAuditionQueue)) {
            return;
        }
        this.mCurrentIndex = currentIndex;
    }

    @Override
    public boolean skipQueuePosition(int amount) {
        if (mAuditionQueue.size() == 0) {
            return false;
        } else {
            int index = mCurrentIndex + amount;
            if (index < 0) {
                // 在第一首歌曲是上一首，让你在第一首歌曲上
                int playModel = mPlayMode.getCurrPlayMode(mContext);
                if (playModel == PlayMode.PLAY_IN_FLASHBACK || playModel == PlayMode.PLAY_IN_LIST_LOOP) { //如果是倒序或者列表循环，则回去最后一首
                    index = mAuditionQueue.size() - 1;
                } else {
                    index = 0;
                }
            } else {
                //当在最后一首歌时点下一首将返回第一首个
                index %= mAuditionQueue.size();
            }
            if (!QueueHelper.isIndexPlayable(index, mAuditionQueue)) {
                return false;
            }
            mCurrentIndex = index;
            return true;
        }
    }

    @Override
    public void updateSongCoverBitmap(String musicId, Bitmap bitmap) {
        SongInfo musicInfo = QueueHelper.getMusicInfoById(mAuditionQueue, musicId);
        if (musicInfo == null) {
            return;
        }
        musicInfo.setSongCoverBitmap(bitmap);
        int index = mAuditionQueue.indexOf(musicInfo);
        mAuditionQueue.set(index, musicInfo);
    }

    @Override
    public void setCurrentQueueItem(String musicId, boolean isJustPlay, boolean isSwitchMusic) {
        int index = QueueHelper.getMusicIndexOnQueue(mAuditionQueue, musicId);
        setCurrentQueueIndex(index, isJustPlay, isSwitchMusic);
    }

    @Override
    public void setCurrentQueueIndex(int index, boolean isJustPlay, boolean isSwitchMusic) {
        if (index >= 0 && index < mAuditionQueue.size()) {
            mCurrentIndex = index;
            if (mListener != null) {
                mListener.onCurrentQueueIndexUpdated(mCurrentIndex, isJustPlay, isSwitchMusic);
            }
        }
    }

    @Override
    public SongInfo getPreMusicInfo() {
        return getNextOrPreMusicInfo(-1);
    }

    @Override
    public SongInfo getNextMusicInfo() {
        return getNextOrPreMusicInfo(1);
    }

    @Override
    public SongInfo getNextOrPreMusicInfo(int amount) {
        SongInfo info = null;
        SongInfo songInfo = mAuditionQueue.get(mCurrentIndex + amount);
        switch (mPlayMode.getCurrPlayMode(mContext)) {
            //单曲循环
            case PlayMode.PLAY_IN_SINGLE_LOOP:
                info = getCurrentMusic();
                break;
            case PlayMode.PLAY_IN_RANDOM:     //随机播放
            case PlayMode.PLAY_IN_FLASHBACK:  //倒叙播放
            case PlayMode.PLAY_IN_LIST_LOOP:  //列表循环
                info = songInfo == null ? getCurrentMusic() : songInfo;
                break;
            //顺序播放
            case PlayMode.PLAY_IN_ORDER:
                if (amount == 1) {
                    if (mCurrentIndex != mAuditionQueue.size() - 1) {
                        info = songInfo == null ? getCurrentMusic() : songInfo;
                    } else {
                        info = getCurrentMusic();
                    }
                } else if (amount == -1) {
                    if (mCurrentIndex != 0) {
                        info = songInfo == null ? getCurrentMusic() : songInfo;
                    } else {
                        info = getCurrentMusic();
                    }
                }
                break;
            default:
                info = null;
                break;
        }
        return info;
    }

    @Override
    public void updateMetadata() {
        SongInfo currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            if (mListener != null) {
                mListener.onMetadataRetrieveError();
            }
            return;
        }
        final String musicId = currentMusic.getSongId();
        SongInfo metadata = QueueHelper.getMusicInfoById(mAuditionQueue, musicId);
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
                    SongInfo currentMusic = getCurrentMusic();
                    if (currentMusic == null) {
                        return;
                    }
                    String currentPlayingId = currentMusic.getSongId();
                    if (musicId.equals(currentPlayingId)) {
                        if (mListener != null) {
                            mListener.onMetadataChanged(QueueHelper.getMusicInfoById(mAuditionQueue, currentPlayingId));
                        }
                    }
                }
            });
        }
    }
}
