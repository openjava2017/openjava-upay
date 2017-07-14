package org.openjava.upay.proxy.controller;

import org.openjava.upay.proxy.domain.MessageEnvelop;
import org.openjava.upay.proxy.domain.RequestContext;
import org.openjava.upay.proxy.domain.ServiceResponse;
import org.openjava.upay.proxy.exception.PackDataEnvelopException;
import org.openjava.upay.proxy.exception.ServiceAccessException;
import org.openjava.upay.proxy.exception.UnpackDataEnvelopException;
import org.openjava.upay.proxy.util.AjaxHttpUtils;
import org.openjava.upay.proxy.util.Constants;
import org.openjava.upay.shared.type.ErrorCode;
import org.openjava.upay.util.AssertUtils;
import org.openjava.upay.util.ObjectUtils;
import org.openjava.upay.util.json.JsonUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/spi/payment")
public class PaymentServiceEndpoint extends AbstractServiceEndpoint
{
    @RequestMapping("/doService.do")
    public void doService(HttpServletRequest request, HttpServletResponse response)
    {
        ServiceResponse<?> message;
        MessageEnvelop envelop = null;
        MessageEnvelop reply = new MessageEnvelop();

        try {
            String body = AjaxHttpUtils.extractHttpBody(request);
            LOG.debug("doService request received: " + body);
            AssertUtils.notEmpty(body, "Cannot extract data from http body");

            envelop = JsonUtils.fromJsonString(body, MessageEnvelop.class);
            if (ObjectUtils.isEmpty(envelop.getService())) {
                String service = request.getParameter(Constants.HTTP_PARAM_SERVICE);
                if (ObjectUtils.isEmpty(service)) {
                    service = request.getHeader(Constants.HTTP_PARAM_SERVICE);
                }
                envelop.setService(service);
            }
            AssertUtils.notEmpty(envelop.getService(), "Argument missed: service");

            RequestContext context = checkAccessPermission(envelop);
            unpackEnvelop(envelop, context.getMerchant().getSecretKey());
            message = sendEnvelop(context, envelop);
        } catch (IllegalArgumentException aex) {
            LOG.error(aex.getMessage());
            message = ServiceResponse.failure(ErrorCode.ILLEGAL_ARGUMENT.getCode(), aex.getMessage());
        } catch (ServiceAccessException sax) {
            LOG.error("Service access exception", sax);
            message = ServiceResponse.failure(sax.getCode(), sax.getMessage());
        } catch (UnpackDataEnvelopException dex) {
            LOG.error("Unpack message envelop exception", dex);
            message = ServiceResponse.failure(dex.getCode(), dex.getMessage());
        } catch (Throwable ex) {
            LOG.error("Handle payment request exception", ex);
            ErrorCode errorCode = ErrorCode.UNKNOWN_EXCEPTION;
            message = ServiceResponse.failure(errorCode.getCode(), errorCode.getName());
        }

        try {
            String content = JsonUtils.toJsonString(message);
            reply = packEnvelop(envelop == null ? reply : envelop, content);
        } catch (PackDataEnvelopException dex) {
            reply.setStatus(ErrorCode.DATA_SIGN_FAILED.getCode());
            LOG.error("Pack data envelop exception", dex);
        }

        AjaxHttpUtils.sendResponse(response, reply);
    }
}