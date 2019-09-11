package com.lzx.starrysky.utils.delayaction;

import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.SongInfo;

import java.util.ArrayList;
import java.util.List;

public class PlayValidManager {

    private static PlayValidManager playValidManager;
    private List<Valid> mValidQueue = new ArrayList<>();
    private Action action;
    private int validIndex = 0;

    public static PlayValidManager get() {
        if (playValidManager == null) {
            synchronized (StarrySky.class) {
                if (playValidManager == null) {
                    playValidManager = new PlayValidManager();
                }
            }
        }
        return playValidManager;
    }

    public void addValid(Valid valid) {
        //只添加无效的，验证不通过的
        if (valid.preCheck()) {
            return;
        }
        if (!mValidQueue.contains(valid)) {
            mValidQueue.add(valid);
        }
    }

    public PlayValidManager setAction(Action action) {
        this.action = action;
        return this;
    }

    public void doCall(SongInfo songInfo) {
        //执行验证
        if (validIndex < mValidQueue.size()) {
            Valid valid = mValidQueue.get(0);
            if (valid != null) {
                if (!valid.preCheck()) {
                    valid.doValid(songInfo);
                }
                validIndex++;
            }
        } else {
            //执行action
            doAction(songInfo);
        }
    }

    public void doAction(SongInfo songInfo) {
        if (action != null) {
            action.call(songInfo);
            validIndex = 0;
        }
    }

    public void doCall(String mediaId) {
        SongInfo songInfo = StarrySky.get().getMediaQueueProvider().getSongInfo(mediaId);
        doCall(songInfo);
    }


    public void clear() {
        validIndex = 0;
        mValidQueue.clear();
    }
}
