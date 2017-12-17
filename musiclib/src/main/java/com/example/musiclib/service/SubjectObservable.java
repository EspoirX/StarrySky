package com.example.musiclib.service;

import java.util.Observable;

/**
 * 被观察者
 *
 * @author lzx
 * @date 2017/12/15
 */

public class SubjectObservable extends Observable {

    public SubjectObservable() {

    }

    public void subjectNotifyObservers(Object data) {
        setChanged();
        notifyObservers(data);
    }
}