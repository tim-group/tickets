package com.timgroup.tickets;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HashMacGenerator implements TicketMacGenerator {
    private static final Charset UTF8 = Charset.forName("UTF8");
    private final byte[] secret;
    private final String algorithm;
    private final int sliceOffset;
    private final int sliceLength;

    private static int macLength(String algorithm) throws NoSuchAlgorithmException {
        return Mac.getInstance(algorithm).getMacLength();
    }

    public static HashMacGenerator sha1(byte[] secret, int sliceOffset, int sliceLength) {
        try {
            return new HashMacGenerator(secret, "HmacSHA1", sliceOffset, sliceLength);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Really expected HmacSHA1 to be available", e);
        }
    }

    public static HashMacGenerator sha1(String secret, int sliceOffset, int sliceLength) {
        return sha1(secret.getBytes(UTF8), sliceOffset, sliceLength);
    }

    public static HashMacGenerator sha1(byte[] secret) {
        try {
            return new HashMacGenerator(secret, "HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Really expected HmacSHA1 to be available", e);
        }
    }

    public static HashMacGenerator sha1(String secret) {
        return sha1(secret.getBytes(UTF8));
    }

    public static HashMacGenerator sha256(byte[] secret, int sliceOffset, int sliceLength) {
        try {
            return new HashMacGenerator(secret, "HmacSHA256", sliceOffset, sliceLength);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Really expected HmacSHA256 to be available", e);
        }
    }

    public static HashMacGenerator sha256(String secret, int sliceOffset, int sliceLength) {
        return sha256(secret.getBytes(UTF8), sliceOffset, sliceLength);
    }

    public static HashMacGenerator sha256(byte[] secret) {
        try {
            return new HashMacGenerator(secret, "HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Really expected HmacSHA256 to be available", e);
        }
    }

    public static HashMacGenerator sha256(String secret) {
        return sha256(secret.getBytes(UTF8));
    }

    public HashMacGenerator(byte[] secret, String algorithm) throws NoSuchAlgorithmException {
        this.secret = secret;
        this.algorithm = algorithm;
        this.sliceOffset = 0;
        this.sliceLength = macLength(algorithm);
    }

    public HashMacGenerator(byte[] secret, String algorithm, int sliceOffset, int sliceLength) throws NoSuchAlgorithmException {
        this.secret = secret;
        this.algorithm = algorithm;
        int macLength = macLength(algorithm);
        if (sliceOffset >= macLength || (sliceOffset + sliceLength) >= macLength) {
            throw new IllegalArgumentException("MAC '" + algorithm + "' is only " + macLength + " bytes long");
        }
        this.sliceOffset = sliceOffset;
        this.sliceLength = sliceLength;
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
        StringBuilder builder = new StringBuilder(sliceLength * 2);
        for (int i = 0; i < sliceLength; i++) {
            byte b = bytes[sliceOffset + i];
            int hi = (((int) b) & 0xf0) >> 4;
            int lo = ((int) b) & 0x0f;
            builder.append(digits.charAt(hi));
            builder.append(digits.charAt(lo));
        }
        return builder.toString();
    }
}
