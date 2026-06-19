package com.partnersdashboard.util;

import java.security.SecureRandom;

/** Generates secure random 6-digit numeric OTP codes. */
public final class OtpUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateOtp() {
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    private OtpUtil() {}
}
