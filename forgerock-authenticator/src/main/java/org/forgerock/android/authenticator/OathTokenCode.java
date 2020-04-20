/*
 * Copyright (c) 2020 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.authenticator;

import org.forgerock.android.authenticator.util.TimeKeeper;

/**
 * Represents a currently active token.
 */
public class OathTokenCode {
    private final String code;
    private final long start;
    private final long until;
    private TimeKeeper timeKeeper;
    private final int MAX_VALUE = 1000;

    /**
     * Creates a OathTokenCode wrap with given data
     * @param timeKeeper class containing timekeeping functionality
     * @param code OTP code
     * @param start start time
     * @param until end time
     */
    protected OathTokenCode(TimeKeeper timeKeeper, String code, long start, long until) {
        this.timeKeeper = timeKeeper;
        this.code = code;
        this.start = start;
        this.until = until;
    }

    /**
     * Gets the code which is currently active.
     * @return The currently active token.
     */
    public String getCurrentCode() {
        return code;
    }

    /**
     * Returns true if the OathTokenCode has not yet expired.
     * @return True if the OathTokenCode is still valid, false otherwise.
     */
    public boolean isValid() {
        long cur = timeKeeper.getCurrentTimeMillis();

        return cur < until;
    }

    /**
     * Get the current progress of the OathTokenCode. This is a number between 0 and 1000, and represents
     * the amount of time that has passed between the start and end times of the code.
     * @return The total progress, a number between 0 and 1000.
     */
    public int getCurrentProgress() {
        long cur = timeKeeper.getCurrentTimeMillis();
        long total = until - start;
        long state = cur - start;
        int progress = (int) (state * MAX_VALUE / total);
        return progress < MAX_VALUE ? progress : MAX_VALUE;
    }

}