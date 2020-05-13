/*
 * Copyright (c) 2020 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.auth;

import org.forgerock.android.auth.OathTokenCode;
import org.forgerock.android.auth.exception.MechanismCreationException;
import org.forgerock.android.auth.util.TimeKeeper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OathTokenCodeTest {

    private final String CODE = "CODE";
    private final long EXPIRY_DELAY = 30000;
    private OathTokenCode oathTokenCode;
    private TimeKeeper timeKeeper;

    @Before
    public void setUp() throws MechanismCreationException {
        timeKeeper = new TimeKeeper() {
            private long offset = 0;

            @Override
            public long getCurrentTimeMillis() {
                return System.currentTimeMillis() + offset;
            }

            @Override
            public void timeTravel(long addTime) {
                offset += addTime;
            }
        };
        oathTokenCode = new OathTokenCode(timeKeeper, CODE, System.currentTimeMillis(), System.currentTimeMillis() + EXPIRY_DELAY);
    }

    @Test
    public void testShouldContainCorrectCode() throws Exception {
        assertEquals(oathTokenCode.getCurrentCode(), CODE);
    }

    @Test
    public void testShouldReportCorrectValidityBeforeAndAfterExpiry() throws Exception {
        assertEquals(oathTokenCode.isValid(), true);
        timeKeeper.timeTravel(EXPIRY_DELAY);
        assertEquals(oathTokenCode.isValid(), false);
    }

    @Test
    public void testShouldReportProgressBeforeExpiry() throws Exception {
        assertEquals(oathTokenCode.getCurrentProgress(), 0);
        timeKeeper.timeTravel(EXPIRY_DELAY / 4);
        assertEquals(oathTokenCode.getCurrentProgress(), 250);
        timeKeeper.timeTravel(EXPIRY_DELAY / 4);
        assertEquals(oathTokenCode.getCurrentProgress(), 500);
        timeKeeper.timeTravel(EXPIRY_DELAY / 4);
        assertEquals(oathTokenCode.getCurrentProgress(), 750);
        timeKeeper.timeTravel(EXPIRY_DELAY / 4);
        assertEquals(oathTokenCode.getCurrentProgress(), 1000);
    }

    @Test
    public void testShouldReportFullProgressAfterExpiry() throws Exception {
        timeKeeper.timeTravel(EXPIRY_DELAY * 2);
        assertEquals(oathTokenCode.getCurrentProgress(), 1000);
    }

}
