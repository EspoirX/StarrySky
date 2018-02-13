package com.lzx.musiclibrary.helper;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.manager.QueueManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xian on 2018/1/22.
 */

public class QueueHelper {

//    public static List<SongInfo> fetchListWithMediaMetadata(List<SongInfo> list) {
//        List<SongInfo> infos = new ArrayList<>();
//        for (SongInfo info : list) {
//            info.setMetadataCompat(getMediaMetadataCompat(info));
//            infos.add(info);
//        }
//        return infos;
//    }

    public static MediaMetadataCompat fetchInfoWithMediaMetadata(SongInfo info) {
        return getMediaMetadataCompat(info);
    }

    private static MediaMetadataCompat getMediaMetadataCompat(SongInfo info) {
        if (info.getAlbumInfo() == null) {
            throw new RuntimeException("albumInfo must not be null.");
        }
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, info.getSongId())
                .putString("__SOURCE__", info.getSongUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, info.getAlbumInfo().getAlbumName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, info.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, info.getDuration())
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, info.getGenre())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, info.getAlbumInfo().getAlbumCover())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.getSongName())
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, info.getTrackNumber())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, info.getSongCoverBitmap())
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, info.getAlbumInfo().getSongCount())
                .build();
    }

    public static List<MediaSessionCompat.QueueItem> getQueueItems(ConcurrentMap<String, SongInfo> musicListById) {
        List<MediaMetadataCompat> result = new ArrayList<>();
        Iterable<MediaMetadataCompat> musics = getMusics(musicListById);
        for (MediaMetadataCompat metadata : musics) {
            result.add(metadata);
        }
        return convertToQueue(result);
    }

    private static Iterable<MediaMetadataCompat> getMusics(ConcurrentMap<String, SongInfo> musicListById) {
        if (musicListById.size() == 0) {
            return Collections.emptyList();
        }
        List<MediaMetadataCompat> compatArrayList = new ArrayList<>(musicListById.size());
        for (SongInfo songInfo : musicListById.values()) {
            compatArrayList.add(getMediaMetadataCompat(songInfo));
        }
        return compatArrayList;
    }

    private static List<MediaSessionCompat.QueueItem> convertToQueue(Iterable<MediaMetadataCompat> tracks) {
        List<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
        int count = 0;
        for (MediaMetadataCompat track : tracks) {

            String hierarchyAwareMediaID = track.getDescription().getMediaId();

            MediaMetadataCompat trackCopy = new MediaMetadataCompat.Builder(track)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                    .build();

            MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(
                    trackCopy.getDescription(), count++);
            queue.add(item);
        }
        return queue;
    }


    public static SongInfo getMusicInfoById(ConcurrentMap<String, SongInfo> musicListById, String musicId) {
        return musicListById.containsKey(musicId) ? musicListById.get(musicId) : null;
    }

    /**
     * 判断index是否合法
     */
    public static boolean isIndexPlayable(int index, List<SongInfo> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }

    public static int getMusicIndexOnQueue(Iterable<SongInfo> queue, String mediaId) {
        int index = 0;
        for (SongInfo item : queue) {
            if (mediaId.equals(item.getSongId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public static int getMusicIndexOnQueue(List<SongInfo> queue, String mediaId) {
        int index = 0;
        for (SongInfo item : queue) {
            if (mediaId.equals(item.getSongId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * 是否需要切歌
     */
    public static boolean isNeedToSwitchMusic(QueueManager queueManager, List<SongInfo> list, int index) {
        return isNeedToSwitchMusic(queueManager, list.get(index));
    }

    /**
     * 是否需要切歌
     */
    public static boolean isNeedToSwitchMusic(QueueManager queueManager, SongInfo info) {
        SongInfo songInfo = queueManager.getCurrentMusic();
        if (songInfo == null) {
            return true;
        } else {
            String playingMusicId = songInfo.getSongId();
            String currMusicId = info.getSongId();
            return !playingMusicId.equals(currMusicId);
        }
    }

}
