package com.lzx.starrysky.registry;

import android.support.annotation.NonNull;
import android.util.ArrayMap;

import com.lzx.starrysky.playback.manager.IPlaybackManager;
import com.lzx.starrysky.provider.MediaResource;
import com.lzx.starrysky.provider.MediaQueueProvider;
import com.lzx.starrysky.control.PlayerControl;
import com.lzx.starrysky.utils.delayaction.Valid;
import com.lzx.starrysky.utils.imageloader.ILoaderStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StarrySkyRegistry {

    private ValidRegistry mValidRegistry;

    private final List<Entry<?, ?>> entries = new ArrayList<>();

    public StarrySkyRegistry() {
        mValidRegistry = new ValidRegistry();
    }

    public void appendValidRegistry(Valid valid) {
        mValidRegistry.append(valid);
    }

    public ValidRegistry getValidRegistry() {
        return mValidRegistry;
    }

    public synchronized <Z> Z get(@NonNull Class<Z> modelClass) {
        for (int i = 0, size = entries.size(); i < size; i++) {
            Entry<?, ?> entry = entries.get(i);
            if (entry.handles(modelClass)) {
                return (Z) entry.data;
            }
        }
        return null;
    }

    public synchronized <Model, Data> StarrySkyRegistry append(
            @NonNull Class<Model> modelClass,
            @NonNull Data data) {
        Entry<Model, Data> entry = new Entry<>(modelClass, data);
        entries.add(entries.size(), entry);
        return this;
    }

    public <Model, Data> StarrySkyRegistry replace(
            @NonNull Class<Model> modelClass,
            @NonNull Data data) {
        remove(modelClass);
        append(modelClass, data);
        return this;
    }

    public <Model> StarrySkyRegistry remove(@NonNull Class<Model> modelClass) {
        for (Iterator<Entry<?, ?>> iterator = entries.iterator(); iterator.hasNext(); ) {
            Entry<?, ?> entry = iterator.next();
            if (entry.handles(modelClass)) {
                iterator.remove();
            }
        }
        return this;
    }

    private static class Entry<Model, Data> {
        private final Class<Model> modelClass;
        private final Data data;

        Entry(@NonNull Class<Model> modelClass,
              @NonNull Data data) {
            this.modelClass = modelClass;
            this.data = data;
        }

        boolean handles(@NonNull Class<?> modelClass) {
            return this.modelClass.isAssignableFrom(modelClass);
        }
    }


}
