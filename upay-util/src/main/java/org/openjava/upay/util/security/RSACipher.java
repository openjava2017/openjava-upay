package org.openjava.upay.util.security;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSACipher
{
    private static final String KEY_ALGORITHM = "RSA";

    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    public static String[] generateRSAKeyPair() throws Exception
    {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);;
        keyPairGen.initialize(512, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String[] keyPairArray = new String[2];
        keyPairArray[0] = HexUtils.encodeHexStr(privateKey.getEncoded(), false);
        keyPairArray[1] = HexUtils.encodeHexStr(publicKey.getEncoded(), false);

        return keyPairArray;
    }

    public static byte[] encrypt(byte[] data, Key secretKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, Key secretKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) throws Exception
    {
        Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
        signature.initSign(privateKey, new SecureRandom());
        signature.update(data);
        return signature.sign();
    }

    public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey) throws Exception
    {
        Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(sign);
    }

    public static PrivateKey getPrivateKey(String key) throws Exception
    {
        byte[] keyBytes = HexUtils.decodeHex(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey getPublicKey(String key) throws Exception
    {
        byte[] keyBytes = HexUtils.decodeHex(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }
}
