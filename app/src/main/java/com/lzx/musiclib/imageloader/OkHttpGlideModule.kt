package com.lzx.musiclib.imageloader

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream

/**
 * Glide 加载 https 图片忽略证书
 */
@GlideModule
class OkHttpGlideModule : AppGlideModule() {
    override fun registerComponents(
        context: Context, glide: Glide, registry: Registry
    ) {
        super.registerComponents(context, glide, registry)
        val sslParams = HttpsUtils.getSslSocketFactory(null, null, null)
        val mHttpClient = OkHttpClient().newBuilder()
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
            .build()
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(mHttpClient))
    }
}