package org.openjava.upay.util.security;

import org.openjava.upay.util.ClassUtils;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public abstract class KeyStoreUtils
{
    public static PrivateKey getPrivateKey(String keyStorePath, String storeType, String storePass,
                                           String alias, String keyPass) throws Exception
    {
        InputStream in = ClassUtils.getDefaultClassLoader().getResourceAsStream(keyStorePath);
        return getPrivateKey(in, storeType, storePass, alias, keyPass);
    }

    public static String getPublicKeyAsHex(String keyStorePath, String storeType, String storePass, String alias) throws Exception
    {
        InputStream in = ClassUtils.getDefaultClassLoader().getResourceAsStream(keyStorePath);
        PublicKey publicKey = getPublicKey(in, storeType, storePass, alias);
        return HexUtils.encodeHexStr(publicKey.getEncoded());
    }

    public static KeyStore getKeyStore(InputStream in, String storeType, String storePass) throws Exception
    {
        KeyStore ks = KeyStore.getInstance(storeType);
        ks.load(in, storePass.toCharArray());
        in.close();
        return ks;
    }

    public static PrivateKey getPrivateKey(InputStream in, String storeType, String storePass, String alias, String keyPass) throws Exception
    {
        KeyStore ks = getKeyStore(in, storeType, storePass);
        PrivateKey key = (PrivateKey) ks.getKey(alias, keyPass.toCharArray());
        return key;
    }

    public static PublicKey getPublicKey(InputStream in, String storeType, String storePass, String alias) throws Exception
    {
        KeyStore ks = getKeyStore(in, storeType, storePass);
        Certificate cert = ks.getCertificate(alias);
        return cert.getPublicKey();
    }
}
