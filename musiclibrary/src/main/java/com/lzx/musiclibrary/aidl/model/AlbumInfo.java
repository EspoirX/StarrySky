package com.lzx.musiclibrary.aidl.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 专辑信息
 * lzx
 * 2018/2/11
 */

public class AlbumInfo implements Parcelable {
    private String albumId = "";    //专辑id
    private String albumName = "";  //专辑名称
    private String albumCover = ""; //专辑封面
    private String albumHDCover = ""; //专辑封面(高清)
    private String albumSquareCover = ""; //专辑封面(正方形)
    private String albumRectCover = ""; //专辑封面(矩形)
    private String albumRoundCover = ""; //专辑封面(圆形)
    private String artist = "";     //专辑艺术家
    private int songCount = 0;      //专辑音乐数
    private int playCount = 0;      //专辑播放数

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumCover() {
        return albumCover;
    }

    public void setAlbumCover(String albumCover) {
        this.albumCover = albumCover;
    }

    public String getAlbumHDCover() {
        return albumHDCover;
    }

    public void setAlbumHDCover(String albumHDCover) {
        this.albumHDCover = albumHDCover;
    }

    public String getAlbumSquareCover() {
        return albumSquareCover;
    }

    public void setAlbumSquareCover(String albumSquareCover) {
        this.albumSquareCover = albumSquareCover;
    }

    public String getAlbumRectCover() {
        return albumRectCover;
    }

    public void setAlbumRectCover(String albumRectCover) {
        this.albumRectCover = albumRectCover;
    }

    public String getAlbumRoundCover() {
        return albumRoundCover;
    }

    public void setAlbumRoundCover(String albumRoundCover) {
        this.albumRoundCover = albumRoundCover;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.albumId);
        dest.writeString(this.albumName);
        dest.writeString(this.albumCover);
        dest.writeString(this.albumHDCover);
        dest.writeString(this.albumSquareCover);
        dest.writeString(this.albumRectCover);
        dest.writeString(this.albumRoundCover);
        dest.writeString(this.artist);
        dest.writeInt(this.songCount);
        dest.writeInt(this.playCount);
    }

    public AlbumInfo() {
    }

    protected AlbumInfo(Parcel in) {
        this.albumId = in.readString();
        this.albumName = in.readString();
        this.albumCover = in.readString();
        this.albumHDCover = in.readString();
        this.albumSquareCover = in.readString();
        this.albumRectCover = in.readString();
        this.albumRoundCover = in.readString();
        this.artist = in.readString();
        this.songCount = in.readInt();
        this.playCount = in.readInt();
    }

    public static final Parcelable.Creator<AlbumInfo> CREATOR = new Parcelable.Creator<AlbumInfo>() {
        @Override
        public AlbumInfo createFromParcel(Parcel source) {
            return new AlbumInfo(source);
        }

        @Override
        public AlbumInfo[] newArray(int size) {
            return new AlbumInfo[size];
        }
    };
}
