package com.lzx.starrysky.manager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.notification.factory.INotification;
import com.lzx.starrysky.playback.ExoPlayback;
import com.lzx.starrysky.utils.MD5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StarrySkyPlayerControl implements PlayerControl {

    private Context mContext;
    private MediaSessionConnection connection;

    public StarrySkyPlayerControl(Context context, MediaSessionConnection connection) {
        this.connection = connection;
        mContext = context;
    }

    @Override
    public void playMusicById(String songId) {
        if (MusicProvider.getInstance().hasSongInfo(songId)) {
            connection.getTransportControls().playFromMediaId(songId, null);
        }
    }

    @Override
    public void playMusicByInfo(SongInfo info) {
        MusicProvider.getInstance().addSongInfo(info);
        connection.getTransportControls().playFromMediaId(info.getSongId(), null);
    }

    @Override
    public void playMusicByIndex(int index) {
        List<SongInfo> list = MusicProvider.getInstance().getSongInfos();
        if (list != null && index >= 0 && index < list.size()) {
            connection.getTransportControls().playFromMediaId(list.get(index).getSongId(), null);
        }
    }

    @Override
    public void playMusic(List<SongInfo> songInfos, int index) {
        MusicProvider.getInstance().setSongInfos(songInfos);
        connection.getTransportControls().playFromMediaId(songInfos.get(index).getSongId(), null);
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
        connection.getTransportControls().prepareFromMediaId(songId, null);
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
        connection.getMediaController().sendCommand(ExoPlayback.ACTION_DERAILLEUR, bundle, null);
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
        return connection.getMediaController().getShuffleMode();
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        connection.getTransportControls().setRepeatMode(repeatMode);
    }

    @Override
    public int getRepeatMode() {
        return connection.getMediaController().getRepeatMode();
    }

    @Override
    public List<SongInfo> getPlayList() {
        return MusicProvider.getInstance().getSongInfos();
    }

    @Override
    public void updatePlayList(List<SongInfo> songInfos) {
        MusicProvider.getInstance().setSongInfos(songInfos);
    }

    @Override
    public SongInfo getNowPlayingSongInfo() {
        SongInfo songInfo = null;
        MediaMetadataCompat metadataCompat = connection.getNowPlaying();
        if (metadataCompat != null) {
            String songId = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            songInfo = MusicProvider.getInstance().getSongInfo(songId);
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
            index = MusicProvider.getInstance().getIndexBySongInfo(songId);
        }
        return index;
    }

    @Override
    public long getBufferedPosition() {
        return 0;
    }

    @Override
    public long getPlayingPosition() {
        return 0;
    }

    @Override
    public boolean isSkipToNextEnabled() {
        return (connection.getPlaybackState().getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0;
    }

    @Override
    public boolean isSkipToPreviousEnabled() {
        return (connection.getPlaybackState().getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0;
    }

    @Override
    public float getPlaybackSpeed() {
        return connection.getPlaybackState().getPlaybackSpeed();
    }

    @Override
    public Object getPlaybackState() {
        return connection.getPlaybackState().getPlaybackState();
    }

    @Override
    public CharSequence getErrorMessage() {
        return connection.getPlaybackState().getErrorMessage();
    }

    @Override
    public int getErrorCode() {
        return connection.getPlaybackState().getErrorCode();
    }

    @Override
    public int getState() {
        return connection.getPlaybackState().getState();
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
        connection.getMediaController().sendCommand(ExoPlayback.ACTION_CHANGE_VOLUME, bundle, null);
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public void updateFavoriteUI(boolean isFavorite) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFavorite", isFavorite);
        connection.getMediaController().sendCommand(INotification.ACTION_UPDATE_FAVORITE_UI, bundle, null);
    }

    @Override
    public void updateLyricsUI(boolean isChecked) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isChecked", isChecked);
        connection.getMediaController().sendCommand(INotification.ACTION_UPDATE_LYRICS_UI, bundle, null);
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

    }

    @Override
    public void removePlayerEventListener(OnPlayerEventListener listener) {

    }

    @Override
    public void clearPlayerEventListener() {

    }

    @Override
    public CopyOnWriteArrayList<OnPlayerEventListener> getPlayerEventListeners() {
        return null;
    }
}
