package com.lzx.musiclibrary.manager.queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.media.session.MediaSessionCompat;

import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.constans.PlayMode;

import java.util.List;

public interface SongQueue {
    Context getContext();

    void updatePlayModel(PlayMode playModel);

    void setUpRandomQueue();

    void setListener(MetadataUpdateListener listener);

    List<SongInfo> getPlayingQueue();

    int getCurrentIndex();

    void setCurrentQueue(List<SongInfo> newQueue, int currentIndex);

    void setCurrentQueue(List<SongInfo> newQueue);

    void addQueueItem(SongInfo info);

    void deleteQueueItem(SongInfo info, boolean isNeedToPlayNext);

    int getCurrentQueueSize();

    SongInfo getCurrentMusic();

    void setCurrentMusic(int currentIndex);

    boolean skipQueuePosition(int amount);

    void updateSongCoverBitmap(String musicId, Bitmap bitmap);

    void setCurrentQueueItem(String musicId, boolean isJustPlay, boolean isSwitchMusic);

    void setCurrentQueueIndex(int index, boolean isJustPlay, boolean isSwitchMusic);

    SongInfo getPreMusicInfo();

    SongInfo getNextMusicInfo();

    SongInfo getNextOrPreMusicInfo(int amount);

    void updateMetadata();

    interface MetadataUpdateListener {
        void onMetadataChanged(SongInfo metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex, boolean isJustPlay, boolean isSwitchMusic);

        void onQueueUpdated(List<MediaSessionCompat.QueueItem> newQueue, List<SongInfo> playingQueue);
    }
}
