package com.lzx.starrysky.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.manager.StarrySkyBuilder;
import com.lzx.starrysky.manager.StarrySkyRegistry;

public abstract class StarrySkyModule {

    public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
        // Default empty impl.
    }

    public void applyMediaValid(@NonNull Context context, StarrySkyRegistry registry) {

    }
}
