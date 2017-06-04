package org.openjava.upay.proxy.controller;

import org.openjava.upay.Constants;
import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IMerchantService;
import org.openjava.upay.core.type.MerchantStatus;
import org.openjava.upay.proxy.domain.MessageEnvelop;
import org.openjava.upay.proxy.exception.PackDataEnvelopException;
import org.openjava.upay.proxy.exception.UnpackDataEnvelopException;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.service.IServiceEndpointFactory;
import org.openjava.upay.trade.support.ICallableEndpoint;
import org.openjava.upay.trade.support.RequestContext;
import org.openjava.upay.trade.support.ServiceRequest;
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
    private IServiceEndpointFactory serviceEndpointFactory;

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

            String charset = envelop.getCharset() == null ? Constants.DEFAULT_CHARSET : envelop.getCharset();
            String content = new String(data, charset);
            envelop.setBody(content);
            LOG.debug("unpackEnvelop: " + content);
        } catch (UnpackDataEnvelopException dex) {
            throw dex;
        } catch (Exception ex) {
            throw new UnpackDataEnvelopException(ErrorCode.UNKNOWN_EXCEPTION, ex);
        }
    }

    protected final Object sendEnvelop(RequestContext context, MessageEnvelop envelop) throws Exception
    {
        ICallableEndpoint<?> endpoint = serviceEndpointFactory.getServiceEndpoint(envelop.getService());
        if (endpoint == null) {
            LOG.error("endpoint not found, service unavailable: " + envelop.getService());
            throw new FundTransactionException(ErrorCode.SERVICE_UNAVAILABLE);
        }

        ServiceRequest request = new ServiceRequest();
        request.setContext(context);
        request.setData(JsonUtils.fromJsonString(envelop.getBody(), endpoint.getRequiredType()));
        return endpoint.call(request);
    }

    protected final MessageEnvelop packEnvelop(MessageEnvelop template, String content) throws PackDataEnvelopException
    {
        try {
            String charset = template.getCharset() == null ? Constants.DEFAULT_CHARSET : template.getCharset();
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
        String charset = template.getCharset() == null ? Constants.DEFAULT_CHARSET : template.getCharset();
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
