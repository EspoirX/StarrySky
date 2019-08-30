package com.lzx.musiclib;

import android.content.Context;
import android.widget.Toast;

import com.lzx.starrysky.model.SongInfo;
import com.lzx.starrysky.utils.delayaction.DelayAction;
import com.lzx.starrysky.utils.delayaction.Valid;

/**
 * 请求接口验证模型
 */
public class RequestMusicUrlValid implements Valid {

    private boolean isGetUrl; //是否已经得到url
    private SongInfo mSongInfo;
    private Context mContext;

    public RequestMusicUrlValid(Context context) {
        mContext = context;
    }

    @Override
    public boolean preCheck() {
        return isGetUrl;
    }

    public SongInfo getSongInfo() {
        return mSongInfo;
    }

    @Override
    public void doValid(SongInfo songInfo) {
        //这里模拟请求接口操作，请求完成后修改 preCheck 的状态，然后做自己要做的操作，做完后调用一下 doCall 方方法

        //模拟接口请求成功
        Toast.makeText(mContext, "请求接口成功", Toast.LENGTH_SHORT).show();

        //请求成功后修改一下状态，告诉模型请求成功了，不需要再请求
        isGetUrl = true;

        //请求完后做自己的操作，这里举例把接口信息包装成songInfo
        mSongInfo = new SongInfo();
        mSongInfo.setSongId("111");
        mSongInfo.setSongUrl("http://music.163.com/song/media/outer/url?id=317151.mp3&a=我");
        mSongInfo.setSongCover("https://www.qqkw.com/d/file/p/2018/04-21/c24fd86006670f964e63cb8f9c129fc6.jpg");
        mSongInfo.setSongName("心雨");
        mSongInfo.setArtist("贤哥");

        //调用一下 doCall ，继续执行，才会执行后续的 Action
        DelayAction.getInstance().doCall(songInfo);
    }
}
