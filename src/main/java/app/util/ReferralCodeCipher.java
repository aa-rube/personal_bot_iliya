package app.util;

import java.util.Base64;

public class ReferralCodeCipher {

    public static String encrypt(String input) {
        Base64.Encoder encoder = Base64.getUrlEncoder();
        return encoder.encodeToString(input.getBytes());
    }

    public static String decrypt(String input) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return new String(decoder.decode(input));
    }
}

