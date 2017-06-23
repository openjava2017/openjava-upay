package org.openjava.upay.util.security;

public class PasswordUtils
{
    private static final String CHARSET = "UTF-8";

    public static String encrypt(String password, String secretKey) throws Exception
    {
        if (password == null) {
            return null;
        }

        byte[] data = password.getBytes(CHARSET);
        AESCipher.encrypt(data, secretKey);
        return HexUtils.encodeHexStr(SHACipher.encrypt(AESCipher.encrypt(data, secretKey)));
    }
}
