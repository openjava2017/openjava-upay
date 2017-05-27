package org.openjava.upay.proxy.util;

import org.openjava.upay.rpc.payment.PaymentServiceEndpoint;

import java.io.FileInputStream;
import java.io.InputStream;

public class TestPaymentEndpoint
{
    public static void main(String[] args) throws Exception
    {
        InputStream privateKeyIn, publicKeyIn;

        PaymentServiceEndpoint endpoint = new PaymentServiceEndpoint();
        privateKeyIn = new FileInputStream("E:\\certification\\client.jks");
        publicKeyIn = new FileInputStream("E:\\certification\\upay.jks");
        endpoint.testRechargeAccount(privateKeyIn, publicKeyIn);
    }
}
