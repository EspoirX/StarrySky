package com.lzx.starrysky.delayaction;

import com.lzx.starrysky.StarrySky;
import com.lzx.starrysky.provider.SongInfo;
import com.lzx.starrysky.utils.StarrySkyUtils;

import java.util.ArrayList;
import java.util.List;

public class PlayValidManager {

    private static PlayValidManager playValidManager;
    private List<Valid> mValidQueue = new ArrayList<>();
    private Action action;
    private int validIndex = 0;
    private boolean doCallAfterAction = true;

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
        if (!mValidQueue.contains(valid)) {
            mValidQueue.add(valid);
        }
    }

    public PlayValidManager setAction(Action action) {
        if (action != null) {
            this.action = action;
        }
        return this;
    }

    public List<Valid> getValidQueue() {
        return mValidQueue;
    }

    public void doCall(SongInfo songInfo) {
        StarrySkyUtils.log("doCall#validIndex = " + validIndex);
        //执行验证
        if (validIndex < mValidQueue.size()) {
            Valid valid = mValidQueue.get(0);
            if (valid != null) {
                valid.doValid(songInfo, new Valid.ValidCallback() {
                    @Override
                    public void finishValid() {
                        validIndex++;
                        StarrySkyUtils.log("doCall#  validIndex++ ");
                        doCall(songInfo);
                    }

                    @Override
                    public void doActionDirect() {
                        StarrySkyUtils.log("直接执行 Action");
                        doAction(songInfo); //直接执行
                    }
                });
            }
        } else {
            //执行action
            doAction(songInfo, true);
        }
    }

    public void doAction(SongInfo songInfo) {
        doAction(songInfo, false);
    }

    private void doAction(SongInfo songInfo, boolean doCallAfterAction) {
        if (action != null) {
            this.doCallAfterAction = doCallAfterAction;
            action.call(songInfo);
            validIndex = 0;
            StarrySkyUtils.log("doAction#validIndex = " + validIndex);
        }
    }

    public void doCall(String mediaId) {
        SongInfo songInfo = StarrySky.get().getMediaQueueProvider().getSongInfo(mediaId);
        doCall(songInfo);
    }

    public void resetValidIndex() {
        validIndex = 0;
    }

    public void clear() {
        validIndex = 0;
        mValidQueue.clear();
    }
}
