/*
 * Copyright (c) 2020 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.auth;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;

public abstract class AndroidBaseTest {

    protected Context context = ApplicationProvider.getApplicationContext();
    public static String USERNAME = "username";
    public static String PASSWORD = "password";

    protected String TREE = "Simple";

    @Before
    public void setUpSDK() {
        Logger.set(Logger.Level.DEBUG);
        FRAuth.start(context);
    }
}
