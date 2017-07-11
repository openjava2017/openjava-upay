package org.openjava.upay.proxy.endpoint;

import org.openjava.upay.core.type.AccountType;
import org.openjava.upay.rpc.http.ServiceEndpointSupport;
import org.openjava.upay.shared.type.Gender;
import org.openjava.upay.util.json.JsonUtils;
import org.openjava.upay.util.security.HexUtils;
import org.openjava.upay.util.security.KeyStoreUtils;
import org.openjava.upay.util.security.RSACipher;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class TestAccountServiceEndpoint extends ServiceEndpointSupport
{
    public void testRegisterAccount(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("type", AccountType.INDIVIDUAL);
        transaction.put("code","2017062800214");
        transaction.put("name", "曾华");
        transaction.put("Gender", Gender.MALE);
        transaction.put("mobile", "13688182561");
        transaction.put("email", "zenghua@diligrp.com");
        transaction.put("idCode", "511023198612299398");
        transaction.put("address", "四川成都市温江区");
        transaction.put("password", "abcd1234");

        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.account:register");
        ServiceEndpointSupport.HttpResult result = execute("http://www.diligrp.com:8080/spi/payment/doService.do", headers, json);
        System.out.println(result.responseText);
        Map<String, Object> callBack = JsonUtils.fromJsonString(result.responseText, HashMap.class);
        byte[] body = HexUtils.decodeHex(callBack.get("body").toString());
        byte[] signature = HexUtils.decodeHex(callBack.get("signature").toString());
        System.out.println(new String(body, callBack.get("charset").toString()));

        RSACipher.verify(body, signature, publicKey);
        System.out.println(System.currentTimeMillis() - start);
    }

    public void testFreezeAccount(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("id", 200011L);

        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.account:freeze");
        ServiceEndpointSupport.HttpResult result = execute("http://www.diligrp.com:8080/spi/payment/doService.do", headers, json);
        System.out.println(result.responseText);
        Map<String, Object> callBack = JsonUtils.fromJsonString(result.responseText, HashMap.class);
        byte[] body = HexUtils.decodeHex(callBack.get("body").toString());
        byte[] signature = HexUtils.decodeHex(callBack.get("signature").toString());
        System.out.println(new String(body, callBack.get("charset").toString()));

        RSACipher.verify(body, signature, publicKey);
        System.out.println(System.currentTimeMillis() - start);
    }

    public void testUnfreezeAccount(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("id", 200011L);

        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.account:unfreeze");
        ServiceEndpointSupport.HttpResult result = execute("http://www.diligrp.com:8080/spi/payment/doService.do", headers, json);
        System.out.println(result.responseText);
        Map<String, Object> callBack = JsonUtils.fromJsonString(result.responseText, HashMap.class);
        byte[] body = HexUtils.decodeHex(callBack.get("body").toString());
        byte[] signature = HexUtils.decodeHex(callBack.get("signature").toString());
        System.out.println(new String(body, callBack.get("charset").toString()));

        RSACipher.verify(body, signature, publicKey);
        System.out.println(System.currentTimeMillis() - start);
    }

    public void testFreezeFund(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("accountId", 200011L);
        transaction.put("amount", 100L);
        transaction.put("userId", 1001L);
        transaction.put("userName", "曾华");
        transaction.put("description", "freeze fund");

        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.fund:freeze");
        ServiceEndpointSupport.HttpResult result = execute("http://www.diligrp.com:8080/spi/payment/doService.do", headers, json);
        System.out.println(result.responseText);
        Map<String, Object> callBack = JsonUtils.fromJsonString(result.responseText, HashMap.class);
        byte[] body = HexUtils.decodeHex(callBack.get("body").toString());
        byte[] signature = HexUtils.decodeHex(callBack.get("signature").toString());
        System.out.println(new String(body, callBack.get("charset").toString()));

        RSACipher.verify(body, signature, publicKey);
        System.out.println(System.currentTimeMillis() - start);
    }

    public void testUnfreezeFund(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("serialNo", "20170711400004");
        transaction.put("userId", 1021L);
        transaction.put("userName", "曾华1");

        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.fund:unfreeze");
        ServiceEndpointSupport.HttpResult result = execute("http://www.diligrp.com:8080/spi/payment/doService.do", headers, json);
        System.out.println(result.responseText);
        Map<String, Object> callBack = JsonUtils.fromJsonString(result.responseText, HashMap.class);
        byte[] body = HexUtils.decodeHex(callBack.get("body").toString());
        byte[] signature = HexUtils.decodeHex(callBack.get("signature").toString());
        System.out.println(new String(body, callBack.get("charset").toString()));

        RSACipher.verify(body, signature, publicKey);
        System.out.println(System.currentTimeMillis() - start);
    }

    public static void main(String[] args) throws Exception
    {
        InputStream privateKeyIn = new FileInputStream("E:\\certification\\client.jks");
        InputStream publicKeyIn = new FileInputStream("E:\\certification\\upay.jks");
        PrivateKey privateKey = KeyStoreUtils.getPrivateKey(privateKeyIn, "JKS", "abcd1234", "clientkey", "abcd1234");
        PublicKey publicKey = KeyStoreUtils.getPublicKey(publicKeyIn, "JKS", "abcd1234", "upaykey");
        TestAccountServiceEndpoint endpoint = new TestAccountServiceEndpoint();
        endpoint.testUnfreezeFund(privateKey, publicKey);
    }
}
