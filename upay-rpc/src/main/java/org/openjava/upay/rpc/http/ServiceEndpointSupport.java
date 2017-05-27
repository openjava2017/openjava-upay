package org.openjava.upay.rpc.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import org.openjava.upay.rpc.exception.SSLContextInitException;
import org.openjava.upay.rpc.exception.ServiceAccessException;

public abstract class ServiceEndpointSupport
{
    private static final int MAX_KEEP_ALIVE_TIME = 8000;
    
    private CloseableHttpClient httpClient;
    
    public HttpResult execute(String requestUrl, HttpHeader[] headers, String body) throws ServiceAccessException
    {
        if (StringUtils.isEmpty(requestUrl)) {
            throw new IllegalArgumentException("Invalid http request url, url=" + requestUrl);
        }
        
        HttpPost method = new HttpPost(requestUrl);
        // Wrap the HTTP headers
        if (headers != null) {
            for (HttpHeader header : headers) {
                method.addHeader(header.param, header.value);
            }
        }
        
        try {
            StringEntity entity =  new StringEntity(body, ContentType.APPLICATION_JSON);
            method.setEntity(entity);
            HttpResult result = HttpResult.create();
            CloseableHttpResponse response = getHttpClient().execute(method);
            result.statusCode = response.getStatusLine().getStatusCode();
            result.responseText = EntityUtils.toString(response.getEntity());
            return result;
        } catch (ClientProtocolException  cpe) {
            throw new ServiceAccessException("Invalid client protocol exception", cpe);
        } catch (IOException ioe) {
            throw new ServiceAccessException("Service access exception", ioe);
        }
    }
    
    public HttpResult execute(String requestUrl, HttpHeader[] headers, HttpParam[] params) throws ServiceAccessException
    {
        if (StringUtils.isEmpty(requestUrl)) {
            throw new IllegalArgumentException("Invalid http request url, url=" + requestUrl);
        }
        
        HttpPost method = new HttpPost(requestUrl);
        // Wrap the HTTP headers
        if (headers != null) {
            for (HttpHeader header : headers) {
                method.addHeader(header.param, header.value);
            }
        }
        
        // Wrap the HTTP parameters
        if (params != null) {
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            for (HttpParam param : params) {
                paramList.add(new BasicNameValuePair(param.param, param.value));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, Consts.UTF_8);
            method.setEntity(entity);
        }
        
        try {
            HttpResult result = HttpResult.create();
            CloseableHttpResponse response = getHttpClient().execute(method);
            result.statusCode = response.getStatusLine().getStatusCode();
            result.responseText = EntityUtils.toString(response.getEntity());
            return result;
        } catch (ClientProtocolException  cpe) {
            throw new ServiceAccessException("Invalid client protocol exception", cpe);
        } catch (IOException ioe) {
            throw new ServiceAccessException("Service access exception", ioe);
        }
    }
    
    public HttpResult execute(String requestUrl, HttpParam... params) throws ServiceAccessException
    {
        if (StringUtils.isEmpty(requestUrl)) {
            throw new IllegalArgumentException("Invalid http request url, url=" + requestUrl);
        }
        
        HttpPost method = new HttpPost(requestUrl);
        // Wrap the HTTP parameters
        if (params != null) {
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            for (HttpParam param : params) {
                paramList.add(new BasicNameValuePair(param.param, param.value));
            }
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, Consts.UTF_8);
            method.setEntity(entity);
        }
        
        try {
            HttpResult result = HttpResult.create();
            CloseableHttpResponse response = getHttpClient().execute(method);
            result.statusCode = response.getStatusLine().getStatusCode();
            result.responseText = EntityUtils.toString(response.getEntity());
            return result;
        } catch (ClientProtocolException  cpe) {
            throw new ServiceAccessException("Invalid client protocol exception", cpe);
        } catch (IOException ioe) {
            throw new ServiceAccessException("Service access exception", ioe);
        }
    }
    
    protected CloseableHttpClient getHttpClient() throws ServiceAccessException
    {
        if (httpClient == null) {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            SSLContext sslContext = buildSSLContext();
            
            if (sslContext != null) {
                httpClientBuilder.setSslcontext(sslContext);
                httpClientBuilder.setHostnameVerifier(new AllowAllHostnameVerifier());
            }
            
            ConnectionKeepAliveStrategy keepAliveStrategy = new DefaultConnectionKeepAliveStrategy() {
                @Override
                public long getKeepAliveDuration(HttpResponse response, HttpContext context)
                {
                    long keepAlive = super.getKeepAliveDuration(response, context);
                    if (keepAlive == -1) {
                        // Keep connections alive 8 seconds if a keep-alive value 
                        // has not be explicitly set by the server
                        keepAlive = MAX_KEEP_ALIVE_TIME;
                    }
                    return keepAlive;
                }
    
            };
            
            httpClientBuilder.setKeepAliveStrategy(keepAliveStrategy);
            httpClient = httpClientBuilder.build();
        }
        
        return httpClient;
    }
    
    protected SSLContext buildSSLContext() throws SSLContextInitException
    {
        return null;
    }
    
    public static class HttpParam
    {
        public String param;
        public String value;
        
        private HttpParam(String param, String value)
        {
            this.param = param;
            this.value = value;
        }
        
        public static HttpParam create(String param, String value)
        {
            return new HttpParam(param, value);
        }
    }
    
    public static class HttpHeader
    {
        public String param;
        public String value;
        
        private HttpHeader(String param, String value)
        {
            this.param = param;
            this.value = value;
        }
        
        public static HttpHeader create(String param, String value)
        {
            return new HttpHeader(param, value);
        }
    }
    
    public static class HttpResult
    {
        public int statusCode = -1;
        public String responseText;
        
        public static HttpResult create()
        {
            return new HttpResult();
        }
    }
}
