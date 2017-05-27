package org.openjava.upay.web.exception;

import org.openjava.upay.web.domain.AjaxMessage;
import org.openjava.upay.web.util.AjaxHttpUtils;
import org.openjava.upay.web.infrastructure.httl.HttlLayoutViewSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class DefaultGlobalExceptionResolver extends HttlLayoutViewSupport implements HandlerExceptionResolver
{
    private static Logger LOG = LoggerFactory.getLogger(DefaultGlobalExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    {
        LOG.error("系统未知异常", ex);
        if (AjaxHttpUtils.isAjaxRequest(request)) {
            AjaxMessage message = AjaxMessage.failure("系统未知异常", ex);
            AjaxHttpUtils.sendResponse(response, message);
            return null;
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("requestUri", request.getRequestURI());
            params.put("message", ex.getMessage());
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            params.put("stackTrace", sw.toString());
            return toDefault("application/exception", params);
        }
    }
}
