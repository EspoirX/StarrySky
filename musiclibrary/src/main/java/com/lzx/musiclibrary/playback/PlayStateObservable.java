package com.lzx.musiclibrary.playback;

import java.util.Observable;

/**
 * @author lzx
 * @date 2018/2/8
 */

public class PlayStateObservable extends Observable {

    public PlayStateObservable() {

    }

    public void stateChangeNotifyObservers(Object data) {
        setChanged();
        notifyObservers(data);
    }
}
