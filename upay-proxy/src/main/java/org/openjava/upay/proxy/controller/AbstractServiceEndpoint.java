package org.openjava.upay.proxy.controller;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IMerchantService;
import org.openjava.upay.core.type.MerchantStatus;
import org.openjava.upay.proxy.domain.MessageEnvelop;
import org.openjava.upay.proxy.exception.PackDataEnvelopException;
import org.openjava.upay.proxy.exception.UnpackDataEnvelopException;
import org.openjava.upay.proxy.test.ServiceRequest;
import org.openjava.upay.proxy.util.Constants;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.domain.PaymentServiceRequest;
import org.openjava.upay.trade.service.XATransactionService;
import org.openjava.upay.trade.service.XATransactionServiceFactory;
import org.openjava.upay.util.ObjectUtils;
import org.openjava.upay.util.json.JsonUtils;
import org.openjava.upay.util.security.HexUtils;
import org.openjava.upay.util.security.KeyStoreUtils;
import org.openjava.upay.util.security.RSACipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class AbstractServiceEndpoint
{
    private static final String DEFAULT_CHARSET = "UTF-8";

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource
    private IMerchantService merchantService;

    @Resource
    private XATransactionServiceFactory transactionServiceFactory;

    @Value("${keyStore.path}")
    private String keyStorePath;

    @Value("${keyStore.keyAlias}")
    private String keyAlias;

    @Value("${keyStore.storeType}")
    private String storeType;

    @Value("${keyStore.storePass}")
    private String storePass;

    @Value("${keyStore.keyPass}")
    private String keyPass;


    private volatile PrivateKey privateKey;

    private Object lock = new Object();

    protected final Object sendEnvelop(MessageEnvelop envelop) throws Exception
    {
        if (ObjectUtils.isEmpty(envelop.getService())) {
            LOG.error("Argument missed: service");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }

        String[] services = envelop.getService().split(Constants.COLON);
        String serviceId = services[0];
        String serviceName = services.length > 1 ? services[1] : Constants.DEFAULT_SERVICE_NAME;
        XATransactionService service = transactionServiceFactory.getTransactionService(serviceId);

        Method method;
        try {
            method = service.getClass().getDeclaredMethod(serviceName, ServiceRequest.class);
        } catch (NoSuchMethodException mex) {
            LOG.error("Service unavailable, no method found: " + serviceName);
            throw new FundTransactionException(ErrorCode.SERVICE_UNAVAILABLE);
        }
        Type[] types = method.getGenericParameterTypes();
        if (types[0] instanceof Class) { // Sub class of ServiceRequest not supported for now
            throw new FundTransactionException(ErrorCode.SERVICE_UNAVAILABLE);
        }
        ParameterizedType type = (ParameterizedType) types[0];
        Class<?> dataType = (Class) type.getActualTypeArguments()[0];

        Merchant merchant = checkAccessPermission(envelop);

        byte[] data = HexUtils.decodeHex(envelop.getBody());
        byte[] sign = HexUtils.decodeHex(envelop.getSignature());

        PublicKey publicKey = RSACipher.getPublicKey(merchant.getSecretKey());
        boolean result = RSACipher.verify(data, sign, publicKey);
        if (!result) {
            throw new UnpackDataEnvelopException(ErrorCode.DATA_VERIFY_FAILED);
        }

        String charset = envelop.getCharset() == null ? DEFAULT_CHARSET : envelop.getCharset();
        String content = new String(data, charset);
        LOG.debug("unpackEnvelop: " + content);

        PaymentServiceRequest request = new PaymentServiceRequest();
        request.getContext().setMerchant(merchant);
        request.setData(JsonUtils.fromJsonString(content, dataType));
        return method.invoke(service, request);
    }

    protected final MessageEnvelop packEnvelop(MessageEnvelop template, String content) throws PackDataEnvelopException
    {
        try {
            String charset = template.getCharset() == null ? DEFAULT_CHARSET : template.getCharset();
            LOG.debug("packEnvelop: " + content);

            byte[] data = content.getBytes(charset);
            template.setBody(HexUtils.encodeHexStr(data));
            try {
                byte[] sign = RSACipher.sign(data, loadPrivateKey());
                template.setSignature(HexUtils.encodeHexStr(sign));
            } catch (Exception ex) {
                throw new PackDataEnvelopException(ErrorCode.DATA_SIGN_FAILED, ex);
            }
            return template;
        } catch (PackDataEnvelopException dex) {
            throw  dex;
        } catch (Exception ex) {
            throw new PackDataEnvelopException(ErrorCode.UNKNOWN_EXCEPTION, ex);
        }
    }

    protected MessageEnvelop noSignPackEnvelop(MessageEnvelop template, String content)
    {
        String charset = template.getCharset() == null ? DEFAULT_CHARSET : template.getCharset();
        LOG.debug("createEmptyEnvelop: " + content);
        try {
            byte[] data = content.getBytes(charset);
            template.setBody(HexUtils.encodeHexStr(data));
        } catch (Exception ex) {
            template.setBody("{}");
            LOG.error("noSignPackEnvelop exception", ex);
        }


        return template;
    }

    private Merchant checkAccessPermission(MessageEnvelop envelop)
    {
        if (envelop.getAppId() == null) {
            LOG.error("Argument missed: Merchant Id");
            throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
        }

        Merchant merchant = merchantService.findMerchantById(envelop.getAppId());
        if (merchant == null || merchant.getStatus() != MerchantStatus.NORMAL) {
            LOG.error("Invalid merchant information");
            throw new FundTransactionException(ErrorCode.INVALID_MERCHANT);
        }
        if (!merchant.getAccessToken().equalsIgnoreCase(envelop.getAccessToken())) {
            throw new FundTransactionException(ErrorCode.SERVICE_ACCESS_DENIED);
        }

        return merchant;
    }

    private PrivateKey loadPrivateKey() throws Exception
    {
        if (privateKey == null) {
            synchronized (lock) {
                if (privateKey == null) {
                    privateKey = KeyStoreUtils.getPrivateKey(keyStorePath, storeType, storePass, keyAlias, keyPass);
                    LOG.debug("PrivateKey loaded from " + keyStorePath);
                }
            }
        }
        return privateKey;
    }
}
