/*
 * Copyright (c) 2020 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.auth;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import okhttp3.CookieJar;
import okhttp3.Interceptor;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Manages Network configuration information
 */
@Getter
class NetworkConfig {

    private String host;

    private Integer timeout;

    private TimeUnit timeUnit;

    private List<String> pins;

    private Supplier<CookieJar> cookieJarSupplier;

    private Supplier<List<Interceptor>> interceptorSupplier;

    @Builder(builderMethodName = "networkBuilder")
    NetworkConfig(@NonNull String host,
                         Integer timeout,
                         TimeUnit timeUnit,
                         Supplier<CookieJar> cookieJarSupplier,
                         @Singular List<String> pins,
                         Supplier<List<Interceptor>> interceptorSupplier) {

        this.host = host;

        this.timeout = timeout == null ? 30 : timeout;
        this.timeUnit = timeUnit == null ? SECONDS : timeUnit;
        this.pins = pins;
        this.cookieJarSupplier = cookieJarSupplier;
        this.interceptorSupplier = interceptorSupplier;
    }

    CookieJar getCookieJar() {
        if (cookieJarSupplier != null) {
            return cookieJarSupplier.get();
        } else {
            return CookieJar.NO_COOKIES;
        }
    }
}
