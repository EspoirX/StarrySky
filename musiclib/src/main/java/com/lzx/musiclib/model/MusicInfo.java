package com.lzx.musiclib.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.lzx.musiclib.service.MusicPlayService;


/**
 * 统一音乐信息
 *
 * @author lzx
 * @date 2017/12/15
 */

public class MusicInfo implements Parcelable {

    private String albumTitle = "";
    private String albumIntro= "";
    private String albumCover= "";
    private String albumNickname= "";
    private String totalMusicCount= "0";
    private String totalPlayCount= "0";

    private String musicId= "";
    private String musicCover= "";
    private String musicTitle= "";
    private String musicUrl= "";
    private String musicDay= "";
    private String musicPlayCount= "";
    private String musicTime= "0";

    private int page= -1;
    private String articleType;
    private String articleId;

    private int playStatus = MusicPlayService.STATE_IDLE;

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public String getAlbumIntro() {
        return albumIntro;
    }

    public void setAlbumIntro(String albumIntro) {
        this.albumIntro = albumIntro;
    }

    public String getAlbumCover() {
        return albumCover;
    }

    public void setAlbumCover(String albumCover) {
        this.albumCover = albumCover;
    }

    public String getAlbumNickname() {
        return albumNickname;
    }

    public void setAlbumNickname(String albumNickname) {
        this.albumNickname = albumNickname;
    }

    public String getTotalMusicCount() {
        return totalMusicCount;
    }

    public void setTotalMusicCount(String totalMusicCount) {
        this.totalMusicCount = totalMusicCount;
    }

    public String getTotalPlayCount() {
        return totalPlayCount;
    }

    public void setTotalPlayCount(String totalPlayCount) {
        this.totalPlayCount = totalPlayCount;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getMusicCover() {
        return musicCover;
    }

    public void setMusicCover(String musicCover) {
        this.musicCover = musicCover;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getMusicDay() {
        return musicDay;
    }

    public void setMusicDay(String musicDay) {
        this.musicDay = musicDay;
    }

    public String getMusicPlayCount() {
        return musicPlayCount;
    }

    public void setMusicPlayCount(String musicPlayCount) {
        this.musicPlayCount = musicPlayCount;
    }

    public String getMusicTime() {
        return musicTime;
    }

    public void setMusicTime(String musicTime) {
        this.musicTime = musicTime;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(int playStatus) {
        this.playStatus = playStatus;
    }

    public String getArticleType() {
        return articleType;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.albumTitle);
        dest.writeString(this.albumIntro);
        dest.writeString(this.albumCover);
        dest.writeString(this.albumNickname);
        dest.writeString(this.totalMusicCount);
        dest.writeString(this.totalPlayCount);
        dest.writeString(this.musicId);
        dest.writeString(this.musicCover);
        dest.writeString(this.musicTitle);
        dest.writeString(this.musicUrl);
        dest.writeString(this.musicDay);
        dest.writeString(this.musicPlayCount);
        dest.writeString(this.musicTime);
        dest.writeString(this.articleId);
        dest.writeString(this.articleType);
        dest.writeInt(this.page);
        dest.writeInt(this.playStatus);
    }

    public MusicInfo() {
    }

    protected MusicInfo(Parcel in) {
        this.albumTitle = in.readString();
        this.albumIntro = in.readString();
        this.albumCover = in.readString();
        this.albumNickname = in.readString();
        this.totalMusicCount = in.readString();
        this.totalPlayCount = in.readString();
        this.musicId = in.readString();
        this.musicCover = in.readString();
        this.musicTitle = in.readString();
        this.musicUrl = in.readString();
        this.musicDay = in.readString();
        this.musicPlayCount = in.readString();
        this.musicTime = in.readString();
        this.articleId = in.readString();
        this.articleType = in.readString();
        this.page = in.readInt();
        this.playStatus = in.readInt();
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel source) {
            return new MusicInfo(source);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    public MusicInfo readFromParcel(Parcel source) {
        return CREATOR.createFromParcel(source);
    }
}
