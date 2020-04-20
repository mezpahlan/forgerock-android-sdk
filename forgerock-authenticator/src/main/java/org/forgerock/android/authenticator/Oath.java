/*
 * Copyright (c) 2020 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.authenticator;

/**
 * Represents an instance of a OATH authentication mechanism. Associated with an Account.
 */
public class Oath extends Mechanism {

    public enum TokenType {
        HOTP, TOTP
    }

    /** OATH type, must be either 'TOTP' or 'HOTP' */
    private TokenType oathType;
    /** Algorithm of HMAC-based OTP */
    private String algorithm;
    /** Digits as in Int for length of OTP credentials */
    private int digits;
    /** Counter as in Int for number of OTP credentials generated */
    private long counter;
    /** Unique identifier of the Mechanism */
    private int period;

    private Oath(String mechanismUID, String issuer, String accountName, String type, TokenType oathType,
                String algorithm, String secret, int digits, long counter, int period) {
        super(mechanismUID, issuer, accountName, type, secret);
        this.oathType = oathType;
        this.algorithm = algorithm;
        this.digits = digits;
        this.counter = counter;
        this.period = period;
    }

    /**
     * Returns the number of digits that are in OTPs generated by this Token.
     * @return The OTP length.
     */
    public int getDigits() {
        return digits;
    }

    /**
     * Returns the algorithm used by this Oath.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the value of the counter of this Oath.
     */
    public long getCounter() {
        return counter;
    }

    /**
     * Returns the period of this Oath. The frequency with which the OTP changes in seconds
     * @return
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Returns the token type (HOTP, TOTP)
     * @return The token type.
     */
    public TokenType getOathType() {
        return oathType;
    }

    public void incremetCounter() {
        counter++;
    }

    /**
     * Returns a builder for creating a Oath Mechanism.
     * @return The Oath builder.
     */
    public static OathBuilder builder() {
        return new OathBuilder();
    }

    /**
     * Builder class responsible for producing a Token.
     */
    public static class OathBuilder {
        private String mechanismUID;
        private String issuer;
        private String accountName;
        private TokenType oathType;
        private String algorithm;
        private String secret;
        private int digits;
        private long counter;
        private int period;

        /**
         * Sets the mechanism unique Id.
         * @param mechanismUID the mechanism unique Id.
         * @return The current builder.
         */
        public OathBuilder setMechanismUID(String mechanismUID) {
            this.mechanismUID = mechanismUID;
            return this;
        }

        /**
         * Sets the name of the IDP that issued this account.
         * @param issuer The IDP name.
         * @return The current builder.
         */
        public OathBuilder setIssuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        /**
         * Sets the name of the account.
         * @param accountName The account name.
         * @return The current builder.
         */
        public OathBuilder setAccountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        /**
         * Sets the type of OTP that will be used.
         * @param type Type must be 'totp' or 'hotp'.
         * @return The current builder.
         */
        public OathBuilder setType(TokenType type) {
            this.oathType = type;
            return this;
        }

        /**
         * Sets the algorithm used for generating the OTP.
         * Assumption: algorithm name is valid if a corresponding algorithm can be loaded.
         *
         * @param algorithm algorithm to assign.
         * @return The current builder.
         */
        public OathBuilder setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        /**
         * Sets the secret used for generating the OTP.
         * Base32 encoding based on: http://tools.ietf.org/html/rfc4648#page-8
         *
         * @param secret A Base32 encoded secret key.
         * @return The current builder.
         */
        public OathBuilder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        /**
         * Sets the length of the OTP to generate.
         * @param digits Number of digits, either 6 or 8.
         * @return The current builder.
         */
        public OathBuilder setDigits(int digits) {
            this.digits = digits;
            return this;
        }

        /**
         * Sets the frequency with which the OTP changes.
         * @param period Non null period in seconds.
         * @return The current builder.
         */
        public OathBuilder setPeriod(int period) {
            this.period = period;
            return this;
        }

        /**
         * Sets the counter for the OTP. Only useful for HOTP.
         * @param counter counter as an long number.
         * @return The current builder.
         */
        public OathBuilder setCounter(long counter) {
            this.counter = counter;
            return this;
        }

        /**
         * Produce the described Oath Token.
         * @return The built Token.
         */
        protected Oath build() {
            return new Oath(mechanismUID, issuer, accountName, Mechanism.OATH, oathType, algorithm,
                    secret, digits, counter, period);
        }

    }

}