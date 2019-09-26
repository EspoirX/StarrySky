package com.lzx.starrysky;

import android.support.annotation.Nullable;

public class BaseMediaInfo implements Cloneable {
    private String mediaId = "";
    private String mediaUrl;
    private String mediaCover;
    private String mediaTitle;
    private long duration;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaCover() {
        return mediaCover;
    }

    public void setMediaCover(String mediaCover) {
        this.mediaCover = mediaCover;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof BaseMediaInfo) {
            BaseMediaInfo info = (BaseMediaInfo) obj;
            return info.getMediaId().equals(mediaId) && info.getMediaUrl().equals(mediaUrl);
        } else {
            return false;
        }
    }

    @Override
    protected Object clone() {
        Object obj = null;
        //调用Object类的clone方法，返回一个Object实例
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public String toString() {
        return "BaseMediaInfo{" +
                "mediaId='" + mediaId + '\'' +
                '}';
    }
}
