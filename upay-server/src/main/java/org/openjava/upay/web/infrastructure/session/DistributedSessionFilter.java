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

public class DistributedSessionFilter implements Filter
{
    private DistributedSessionManager sessionManager;
    
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
        if (!shouldFilter(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequestAdapter requestWrapper = new HttpServletRequestAdapter(httpRequest, httpResponse, sessionManager);
        try {
            chain.doFilter(requestWrapper, httpResponse);
        } finally {
            requestWrapper.finished();
        }
    }

    @Override
    public void destroy()
    {
    }
    
    public void setSessionManager(DistributedSessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }
    
    protected boolean shouldFilter(HttpServletRequest request)
    {
        return true;
    }
}
