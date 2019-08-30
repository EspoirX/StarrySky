package com.lzx.starrysky.utils.delayaction;

import com.lzx.starrysky.model.SongInfo;

/**
 * 目标方法前置检验模型设计与实现
 * https://github.com/feelschaotic/DelayAction
 */
public class DelayAction {
    private Call mCall = new Call();

    public static DelayAction getInstance() {
        return SingletonHolder.mInstance;
    }

    private static class SingletonHolder {
        private static DelayAction mInstance = new DelayAction();
    }

    public DelayAction addAction(Action action) {
        clear();
        mCall.setAction(action);
        return this;
    }

    public DelayAction addValid(Valid valid) {
        //只添加无效的，验证不通过的
        if (valid.preCheck()) {
            return this;
        }
        mCall.addValid(valid);
        return this;
    }

    public void doCall(SongInfo songInfo) {
        //如果上一条valid没有通过，是不允许再发起call的
        if (mCall.getLastValid() != null && !mCall.getLastValid().preCheck()) {
            return;
        }
        //执行action
        if (mCall.getValidQueue().size() == 0) {
            if (mCall.getAction() != null) {
                mCall.getAction().call(songInfo);
                clear();
            }
        } else {
            //执行验证
            Valid valid = mCall.getValidQueue().poll();
            if (valid != null) {
                mCall.setLastValid(valid);
                valid.doValid(songInfo);
            }
        }
    }

    private void clear() {
        mCall.getValidQueue().clear();
        mCall.setAction(null);
        mCall.setLastValid(null);
    }
}
