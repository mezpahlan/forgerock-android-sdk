/*
 * Copyright (c) 2019 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.CertificatePinner;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Provider to Cache and provide OKHttpClient
 */
class OkHttpClientProvider {

    private static final OkHttpClientProvider INSTANCE = new OkHttpClientProvider();

    private Map<String, OkHttpClient> cache = new ConcurrentHashMap<>();

    private OkHttpClientProvider() {
    }

    public static OkHttpClientProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Create or lookup a cached OKHttpClient
     *
     * @param networkConfig The Server configuration
     * @return The OkHttpClient
     */
    OkHttpClient lookup(NetworkConfig networkConfig) {
        OkHttpClient client = cache.get(networkConfig);

        if (client != null) {
            return client;
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(networkConfig.getTimeout(), networkConfig.getTimeUnit())
                .readTimeout(networkConfig.getTimeout(), networkConfig.getTimeUnit())
                .writeTimeout(networkConfig.getTimeout(), networkConfig.getTimeUnit())
                .followRedirects(false);

        if(networkConfig.getCookieJar() == null){
            builder.cookieJar(CookieJar.NO_COOKIES);
        } else {
            builder.cookieJar(networkConfig.getCookieJar());
        }

        if (networkConfig.getInterceptorSupplier() != null &&
                networkConfig.getInterceptorSupplier().get() != null) {
            for (Interceptor i : networkConfig.getInterceptorSupplier().get()) {
                builder.addInterceptor(i);
            }
        }

        if (Logger.isDebugEnabled()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.level(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        if (!networkConfig.getPins().isEmpty()) {
            CertificatePinner.Builder cpBuilder = new CertificatePinner.Builder();
            for (String s : networkConfig.getPins()) {
                cpBuilder.add(networkConfig.getHost(), s);
            }
            builder.certificatePinner(cpBuilder.build());
        }

        client = builder.build();
        cache.put(networkConfig.getHost(), client);
        return client;

    }

    /**
     * Clear the cached {{@link OkHttpClient}}
     */
    public void clear() {
        cache.clear();
    }
}
