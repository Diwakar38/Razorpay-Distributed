package com.projects.razorpay.common_lib.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {

    public static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String randomBase64(int length) {
        byte[] buffer = new byte[length/2];
        SECURE_RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(buffer);
    }

}
