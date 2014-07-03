package com.timgroup.tickets;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HashMacGenerator implements TicketMacGenerator {
    private static final Charset UTF8 = Charset.forName("UTF8");
    private final byte[] secret;
    private final String algorithm;

    public HashMacGenerator(byte[] secret, String algorithm) {
        this.secret = secret;
        this.algorithm = algorithm;
    }

    @Override public String generateMAC(String input) {
        return encode(calculate(input.getBytes(UTF8)));
    }

    private byte[] calculate(byte[] inputBytes) {
        byte[] macResult;
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret, algorithm));
            macResult = mac.doFinal(inputBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Unable to use '" + algorithm + "' MAC algorithm", e);
        }
        return macResult;
    }

    private String encode(byte[] bytes) {
        String digits = "0123456789abcdef";
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int hi = (((int) b) & 0xf0) >> 4;
            int lo = ((int) b) & 0x0f;
            builder.append(digits.charAt(hi));
            builder.append(digits.charAt(lo));
        }
        return builder.toString();
    }
}
