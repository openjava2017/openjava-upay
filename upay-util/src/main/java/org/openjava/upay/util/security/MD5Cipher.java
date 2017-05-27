package org.openjava.upay.util.security;

import java.security.MessageDigest;

public abstract class MD5Cipher
{
    private static final String KEY_MD5 = "MD5";

    public static byte[] encrypt(byte[] data) throws Exception
    {
        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
        md5.update(data);
        return md5.digest();
    }
}
