package com.lzx.starrysky.utils.delayaction;

public abstract class MediaValid implements Valid {
    @Override
    public boolean preCheck() {
        return false;
    }

    @Override
    public void doValid() {

    }
}
