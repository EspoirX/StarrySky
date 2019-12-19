/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.lzx.starrysky.playback.player;

import android.support.annotation.Nullable;

import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource.BaseFactory;
import com.google.android.exoplayer2.upstream.TransferListener;

/**
 * 代码来自 gsyvideoplayer
 */
public final class SkipSSLHttpDataSourceFactory extends BaseFactory {

    private final String userAgent;
    private final
    TransferListener listener;
    private final long connectTimeoutMillis;
    private final long readTimeoutMillis;
    private final boolean allowCrossProtocolRedirects;

    public SkipSSLHttpDataSourceFactory(String userAgent) {
        this(userAgent, null);
    }


    public SkipSSLHttpDataSourceFactory(String userAgent, @Nullable TransferListener listener) {
        this(userAgent, listener, SkipSSLHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                SkipSSLHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, false);
    }


    public SkipSSLHttpDataSourceFactory(
            String userAgent,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            boolean allowCrossProtocolRedirects) {
        this(
                userAgent,
                /* listener= */ null,
                connectTimeoutMillis,
                readTimeoutMillis,
                allowCrossProtocolRedirects);
    }

    public SkipSSLHttpDataSourceFactory(
            String userAgent,
            @Nullable TransferListener listener,
            long connectTimeoutMillis,
            long readTimeoutMillis,
            boolean allowCrossProtocolRedirects) {
        this.userAgent = userAgent;
        this.listener = listener;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
    }

    @Override
    protected SkipSSLHttpDataSource createDataSourceInternal(
            HttpDataSource.RequestProperties defaultRequestProperties) {
        SkipSSLHttpDataSource dataSource =
                new SkipSSLHttpDataSource(
                        userAgent,
                        /* contentTypePredicate= */ null,
                        connectTimeoutMillis,
                        readTimeoutMillis,
                        allowCrossProtocolRedirects,
                        defaultRequestProperties);
        if (listener != null) {
            dataSource.addTransferListener(listener);
        }
        return dataSource;
    }
}
