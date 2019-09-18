package com.lzx.starrysky.registry;

import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.StarrySkyUtils;
import com.lzx.starrysky.delayaction.Valid;

import java.util.ArrayList;
import java.util.List;

public class ValidRegistry {

    private List<Valid> mValids = new ArrayList<>();

    void append(Valid valid) {
        if (!mValids.contains(valid)) {
            mValids.add(valid);
        }
    }

    public List<Valid> getValids() {
        return mValids;
    }

    public boolean hasValid() {
        return mValids.size() > 0;
    }

    public static class DefaultValid implements Valid {

        @Override
        public void doValid(SongInfo songInfo, ValidCallback callback) {
            StarrySkyUtils.log("执行了 DefaultValid");
            callback.finishValid();
        }
    }
}
