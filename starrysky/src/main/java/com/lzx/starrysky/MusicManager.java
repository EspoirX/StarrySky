package com.lzx.starrysky;


import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.lzx.starrysky.model.MusicProvider;
import com.lzx.starrysky.model.SongInfo;

import java.util.List;

import androidx.annotation.NonNull;

public class MusicManager {
    public static MusicManager getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final MusicManager sInstance = new MusicManager();
    }

    private static Context sContext;

    public static void initMusicManager(Context context) {
        sContext = context;
    }

    /**
     * 根据songId播放,调用前请确保已经设置了播放列表
     */
    public void playMusicById(String songId) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().playFromMediaId(songId, null);
        }
    }

    /**
     * 根据 SongInfo 播放，实际也是根据 songId 播放
     */
    public void playMusicByInfo(SongInfo info) {
        playMusicById(info.getSongId());
    }

    /**
     * 根据要播放的歌曲在播放列表中的下标播放,调用前请确保已经设置了播放列表
     */
    public void playMusicByIndex(int index) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.subscribe(MusicService.UPDATE_PARENT_ID, new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    super.onChildrenLoaded(parentId, children);
                    connection.getTransportControls().playFromMediaId(children.get(index).getMediaId(), null);
                }
            });
        }
    }

    /**
     * 播放
     *
     * @param songInfos       播放列表
     * @param index           要播放的歌曲在播放列表中的下标
     * @param isResetPlayList 是否重新设置播放列表，如果true，则会重新加载播放列表中的资源，比如封面下载等，
     *                        如果false,则使用原来的，相当于缓存，建议当播放列表改变或者第一次播放时才设为true
     */
    public void playMusic(List<SongInfo> songInfos, int index, boolean isResetPlayList) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            //如果列表为空（第一次）或者 isResetPlayList 为 true 都会重新设置播放列表
            if (MusicProvider.getInstance().getSongInfos().isEmpty() || isResetPlayList) {
                MusicProvider.getInstance().nonInitialized();
                MusicProvider.getInstance().setSongInfos(songInfos);
            }
            connection.subscribe(MusicService.UPDATE_PARENT_ID, new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    super.onChildrenLoaded(parentId, children);
                    connection.getTransportControls().playFromMediaId(songInfos.get(index).getSongId(), null);
                }
            });
        }
    }

    /**
     * 播放
     */
    public void playMusic(List<SongInfo> songInfos, int index) {
        playMusic(songInfos, index, false);
    }

    /**
     * 暂停
     */
    public void pauseMusic() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().pause();
        }
    }

    /**
     * 恢复播放
     */
    public void playMusic() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().play();
        }
    }

    /**
     * 停止播放
     */
    public void stopMusic() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().stop();
        }
    }

    /**
     * 准备播放
     */
    public void prepare() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().prepare();
        }
    }

    /**
     * 准备播放，根据songId
     */
    public void prepareFromSongId(String songId) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().prepareFromMediaId(songId, null);
        }
    }

    /**
     * 下一首
     */
    public void skipToNext() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().skipToNext();
        }
    }

    /**
     * 上一首
     */
    public void skipToPrevious() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().skipToPrevious();
        }
    }

    /**
     * 开始快进，每调一次加 0.5 倍
     */
    public void fastForward() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().fastForward();
        }
    }

    /**
     * 开始倒带 每调一次减 0.5 倍，最小为 0
     */
    public void rewind() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().rewind();
        }
    }

    /**
     * 移动到媒体流中的新位置,以毫秒为单位。
     */
    public void seekTo(long pos) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().seekTo(pos);
        }
    }

    /**
     * 设置随机播放模式
     * 必须是以下之一：
     * PlaybackStateCompat.SHUFFLE_MODE_NONE 顺序播放
     * PlaybackStateCompat.SHUFFLE_MODE_ALL  随机播放
     */
    public void setShuffleMode(int shuffleMode) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().setShuffleMode(shuffleMode);
        }
    }

    /**
     * 获取随机播放模式
     */
    public int getShuffleMode() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getMediaController().getShuffleMode();
        }
        return -1;
    }

    /**
     * 设置播放模式
     * 必须是以下之一：
     * PlaybackStateCompat.REPEAT_MODE_NONE  顺序播放
     * PlaybackStateCompat.REPEAT_MODE_ONE   单曲循环
     * PlaybackStateCompat.REPEAT_MODE_ALL   列表循环
     */
    public void setRepeatMode(int repeatMode) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            connection.getTransportControls().setRepeatMode(repeatMode);
        }
    }

    /**
     * 获取播放模式
     */
    public int getRepeatMode() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getMediaController().getRepeatMode();
        }
        return -1;
    }

    /**
     * 获取播放列表
     */
    public List<SongInfo> getPlayList() {
        return MusicProvider.getInstance().getSongInfos();
    }

    /**
     * 更新播放列表
     */
    public void updatePlayList(List<SongInfo> songInfos) {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            MusicProvider.getInstance().nonInitialized();
            MusicProvider.getInstance().setSongInfos(songInfos);
            connection.subscribe(MusicService.UPDATE_PARENT_ID, new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    super.onChildrenLoaded(parentId, children);
                }
            });
        }
    }

    /**
     * 获取当前播放的歌曲信息
     */
    public SongInfo getNowPlayingSongInfo() {
        SongInfo songInfo = null;
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            MediaMetadataCompat metadataCompat = connection.getNowPlaying();
            if (metadataCompat != null) {
                String songId = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                List<SongInfo> songInfos = MusicProvider.getInstance().getSongInfos();
                for (SongInfo info : songInfos) {
                    if (info.getSongId().equals(songId)) {
                        songInfo = info;
                        break;
                    }
                }
            }
        }
        return songInfo;
    }

    /**
     * 获取当前播放的歌曲songId
     */
    public String getNowPlayingSongId() {
        String songId = "";
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            MediaMetadataCompat metadataCompat = connection.getNowPlaying();
            if (metadataCompat != null) {
                songId = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            }
        }
        return songId;
    }

    /**
     * 获取当前播放歌曲的下标
     */
    public int getNowPlayingIndex() {
        int index = -1;
        String songId = getNowPlayingSongId();
        if (!TextUtils.isEmpty(songId)) {
            List<SongInfo> songInfos = MusicProvider.getInstance().getSongInfos();
            for (int i = 0; i < songInfos.size(); i++) {
                if (songId.equals(songInfos.get(i).getSongId())) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 以ms为单位获取当前缓冲的位置。
     */
    public long getBufferedPosition() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getPlaybackState().getBufferedPosition();
        } else {
            return 0;
        }
    }

    /**
     * 获取播放位置 毫秒为单位。
     */
    public long getPlayingPosition() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getPlaybackState().getPosition();
        } else {
            return 0;
        }
    }

    /**
     * 将当前播放速度作为正常播放的倍数。 倒带时这应该是负数， 值为1表示正常播放，0表示暂停。
     */
    public float getPlaybackSpeed() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getPlaybackState().getPlaybackSpeed();
        } else {
            return -1;
        }
    }

    /**
     * 获取底层框架{@link android.media.session.PlaybackState}对象。
     * 此方法仅在API 21+上受支持。
     */
    public Object getPlaybackState() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getPlaybackState().getPlaybackState();
        } else {
            return null;
        }
    }

    /**
     * 获取发送错误时的错误信息
     */
    public CharSequence getErrorMessage() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getPlaybackState().getErrorMessage();
        } else {
            return "connection is not connect";
        }
    }

    /**
     * 获取发送错误时的错误码
     * 0 : 这是默认的错误代码
     * 1 : 当应用程序状态无效以满足请求时的错误代码。
     * 2 : 应用程序不支持请求时的错误代码。
     * 3 : 由于身份验证已过期而无法执行请求时出现错误代码。
     * 4 : 成功请求需要高级帐户时的错误代码。
     * 5 : 检测到太多并发流时的错误代码。
     * 6 : 由于家长控制而阻止内容时出现错误代码。
     * 7 : 内容因区域不可用而被阻止时的错误代码。
     * 8 : 请求的内容已在播放时出现错误代码。
     * 9 : 当应用程序无法跳过任何更多歌曲时出现错误代码，因为已达到跳过限制。
     * 10: 由于某些外部事件而导致操作中断时的错误代码。
     * 11: 由于队列耗尽而无法播放导航（上一个，下一个）时出现错误代码。
     */
    public int getErrorCode() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getPlaybackState().getErrorCode();
        } else {
            return -1;
        }
    }

    /**
     * 获取当前的播放状态。 以下之一：
     * PlaybackStateCompat.STATE_NONE                   默认播放状态，表示尚未添加媒体，或者表示已重置且无内容可播放。
     * PlaybackStateCompat.STATE_STOPPED                当前已停止。
     * PlaybackStateCompat.STATE_PLAYING                正在播放
     * PlaybackStateCompat.STATE_PAUSED                 已暂停
     * PlaybackStateCompat.STATE_FAST_FORWARDING        当前正在快进
     * PlaybackStateCompat.STATE_REWINDING              当前正在倒带
     * PlaybackStateCompat.STATE_BUFFERING              当前正在缓冲
     * PlaybackStateCompat.STATE_ERROR                  当前处于错误状态
     * PlaybackStateCompat.STATE_CONNECTING             正在连接中
     * PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS   正在转跳到上一首
     * PlaybackStateCompat.STATE_SKIPPING_TO_NEXT       正在转跳到下一首
     * PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM 正在切歌
     */
    public int getState() {
        MediaSessionConnection connection = MediaSessionConnection.getInstance(sContext);
        if (connection.isConnected()) {
            return connection.getPlaybackState().getState();
        } else {
            return -1;
        }
    }


}
