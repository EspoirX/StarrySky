package com.lzx.starrysky.control;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.lzx.starrysky.BaseMediaInfo;
import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.common.MediaSessionConnection;
import com.lzx.starrysky.common.PlaybackStage;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.notification.factory.INotification;
import com.lzx.starrysky.playback.player.ExoPlayback;
import com.lzx.starrysky.playback.player.Playback;
import com.lzx.starrysky.utils.MD5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 播放控制默认实现类
 */
public class StarrySkyPlayerControl implements PlayerControl {

    private Context mContext;
    private MediaSessionConnection connection;
    private MediaQueueProvider mMediaQueueProvider;
    private Playback mPlayback;
    private CopyOnWriteArrayList<OnPlayerEventListener> mPlayerEventListeners = new CopyOnWriteArrayList<>();

    public StarrySkyPlayerControl(Context context) {
        mContext = context;
        StarrySky starrySky = StarrySky.get();
        this.mMediaQueueProvider = starrySky.getMediaQueueProvider();
        this.connection = starrySky.getConnection();
        this.mPlayback = starrySky.getPlayback();
        starrySky.registerPlayerControl(this);
    }

    @Override
    public void playMusicById(String songId) {
        if (mMediaQueueProvider.hasMediaInfo(songId)) {
            playMusicImpl(songId);
        }
    }

    @Override
    public void playMusicByInfo(SongInfo info) {
        if (mMediaQueueProvider.hasMediaInfo(info.getSongId())) {
            playMusicImpl(info.getSongId());
        }
    }

    @Override
    public void playMusicByIndex(int index) {
        BaseMediaInfo info = mMediaQueueProvider.getMediaInfo(index);
        if (info != null) {
            playMusicImpl(info.getMediaId());
        }
    }

    @Override
    public void playMusic(List<SongInfo> songInfos, int index) {
        mMediaQueueProvider.updateMediaListBySongInfo(songInfos);
        playMusicByIndex(index);
    }

    private void playMusicImpl(String mediaId) {
        connection.getTransportControls().playFromMediaId(mediaId, null);
    }

    @NonNull
    private Bundle getMediaBundle(BaseMediaInfo info) {
        Bundle extras = new Bundle();
        extras.putString("mediaUrl", info.getMediaUrl());
        extras.putString("mediaCover", info.getMediaCover());
        extras.putString("mediaTitle", info.getMediaTitle());
        extras.putLong("duration", info.getDuration());
        return extras;
    }

    @Override
    public void pauseMusic() {
        connection.getTransportControls().pause();
    }

    @Override
    public void playMusic() {
        connection.getTransportControls().play();
    }

    @Override
    public void stopMusic() {
        connection.getTransportControls().stop();
    }

    @Override
    public void prepare() {
        connection.getTransportControls().prepare();
    }

    @Override
    public void prepareFromSongId(String songId) {
        if (mMediaQueueProvider.hasMediaInfo(songId)) {
            connection.getTransportControls().prepareFromMediaId(songId, null);
        }
    }

    @Override
    public void skipToNext() {
        connection.getTransportControls().skipToNext();
    }

    @Override
    public void skipToPrevious() {
        connection.getTransportControls().skipToPrevious();
    }

    @Override
    public void fastForward() {
        connection.getTransportControls().fastForward();
    }

    @Override
    public void rewind() {
        connection.getTransportControls().rewind();
    }

