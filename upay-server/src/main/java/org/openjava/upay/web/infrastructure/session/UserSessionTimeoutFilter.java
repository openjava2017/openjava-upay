package org.openjava.upay.web.infrastructure.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openjava.upay.web.domain.AjaxMessage;
import org.openjava.upay.web.util.AjaxHttpUtils;
import org.openjava.upay.web.util.WebConstants;

public class UserSessionTimeoutFilter implements Filter
{
    private String redirectUrl = "/";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Expired user session
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute(WebConstants.SESSION_KEY_OPERATOR) == null) {
            httpResponse.setHeader(WebConstants.HTTP_SESSION_STATUS, WebConstants.HTTP_SESSION_EXPIRED);
            if (!AjaxHttpUtils.isAjaxRequest(httpRequest)) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + redirectUrl);
//                httpRequest.getRequestDispatcher("/").forward(httpRequest, httpResponse);
            } else {
                AjaxMessage message = AjaxMessage.create("session_expired", "Invalid user session");
                AjaxHttpUtils.sendResponse(httpResponse, message);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
    
    @Override
    public void destroy()
    {
    }

    public void setRedirectUrl(String redirectUrl)
    {
        this.redirectUrl = redirectUrl;
    }
}
