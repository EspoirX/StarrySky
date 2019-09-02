package com.lzx.starrysky.common;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.SongInfo;

import java.util.HashMap;

public class PlaybackStage {

    public static final String NONE = "NONE";
    public static final String START = "START";
    public static final String PAUSE = "PAUSE";
    public static final String STOP = "STOP";
    public static final String COMPLETION = "COMPLETION";
    public static final String BUFFERING = "BUFFERING";
    public static final String ERROR = "ERROR";

    private String stage;
    private SongInfo songInfo;
    private HashMap<Key, PlaybackStage> cacheMap = new HashMap<>();
    private int errorCode = -1;
    private String errorMessage = "";
    private StateKey stateKey;

    private PlaybackStage(String stage, SongInfo songInfo) {
        this.stage = stage;
        this.songInfo = songInfo;
        stateKey = new StateKey();
    }

    public String getStage() {
        return stage;
    }

    public SongInfo getSongInfo() {
        return songInfo;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    private void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static class Builder {
        private String stage;
        private SongInfo songInfo;
        private int errorCode = -1;
        private String errorMessage = "";

        Builder setState(String stage) {
            this.stage = stage;
            return this;
        }

        Builder setErrorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        Builder setErrorMsg(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        Builder setSongId(String songId) {
            if (!TextUtils.isEmpty(songId)) {
                this.songInfo = StarrySky.get().getRegistry().getMediaQueueProvider().getSongInfo(songId);
            }
            return this;
        }

        PlaybackStage build() {
            PlaybackStage playbackStage = new PlaybackStage(stage, songInfo);
            playbackStage.setErrorCode(errorCode);
            playbackStage.setErrorMessage(errorMessage);
            return playbackStage;
        }
    }

    static PlaybackStage buildNone() {
        return new PlaybackStage.Builder()
                .setState(PlaybackStage.NONE)
                .setSongId("")
                .build();
    }

    PlaybackStage buildStart(String songId) {
        if (TextUtils.isEmpty(songId)) {
            throw new IllegalStateException("songId is null");
        }
        Key key = stateKey.get(START, songId);
        PlaybackStage stage = cacheMap.get(key);
        if (stage == null) {
            stage = new Builder()
                    .setState(START)
                    .setSongId(songId)
                    .build();
            cacheMap.put(key, stage);
        } else {
            key.offer();
        }
        return stage;
    }

    PlaybackStage buildPause(String songId) {
        if (TextUtils.isEmpty(songId)) {
            throw new IllegalStateException("songId is null");
        }
        Key key = stateKey.get(PAUSE, songId);
        PlaybackStage stage = cacheMap.get(key);
        if (stage == null) {
            stage = new Builder()
                    .setState(PAUSE)
                    .setSongId(songId)
                    .build();
        } else {
            key.offer();
        }
        return stage;
    }

    PlaybackStage buildStop(String songId) {

        if (TextUtils.isEmpty(songId)) {
            throw new IllegalStateException("songId is null");
        }
        Key key = stateKey.get(STOP, songId);
        PlaybackStage stage = cacheMap.get(key);
        if (stage == null) {
            stage = new Builder()
                    .setState(STOP)
                    .setSongId(songId)
                    .build();
        } else {
            key.offer();
        }
        return stage;
    }

    PlaybackStage buildCompletion(String songId) {

        if (TextUtils.isEmpty(songId)) {
            throw new IllegalStateException("songId is null");
        }
        Key key = stateKey.get(COMPLETION, songId);
        PlaybackStage stage = cacheMap.get(key);
        if (stage == null) {
            stage = new Builder()
                    .setState(COMPLETION)
                    .setSongId(songId)
                    .build();
        } else {
            key.offer();
        }
        return stage;
    }

    PlaybackStage buildBuffering(String songId) {
        Key key = stateKey.get(BUFFERING, songId);
        PlaybackStage stage = cacheMap.get(key);
        if (stage == null) {
            stage = new Builder()
                    .setState(BUFFERING)
                    .setSongId(songId)
                    .build();
        } else {
            key.offer();
        }
        return stage;
    }

    PlaybackStage buildError(String songId, int errorCode, String errorMsg) {
        if (TextUtils.isEmpty(songId)) {
            throw new IllegalStateException("songId is null");
        }
        Key key = stateKey.get(ERROR, songId, errorCode, errorMessage);
        PlaybackStage stage = cacheMap.get(key);
        if (stage == null) {
            stage = new Builder()
                    .setState(ERROR)
                    .setSongId(songId)
                    .setErrorCode(errorCode)
                    .setErrorMsg(errorMsg)
                    .build();
        } else {
            key.offer();
        }
        return stage;
    }

    private static class StateKey extends BaseKey<Key> {
        Key get(String state, String songId) {
            return get(state, songId, 0, "");
        }

        Key get(String state, String songId, int errorCode, String errorMsg) {
            Key result = super.get();
            result.init(state, songId, errorCode, errorMsg);
            return result;
        }

        @Override
        public Key create() {
            return new Key(this);
        }
    }

    private static class Key {
        private StateKey pool;

        Key(StateKey pool) {
            this.pool = pool;
        }

        String state = NONE;
        String songId = "";
        int errorCode = 0;
        String errorMsg = "";

        void init(String state, String songId, int errorCode, String errorMsg) {
            this.state = state;
            this.songId = songId;
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof Key) {
                Key key = (Key) obj;
                return state.equals(key.state)
                        && songId.equals(key.songId)
                        && errorCode == key.errorCode
                        && errorMsg.equals(key.errorMsg);
            } else {
                return false;
            }
        }

        void offer() {
            pool.offer(this);
        }

        @Override
        public int hashCode() {
            int result = pool.hashCode();
            result = 31 * result + state.hashCode();
            result = 31 * result + songId.hashCode();
            result = 31 * result + errorCode;
            result = 31 * result + errorMsg.hashCode();
            return result;
        }
    }

}
