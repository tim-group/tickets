package com.timgroup.tickets;

import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.timgroup.tickets.HashMacGenerator;

import static org.junit.Assert.assertEquals;

public class HashMacGeneratorTest {
    // perl -l -MDigest::HMAC -MDigest::SHA \
    //  -e '$hmac = Digest::HMAC->new("secret", "Digest::SHA"); $hmac->add("data"); print $hmac->hexdigest'
    // or go to http://www.freeformatter.com/hmac-generator.html
    @Test public void generates_sha1_hmac_for_example_string() throws Exception {
        HashMacGenerator generator = new HashMacGenerator("secret".getBytes("UTF-8"), "HmacSHA1");
        assertEquals("9818e3306ba5ac267b5f2679fe4abd37e6cd7b54", generator.generateMAC("data"));
    }

    @Test public void generates_sha256_hmac_for_example_string() throws Exception {
        HashMacGenerator generator = new HashMacGenerator("secret".getBytes("UTF-8"), "HmacSHA256");
        assertEquals("1b2c16b75bd2a870c114153ccda5bcfca63314bc722fa160d690de133ccbb9db", generator.generateMAC("data"));
    }

    @Test public void generates_sha1_hmac_for_utf8_data() throws Exception {
        HashMacGenerator generator = new HashMacGenerator("secret".getBytes("UTF-8"), "HmacSHA1");
        assertEquals("b39085119a5487ed66b6736880fe25ca55b361aa", generator.generateMAC("\u20ac"));
    }

    @Test public void generates_sha1_hmac_slice() throws Exception {
        HashMacGenerator generator = new HashMacGenerator("secret".getBytes("UTF-8"), "HmacSHA1", 1, 4);
        assertEquals("18e3306b", generator.generateMAC("data"));
    }

    @Test(expected = NoSuchAlgorithmException.class) public void unknown_algorithm_detected_on_construction() throws Exception {
        new HashMacGenerator("secret".getBytes("UTF-8"), "AnInvalidAlgorithmName");
    }

    @Test public void sha1_unsliced_factory() throws Exception {
        HashMacGenerator generator = HashMacGenerator.sha1("secret".getBytes("UTF-8"));
        assertEquals("9818e3306ba5ac267b5f2679fe4abd37e6cd7b54", generator.generateMAC("data"));
    }

    @Test public void sha1_string_unsliced_factory() throws Exception {
        HashMacGenerator generator = HashMacGenerator.sha1("secret");
        assertEquals("9818e3306ba5ac267b5f2679fe4abd37e6cd7b54", generator.generateMAC("data"));
    }

    @Test public void sha1_sliced_factory() throws Exception {
        HashMacGenerator generator = HashMacGenerator.sha1("secret".getBytes("UTF-8"), 1, 4);
        assertEquals("18e3306b", generator.generateMAC("data"));
    }

    @Test public void sha1_string_sliced_factory() throws Exception {
        HashMacGenerator generator = HashMacGenerator.sha1("secret", 1, 4);
        assertEquals("18e3306b", generator.generateMAC("data"));
    }

    @Test public void sha256_unsliced_factory() throws Exception {
        HashMacGenerator generator = HashMacGenerator.sha256("secret".getBytes("UTF-8"));
        assertEquals("1b2c16b75bd2a870c114153ccda5bcfca63314bc722fa160d690de133ccbb9db", generator.generateMAC("data"));
    }

    @Test public void sha256_string_unsliced_factory() throws Exception {
        HashMacGenerator generator = HashMacGenerator.sha256("secret");
        assertEquals("1b2c16b75bd2a870c114153ccda5bcfca63314bc722fa160d690de133ccbb9db", generator.generateMAC("data"));
    }

    @Test public void sha256_sliced_factory() throws Exception {
        HashMacGenerator generator = HashMacGenerator.sha256("secret".getBytes("UTF-8"), 1, 4);
        assertEquals("2c16b75b", generator.generateMAC("data"));
    }

    @Test public void sha256_string_sliced_factory() throws Exception {
        HashMacGenerator generator = HashMacGenerator.sha256("secret", 1, 4);
        assertEquals("2c16b75b", generator.generateMAC("data"));
    }
}
