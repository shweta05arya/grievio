package com.grievio.util;
import java.security.SecureRandom;
public class OTPGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private OTPGenerator() {}
    public static String generateOTP() {
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
