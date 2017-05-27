package org.openjava.upay.proxy.controller;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.core.service.IMerchantService;
import org.openjava.upay.core.type.MerchantStatus;
import org.openjava.upay.proxy.domain.MessageEnvelop;
import org.openjava.upay.proxy.exception.PackDataEnvelopException;
import org.openjava.upay.proxy.exception.UnpackDataEnvelopException;
import org.openjava.upay.shared.type.ErrorCode;
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
    private static final String DEFAULT_CHARSET = "UTF-8";

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource
    private IMerchantService merchantService;

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

    protected final Merchant checkAccessPermission(MessageEnvelop envelop)
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

    protected final <T> T unpackEnvelop(MessageEnvelop envelop, String secretKey, Class<T> type) throws UnpackDataEnvelopException
    {
        try {
            byte[] data = HexUtils.decodeHex(envelop.getBody());
            byte[] sign = HexUtils.decodeHex(envelop.getSignature());

            PublicKey publicKey = RSACipher.getPublicKey(secretKey);
            boolean result = RSACipher.verify(data, sign, publicKey);
            if (!result) {
                throw new UnpackDataEnvelopException(ErrorCode.DATA_VERIFY_FAILED);
            }

            String charset = envelop.getCharset() == null ? DEFAULT_CHARSET : envelop.getCharset();
            String content = new String(data, charset);
            LOG.debug("unpackEnvelop: " + content);

            return JsonUtils.fromJsonString(content, type);
        } catch (UnpackDataEnvelopException dex) {
            throw dex;
        } catch (Exception ex) {
            throw new UnpackDataEnvelopException(ErrorCode.UNKNOWN_EXCEPTION, ex);
        }
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
