package com.lzx.starrysky.registry;

import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.delayaction.DelayAction;
import com.lzx.starrysky.utils.delayaction.Valid;

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
        public boolean preCheck() {
            return false;
        }

        @Override
        public void doValid(SongInfo songInfo) {
            //empty do
            DelayAction.getInstance().doCall(songInfo);
        }
    }
}
