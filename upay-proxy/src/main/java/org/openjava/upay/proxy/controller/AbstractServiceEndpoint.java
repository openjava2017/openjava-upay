package org.openjava.upay.proxy.controller;

import org.openjava.upay.Constants;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IMerchantService;
import org.openjava.upay.core.type.MerchantStatus;
import org.openjava.upay.proxy.component.ICallableServiceEngine;
import org.openjava.upay.proxy.domain.*;
import org.openjava.upay.proxy.exception.PackDataEnvelopException;
import org.openjava.upay.proxy.exception.ServiceAccessException;
import org.openjava.upay.proxy.exception.UnpackDataEnvelopException;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.util.AssertUtils;
import org.openjava.upay.util.json.JsonUtils;
import org.openjava.upay.util.security.HexUtils;
import org.openjava.upay.util.security.KeyStoreUtils;
import org.openjava.upay.util.security.RSACipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.security.PublicKey;

public abstract class AbstractServiceEndpoint
{
    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    private volatile PrivateKey privateKey;

    private Object lock = new Object();

    @Resource
    private IMerchantService merchantService;

    @Resource
    private ICallableServiceEngine callableServiceEngine;

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

    protected RequestContext checkAccessPermission(MessageEnvelop envelop)
    {
        AssertUtils.notNull(envelop.getAppId(), "Argument missed: Merchant Id");

        Merchant merchant = merchantService.findMerchantById(envelop.getAppId());
        if (merchant == null || merchant.getStatus() != MerchantStatus.NORMAL) {
            throw new ServiceAccessException(ErrorCode.INVALID_MERCHANT);
        }
        if (!merchant.getAccessToken().equalsIgnoreCase(envelop.getAccessToken())) {
            throw new ServiceAccessException(ErrorCode.SERVICE_ACCESS_DENIED);
        }

        RequestContext context = new RequestContext();
        context.setMerchant(merchant);
        return context;
    }

    protected final void unpackEnvelop(MessageEnvelop envelop, String secretKey) throws UnpackDataEnvelopException
    {
        try {
            byte[] data = HexUtils.decodeHex(envelop.getBody());
            byte[] sign = HexUtils.decodeHex(envelop.getSignature());

            PublicKey publicKey = RSACipher.getPublicKey(secretKey);
            boolean result = RSACipher.verify(data, sign, publicKey);
            if (!result) {
                throw new UnpackDataEnvelopException(ErrorCode.DATA_VERIFY_FAILED);
            }

            if (envelop.getCharset() == null) {
                envelop.setCharset(Constants.DEFAULT_CHARSET);
            }
            String content = new String(data, envelop.getCharset());
            envelop.setBody(content);
            LOG.debug("unpackEnvelop: " + content);
        } catch (UnpackDataEnvelopException dex) {
            throw dex;
        } catch (Exception ex) {
            throw new UnpackDataEnvelopException(ErrorCode.UNKNOWN_EXCEPTION, ex);
        }
    }

    protected final ServiceResponse<?> sendEnvelop(RequestContext context, MessageEnvelop envelop) throws Throwable
    {
        CallableServiceEndpoint<?> endpoint = callableServiceEngine.getCallableServiceEndpoint(envelop.getService());
        if (endpoint == null) {
            throw new ServiceAccessException(ErrorCode.SERVICE_UNAVAILABLE);
        }

        ServiceRequest request = new ServiceRequest();
        request.setContext(context);
        request.setData(JsonUtils.fromJsonString(envelop.getBody(), endpoint.getRequiredType()));

        Object result = endpoint.call(request);
        if (result instanceof ServiceResponse) {
            return (ServiceResponse) result;
        } else {
            return ServiceResponse.success(result);
        }
    }

    protected final MessageEnvelop packEnvelop(MessageEnvelop template, String content) throws PackDataEnvelopException
    {
        try {
            LOG.debug("packEnvelop: " + content);
            if (template.getCharset() == null) {
                template.setCharset(Constants.DEFAULT_CHARSET);
            }

            byte[] data = content.getBytes(template.getCharset());
            template.setBody(HexUtils.encodeHexStr(data));
            try {
                byte[] sign = RSACipher.sign(data, loadPrivateKey());
                template.setSignature(HexUtils.encodeHexStr(sign));
            } catch (Exception ex) {
                throw new PackDataEnvelopException(ErrorCode.DATA_SIGN_FAILED, ex);
            }
            return template;
        } catch (PackDataEnvelopException dex) {
            throw dex;
        } catch (Exception ex) {
            throw new PackDataEnvelopException(ErrorCode.UNKNOWN_EXCEPTION, ex);
        }
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
