package com.lzx.musiclibrary.helper;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.lzx.musiclibrary.aidl.model.SongInfo;
import com.lzx.musiclibrary.playback.PlaybackManager;
import com.lzx.musiclibrary.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xian on 2018/1/22.
 */

public class QueueHelper {

    public static MediaMetadataCompat fetchInfoWithMediaMetadata(SongInfo info) {
        return getMediaMetadataCompat(info);
    }

    private static MediaMetadataCompat getMediaMetadataCompat(SongInfo info) {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, info.getSongId())
                .putString("__SOURCE__", info.getSongUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, info.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, info.getDuration())
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, info.getGenre())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, info.getSongName())
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, info.getTrackNumber())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, info.getSongCoverBitmap());
        if (info.getAlbumInfo() != null) {
            builder
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, info.getAlbumInfo().getAlbumName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, info.getAlbumInfo().getAlbumCover())
                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, info.getAlbumInfo().getSongCount());
        }
        return builder.build();
    }

    public static List<MediaSessionCompat.QueueItem> getQueueItems(List<SongInfo> list) {
        List<MediaMetadataCompat> result = new ArrayList<>();
        Iterable<MediaMetadataCompat> musics = getMusics(list);
        for (MediaMetadataCompat metadata : musics) {
            result.add(metadata);
        }
        return convertToQueue(result);
    }

    private static Iterable<MediaMetadataCompat> getMusics(List<SongInfo> list) {
        if (list.size() == 0) {
            return Collections.emptyList();
        }
        List<MediaMetadataCompat> compatArrayList = new ArrayList<>(list.size());
        for (SongInfo songInfo : list) {
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


    public static SongInfo getMusicInfoById(List<SongInfo> list, String musicId) {
        SongInfo songInfo = null;
        for (SongInfo info : list) {
            if (info.getSongId().equals(musicId)) {
                songInfo = info;
                break;
            }
        }
        return songInfo;
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
    public static boolean isNeedToSwitchMusic(PlaybackManager manager, List<SongInfo> list, int index) {
        return isNeedToSwitchMusic(manager, list.get(index));
    }

    /**
     * 是否需要切歌
     */
    public static boolean isNeedToSwitchMusic(PlaybackManager manager, SongInfo info) {
        String mCurrentMediaId = manager.getCurrentMediaId();
        if (TextUtils.isEmpty(mCurrentMediaId)) {
            return true;
        } else {
            String currMusicId = info.getSongId();
            return !mCurrentMediaId.equals(currMusicId);
        }
    }

}
