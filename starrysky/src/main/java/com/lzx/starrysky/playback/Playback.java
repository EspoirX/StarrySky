/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzx.starrysky.playback;



import static android.support.v4.media.session.MediaSessionCompat.QueueItem;


public interface Playback {

    void start();

    void stop(boolean notifyListeners);

    void setState(int state);

    int getState();

    boolean isConnected();

    boolean isPlaying();

    long getCurrentStreamPosition();

    void updateLastKnownStreamPosition();

    void play(QueueItem item);

    void pause();

    void seekTo(long position);

    void setCurrentMediaId(String mediaId);

    String getCurrentMediaId();

    void onFastForward();

    void onRewind();

    void setVolume(float audioVolume);

    interface Callback {
        void onCompletion();

        void onPlaybackStatusChanged(int state);

        void onError(String error);

        void setCurrentMediaId(String mediaId);
    }

    void setCallback(Callback callback);
}
