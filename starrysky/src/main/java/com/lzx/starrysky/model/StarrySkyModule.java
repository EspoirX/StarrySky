package com.lzx.starrysky.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.starrysky.manager.StarrySkyBuilder;

public abstract class StarrySkyModule {

    public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
        // Default empty impl.
    }
}
