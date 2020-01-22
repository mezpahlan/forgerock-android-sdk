/*
 * Copyright (c) 2019 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.auth;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;

import lombok.Builder;

/**
 * Model of Device Identifier
 */
public class DeviceIdentifier {

    private String keyAlias;
    private KeyStoreManager keyStoreManager;

    @Builder
    DeviceIdentifier(@NotNull Context context, KeyStoreManager keyStoreManager) {

        this.keyAlias = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.keyStoreManager = Config.getInstance(context).applyDefaultIfNull(keyStoreManager);

    }

    /**
     * Retrieve the Device Identifier, the device identifier is stable except when:
     * <p>
     * * App is restored on a new device
     * <p>
     * * User uninstalls/reinstall the App
     * <p>
     * * User clears app data.
     *
     * @return The Device Identifier.
     */
    public String getIdentifier() {
        try {
            return keyAlias + "-" + Base64.encodeToString(
                    MessageDigest.getInstance("SHA1").digest(keyStoreManager.getIdentifierKey(keyAlias).getEncoded())
                    , Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