    @Override
    public void onDerailleur(boolean refer, float multiple) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("refer", refer);
        bundle.putFloat("multiple", multiple);
        connection.sendCommand(ExoPlayback.ACTION_DERAILLEUR, bundle);
    }

    @Override
    public void seekTo(long pos) {
        connection.getTransportControls().seekTo(pos);
    }

    @Override
    public void setShuffleMode(int shuffleMode) {
        connection.getTransportControls().setShuffleMode(shuffleMode);
    }

    @Override
    public int getShuffleMode() {
        return connection.getShuffleMode();
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        connection.getTransportControls().setRepeatMode(repeatMode);
    }

    @Override
    public int getRepeatMode() {
        return connection.getRepeatMode();
    }

    @Override
    public List<SongInfo> getPlayList() {
        return mMediaQueueProvider.getSongList();
    }

    @Override
    public void updatePlayList(List<SongInfo> songInfos) {
        mMediaQueueProvider.updateMediaListBySongInfo(songInfos);
    }

    @Override
    public SongInfo getNowPlayingSongInfo() {
        SongInfo songInfo = null;
        MediaMetadataCompat metadataCompat = connection.getNowPlaying();
        if (metadataCompat != null) {
            String songId = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            songInfo = mMediaQueueProvider.getSongInfo(songId);
            //播放列表改变了或者清空了，如果还在播放歌曲，这时候 getSongInfo 就会获取不到，
            //此时需要从 metadataCompat 中获取
            if (songInfo == null && !TextUtils.isEmpty(songId)) {
                songInfo = getSongInfoFromMediaMetadata(metadataCompat);
            }
        }
        return songInfo;
    }

    private SongInfo getSongInfoFromMediaMetadata(MediaMetadataCompat metadata) {
        SongInfo songInfo = new SongInfo();
        songInfo.setSongId(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        songInfo.setSongUrl(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
        songInfo.setAlbumName(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        songInfo.setArtist(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        songInfo.setDuration(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        songInfo.setGenre(metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE));
        songInfo.setSongCover(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
        songInfo.setAlbumCover(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
        songInfo.setSongName(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        songInfo.setTrackNumber((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER));
        songInfo.setAlbumSongCount((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS));
        songInfo.setSongCoverBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
        return songInfo;
    }

    @Override
    public String getNowPlayingSongId() {
        String songId = "";
        MediaMetadataCompat metadataCompat = connection.getNowPlaying();
        if (metadataCompat != null) {
            songId = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        }
        return songId;
    }

    @Override
    public int getNowPlayingIndex() {
        int index = -1;
        String songId = getNowPlayingSongId();
        if (!TextUtils.isEmpty(songId)) {
            index = mMediaQueueProvider.getIndexByMediaId(songId);
        }
        return index;
    }

    @Override
    public long getBufferedPosition() {
        return mPlayback != null ? mPlayback.getBufferedPosition() : 0;
    }

    @Override
    public long getPlayingPosition() {
        return mPlayback != null ? mPlayback.getCurrentStreamPosition() : 0;
    }

    @Override
    public boolean isSkipToNextEnabled() {
        PlaybackStateCompat stateCompat = connection.getPlaybackStateCompat();
        return (stateCompat.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0;
    }

    @Override
    public boolean isSkipToPreviousEnabled() {
        PlaybackStateCompat stateCompat = connection.getPlaybackStateCompat();
        return (stateCompat.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0;
    }

    @Override
    public float getPlaybackSpeed() {
        PlaybackStateCompat stateCompat = connection.getPlaybackStateCompat();
        return stateCompat.getPlaybackSpeed();
    }

    @Override
    public Object getPlaybackState() {
        PlaybackStateCompat stateCompat = connection.getPlaybackStateCompat();
        return stateCompat.getPlaybackState();
    }

    @Override
    public CharSequence getErrorMessage() {
        PlaybackStateCompat stateCompat = connection.getPlaybackStateCompat();
        return stateCompat.getErrorMessage();
    }

    @Override
    public int getErrorCode() {
        PlaybackStateCompat stateCompat = connection.getPlaybackStateCompat();
        return stateCompat.getErrorCode();
    }

    @Override
    public int getState() {
        PlaybackStateCompat stateCompat = connection.getPlaybackStateCompat();
        return stateCompat.getState();
    }

    @Override
    public boolean isPlaying() {
        return getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return getState() == PlaybackStateCompat.STATE_PAUSED;
    }

    @Override
    public boolean isIdea() {
        return getState() == PlaybackStateCompat.STATE_NONE;
    }

    @Override
    public boolean isCurrMusicIsPlayingMusic(String songId) {
        if (TextUtils.isEmpty(songId)) {
            return false;
        } else {
            SongInfo playingMusic = getNowPlayingSongInfo();
            return playingMusic != null && songId.equals(playingMusic.getSongId());
        }
    }

    @Override
    public boolean isCurrMusicIsPlaying(String songId) {
        return isCurrMusicIsPlayingMusic(songId) && isPlaying();
    }

    @Override
    public boolean isCurrMusicIsPaused(String songId) {
        return isCurrMusicIsPlayingMusic(songId) && isPaused();
    }

    @Override
    public void setVolume(float audioVolume) {
        if (audioVolume < 0) {
            audioVolume = 0;
        }
        if (audioVolume > 1) {
            audioVolume = 1;
        }
        Bundle bundle = new Bundle();
        bundle.putFloat("AudioVolume", audioVolume);
        connection.sendCommand(ExoPlayback.ACTION_CHANGE_VOLUME, bundle);
    }

    @Override
    public float getVolume() {
        return mPlayback != null ? mPlayback.getVolume() : -1;
    }

    @Override
    public long getDuration() {
        long duration = connection.getNowPlaying().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        //如果没设置duration
        if (duration == 0) {
            if (mPlayback != null) {
                duration = mPlayback.getDuration();
            }
        }
        //当切换歌曲的时候偶尔回调为 -9223372036854775807  Long.MIN_VALUE
        if (duration < -1) {
            return -1;
        }
        return duration;
    }

    @Override
    public void updateFavoriteUI(boolean isFavorite) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFavorite", isFavorite);
        connection.sendCommand(INotification.ACTION_UPDATE_FAVORITE_UI, bundle);
    }

    @Override
    public void updateLyricsUI(boolean isChecked) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isChecked", isChecked);
        connection.sendCommand(INotification.ACTION_UPDATE_LYRICS_UI, bundle);
    }

    @Override
    public List<SongInfo> querySongInfoInLocal() {
        List<SongInfo> songInfos = new ArrayList<>();
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        if (cursor == null) {
            return songInfos;
        }
        while (cursor.moveToNext()) {
            SongInfo song = new SongInfo();
            song.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)));
            song.setAlbumCover(getAlbumArtPicPath(mContext, song.getAlbumId()));
            song.setSongNameKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE_KEY)));
            song.setArtistKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST_KEY)));
            song.setAlbumNameKey(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_KEY)));
            song.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)));
            song.setAlbumName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
            song.setSongUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
            song.setDescription(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)));
            song.setSongName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
            song.setMimeType(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE)));
            song.setYear(String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.YEAR))));
            song.setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
            song.setSize(String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE))));
            song.setPublishTime(String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_ADDED))));
            song.setModifiedTime(String.valueOf(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED))));
            String songId = !TextUtils.isEmpty(song.getSongUrl()) ? MD5.hexdigest(song.getSongUrl())
                    : MD5.hexdigest(String.valueOf(System.currentTimeMillis()));
            song.setSongId(songId);
            songInfos.add(song);
        }
        cursor.close();
        return songInfos;
    }

    private synchronized String getAlbumArtPicPath(Context context, String albumId) {
        if (TextUtils.isEmpty(albumId)) {
            return null;
        }
        String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
        String imagePath = null;
        Uri uri = Uri.parse("content://media" + MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.getPath() + "/" + albumId);
        Cursor cur = context.getContentResolver().query(uri, projection, null, null, null);
        if (cur == null) {
            return null;
        }
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            imagePath = cur.getString(0);
        }
        cur.close();
        return imagePath;
    }

    @Override
    public void addPlayerEventListener(OnPlayerEventListener listener) {
        if (listener != null) {
            if (!mPlayerEventListeners.contains(listener)) {
                mPlayerEventListeners.add(listener);
            }
        }
    }

    @Override
    public void removePlayerEventListener(OnPlayerEventListener listener) {
        if (listener != null) {
            mPlayerEventListeners.remove(listener);
        }
    }

    @Override
    public void clearPlayerEventListener() {
        mPlayerEventListeners.clear();
    }

    @Override
    public CopyOnWriteArrayList<OnPlayerEventListener> getPlayerEventListeners() {
        return mPlayerEventListeners;
    }

    @Override
    public void setPlayBack(Playback playBack) {
        mPlayback = playBack;
    }

    @Override
    public MutableLiveData<PlaybackStage> playbackState() {
        return connection.getPlaybackState();
    }
}
