package com.lzx.musiclibrary.playback;

import java.util.Observable;

/**
 * lzx
 * 2018/2/8
 */

public class PlayStateObservable extends Observable {

    public PlayStateObservable() {

    }

    public void stateChangeNotifyObservers(Object data) {
        setChanged();
        notifyObservers(data);
    }
}
