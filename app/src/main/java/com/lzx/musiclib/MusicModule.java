package com.lzx.musiclib;

import android.content.Context;
import android.support.annotation.NonNull;

import com.lzx.musiclib.imageloader.GlideLoader;
import com.lzx.starrysky.StarrySkyBuilder;
import com.lzx.starrysky.registry.StarrySkyRegistry;
import com.lzx.starrysky.provider.StarrySkyModule;

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
