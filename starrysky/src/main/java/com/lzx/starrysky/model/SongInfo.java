package com.lzx.starrysky.model;

import android.graphics.Bitmap;

public class SongInfo {
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
    private long duration = -1; //音乐长度
    private String artist = ""; //音乐艺术家
    private String artistId = ""; //音乐艺术家id
    private String downloadUrl = ""; //音乐下载地址
    private String site = ""; //地点
    private int favorites = 0; //喜欢数
    private int playCount = 0; //播放数
    private int trackNumber = -1; //媒体的曲目号码（序号：1234567……）
    private String language = "";//语言
    private String country = ""; //地区
    private String proxyCompany = "";//代理公司
    private String publishTime = "";//发布时间
    private String description = ""; //音乐描述
    private String versions = ""; //版本

    private String albumId = "";    //专辑id
    private String albumName = "";  //专辑名称
    private String albumCover = ""; //专辑封面
    private String albumHDCover = ""; //专辑封面(高清)
    private String albumSquareCover = ""; //专辑封面(正方形)
    private String albumRectCover = ""; //专辑封面(矩形)
    private String albumRoundCover = ""; //专辑封面(圆形)
    private String albumArtist = "";     //专辑艺术家
    private int albumSongCount = 0;      //专辑音乐数
    private int albumPlayCount = 0;      //专辑播放数

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

    public Bitmap getSongCoverBitmap() {
        return songCoverBitmap;
    }

    public void setSongCoverBitmap(Bitmap songCoverBitmap) {
        this.songCoverBitmap = songCoverBitmap;
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

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public int getAlbumSongCount() {
        return albumSongCount;
    }

    public void setAlbumSongCount(int albumSongCount) {
        this.albumSongCount = albumSongCount;
    }

    public int getAlbumPlayCount() {
        return albumPlayCount;
    }

    public void setAlbumPlayCount(int albumPlayCount) {
        this.albumPlayCount = albumPlayCount;
    }
}
