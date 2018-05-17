package com.lzx.musiclibrary.aidl.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 统一音乐信息
 *
 *  lzx
 *  2018/1/17
 */

public class SongInfo implements Parcelable {
    private String songId = ""; //音乐id
    private String songName = ""; //音乐标题
    private String songCover = ""; //音乐封面
    private String songHDCover = ""; //专辑封面(高清)
    private String songSquareCover = ""; //专辑封面(正方形)
    private String songRectCover = ""; //专辑封面(矩形)
    private String songRoundCover = ""; //专辑封面(圆形)
    private Bitmap songCoverBitmap;
    private String songUrl = ""; //音乐播放地址
    private String genre = ""; //类型（流派）
    private String type = ""; //类型
    private String size = "0"; //音乐大小
    private long duration = 0; //音乐长度
    private String artist = ""; //音乐艺术家
    private String artistId = ""; //音乐艺术家id
    private String downloadUrl = ""; //音乐下载地址
    private String site = ""; //地点
    private int favorites = 0; //喜欢数
    private int playCount = 0; //播放数
    private int trackNumber = 0; //媒体的曲目号码（序号：1234567……）
    private String language = "";//语言
    private String country = ""; //地区
    private String proxyCompany = "";//代理公司
    private String publishTime = "";//发布时间
    private String description = ""; //音乐描述
    private String versions = ""; //版本


    private AlbumInfo albumInfo;  //专辑信息
    private TempInfo tempInfo;   //其他信息

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SongInfo && this.songId.equals(((SongInfo) obj).songId);
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongCover() {
        return songCover;
    }

    public void setSongCover(String songCover) {
        this.songCover = songCover;
    }

    public String getSongHDCover() {
        return songHDCover;
    }

    public void setSongHDCover(String songHDCover) {
        this.songHDCover = songHDCover;
    }

    public String getSongSquareCover() {
        return songSquareCover;
    }

    public void setSongSquareCover(String songSquareCover) {
        this.songSquareCover = songSquareCover;
    }

    public String getSongRectCover() {
        return songRectCover;
    }

    public void setSongRectCover(String songRectCover) {
        this.songRectCover = songRectCover;
    }

    public String getSongRoundCover() {
        return songRoundCover;
    }

    public void setSongRoundCover(String songRoundCover) {
        this.songRoundCover = songRoundCover;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProxyCompany() {
        return proxyCompany;
    }

    public void setProxyCompany(String proxyCompany) {
        this.proxyCompany = proxyCompany;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }



    public AlbumInfo getAlbumInfo() {
        return albumInfo;
    }

    public void setAlbumInfo(AlbumInfo albumInfo) {
        this.albumInfo = albumInfo;
    }

    public TempInfo getTempInfo() {
        return tempInfo;
    }

    public void setTempInfo(TempInfo tempInfo) {
        this.tempInfo = tempInfo;
    }

    public Bitmap getSongCoverBitmap() {
        return songCoverBitmap;
    }

    public void setSongCoverBitmap(Bitmap songCoverBitmap) {
        this.songCoverBitmap = songCoverBitmap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.songId);
        dest.writeString(this.songName);
        dest.writeString(this.songCover);
        dest.writeString(this.songHDCover);
        dest.writeString(this.songSquareCover);
        dest.writeString(this.songRectCover);
        dest.writeString(this.songRoundCover);
        dest.writeString(this.songUrl);
        dest.writeString(this.genre);
        dest.writeString(this.type);
        dest.writeString(this.size);
        dest.writeLong(this.duration);
        dest.writeString(this.artist);
        dest.writeString(this.artistId);
        dest.writeString(this.downloadUrl);
        dest.writeString(this.site);
        dest.writeInt(this.favorites);
        dest.writeInt(this.playCount);
        dest.writeInt(this.trackNumber);
        dest.writeString(this.language);
        dest.writeString(this.country);
        dest.writeString(this.proxyCompany);
        dest.writeString(this.publishTime);
        dest.writeString(this.description);
        dest.writeString(this.versions);
        //dest.writeParcelable(this.metadataCompat, flags);
        dest.writeParcelable(this.albumInfo, flags);
        dest.writeParcelable(this.tempInfo, flags);

    }

    public SongInfo() {
    }

    protected SongInfo(Parcel in) {
        this.songId = in.readString();
        this.songName = in.readString();
        this.songCover = in.readString();
        this.songHDCover = in.readString();
        this.songSquareCover = in.readString();
        this.songRectCover = in.readString();
        this.songRoundCover = in.readString();
        this.songUrl = in.readString();
        this.genre = in.readString();
        this.type = in.readString();
        this.size = in.readString();
        this.duration = in.readLong();
        this.artist = in.readString();
        this.artistId = in.readString();
        this.downloadUrl = in.readString();
        this.site = in.readString();
        this.favorites = in.readInt();
        this.playCount = in.readInt();
        this.trackNumber = in.readInt();
        this.language = in.readString();
        this.country = in.readString();
        this.proxyCompany = in.readString();
        this.publishTime = in.readString();
        this.description = in.readString();
        this.versions = in.readString();
       // this.metadataCompat = in.readParcelable(MediaMetadataCompat.class.getClassLoader());
        this.albumInfo = in.readParcelable(AlbumInfo.class.getClassLoader());
        this.tempInfo = in.readParcelable(TempInfo.class.getClassLoader());

    }

    public static final Parcelable.Creator<SongInfo> CREATOR = new Parcelable.Creator<SongInfo>() {
        @Override
        public SongInfo createFromParcel(Parcel source) {
            return new SongInfo(source);
        }

        @Override
        public SongInfo[] newArray(int size) {
            return new SongInfo[size];
        }
    };

    public SongInfo readFromParcel(Parcel source){
        return new SongInfo(source);
    }
}
