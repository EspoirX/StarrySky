package com.lzx.musiclibrary.cache;

import android.text.TextUtils;

import com.danikula.videocache.ProxyCacheUtils;
import com.danikula.videocache.file.FileNameGenerator;

/**
 * Created by xian on 2018/4/2.
 */

public class MusicMd5Generator implements FileNameGenerator {

    @Override
    public String generate(String url) {
        return ProxyCacheUtils.computeMD5(url);
    }
}
