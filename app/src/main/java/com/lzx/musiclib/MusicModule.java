package com.lzx.musiclib;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.musiclib.imageloader.GlideLoader;
import com.lzx.starrysky.manager.StarrySkyBuilder;
import com.lzx.starrysky.manager.StarrySkyRegistry;
import com.lzx.starrysky.model.StarrySkyModule;

public class MusicModule extends StarrySkyModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull StarrySkyBuilder builder) {
        super.applyOptions(context, builder);
        builder.setImageLoader(new GlideLoader());
    }

    @Override
    public void applyMediaValid(@NonNull Context context, StarrySkyRegistry registry) {
        super.applyMediaValid(context, registry);
        registry.appendValidRegistry(new RequestMusicUrlValid(context));
    }
}
