package com.lzx.musiclibrary.cache;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xian on 2018/4/2.
 */

public class CacheConfig implements Parcelable {

    private boolean openCacheWhenPlaying = false;
    private String cachePath;
    private int maxCacheSize = 0;
    private int maxCacheFilesCount = 0;

    private CacheConfig(Builder builder) {
        this.openCacheWhenPlaying = builder.openCacheWhenPlaying;
        this.cachePath = builder.cachePath;
        this.maxCacheSize = builder.maxCacheSize;
        this.maxCacheFilesCount = builder.maxCacheFilesCount;
    }

    public static CacheConfig DEFAULT = new CacheConfig.Builder()
            .setCachePath("/musicLibrary/song-cache/")
            .setMaxCacheFilesCount(512 * 1024 * 1024)
            .setMaxCacheSize(1024 * 1024 * 1024)
            .setOpenCacheWhenPlaying(false)
            .build();

    public static class Builder {
        private boolean openCacheWhenPlaying = false;
        private String cachePath;
        private int maxCacheSize = 0;
        private int maxCacheFilesCount = 0;

        public Builder setOpenCacheWhenPlaying(boolean openCacheWhenPlaying) {
            this.openCacheWhenPlaying = openCacheWhenPlaying;
            return this;
        }

        public Builder setCachePath(String cachePath) {
            this.cachePath = cachePath;
            return this;
        }

        public Builder setMaxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public Builder setMaxCacheFilesCount(int maxCacheFilesCount) {
            this.maxCacheFilesCount = maxCacheFilesCount;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(this);
        }
    }

    public String getCachePath() {
        return cachePath;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

    public int getMaxCacheFilesCount() {
        return maxCacheFilesCount;
    }

    public boolean isOpenCacheWhenPlaying() {
        return openCacheWhenPlaying;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.openCacheWhenPlaying ? (byte) 1 : (byte) 0);
        dest.writeString(this.cachePath);
        dest.writeInt(this.maxCacheSize);
        dest.writeInt(this.maxCacheFilesCount);
    }

    protected CacheConfig(Parcel in) {
        this.openCacheWhenPlaying = in.readByte() != 0;
        this.cachePath = in.readString();
        this.maxCacheSize = in.readInt();
        this.maxCacheFilesCount = in.readInt();
    }

    public static final Parcelable.Creator<CacheConfig> CREATOR = new Parcelable.Creator<CacheConfig>() {
        @Override
        public CacheConfig createFromParcel(Parcel source) {
            return new CacheConfig(source);
        }

        @Override
        public CacheConfig[] newArray(int size) {
            return new CacheConfig[size];
        }
    };
}
