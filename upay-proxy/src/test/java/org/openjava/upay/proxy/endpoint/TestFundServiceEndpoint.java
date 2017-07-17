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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestFundServiceEndpoint extends ServiceEndpointSupport
{
    public void testDepositAccount(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", 1000L);
        transaction.put("pipeline", 1);
        transaction.put("accountId", 200011L);
        transaction.put("description", "Deposit");

        Map<String, Object> fee = new HashMap<>();
        fee.put("type", 2);
        fee.put("pipeline", 2);
        fee.put("amount", 100L);
        List<Map<String, Object>> fees = new ArrayList<>();
        fees.add(fee);
        transaction.put("fees", fees);
        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.fund:deposit");
        ServiceEndpointSupport.HttpResult result = execute("http://www.diligrp.com:8080/spi/payment/doService.do", headers, json);
        System.out.println(result.responseText);
        Map<String, Object> callBack = JsonUtils.fromJsonString(result.responseText, HashMap.class);
        byte[] body = HexUtils.decodeHex(callBack.get("body").toString());
        byte[] signature = HexUtils.decodeHex(callBack.get("signature").toString());
        System.out.println(new String(body, callBack.get("charset").toString()));

        RSACipher.verify(body, signature, publicKey);
        System.out.println(System.currentTimeMillis() - start);
    }

    public void testWithdrawAccount(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", 1000L);
        transaction.put("pipeline", 1);
        transaction.put("accountId", 200011L);
        transaction.put("password", "abcd1234");
        transaction.put("description", "Withdraw");

        Map<String, Object> fee = new HashMap<>();
        fee.put("type", 2);
        fee.put("pipeline", 2);
        fee.put("amount", 100L);
        List<Map<String, Object>> fees = new ArrayList<>();
        fees.add(fee);
        transaction.put("fees", fees);
        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.fund:withdraw");
        ServiceEndpointSupport.HttpResult result = execute("http://www.diligrp.com:8080/spi/payment/doService.do", headers, json);
        System.out.println(result.responseText);
        Map<String, Object> callBack = JsonUtils.fromJsonString(result.responseText, HashMap.class);
        byte[] body = HexUtils.decodeHex(callBack.get("body").toString());
        byte[] signature = HexUtils.decodeHex(callBack.get("signature").toString());
        System.out.println(new String(body, callBack.get("charset").toString()));

        RSACipher.verify(body, signature, publicKey);
        System.out.println(System.currentTimeMillis() - start);
    }

    public void testPayFees(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", 100L);
        transaction.put("pipeline", 2);
        transaction.put("accountId", 200011L);
        transaction.put("password", "abcd1234");
        transaction.put("description", "PayFee");

        List<Map<String, Object>> fees = new ArrayList<>();
        Map<String, Object> fee = new HashMap<>();
        fee.put("type", 2);
        fee.put("pipeline", 2);
        fee.put("amount", 100L);
        fees.add(fee);

        transaction.put("fees", fees);
        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.fund:payFees");
        ServiceEndpointSupport.HttpResult result = execute("http://www.diligrp.com:8080/spi/payment/doService.do", headers, json);
        System.out.println(result.responseText);
        Map<String, Object> callBack = JsonUtils.fromJsonString(result.responseText, HashMap.class);
        byte[] body = HexUtils.decodeHex(callBack.get("body").toString());
        byte[] signature = HexUtils.decodeHex(callBack.get("signature").toString());
        System.out.println(new String(body, callBack.get("charset").toString()));

        RSACipher.verify(body, signature, publicKey);
        System.out.println(System.currentTimeMillis() - start);
    }

    public void testAccountTrade(PrivateKey privateKey, PublicKey publicKey) throws Exception
    {
        Map<String, Object> envelop = new HashMap<>();
        envelop.put("appId", 1001L);
        envelop.put("accessToken", "7C748624D08243F2BF741CEAD455B8AC");
        envelop.put("charset", "UTF-8");

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", 700L);
        transaction.put("pipeline", 2);
        transaction.put("fromId", 200011L);
        transaction.put("toId", 2L);
        transaction.put("password", "abcd1234");
        transaction.put("description", "trade");

        List<Map<String, Object>> fees = new ArrayList<>();
        Map<String, Object> fee = new HashMap<>();
        fee.put("type", 2);
        fee.put("pipeline", 2);
        fee.put("amount", 100L);
        fees.add(fee);

        transaction.put("fees", fees);
        String content = JsonUtils.toJsonString(transaction);
        System.out.println(content);

        byte[] data = content.getBytes("UTF-8");
        byte[] sign = RSACipher.sign(data, privateKey);

        envelop.put("body", HexUtils.encodeHexStr(data));
        envelop.put("signature", HexUtils.encodeHexStr(sign));

        Long start = System.currentTimeMillis();
        String json = JsonUtils.toJsonString(envelop);
        ServiceEndpointSupport.HttpHeader[] headers = new ServiceEndpointSupport.HttpHeader[1];
        headers[0] = ServiceEndpointSupport.HttpHeader.create("service", "payment.service.fund:trade");
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
        TestFundServiceEndpoint endpoint = new TestFundServiceEndpoint();
        endpoint.testDepositAccount(privateKey, publicKey);
    }
}
