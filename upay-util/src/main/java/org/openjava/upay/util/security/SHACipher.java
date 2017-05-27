package org.openjava.upay.util.security;

import java.security.MessageDigest;

public class SHACipher
{
    private static final String KEY_SHA = "SHA";
    
    public static byte[] encrypt(byte[] data) throws Exception
    {
        MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
        sha.update(data);
  
        return sha.digest();
    }
}
