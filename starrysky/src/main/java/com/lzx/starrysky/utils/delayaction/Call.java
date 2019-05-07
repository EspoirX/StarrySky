package com.lzx.starrysky.utils.delayaction;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 一个执行单元。
 * 包括一个执行目标体和一个检验队列。
 * 检验队列用来保证所有的前置条件。
 * 当所有的前置条件都通过后，才能进行执行单元。
 */
public class Call {
    //目标对象
    private Action mAction;
    //先进先出验证模型
    private Queue<Valid> mValidQueue = new ArrayDeque<>();
    //上一个执行的 valid
    private Valid mLastValid;

    public Call() {

    }

    public Call(Action action) {
        mAction = action;
    }

    public Call addValid(Valid valid) {
        mValidQueue.add(valid);
        return this;
    }

    public void check() {
        for (Valid valid : mValidQueue) {
            if (valid.preCheck()) {
                mValidQueue.remove(valid);
            }
        }
    }

    public void doCall() {
        MultipleCall.getInstance().postCall(this);
    }

    public Action getAction() {
        return mAction;
    }

    public void setAction(Action action) {
        mAction = action;
    }

    public void setValidQueue(Queue<Valid> validQueue) {
        mValidQueue = validQueue;
    }

    public Queue<Valid> getValidQueue() {
        return mValidQueue;
    }

    public Valid getLastValid() {
        return mLastValid;
    }

    public void setLastValid(Valid lastValid) {
        mLastValid = lastValid;
    }
}
