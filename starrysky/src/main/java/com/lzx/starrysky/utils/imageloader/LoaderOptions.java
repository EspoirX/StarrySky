package com.lzx.starrysky.utils.imageloader;

import android.content.Context;
import android.support.annotation.DrawableRes;

/**
 * 图片加载框架的通用属性封装，不能耦合任何一方的框架
 */
public class LoaderOptions {
    public Context mContext; //上下文
    public int placeholderResId; //占位图
    public int targetWidth; //图片宽
    public int targetHeight; //图片高
    public BitmapCallBack bitmapCallBack; //返回 bitmap 回调
    public String url; //图片连接

    LoaderOptions(String url) {
        this.url = url;
    }

    public void bitmap(BitmapCallBack callBack) {
        this.bitmapCallBack = callBack;
        ImageLoader.getInstance().loadOptions(this);
    }

    public LoaderOptions context(Context context) {
        this.mContext = context;
        return this;
    }

    public LoaderOptions placeholder(@DrawableRes int placeholderResId) {
        this.placeholderResId = placeholderResId;
        return this;
    }

    public LoaderOptions resize(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        return this;
    }
}
