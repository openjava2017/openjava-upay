package org.openjava.upay.proxy.controller;

import org.openjava.upay.core.exception.FundTransactionException;
import org.openjava.upay.core.model.Merchant;
import org.openjava.upay.proxy.domain.AjaxMessage;
import org.openjava.upay.proxy.domain.MessageEnvelop;
import org.openjava.upay.proxy.exception.PackDataEnvelopException;
import org.openjava.upay.proxy.exception.UnpackDataEnvelopException;
import org.openjava.upay.proxy.util.AjaxHttpUtils;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.trade.domain.Transaction;
import org.openjava.upay.trade.service.XATransactionServiceFactory;
import org.openjava.upay.util.ObjectUtils;
import org.openjava.upay.util.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/spi/payment")
public class PaymentServiceEndpoint extends AbstractServiceEndpoint
{
    private static Logger LOG = LoggerFactory.getLogger(PaymentServiceEndpoint.class);

    @Resource
    private XATransactionServiceFactory transactionServiceFactory;

    @RequestMapping("/doService.do")
    public void doService(HttpServletRequest request, HttpServletResponse response)
    {
        String body = AjaxHttpUtils.extractHttpBody(request);
        LOG.debug("doService request received: " + body);
        AjaxMessage<?> message;
        MessageEnvelop envelop = createEmptyEnvelop();

        try {
            if (ObjectUtils.isEmpty(body)) {
                LOG.error("Cannot extract message envelop from http body");
                throw new FundTransactionException(ErrorCode.ARGUMENT_MISSED);
            }

            envelop = JsonUtils.fromJsonString(body, MessageEnvelop.class);
            Merchant merchant = checkAccessPermission(envelop);
            Transaction transaction = unpackEnvelop(envelop, merchant.getSecretKey(), Transaction.class);
            transaction.setMerchant(merchant);
            transaction = transactionServiceFactory.submit(envelop.getPhase(), transaction);
            message = AjaxMessage.success(transaction.getId());
        } catch (FundTransactionException fte) {
            LOG.error("Payment service exception", fte);
            message = AjaxMessage.failure(fte.getCode(), fte.getMessage());
        } catch (UnpackDataEnvelopException dex) {
            LOG.error("Unpack message envelop exception", dex);
            message = AjaxMessage.failure(dex.getCode(), dex.getMessage());
        } catch (Exception ex) {
            LOG.error("Payment service exception", ex);
            message = AjaxMessage.failure(ErrorCode.UNKNOWN_EXCEPTION.getCode(), ex.getMessage());
        }

        MessageEnvelop callback;
        String content = JsonUtils.toJsonString(message);
        try {
            callback = packEnvelop(envelop, content);
        } catch (PackDataEnvelopException dex) {
            LOG.error("Pack message envelop exception", dex);
            callback = noSignPackEnvelop(envelop, content);
        }

        AjaxHttpUtils.sendResponse(response, callback);
    }

    private MessageEnvelop createEmptyEnvelop()
    {
        MessageEnvelop envelop = new MessageEnvelop();
        envelop.setAppId(0L);
        envelop.setCharset("UTF-8");
        return envelop;
    }
}