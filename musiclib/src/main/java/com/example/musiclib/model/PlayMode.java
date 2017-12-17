package com.example.musiclib.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.musiclib.utils.SPUtils;

/**
 * 播放模式
 *
 * @author lzx
 * @date 2017/12/14
 */

public class PlayMode implements Parcelable {
    /**
     * 顺序播放
     */
    public static final String PLAY_IN_ORDER = "play_in_order";

    /**
     * 单曲循环
     */
    public static final String PLAY_IN_SINGLE_LOOP = "play_in_single_loop";

    /**
     * 随机播放
     */
    public static final String PLAY_IN_RANDOM = "play_in_random";

    /**
     * 列表循环
     */
    public static final String PLAY_IN_LIST_LOOP = "play_in_list_loop";

    private String currPlayMode = PLAY_IN_ORDER;

    public String getCurrPlayMode(Context context) {
        return (String) SPUtils.get(context, "music_key_play_model", PLAY_IN_ORDER);
    }

    /**
     * 这里用sp文件并不合适，但是赖
     * @param context
     * @param currPlayMode
     */
    public void setCurrPlayMode(Context context, String currPlayMode) {
        this.currPlayMode = currPlayMode;
        SPUtils.put(context, "music_key_play_model", currPlayMode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.currPlayMode);
    }

    public PlayMode() {
    }

    protected PlayMode(Parcel in) {
        this.currPlayMode = in.readString();
    }

    public static final Parcelable.Creator<PlayMode> CREATOR = new Parcelable.Creator<PlayMode>() {
        @Override
        public PlayMode createFromParcel(Parcel source) {
            return new PlayMode(source);
        }

        @Override
        public PlayMode[] newArray(int size) {
            return new PlayMode[size];
        }
    };
}
