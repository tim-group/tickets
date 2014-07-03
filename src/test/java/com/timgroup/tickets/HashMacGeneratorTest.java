package com.timgroup.tickets;

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
}
