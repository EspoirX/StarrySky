package com.lzx.starrysky.utils.delayaction;

import com.lzx.starrysky.provider.SongInfo;

import java.util.Queue;
import java.util.Stack;

public class MultipleCall {
    private Stack<Call> mDelaysCallStack = new Stack<>();


    private MultipleCall() {
    }

    public static MultipleCall getInstance() {
        return MultipleHolder.mInstance;
    }

    private static class MultipleHolder {
        private static MultipleCall mInstance = new MultipleCall();
    }

    public MultipleCall postCall(Call call, SongInfo songInfo) {
        call.check();
        if (call.getValidQueue().size() == 0 && call.getAction() != null) {
            //如果全部满足，则跳到目标方法
            call.getAction().call(songInfo);
        } else {
            //加入到延迟执行体中来
            mDelaysCallStack.push(call);
            //查找但不删除此队列的头部
            Valid peekValid = call.getValidQueue().peek();
            if (peekValid != null) {
                call.setLastValid(peekValid);
                peekValid.doValid(songInfo);
            }
        }
        return this;
    }

    public void reCheckValid(SongInfo songInfo) {
        if (mDelaysCallStack.size() <= 0) {
            return;
        }
        Call call = mDelaysCallStack.peek();
        if (!call.getLastValid().preCheck()) {
            throw new RuntimeException(String.format("you must pass through the %s,and then reCall()",
                    call.getLastValid().getClass().toString()));
        }
        Queue<Valid> validQueue = call.getValidQueue();
        validQueue.remove(call.getLastValid());
        if (validQueue.size() == 0) {
            if (call.getAction() != null) {
                call.getAction().call(songInfo);
                mDelaysCallStack.remove(call);
            }
        } else {
            Valid peekValid = call.getValidQueue().peek();
            if (peekValid != null) {
                call.setLastValid(peekValid);
                peekValid.doValid(songInfo);
            }
        }
    }
}
